package sm.architecture;

import com.tngtech.archunit.core.domain.JavaAccess;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.Dependency;
import com.tngtech.archunit.core.domain.JavaModifier;
import com.tngtech.archunit.core.domain.JavaMethodCall;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.annotation.Transactional;
import sm.system.aop.log.BizLog;
import sm.system.security.authorization.AdministratorOnly;
import sm.system.security.context.CurrentUserContext;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ArchitectureContractTests {

    private static JavaClasses productionClasses;

    @BeforeAll
    static void importProductionClasses() {
        productionClasses = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages("sm");
    }

    @Test
    void topLevelLayersMustRespectDependencyDirection() {
        noClasses().that().resideInAPackage("sm.infrastructure..")
                .should().dependOnClassesThat().resideInAnyPackage("sm.system..", "sm.domain..")
                .because("infrastructure 是最底层第三方技术与外部设施适配，不得依赖 system 或 domain")
                .check(productionClasses);

        noClasses().that().resideInAPackage("sm.system..")
                .should().dependOnClassesThat().resideInAPackage("sm.domain..")
                .because("system 可以依赖 infrastructure，但不得反向依赖业务领域")
                .check(productionClasses);

        classes().that().resideInAPackage("sm.domain..")
                .should(onlyDependOnExplicitInfrastructureContracts())
                .because("领域只能依赖明确开放的技术 Contract，不能任意耦合 infrastructure 实现")
                .check(productionClasses);

        noClasses().should().resideInAPackage("sm.framework..")
                .because("framework 顶层包已正式替换为 infrastructure，不保留兼容包")
                .check(productionClasses);
    }

    @Test
    void businessDomainsMustOnlyUseExplicitCrossDomainContracts() {
        classes().that().resideInAPackage("sm.domain..")
                .should(onlyUseExplicitCrossDomainContracts())
                .because("跨领域只能依赖目标领域显式稳定 contract，禁止 Mapper、Entity、TxService 等实现级耦合")
                .check(productionClasses);

        noClasses().that().resideInAPackage("sm.domain..contract..")
                .should().haveSimpleNameEndingWith("Mapper")
                .orShould().haveSimpleNameEndingWith("Entity")
                .orShould().haveSimpleNameEndingWith("TxService")
                .because("跨领域 contract 只能暴露稳定 API 和边界模型，不得容纳持久化或事务实现")
                .check(productionClasses);
    }

    @Test
    void controllersMustOnlyUsePublicServiceBoundary() {
        noClasses().that().haveSimpleNameEndingWith("Controller")
                .should().dependOnClassesThat().resideInAnyPackage("..mapper..")
                .because("Controller 禁止绕过公开 Service 直接访问 Mapper")
                .check(productionClasses);

        noClasses().that().haveSimpleNameEndingWith("Controller")
                .should().dependOnClassesThat().haveSimpleNameEndingWith("TxService")
                .because("Controller 禁止绕过公开 Service 直接进入事务实现")
                .check(productionClasses);
    }

    @Test
    void publicServicesMustOwnCommandsButNotTransactions() {
        noClasses().that().haveSimpleNameEndingWith("Service")
                .and().haveSimpleNameNotEndingWith("TxService")
                .should().beAnnotatedWith(Transactional.class)
                .because("公开 Service 是业务入口，事务写入统一委托给 TxService")
                .check(productionClasses);

        methods().that().areAnnotatedWith(BizLog.class)
                .should().beDeclaredInClassesThat().haveSimpleNameEndingWith("Service")
                .andShould().beDeclaredInClassesThat().haveSimpleNameNotEndingWith("TxService")
                .andShould().bePublic()
                .because("业务日志只允许位于公开 Service 的命令入口")
                .allowEmptyShould(true)
                .check(productionClasses);
    }

    @Test
    void administratorOnlyMustBeAServiceBoundary() {
        classes().that().areAnnotatedWith(AdministratorOnly.class)
                .should().resideInAPackage("sm.domain..service")
                .andShould().haveSimpleNameEndingWith("Service")
                .andShould().bePublic()
                .because("类级管理员身份保护只允许标记公开业务 Service")
                .allowEmptyShould(true)
                .check(productionClasses);

        methods().that().areAnnotatedWith(AdministratorOnly.class)
                .should().beDeclaredInClassesThat().resideInAPackage("sm.domain..service")
                .andShould().beDeclaredInClassesThat().haveSimpleNameEndingWith("Service")
                .andShould().bePublic()
                .because("方法级管理员身份保护只允许标记公开业务 Service 入口")
                .allowEmptyShould(true)
                .check(productionClasses);

        noClasses().that().resideInAPackage("sm.domain..")
                .should().callMethod(CurrentUserContext.class, "checkAdministrator")
                .because("管理员身份校验必须由 @AdministratorOnly 切面统一执行，领域代码不得散落直接调用")
                .check(productionClasses);

        classes().that().resideInAPackage("sm.domain..")
                .should(notCallMethodLevelAdministratorEntryOnSelf())
                .because("同类调用不会经过 Spring AOP 代理，方法级 @AdministratorOnly 将被绕过")
                .check(productionClasses);
    }

    @Test
    void administratorOnlySelfInvocationRuleMustDetectProxyBypass() {
        JavaClasses fixtureClasses = new ClassFileImporter().importClasses(SelfInvokingFixture.class);

        assertThrows(AssertionError.class, () -> classes()
                .should(notCallMethodLevelAdministratorEntryOnSelf())
                .check(fixtureClasses));
    }

    @Test
    void transactionServicesMustBeModuleInternalTransactionBoundaries() {
        classes().that().haveSimpleNameEndingWith("TxService")
                .should().resideInAPackage("..service")
                .andShould().notBePublic()
                .andShould().beAnnotatedWith(Transactional.class)
                .andShould(useRequiredRollbackPolicy())
                .andShould(onlyBeAccessedByOwningPublicService())
                .because("TxService 必须是同模块公开 Service 独占的包级事务实现")
                .check(productionClasses);
    }

    @Test
    void coreTypesMustFollowPackageConventions() {
        classes().that().resideInAPackage("sm.domain..").and().haveSimpleNameEndingWith("Controller")
                .should().resideInAPackage("..controller")
                .check(productionClasses);
        classes().that().resideInAPackage("sm.domain..").and().haveSimpleNameEndingWith("Mapper")
                .should().resideInAPackage("..mapper")
                .check(productionClasses);
        classes().that().resideInAPackage("sm.domain..")
                .and().haveSimpleNameEndingWith("Service")
                .should().resideInAPackage("..service")
                .check(productionClasses);
        classes().that().resideInAPackage("sm.domain..").and().haveSimpleNameEndingWith("Converter")
                .should().resideInAPackage("..service")
                .check(productionClasses);
        classes().that().resideInAPackage("sm.domain..").and().haveSimpleNameEndingWith("Entity")
                .should().resideInAPackage("..model.entity")
                .check(productionClasses);
        classes().that().resideInAPackage("sm.domain..").and().haveSimpleNameEndingWith("Form")
                .should().resideInAPackage("..model.form")
                .check(productionClasses);
        classes().that().resideInAPackage("sm.domain..").and().haveSimpleNameEndingWith("VO")
                .should().resideInAPackage("..model.vo")
                .check(productionClasses);
    }

    @Test
    void mappersAndConvertersMustRemainInfrastructureDetails() {
        classes().that().resideInAPackage("sm.domain..mapper")
                .and().haveSimpleNameEndingWith("Mapper")
                .should().onlyBeAccessed().byAnyPackage(
                        "..service..", "..mapper..", "..helper..", "..job..")
                .because("Mapper 是模块实现细节，只允许业务实现和明确的内部技术协作者访问")
                .check(productionClasses);

        noClasses().that().resideInAnyPackage("..controller..", "..model..", "..constant..")
                .should().dependOnClassesThat().resideInAnyPackage("..mapper..")
                .because("Mapper 只能由业务实现与内部技术协作者使用")
                .check(productionClasses);

        noClasses().that().haveSimpleNameEndingWith("Converter")
                .should().dependOnClassesThat().resideInAnyPackage("..mapper..", "..service..")
                .because("Converter 只做纯字段映射，不得访问有副作用组件")
                .check(productionClasses);
        noClasses().that().haveSimpleNameEndingWith("Converter")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "sm.system.helper..", "sm.system.security.context..",
                        "org.springframework.cache..", "com.alicp.jetcache..")
                .because("Converter 不得读取缓存、安全上下文或外部资源")
                .check(productionClasses);
    }

    @Test
    void modelsMustNotDependBackOnBusinessOrPersistenceLayers() {
        noClasses().that().resideInAnyPackage("..model.entity..", "..model.form..", "..model.vo..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "..controller..", "..service..", "..mapper..")
                .because("Entity、Form、VO 是边界模型，不得反向依赖控制、业务或持久化实现")
                .check(productionClasses);

    }

    @Test
    void publicServicesMustUseBusinessMethodNames() {
        methods().that().arePublic()
                .and().areDeclaredInClassesThat().resideInAPackage("sm.domain..service")
                .and().areDeclaredInClassesThat().haveSimpleNameEndingWith("Service")
                .and().areDeclaredInClassesThat().haveSimpleNameNotEndingWith("TxService")
                .and().haveNameMatching("getDetail|getById")
                .should(notExist("标准详情方法统一命名为 detail，公开 Service 不得暴露通用 getById"))
                .allowEmptyShould(true)
                .check(productionClasses);
    }

    private static ArchCondition<JavaClass> useRequiredRollbackPolicy() {
        return new ArchCondition<>("声明 @Transactional(rollbackFor = Exception.class)") {
            @Override
            public void check(JavaClass javaClass, ConditionEvents events) {
                Transactional annotation = javaClass.getAnnotationOfType(Transactional.class);
                boolean valid = annotation != null && annotation.rollbackFor().length == 1
                        && annotation.rollbackFor()[0].equals(Exception.class);
                events.add(new SimpleConditionEvent(javaClass, valid,
                        javaClass.getName() + " 必须声明类级 @Transactional(rollbackFor = Exception.class)"));
            }
        };
    }

    private static ArchCondition<JavaClass> onlyBeAccessedByOwningPublicService() {
        return new ArchCondition<>("只由同模块公开 Service 访问") {
            @Override
            public void check(JavaClass txService, ConditionEvents events) {
                for (JavaAccess<?> access : txService.getAccessesToSelf()) {
                    JavaClass origin = access.getOriginOwner();
                    if (origin.equals(txService)) {
                        continue;
                    }
                    if (!origin.getPackageName().startsWith("sm.")) {
                        continue;
                    }
                    boolean owningService = origin.getPackageName().equals(txService.getPackageName())
                            && origin.getSimpleName().endsWith("Service")
                            && !origin.getSimpleName().endsWith("TxService")
                            && origin.getModifiers().contains(JavaModifier.PUBLIC);
                    events.add(new SimpleConditionEvent(access, owningService,
                            access.getDescription() + "；TxService 只允许同模块公开 Service 调用"));
                }
            }
        };
    }

    private static ArchCondition<JavaClass> onlyUseExplicitCrossDomainContracts() {
        return new ArchCondition<>("跨领域只依赖显式 contract") {
            @Override
            public void check(JavaClass origin, ConditionEvents events) {
                String originDomain = topLevelDomain(origin);
                for (Dependency dependency : origin.getDirectDependenciesFromSelf()) {
                    JavaClass target = dependency.getTargetClass();
                    if (!target.getPackageName().startsWith("sm.domain.")) {
                        continue;
                    }
                    String targetDomain = topLevelDomain(target);
                    boolean sameDomain = targetDomain.equals(originDomain);
                    boolean explicitContract = target.getPackageName().contains(".contract.")
                            || target.getPackageName().endsWith(".contract");
                    boolean valid = sameDomain || explicitContract;
                    events.add(new SimpleConditionEvent(dependency, valid,
                            dependency.getDescription() + "；跨领域只允许依赖目标 Domain 的显式稳定 contract"));
                }
            }

            private String topLevelDomain(JavaClass javaClass) {
                String remainder = javaClass.getPackageName().substring("sm.domain.".length());
                int separatorIndex = remainder.indexOf('.');
                return separatorIndex < 0 ? remainder : remainder.substring(0, separatorIndex);
            }
        };
    }

    private static ArchCondition<JavaClass> onlyDependOnExplicitInfrastructureContracts() {
        return new ArchCondition<>("只依赖明确开放的 infrastructure 技术 Contract") {
            @Override
            public void check(JavaClass origin, ConditionEvents events) {
                for (Dependency dependency : origin.getDirectDependenciesFromSelf()) {
                    JavaClass target = dependency.getTargetClass();
                    if (!target.getPackageName().startsWith("sm.infrastructure.")) {
                        continue;
                    }
                    boolean valid = target.getName().equals("sm.infrastructure.mapping.SmMapperConfig");
                    events.add(new SimpleConditionEvent(dependency, valid,
                            dependency.getDescription()
                                    + "；Domain 目前只允许依赖 infrastructure.mapping 中的 SmMapperConfig"));
                }
            }
        };
    }

    private static ArchCondition<JavaClass> notCallMethodLevelAdministratorEntryOnSelf() {
        return new ArchCondition<>("不在同类内部调用方法级 @AdministratorOnly 入口") {
            @Override
            public void check(JavaClass javaClass, ConditionEvents events) {
                for (JavaMethodCall methodCall : javaClass.getMethodCallsFromSelf()) {
                    boolean sameClass = methodCall.getOriginOwner().equals(methodCall.getTargetOwner());
                    boolean methodLevelProtected = methodCall.getTarget().resolveMember().stream()
                            .anyMatch(method -> method.isAnnotatedWith(AdministratorOnly.class));
                    boolean valid = !sameClass || !methodLevelProtected;
                    events.add(new SimpleConditionEvent(methodCall, valid,
                            methodCall.getDescription()
                                    + "；方法级 @AdministratorOnly 入口不得通过同类调用绕过代理"));
                }
            }
        };
    }

    private static class SelfInvokingFixture {
        void unprotectedEntry() {
            protectedEntry();
        }

        @AdministratorOnly
        public void protectedEntry() {
        }
    }

    private static <TYPE> ArchCondition<TYPE> notExist(String message) {
        return new ArchCondition<>(message) {
            @Override
            public void check(TYPE item, ConditionEvents events) {
                events.add(SimpleConditionEvent.violated(item, message));
            }
        };
    }
}
