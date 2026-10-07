package io.github.benjaminreiprich.fizzbuzz;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

@AnalyzeClasses(packagesOf = FizzBuzzApplication.class, importOptions = ImportOption.DoNotIncludeTests.class)
class ArchitectureTest {

    @ArchTest
    static final ArchRule domain_should_only_depend_on_the_jdk = classes()
            .that()
            .resideInAPackage("..domain..")
            .should()
            .onlyDependOnClassesThat()
            .resideInAnyPackage("java..", "..domain..")
            .because("business rules must stay readable and testable without any framework");

    @ArchTest
    static final ArchRule layers_should_follow_the_dependency_rule = layeredArchitecture()
            .consideringOnlyDependenciesInLayers()
            .layer("Api")
            .definedBy("..fizzbuzz.api..")
            .layer("Application")
            .definedBy("..fizzbuzz.application..")
            .layer("Domain")
            .definedBy("..fizzbuzz.domain..")
            .layer("Infrastructure")
            .definedBy("..fizzbuzz.infrastructure..")
            .layer("Config")
            .definedBy("..fizzbuzz.config..")
            .whereLayer("Api")
            .mayNotBeAccessedByAnyLayer()
            .whereLayer("Application")
            .mayOnlyBeAccessedByLayers("Api", "Infrastructure", "Config")
            .whereLayer("Domain")
            .mayOnlyBeAccessedByLayers("Api", "Application", "Infrastructure", "Config")
            .whereLayer("Infrastructure")
            .mayOnlyBeAccessedByLayers("Config")
            .whereLayer("Config")
            .mayOnlyBeAccessedByLayers("Api");
}
