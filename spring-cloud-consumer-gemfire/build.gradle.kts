plugins {
    id("java-library")
    id("idea")
    id("eclipse")
    alias(libs.plugins.lombok)
    id("gemfire-repo-artifact-publishing")
    id("gemfire-repos-plugin")
}


group = "com.vmware.gemfire.spring.cloud"

java {
    withJavadocJar()
    withSourcesJar()
    toolchain { languageVersion.set(JavaLanguageVersion.of(8)) }
}

tasks.named<Javadoc>("javadoc") {
    title = "Spring Cloud DataFlow 2021.0 for VMware GemFire 9.15 Java API Reference"
    isFailOnError=false
}

publishingDetails {
    artifactName.set("spring-cloud-consumer-2021.0-gemfire-${getGemFireBaseVersion()}")
    longName.set("Spring Cloud Dataflow Consumer for VMware GemFire")
    description.set("Spring Cloud Dataflow Consumer For VMware GemFire")
}

configurations.create("compileJava").apply {
    extendsFrom(configurations.annotationProcessor.get())
}

dependencies {

    annotationProcessor(libs.spring.boot.configuration.processor)

    api(project(":spring-cloud-common-gemfire"))

    testImplementation(libs.jackson.databind)

    testImplementation(libs.gemfire.core) {
        exclude(module = "commons-logging")
    }
    testImplementation(libs.gemfire.cq)
    testImplementation(libs.spring.boot.starter.test)

    testImplementation(platform(libs.junit.bom))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation(libs.testcontainers)
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
