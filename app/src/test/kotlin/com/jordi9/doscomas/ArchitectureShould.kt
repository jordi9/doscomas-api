package com.jordi9.doscomas

import com.tngtech.archunit.core.domain.JavaClasses
import com.tngtech.archunit.core.importer.ClassFileImporter
import com.tngtech.archunit.core.importer.ImportOption
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses
import com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices
import io.kotest.core.spec.style.StringSpec

class ArchitectureShould : StringSpec({

  "keep domain isolated from adapters and frameworks" {
    noClasses()
      .that().resideInAPackage("..domain..")
      .should().dependOnClassesThat().resideInAnyPackage(
        "..inbound..",
        "..outbound..",
        "io.ktor..",
        "org.jdbi.."
      )
      .check(productionClasses)
  }

  "keep inbound adapters from depending on outbound adapters" {
    noClasses()
      .that().resideInAPackage("..feature..inbound..")
      .should().dependOnClassesThat().resideInAPackage("..feature..outbound..")
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
