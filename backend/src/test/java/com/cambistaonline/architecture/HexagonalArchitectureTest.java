package com.cambistaonline.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * Suite de pruebas de arquitectura Hexagonal con ArchUnit.
 * Valida que el dominio y la capa de aplicación estén completamente
 * desacoplados de la infraestructura, adaptadores, Kafka y RabbitMQ.
 */
public class HexagonalArchitectureTest {

    private JavaClasses importedClasses;

    @BeforeEach
    void setUp() {
        importedClasses = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages("com.cambistaonline");
    }

    /**
     * El dominio (entidades, eventos, value objects, ports) NO debe depender
     * de infraestructura, adaptadores ni frameworks de Spring, Kafka o RabbitMQ.
     */
    @Test
    void domainShouldNotDependOnOuterLayers() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("..domain..")
                .should().dependOnClassesThat()
                .resideInAnyPackage(
                        "..infrastructure..",
                        "..adapters..",
                        "org.springframework..",
                        "org.apache.kafka..",
                        "org.springframework.kafka..",
                        "org.springframework.amqp..",
                        "com.rabbitmq.."
                );

        rule.check(importedClasses);
    }

    /**
     * La capa de aplicación (services, use cases, ports) NO debe depender
     * de la infraestructura concreta ni de los adaptadores.
     * Sí puede depender de interfaces de sus propios puertos (ports).
     */
    @Test
    void applicationShouldNotDependOnInfrastructureOrAdapters() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("..application..")
                .should().dependOnClassesThat()
                .resideInAnyPackage(
                        "..infrastructure..",
                        "..adapters..",
                        "org.apache.kafka..",
                        "org.springframework.kafka..",
                        "org.springframework.amqp..",
                        "com.rabbitmq.."
                );

        rule.check(importedClasses);
    }

    /**
     * Los adaptadores de Kafka y RabbitMQ deben vivir exclusivamente en
     * el paquete adapters, no en domain ni application.
     */
    @Test
    void kafkaAndRabbitMQShouldOnlyLiveInAdaptersOrInfrastructure() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("..domain..")
                .or().resideInAPackage("..application..")
                .should().dependOnClassesThat()
                .resideInAnyPackage(
                        "org.apache.kafka..",
                        "org.springframework.kafka..",
                        "org.springframework.amqp..",
                        "com.rabbitmq.."
                );

        rule.check(importedClasses);
    }
}
