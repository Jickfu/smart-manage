package sm.architecture;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.doctree.TextTree;
import com.sun.source.util.DocTrees;
import com.sun.source.util.JavacTask;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreePathScanner;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import javax.lang.model.element.Modifier;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.ToolProvider;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** 实体注释不进入字节码，使用 JDK 文档语法树验证，避免用正则误认普通注释。 */
class EntityDocumentationContractTests {

    @Test
    void assembledEntitiesMustDocumentTypesAndDeclaredFields() throws Exception {
        Path serverRoot = findServerRoot();
        var sources = new ArrayList<Path>();
        var productionClasses = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS).importPackages("sm");
        for (var model : productionClasses) {
            String packageName = model.getPackageName();
            if ((!packageName.contains(".model.entity") && !packageName.equals("sm.system.entity"))
                    || model.getEnclosingClass().isPresent()) {
                continue;
            }
            Path moduleRoot = serverRoot.resolve("platform");
            if (packageName.startsWith("sm.domain.") && !packageName.startsWith("sm.domain.sys.")) {
                String domainName = packageName.split("\\.")[2];
                moduleRoot = serverRoot.resolve("domains").resolve(domainName);
            }
            Path source = moduleRoot.resolve("src/main/java")
                    .resolve(model.getName().replace('.', '/') + ".java");
            assertTrue(Files.isRegularFile(source), "实体源码必须存在：" + source);
            sources.add(source);
        }
        assertFalse(sources.isEmpty(), "实体文档门禁必须发现实际装配的实体");
        var compiler = ToolProvider.getSystemJavaCompiler();
        assertNotNull(compiler, "实体文档验证必须使用 JDK");
        try (var manager = compiler.getStandardFileManager(null, null, StandardCharsets.UTF_8)) {
            assertEquals(List.of(), undocumentedMembers(manager.getJavaFileObjectsFromPaths(sources)));
        }
    }

    @Test
    void documentationParserRejectsMissingAuthorOnlyAndOrdinaryComments() throws Exception {
        assertFalse(checkSource("class Sample { private String name; }").isEmpty());
        assertFalse(checkSource("/** @author 作者 */ class Sample { /** 名称 */ private String name; }")
                .isEmpty());
        assertFalse(checkSource("/** 示例实体 */ class Sample { /* 名称 */ private String name; }")
                .isEmpty());
        assertFalse(checkSource("/** 示例实体 */ class Sample { // 名称\n private String name; }")
                .isEmpty());
        assertFalse(checkSource("/** Sample */ class Sample { /** name */ private String name; }")
                .isEmpty());
        assertEquals(List.of(), checkSource("/** 示例实体 */ class Sample { "
                + "static final long SERIAL_VERSION = 1; /**\n * 名称\n */\n private String name; }"));
        assertEquals(List.of(), checkSource("/** 示例实体 */ class Sample { "
                + "/**\n * 名称\n */\n @Deprecated private String name; }"));
        assertEquals(List.of(), checkSource("/** 示例实体 */ class Sample { "
                + "/**\n * ID\n */\n private Long id; }"));
        assertFalse(checkSource("/** 示例实体 */ class Sample { /** ID */ private String name; }")
                .isEmpty());
        assertFalse(checkSource("/** 示例实体 */ class Sample { /** 名称 */ private String name; }")
                .isEmpty());
        assertFalse(checkSource("/** 示例实体 */ class Sample { "
                + "/**\n * 名称。\n */\n private String name; }").isEmpty());
        assertFalse(checkSource("/** 示例实体 */ class Sample { "
                + "/**\n * 记录 ID\n */\n private Long id; }").isEmpty());
        assertFalse(checkSource("/** 示例实体 */ class Sample { /** */ private String name; }").isEmpty());
    }

    private static List<String> checkSource(String source) throws Exception {
        JavaFileObject file = new SimpleJavaFileObject(URI.create("string:///Sample.java"),
                JavaFileObject.Kind.SOURCE) {
            @Override
            public CharSequence getCharContent(boolean ignoreEncodingErrors) {
                return source;
            }
        };
        return undocumentedMembers(List.of(file));
    }

    private static List<String> undocumentedMembers(Iterable<? extends JavaFileObject> sources)
            throws Exception {
        var compiler = ToolProvider.getSystemJavaCompiler();
        assertNotNull(compiler, "实体文档验证必须使用 JDK");
        try (var manager = compiler.getStandardFileManager(null, null, StandardCharsets.UTF_8)) {
            // 只解析语法，不执行注解处理和类型分析，不依赖 Spring 启动或数据库。
            var task = (JavacTask) compiler.getTask(null, manager, null,
                    List.of("-proc:none"), null, sources);
            Iterable<? extends CompilationUnitTree> units = task.parse();
            var docs = DocTrees.instance(task);
            var failures = new ArrayList<String>();
            for (var unit : units) {
                new TreePathScanner<Void, Void>() {
                    @Override
                    public Void visitClass(ClassTree declaration, Void unused) {
                        requireChineseDocumentation(getCurrentPath(), declaration.getSimpleName() + " 类");
                        for (var member : declaration.getMembers()) {
                            if (member instanceof VariableTree field
                                    && !field.getModifiers().getFlags().contains(Modifier.STATIC)) {
                                requireChineseDocumentation(new TreePath(getCurrentPath(), field),
                                        declaration.getSimpleName() + "." + field.getName());
                            }
                        }
                        return super.visitClass(declaration, unused);
                    }

                    private void requireChineseDocumentation(TreePath member, String name) {
                        var comment = docs.getDocCommentTree(member);
                        String text = comment == null ? "" : comment.getFullBody().stream()
                                .filter(part -> part instanceof TextTree)
                                .map(part -> ((TextTree) part).getBody())
                                .collect(java.util.stream.Collectors.joining()).strip();
                        boolean documented = text.codePoints()
                                .anyMatch(character -> Character.UnicodeScript.of(character)
                                        == Character.UnicodeScript.HAN)
                                || (member.getLeaf() instanceof VariableTree field
                                && field.getName().contentEquals("id") && text.equals("ID"));
                        if (!documented) {
                            failures.add(unit.getSourceFile().getName() + ": " + name + " 缺少中文 Javadoc 正文");
                        }
                        if (comment != null && member.getLeaf() instanceof VariableTree) {
                            // 先由文档语法树定位真实注释，再检查源码布局，不把普通注释或字符串当作 Javadoc。
                            String source;
                            try {
                                source = unit.getSourceFile().getCharContent(true).toString();
                            } catch (java.io.IOException exception) {
                                throw new java.io.UncheckedIOException(exception);
                            }
                            long position = docs.getSourcePositions().getStartPosition(unit, comment, comment);
                            int opening = source.lastIndexOf("/**", Math.toIntExact(position));
                            int closing = source.indexOf("*/", opening);
                            String[] lines = opening < 0 || closing < opening ? new String[0]
                                    : source.substring(opening, closing + 2).split("\\R");
                            boolean multiline = lines.length >= 3 && lines[0].strip().equals("/**")
                                    && lines[lines.length - 1].strip().equals("*/");
                            for (int lineIndex = 1; lineIndex < lines.length - 1; lineIndex++) {
                                multiline &= lines[lineIndex].stripLeading().startsWith("*");
                            }
                            if (!multiline) {
                                failures.add(unit.getSourceFile().getName() + ": " + name + " 必须使用多行 Javadoc");
                            }
                            if (text.endsWith("。")) {
                                failures.add(unit.getSourceFile().getName() + ": " + name + " 字段说明末尾不得添加句号");
                            }
                            VariableTree field = (VariableTree) member.getLeaf();
                            if (field.getName().contentEquals("id")
                                    && (field.getType().toString().equals("Long")
                                    || field.getType().toString().equals("java.lang.Long"))
                                    && !text.equals("ID")) {
                                failures.add(unit.getSourceFile().getName() + ": " + name + " 的说明应直接使用 ID");
                            }
                        }
                    }
                }.scan(unit, null);
            }
            return failures;
        }
    }

    private static Path findServerRoot() {
        Path current = Path.of(System.getProperty("user.dir")).toAbsolutePath();
        while (current != null) {
            if (Files.isDirectory(current.resolve("platform/src/main/java"))
                    && Files.isDirectory(current.resolve("app/src/test/java"))) {
                return current;
            }
            current = current.getParent();
        }
        throw new IllegalStateException("找不到 smart-manage-server 源码根目录");
    }
}
