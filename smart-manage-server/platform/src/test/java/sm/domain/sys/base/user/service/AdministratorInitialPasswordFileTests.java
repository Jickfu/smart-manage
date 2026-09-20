package sm.domain.sys.base.user.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AdministratorInitialPasswordFileTests {
    @TempDir
    Path temporaryDirectory;

    @Test
    void runtimeDirectoryDistinguishesIdeAndPackagedJar() throws Exception {
        Path ideWorkingDirectory = temporaryDirectory.resolve("idea-workdir");
        Files.createDirectories(ideWorkingDirectory);
        Path classesDirectory = temporaryDirectory.resolve("target/classes");
        Files.createDirectories(classesDirectory);
        Path jarDirectory = temporaryDirectory.resolve("release");
        Files.createDirectories(jarDirectory);
        Path jarFile = Files.createFile(jarDirectory.resolve("smart-manage-bootstrap.jar"));

        assertEquals(ideWorkingDirectory,
                AdministratorInitialPasswordFile.resolveRuntimeDirectory(
                        classesDirectory.toFile(), ideWorkingDirectory));
        assertEquals(jarDirectory,
                AdministratorInitialPasswordFile.resolveRuntimeDirectory(
                        jarFile.toFile(), ideWorkingDirectory));
    }

    @Test
    void createsPasswordFileDirectlyUnderWorkingDirectory() throws Exception {
        AdministratorInitialPasswordFile passwordFile =
                new AdministratorInitialPasswordFile(temporaryDirectory);

        AdministratorInitialPasswordFile.PreparedPassword preparedPassword = passwordFile.prepare();

        assertEquals(temporaryDirectory.resolve("administrator-initial-password.txt"),
                preparedPassword.path());
        assertEquals(20, preparedPassword.password().length());
        assertEquals(preparedPassword.password(), Files.readString(preparedPassword.path()).strip());
    }

    @Test
    void existingFileIsReusedInsteadOfOverwritten() throws Exception {
        Path path = temporaryDirectory.resolve("administrator-initial-password.txt");
        Files.writeString(path, "Existing-Password-729!\n");
        AdministratorInitialPasswordFile passwordFile =
                new AdministratorInitialPasswordFile(temporaryDirectory);

        AdministratorInitialPasswordFile.PreparedPassword preparedPassword = passwordFile.prepare();

        assertEquals("Existing-Password-729!", preparedPassword.password());
        assertEquals("Existing-Password-729!", Files.readString(path).strip());
    }

    @Test
    void losingInstanceOnlyDeletesItsOwnInvalidPasswordFile() throws Exception {
        AdministratorInitialPasswordFile passwordFile =
                new AdministratorInitialPasswordFile(temporaryDirectory);
        AdministratorInitialPasswordFile.PreparedPassword preparedPassword = passwordFile.prepare();
        Path path = preparedPassword.path();

        Files.writeString(path, "Other-Password-826!\n");
        passwordFile.deleteIfMatches(preparedPassword);
        assertTrue(Files.exists(path));
        assertNotEquals(preparedPassword.password(), Files.readString(path).strip());

        AdministratorInitialPasswordFile.PreparedPassword current =
                new AdministratorInitialPasswordFile.PreparedPassword("Other-Password-826!", path);
        passwordFile.deleteIfMatches(current);
        assertFalse(Files.exists(path));
    }
}
