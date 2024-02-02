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
  toolchain { languageVersion.set(JavaLanguageVersion.of(8)) }
  withJavadocJar()
  withSourcesJar()
}

tasks.named<Javadoc>("javadoc") {
    title = "Spring Cloud Dataflow Supplier 2021.0 for VMware GemFire ${getGemFireBaseVersion()} Java API Reference"
    isFailOnError=false
}

publishingDetails {
  artifactName.set("spring-cloud-supplier-2021.0-gemfire-${getGemFireBaseVersion()}")
  longName.set("Spring Cloud Dataflow Supplier for VMware GemFire")
  description.set("Spring Cloud Dataflow Supplier For VMware GemFire")
}

dependencies {

    annotationProcessor(libs.spring.boot.configuration.processor)

    api(project(":spring-cloud-common-gemfire"))

    testImplementation(libs.jackson.databind)

    testImplementation(libs.spring.boot.starter.test)

    testImplementation(platform(libs.junit.bom))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation(libs.testcontainers)
    testImplementation(libs.reactor.test)
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
