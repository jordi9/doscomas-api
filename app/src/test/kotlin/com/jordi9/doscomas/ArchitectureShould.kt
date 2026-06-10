package com.jordi9.doscomas

import com.tngtech.archunit.core.domain.JavaClasses
import com.tngtech.archunit.core.importer.ClassFileImporter
import com.tngtech.archunit.core.importer.ImportOption
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses
import com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices
import io.kotest.core.spec.style.StringSpec

class ArchitectureShould : StringSpec({

  "keep domain isolated from adapters and framework or persistence packages" {
    noClasses()
      .that().resideInAPackage("..domain..")
      .should().dependOnClassesThat().resideInAnyPackage(
        "..inbound..",
        "..outbound..",
        "io.ktor..",
        "org.jdbi..",
        "java.sql.."
      )
      .check(productionClasses)
  }

  "document AccountDisplay as the domain JSON exception from ADR 0003" {
    // Account appears in bytecode because AccountDisplay is a Kotlin value class wrapping JsonObject.
    noClasses()
      .that().resideInAPackage("..domain..")
      .and().doNotHaveSimpleName("AccountDisplay")
      .and().doNotHaveSimpleName("Account")
      .should().dependOnClassesThat().resideInAPackage("kotlinx.serialization..")
      .check(productionClasses)
  }

  "keep application use cases from depending on inbound adapters" {
    noClasses()
      .that().resideInAPackage("..feature..application..")
      .should().dependOnClassesThat().resideInAPackage("..feature..inbound..")
      .check(productionClasses)
  }

  "keep inbound adapters from depending on outbound adapters" {
    noClasses()
      .that().resideInAPackage("..feature..inbound..")
      .should().dependOnClassesThat().resideInAPackage("..feature..outbound..")
      .check(productionClasses)
  }

  "keep outbound adapters from depending on inbound adapters" {
    noClasses()
      .that().resideInAPackage("..feature..outbound..")
      .should().dependOnClassesThat().resideInAPackage("..feature..inbound..")
      .check(productionClasses)
  }

  "keep shared code independent from feature code" {
    noClasses()
      .that().resideInAPackage("..shared..")
      .should().dependOnClassesThat().resideInAPackage("..feature..")
      .check(productionClasses)
  }

  "keep features independent from each other" {
    slices()
      .matching("com.jordi9.doscomas.feature.(*)..")
      .should()
      .notDependOnEachOther()
      .check(productionClasses)
  }
})

private val productionClasses: JavaClasses by lazy {
  ClassFileImporter()
    .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
    .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_JARS)
    .importPackages("com.jordi9.doscomas")
}
