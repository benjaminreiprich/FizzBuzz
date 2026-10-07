package io.github.benjaminreiprich.fizzbuzz;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

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
}
