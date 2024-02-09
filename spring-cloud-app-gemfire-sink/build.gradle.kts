plugins {
  id("java-library")
  id("idea")
  id("eclipse")
  alias(libs.plugins.lombok)
  id("gemfire-repo-artifact-publishing")
  id("gemfire-repos-plugin")
}

group = "com.vmware.gemfire"

configurations.create("compileJava").apply {
  extendsFrom(configurations.annotationProcessor.get())
}

java {
  withJavadocJar()
  withSourcesJar()
  toolchain { languageVersion.set(JavaLanguageVersion.of(8)) }
}

tasks.named<Javadoc>("javadoc") {
  title = "Spring Cloud Stream App Source for VMware GemFire ${getGemFireBaseVersion()} Java API Reference"
  isFailOnError = false
}

publishingDetails {
  artifactName.set("spring-cloud-app-gemfire-sink")
  longName.set("Spring Cloud Stream App Sink for VMware GemFire")
  description.set("Spring Cloud Stream App Sink For VMware GemFire")
}

dependencies {

  api(platform(libs.spring.cloud.dependencies.bom))
  api(platform(libs.spring.boot.dependencies.bom))
  api(platform(libs.spring.cloud.stream.applications.core))
  api(platform(libs.testcontainers.dependencies.bom))
  api(platform(libs.spring.framework.bom))

  annotationProcessor(libs.spring.boot.configuration.processor)

  api(project(":spring-cloud-consumer-gemfire"))

  testImplementation(variantOf(libs.spring.cloud.stream) { classifier("test-binder") })
  testImplementation(libs.spring.cloud.stream.binder.test)
  testImplementation(libs.spring.boot.starter.test)
  testImplementation(libs.testcontainers.gemfire)
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
