package sm.architecture;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertTrue;

class HighRiskSecurityContractTests {

    @Test
    void highRiskPublicMethodsMustStartWithAdministratorIdentityCheck() {
        Map<Path, List<String>> protectedMethods = Map.of(
                Path.of("src/main/java/sm/domain/sys/monitor/sql/service/SqlService.java"),
                List.of("execute", "listPage", "detail"),
                Path.of("src/main/java/sm/domain/sys/monitor/script/service/ScriptService.java"),
                List.of("execute", "listPage", "detail", "createNewData", "apiMetadata", "save", "delete",
                        "logListPage", "logDetail"),
                Path.of("src/main/java/sm/domain/sys/monitor/thread/service/ThreadDiagnosticService.java"),
                List.of("list", "detail", "hot", "dump", "deadlocks", "localList", "localDetail", "localHot",
                        "localDump", "localDeadlocks"),
                Path.of("src/main/java/sm/domain/sys/monitor/cache/service/CacheService.java"),
                List.of("overview", "runtime", "scopeTree", "listPage", "value", "delete", "clear", "clearAll"),
                Path.of("src/main/java/sm/domain/sys/scheduler/service/JobService.java"),
                List.of("save", "deleteById", "pause", "resume", "syncAll", "trigger", "getAvailableJobClasses",
                        "createNewData"),
                Path.of("src/main/java/sm/domain/sys/base/fileconfig/service/FileConfigService.java"),
                List.of("save", "testFtp"),
                Path.of("src/main/java/sm/domain/sys/base/attachmentconfig/service/AttachmentConfigService.java"),
                List.of("save")
        );
        List<String> violations = protectedMethods.entrySet().stream()
                .flatMap(entry -> entry.getValue().stream()
                        .filter(methodName -> !startsWithAdministratorCheck(readSource(entry.getKey()), methodName))
                        .map(methodName -> entry.getKey() + "#" + methodName))
                .toList();
        assertTrue(violations.isEmpty(),
                () -> "每个高风险公开方法必须先校验 administrator 身份: " + violations);
    }

    private String readSource(Path path) {
        try {
            return Files.readString(path);
        } catch (IOException exception) {
            throw new IllegalStateException("读取源码失败: " + path, exception);
        }
    }

    private boolean startsWithAdministratorCheck(String source, String methodName) {
        String methodPattern = "(?s)public\\s+[^;{}]+\\s+" + Pattern.quote(methodName)
                + "\\s*\\([^)]*\\)\\s*\\{(?:\\s|//[^\\r\\n]*(?:\\r?\\n|$)|/\\*.*?\\*/)*"
                + "currentUserContext\\.checkAdministrator\\(\\);";
        return Pattern.compile(methodPattern).matcher(source).find();
    }
}
