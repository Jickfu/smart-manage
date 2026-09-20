package sm.domain.sys.base.user.service;

import org.springframework.stereotype.Component;
import org.springframework.boot.system.ApplicationHome;
import sm.system.util.PasswordGeneratorUtil;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.AclEntry;
import java.nio.file.attribute.AclEntryPermission;
import java.nio.file.attribute.AclEntryType;
import java.nio.file.attribute.AclFileAttributeView;
import java.nio.file.attribute.PosixFilePermission;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/** 首次安装管理员临时密码的本地文件；文件位置只相对于进程工作目录。 */
@Component
class AdministratorInitialPasswordFile {
    static final String FILE_NAME = "administrator-initial-password.txt";
    private static final int PASSWORD_LENGTH = 20;
    private static final Set<PosixFilePermission> OWNER_READ_WRITE = Set.of(
            PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE);

    private final Path workingDirectory;

    AdministratorInitialPasswordFile() {
        this(resolveRuntimeDirectory());
    }

    AdministratorInitialPasswordFile(Path workingDirectory) {
        this.workingDirectory = workingDirectory.toAbsolutePath().normalize();
    }

    private static Path resolveRuntimeDirectory() {
        ApplicationHome applicationHome = new ApplicationHome(AdministratorInitialPasswordFile.class);
        return resolveRuntimeDirectory(applicationHome.getSource(),
                Path.of("").toAbsolutePath().normalize());
    }

    static Path resolveRuntimeDirectory(File applicationSource, Path ideWorkingDirectory) {
        // 打包运行时落在可执行 JAR 同级；IDE 的 classes 目录不作为凭据目录，使用运行配置工作目录。
        if (applicationSource != null && applicationSource.isFile()) {
            return applicationSource.toPath().toAbsolutePath().normalize().getParent();
        }
        return ideWorkingDirectory.toAbsolutePath().normalize();
    }

    PreparedPassword prepare() {
        Path passwordFile = workingDirectory.resolve(FILE_NAME);
        try {
            if (Files.exists(passwordFile)) {
                securePermissions(passwordFile);
                return new PreparedPassword(readPassword(passwordFile), passwordFile);
            }
            String password = PasswordGeneratorUtil.generate(PASSWORD_LENGTH);
            try {
                Files.writeString(passwordFile, password + System.lineSeparator(), StandardCharsets.UTF_8,
                        java.nio.file.StandardOpenOption.CREATE_NEW,
                        java.nio.file.StandardOpenOption.WRITE);
            } catch (FileAlreadyExistsException exception) {
                // 同一工作目录的并发实例复用唯一文件，保证数据库中的获胜密码仍可获取。
                securePermissions(passwordFile);
                return new PreparedPassword(readPassword(passwordFile), passwordFile);
            }
            securePermissions(passwordFile);
            return new PreparedPassword(password, passwordFile);
        } catch (IOException exception) {
            throw new IllegalStateException("无法准备管理员初始密码文件: " + passwordFile, exception);
        }
    }

    void deleteIfMatches(PreparedPassword preparedPassword) {
        try {
            Path passwordFile = preparedPassword.path();
            if (Files.exists(passwordFile)
                    && readPassword(passwordFile).equals(preparedPassword.password())) {
                Files.delete(passwordFile);
            }
        } catch (IOException exception) {
            throw new IllegalStateException("无法清理无效的管理员初始密码文件: "
                    + preparedPassword.path(), exception);
        }
    }

    private String readPassword(Path passwordFile) throws IOException {
        String password = Files.readString(passwordFile, StandardCharsets.UTF_8).strip();
        if (password.isBlank() || password.indexOf('\n') >= 0 || password.indexOf('\r') >= 0) {
            throw new IllegalStateException("管理员初始密码文件内容无效: " + passwordFile);
        }
        return password;
    }

    private void securePermissions(Path passwordFile) throws IOException {
        if (Files.getFileStore(passwordFile).supportsFileAttributeView("posix")) {
            Files.setPosixFilePermissions(passwordFile, OWNER_READ_WRITE);
            return;
        }
        AclFileAttributeView aclView = Files.getFileAttributeView(passwordFile, AclFileAttributeView.class);
        if (aclView != null) {
            AclEntry ownerEntry = AclEntry.newBuilder()
                    .setType(AclEntryType.ALLOW)
                    .setPrincipal(Files.getOwner(passwordFile))
                    .setPermissions(EnumSet.allOf(AclEntryPermission.class))
                    .build();
            aclView.setAcl(List.of(ownerEntry));
            return;
        }
        File file = passwordFile.toFile();
        file.setReadable(false, false);
        file.setWritable(false, false);
        file.setExecutable(false, false);
        file.setReadable(true, true);
        file.setWritable(true, true);
    }

    record PreparedPassword(String password, Path path) {
    }
}
