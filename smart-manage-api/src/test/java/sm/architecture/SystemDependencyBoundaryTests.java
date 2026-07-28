package sm.architecture;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.assertTrue;

class SystemDependencyBoundaryTests {

    @Test
    void systemAndFrameworkMustNotDependOnDomainPackages() throws IOException {
        assertNoDomainImports(Path.of("src/main/java/sm/system"));
        assertNoDomainImports(Path.of("src/main/java/sm/framework"));
    }

    @Test
    void controllersMustNotDependOnMappersOrTransactionServices() throws IOException {
        assertNoSourceViolation(
                path -> path.toString().endsWith("Controller.java"),
                source -> source.matches("(?s).*import\\s+.*\\.mapper\\..*")
                        || source.matches("(?s).*import\\s+.*TxService\\s*;.*"),
                "Controller 禁止依赖 Mapper 或 TxService"
        );
    }

    @Test
    void publicServicesMustNotDeclareTransactions() throws IOException {
        assertNoSourceViolation(
                path -> path.toString().endsWith("Service.java")
                        && !path.toString().endsWith("TxService.java"),
                source -> source.contains("@Transactional"),
                "公开 Service 禁止声明事务"
        );
    }

    @Test
    void transactionServicesMustBePackagePrivateAndTransactional() throws IOException {
        assertNoSourceViolation(
                path -> path.toString().endsWith("TxService.java"),
                source -> !source.contains("@Transactional(rollbackFor = Exception.class)")
                        || source.matches("(?s).*public\\s+class\\s+\\w+TxService.*"),
                "TxService 必须包级可见并声明类级事务"
        );
    }

    @Test
    void convertersMustRemainPure() throws IOException {
        List<String> forbiddenImports = List.of(
                ".mapper.",
                ".service.",
                "sm.system.helper",
                "sm.domain.sys.base.common.helper",
                "org.springframework.cache",
                "com.alicp.jetcache"
        );
        assertNoSourceViolation(
                path -> path.toString().endsWith("Converter.java"),
                source -> source.lines()
                        .filter(line -> line.startsWith("import "))
                        .anyMatch(line -> forbiddenImports.stream().anyMatch(line::contains)),
                "Converter 只能承担纯字段映射"
        );
    }

    @Test
    void domainVoIdsMustKeepLongType() throws IOException {
        assertNoSourceViolation(
                path -> path.toString().contains("model" + java.io.File.separator + "vo")
                        && path.toString().endsWith("VO.java"),
                source -> source.matches("(?s).*private\\s+String\\s+id\\s*;.*"),
                "后端 VO 的雪花 ID 必须保持 Long，由 JSON 边界统一序列化为字符串"
        );
    }

    @Test
    void bizLogMustOnlyAppearOnPublicServices() throws IOException {
        assertNoSourceViolation(
                path -> path.toString().endsWith(".java"),
                source -> source.contains("@BizLog")
                        && (!source.contains("class ")
                        || !source.matches("(?s).*class\\s+\\w+Service\\b.*")
                        || source.matches("(?s).*class\\s+\\w+TxService\\b.*")),
                "@BizLog 只能出现在公开 Service"
        );
    }

    @Test
    void highRiskServicesMustCheckAdministratorIdentity() throws IOException {
        List<Path> highRiskServices = List.of(
                Path.of("src/main/java/sm/domain/sys/monitor/sql/service/SqlService.java"),
                Path.of("src/main/java/sm/domain/sys/monitor/script/service/ScriptService.java"),
                Path.of("src/main/java/sm/domain/sys/monitor/arthas/service/ArthasService.java"),
                Path.of("src/main/java/sm/domain/sys/monitor/job/service/JobService.java")
        );
        var violations = highRiskServices.stream()
                .filter(path -> !readSource(path).contains("UserHelper.checkAdmin()"))
                .map(Path::toString)
                .toList();
        assertTrue(violations.isEmpty(), () -> "高风险 Service 必须校验 administrator 身份: " + violations);
    }

    @Test
    void serviceDetailMethodsMustUseStandardName() throws IOException {
        assertNoSourceViolation(
                path -> path.toString().endsWith("Service.java"),
                source -> source.matches("(?s).*public\\s+\\w+(?:Detail|Info|List)?VO\\s+(?:getDetail|getById)\\s*\\(.*"),
                "标准详情 Service 方法必须统一命名为 detail"
        );
    }

    @Test
    void publicServicesMustNotExposeGenericEntityLookup() throws IOException {
        assertNoSourceViolation(
                path -> path.toString().endsWith("Service.java")
                        && !path.toString().endsWith("TxService.java"),
                source -> source.matches("(?s).*public\\s+\\w+Entity\\s+getById\\s*\\(.*"),
                "公开 Service 禁止通过通用 getById 暴露 Entity，应改为具有业务语义的方法"
        );
    }

    private void assertNoDomainImports(Path sourceRoot) throws IOException {
        try (var paths = Files.walk(sourceRoot)) {
            var violations = paths
                    .filter(path -> path.toString().endsWith(".java"))
                    .filter(this::containsDomainImport)
                    .map(Path::toString)
                    .toList();
            assertTrue(violations.isEmpty(), () -> "基础层禁止反向依赖领域包: " + violations);
        }
    }

    private boolean containsDomainImport(Path path) {
        try {
            return Files.readString(path).contains("import sm.domain.");
        } catch (IOException exception) {
            throw new IllegalStateException("读取源码失败: " + path, exception);
        }
    }

    private void assertNoSourceViolation(
            Predicate<Path> pathPredicate,
            Predicate<String> sourceViolation,
            String message
    ) throws IOException {
        Path sourceRoot = Path.of("src/main/java");
        try (var paths = Files.walk(sourceRoot)) {
            var violations = paths
                    .filter(pathPredicate)
                    .filter(path -> containsViolation(path, sourceViolation))
                    .map(Path::toString)
                    .toList();
            assertTrue(violations.isEmpty(), () -> message + ": " + violations);
        }
    }

    private boolean containsViolation(Path path, Predicate<String> sourceViolation) {
        return sourceViolation.test(readSource(path));
    }

    private String readSource(Path path) {
        try {
            return Files.readString(path);
        } catch (IOException exception) {
            throw new IllegalStateException("读取源码失败: " + path, exception);
        }
    }
}
