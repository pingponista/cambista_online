package com.cambistaonline.order.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

class OrderOnionArchitectureTest {

    private final JavaClasses importedClasses = new ClassFileImporter().importPackages("com.cambistaonline.order");

    @Test
    @DisplayName("El Dominio de Ordenes de Cambio no debe depender de Spring Framework")
    void domainShouldNotDependOnSpring() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("..order.domain..")
                .should().dependOnClassesThat().resideInAPackage("org.springframework..");

        rule.check(importedClasses);
    }

    @Test
    @DisplayName("El Dominio de Ordenes de Cambio no debe depender de JPA o Persistencia")
    void domainShouldNotDependOnJpa() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("..order.domain..")
                .should().dependOnClassesThat().resideInAPackage("jakarta.persistence..");

        rule.check(importedClasses);
    }

    @Test
    @DisplayName("El Dominio de Ordenes de Cambio no debe depender de la Infraestructura")
    void domainShouldNotDependOnInfrastructure() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("..order.domain..")
                .should().dependOnClassesThat().resideInAPackage("..order.infrastructure..");

        rule.check(importedClasses);
    }
}
