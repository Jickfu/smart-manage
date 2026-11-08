package sm.architecture;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Select;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/** 使用实际 MyBatis 命令类型识别写模块，不靠方法名猜测，也不强迫只读投影建表实体。 */
class PersistenceModelContractTests {
    @Test
    void assembledEntitiesAndWriteMappersHaveExplicitPersistenceModels() throws Exception {
        var classes = new ClassFileImporter().withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages("sm.domain");
        Set<String> entityModules = new HashSet<>();
        int entityCount = 0;
        for (var model : classes) {
            if (!model.getSimpleName().endsWith("Entity")) continue;
            Class<?> entity = model.reflect();
            assertTrue(entity.isAnnotationPresent(TableName.class), entity.getName());
            assertFalse(entity.getAnnotation(TableName.class).value().isBlank(), entity.getName());
            List<Field> fields = fieldsOf(entity);
            var identifiers = fields.stream().filter(field -> field.isAnnotationPresent(TableId.class)).toList();
            assertEquals(1, identifiers.size(), "实体必须有唯一主键声明: " + entity.getName());
            assertEquals(IdType.ASSIGN_ID, identifiers.getFirst().getAnnotation(TableId.class).type(), entity.getName());
            for (Field field : fields) {
                if (field.getName().equals("version")) {
                    assertTrue(field.isAnnotationPresent(Version.class), "版本字段必须声明乐观锁: " + entity.getName());
                }
            }
            entityModules.add(entity.getPackageName().replaceFirst("\\.model\\.entity$", ""));
            entityCount++;
        }
        assertTrue(entityCount > 0, "不得以未发现实体通过检查");
        var resources = new PathMatchingResourcePatternResolver().getResources("classpath*:mapper/**/*.xml");
        assertTrue(resources.length > 0);
        var configuration = new MybatisConfiguration();
        for (var resource : resources) {
            try (var input = resource.getInputStream()) {
                new XMLMapperBuilder(input, configuration, resource.getURL().toString(),
                        configuration.getSqlFragments()).parse();
            }
        }
        // XML 只会注册其 namespace 对应的接口；无 XML 的注解 Mapper 必须单独发现。
        for (var model : classes) {
            if (model.isInterface() && model.isAnnotatedWith(Mapper.class)) {
                registerMapper(configuration, model.reflect());
            }
        }
        int writeCount = 0;
        for (String identity : new HashSet<>(configuration.getMappedStatementNames())) {
            if (!identity.startsWith("sm.domain.")) continue;
            var statement = configuration.getMappedStatement(identity);
            assertRequiredEntity(statement, entityModules);
            if (isWrite(statement.getSqlCommandType())) writeCount++;
        }
        assertTrue(writeCount > 0, "不得以未加载写入映射通过检查");
    }

    @Test
    void missingEntityCannotHideBehindCustomWriteMapperButReadProjectionNeedsNoEntity() {
        String mapper = "sm.domain.example.data.catalog.mapper.CatalogMapper";
        for (SqlCommandType command : List.of(SqlCommandType.INSERT, SqlCommandType.UPDATE, SqlCommandType.DELETE)) {
            assertFalse(hasRequiredEntity(mapper, command, Set.of()));
            assertFalse(hasRequiredEntity(mapper, command, Set.of("sm.domain.other.data.catalog")));
            assertTrue(hasRequiredEntity(mapper, command, Set.of("sm.domain.example.data.catalog")));
        }
        assertTrue(hasRequiredEntity(mapper, SqlCommandType.SELECT, Set.of()));
    }

    @Test
    void annotationOnlyWritesRequireEntityButReadOnlyProjectionDoesNot() {
        var configuration = new MybatisConfiguration();
        registerMapper(configuration, AnnotationOnlyWriteMapper.class);
        registerMapper(configuration, AnnotationOnlyReadMapper.class);
        for (String method : List.of("insert", "update", "delete")) {
            var statement = configuration.getMappedStatement(AnnotationOnlyWriteMapper.class.getName() + "." + method);
            assertTrue(isWrite(statement.getSqlCommandType()));
            assertThrows(AssertionError.class, () -> assertRequiredEntity(statement, Set.of()));
        }
        var projection = configuration.getMappedStatement(AnnotationOnlyReadMapper.class.getName() + ".read");
        assertEquals(SqlCommandType.SELECT, projection.getSqlCommandType());
        assertDoesNotThrow(() -> assertRequiredEntity(projection, Set.of()));
    }

    private static void registerMapper(MybatisConfiguration configuration, Class<?> mapper) {
        if (!configuration.hasMapper(mapper)) configuration.addMapper(mapper);
    }

    private static void assertRequiredEntity(MappedStatement statement, Set<String> entityModules) {
        String identity = statement.getId();
        String namespace = identity.substring(0, identity.lastIndexOf('.'));
        assertTrue(hasRequiredEntity(namespace, statement.getSqlCommandType(), entityModules),
                "持久化写模块必须在自身 model.entity 声明实体: " + identity);
    }

    @Mapper
    interface AnnotationOnlyWriteMapper {
        @Insert("INSERT INTO fixture_record (id) VALUES (1)")
        int insert();
        @Update("UPDATE fixture_record SET id = 2 WHERE id = 1")
        int update();
        @Delete("DELETE FROM fixture_record WHERE id = 2")
        int delete();
    }

    @Mapper
    interface AnnotationOnlyReadMapper {
        @Select("SELECT 1")
        int read();
    }

    private static boolean hasRequiredEntity(String namespace, SqlCommandType command, Set<String> entityModules) {
        if (!isWrite(command)) return true;
        int marker = namespace.lastIndexOf(".mapper.");
        return marker > 0 && entityModules.contains(namespace.substring(0, marker));
    }

    private static boolean isWrite(SqlCommandType command) {
        return command == SqlCommandType.INSERT || command == SqlCommandType.UPDATE || command == SqlCommandType.DELETE;
    }

    private static List<Field> fieldsOf(Class<?> entity) {
        List<Field> fields = new ArrayList<>();
        for (Class<?> current = entity; current != null; current = current.getSuperclass()) {
            fields.addAll(Arrays.asList(current.getDeclaredFields()));
        }
        return fields;
    }
}
