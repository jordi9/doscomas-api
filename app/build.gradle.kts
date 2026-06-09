plugins {
  alias(libs.plugins.kotlin.jvm)
  alias(libs.plugins.kotlin.serialization)
  alias(libs.plugins.spotless)

  application
}

repositories {
  mavenCentral()
}

dependencies {
  implementation(platform(libs.ktor.bom))
  implementation(platform(libs.micrometer.bom))
  implementation(platform(libs.opentelemetry.bom))
  implementation(platform(libs.opentelemetry.bom.alpha))

  implementation(libs.krat.pack.core)
  implementation(libs.krat.pack.cors)
  implementation(libs.krat.pack.otel)
  implementation(libs.krat.logging)
  implementation(libs.krat.otel)
  implementation(libs.krat.time)
  implementation(libs.krat.jdbi)
  implementation(libs.ktor.server.core)
  implementation(libs.ktor.server.content.negotiation)
  implementation(libs.ktor.serialization.kotlinx.json)
  implementation(libs.ktor.server.netty)
  implementation(libs.ktor.server.metrics.micrometer)
  implementation(libs.ktor.server.call.id)
  implementation(libs.ktor.server.status.pages)
  implementation(libs.ktor.redoc)
  implementation(libs.logback)
  implementation(libs.kotlinx.serialization.json)
  implementation(libs.micrometer.core)
  implementation(libs.micrometer.registry.prometheus)
  implementation(libs.opentelemetry.api)
  implementation(libs.opentelemetry.sdk)
  implementation(libs.opentelemetry.ktor)

  implementation(libs.sqlite.jdbc)
  implementation(libs.flyway.core)

  testImplementation(platform(libs.kotest.bom))
  testImplementation(libs.archunit)
  testImplementation(libs.kotest.runner.junit5)
  testImplementation(libs.kotest.assertions.core)
  testImplementation(libs.ktor.server.test.host)
  testImplementation(libs.ktor.client.content.negotiation)
  testImplementation(libs.ktor.client.logging)
  testImplementation(libs.krat.kogiven)
  testImplementation(libs.krat.time.testlib)
  testImplementation(libs.krat.pack.testlib)
  testImplementation(libs.krat.logging.testlib)
  testImplementation(libs.krat.otel.testlib)
}

spotless {
  kotlin {
    ktlint("1.8.0")
  }
  ratchetFrom("origin/main")
}

application {
  mainClass.set("io.ktor.server.netty.EngineMain")
  applicationDefaultJvmArgs = listOf("--enable-native-access=ALL-UNNAMED")
}

tasks.named<JavaExec>("run") {
  workingDir = rootProject.projectDir
}

tasks.named<Test>("test") {
  useJUnitPlatform()
  jvmArgs("--enable-native-access=ALL-UNNAMED")
  testLogging { events("PASSED", "SKIPPED", "FAILED") }
  systemProperty("kotest.framework.config.fqn", "com.jordi9.doscomas.TestingConfig")
}
