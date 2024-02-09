plugins {
  id("java-library")
  id("idea")
  id("eclipse")
  alias(libs.plugins.lombok)
  id("gemfire-repo-artifact-publishing")
  id("gemfire-repos-plugin")
}

group = "com.vmware.gemfire.spring.cloud"


configurations.create("compileJava").apply {
  extendsFrom(configurations.annotationProcessor.get())
}

java {
  java { toolchain { languageVersion.set(JavaLanguageVersion.of(8)) } }
  withJavadocJar()
  withSourcesJar()
}

tasks.named<Javadoc>("javadoc") {
  title = "Spring Cloud Dataflow 2021.0 for VMware GemFire ${getGemFireBaseVersion()} Java API Reference"
  isFailOnError = false
}

publishingDetails {
  artifactName.set("spring-cloud-common-2021.0-gemfire-${getGemFireBaseVersion()}")
  longName.set("Spring Cloud Dataflow Common for VMware GemFire")
  description.set("Spring Cloud Dataflow Common For VMware GemFire")
}

dependencies {

  api(platform(libs.spring.cloud.dependencies.bom))
  api(platform(libs.spring.boot.dependencies.bom))
  api(platform(libs.spring.cloud.stream.applications.core))
  api(platform(libs.testcontainers.dependencies.bom))
  api(platform(libs.spring.framework.bom))

  annotationProcessor(libs.spring.boot.configuration.processor)

  api(libs.spring.data.gemfire) {
    exclude("org.springframework")
    exclude(module = "shiro-event")
    exclude(module = "shiro-lang")
    exclude(module = "shiro-crypto-hash")
    exclude(module = "shiro-crypto-cipher")
    exclude(module = "shiro-config-ogdl")
    exclude(module = "shiro-config-core")
    exclude(module = "shiro-cache")
    exclude(module = "commons-logging")
  }
  api(libs.gemfire.core) {
    exclude(module = "commons-logging")
  }
  api(libs.gemfire.cq)

  api(libs.org.json)
  api(libs.spring.integration.gemfire)

  api("org.springframework.boot:spring-boot-starter")
  api(libs.lombok)
  api(libs.validation.api)
  api(libs.hibernate.validator)

  testImplementation(libs.spring.boot.starter.test)
  testImplementation(libs.spring.boot.gemfire)
  testImplementation(libs.spring.boot.gemfire.logging)
  testImplementation(platform(libs.junit.bom))
  testImplementation("org.junit.jupiter:junit-jupiter")
}

fun getGemFireBaseVersion(): String {
  val gemfireVersion: String by project
  return getBaseVersion(gemfireVersion)
}

fun getBaseVersion(version: String): String {
  val split = version.split(".")
  if (split.size < 2) {
    throw RuntimeException("version is malformed: $version")
  }
  return "${split[0]}.${split[1]}"
}
