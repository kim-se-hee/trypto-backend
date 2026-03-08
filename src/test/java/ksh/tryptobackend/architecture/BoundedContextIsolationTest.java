package ksh.tryptobackend.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static ksh.tryptobackend.architecture.ArchitectureConstants.*;

@AnalyzeClasses(packages = "ksh.tryptobackend", importOptions = ImportOption.DoNotIncludeTests.class)
class BoundedContextIsolationTest {

    // TODO: RuleViolationQueryAdapter가 wallet의 QWalletJpaEntity를 직접 사용 중 — 크로스 컨텍스트 어댑터로 분리 필요
    private static final String RULE_VIOLATION_QUERY_ADAPTER =
        "ksh.tryptobackend.trading.adapter.out.RuleViolationQueryAdapter";

    // TODO: StartRoundService가 wallet의 WalletCommandPort(Output Port)를 직접 사용 중 — Input Port(UseCase)로 전환 필요
    private static final String START_ROUND_SERVICE =
        "ksh.tryptobackend.investmentround.application.service.StartRoundService";

    @ArchTest
    void trading_isolation(JavaClasses classes) {
        assertContextIsolation("trading", classes, RULE_VIOLATION_QUERY_ADAPTER);
    }

    @ArchTest
    void wallet_isolation(JavaClasses classes) {
        assertContextIsolation("wallet", classes);
    }

    @ArchTest
    void transfer_isolation(JavaClasses classes) {
        assertContextIsolation("transfer", classes);
    }

    @ArchTest
    void investmentround_isolation(JavaClasses classes) {
        assertContextIsolation("investmentround", classes, START_ROUND_SERVICE);
    }

    @ArchTest
    void ranking_isolation(JavaClasses classes) {
        assertContextIsolation("ranking", classes);
    }

    @ArchTest
    void regretanalysis_isolation(JavaClasses classes) {
        assertContextIsolation("regretanalysis", classes);
    }

    @ArchTest
    void portfolio_isolation(JavaClasses classes) {
        assertContextIsolation("portfolio", classes);
    }

    @ArchTest
    void marketdata_isolation(JavaClasses classes) {
        assertContextIsolation("marketdata", classes);
    }

    @ArchTest
    void common_should_not_depend_on_any_context(JavaClasses classes) {
        noClasses()
            .that().resideInAPackage(COMMON + "..")
            .should().dependOnClassesThat()
            .resideInAnyPackage(allContextRootPackages())
            .as("Common should not depend on any bounded context")
            .check(classes);
    }

    @ArchTest
    void batch_should_not_depend_on_context_adapters(JavaClasses classes) {
        noClasses()
            .that().resideInAPackage(BATCH + "..")
            .should().dependOnClassesThat()
            .resideInAnyPackage(allContextPackages(ADAPTER))
            .as("Batch should not depend on adapter layer of bounded contexts")
            .check(classes);
    }

    private void assertContextIsolation(String context, JavaClasses classes, String... excludedClasses) {
        for (String other : BOUNDED_CONTEXTS) {
            if (other.equals(context)) continue;

            var rule = noClasses()
                .that().resideInAPackage(contextPkg(context, ".."));

            for (String excluded : excludedClasses) {
                rule = rule.and().doNotHaveFullyQualifiedName(excluded);
            }

            rule.should().dependOnClassesThat()
                .resideInAnyPackage(forbiddenPackagesOf(other))
                .as(context + " should not access forbidden packages of " + other)
                .check(classes);
        }
    }
}
