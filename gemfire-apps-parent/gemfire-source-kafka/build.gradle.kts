import org.springframework.boot.gradle.tasks.bundling.BootBuildImage

plugins {
    id("java-library")
    id("idea")
    id("eclipse")
    alias(libs.plugins.lombok)
    id("io.spring.dependency-management")
    id("org.springframework.boot")
    id("gemfire-repo-artifact-publishing")
    id("gemfire.spring.cloud.metadata-generator")
    id("gemfire.spring.cloud.metadata-docs")
    id("gemfire-repos-plugin")
}

group = "com.vmware.gemfire.spring.cloud.stream.app"

java {
    withJavadocJar()
    withSourcesJar()
    toolchain{ languageVersion.set(JavaLanguageVersion.of(8))}
}

tasks.named<Javadoc>("javadoc") {
    title = "Spring Cloud Dataflow Sink for VMware GemFire Java API Reference"
    isFailOnError=false
}

publishingDetails {
    artifactName.set("gemfire-source-kafka")
    longName.set("Spring Cloud Dataflow Source for VMware GemFire")
    description.set("Spring Cloud Dataflow Source for VMware GemFire using Kafka as a binder")
}

tasks.register<Jar>("metadataJar") {
    from(project.layout.buildDirectory.dir("generated/metadata"))
    archiveClassifier.set("metadata")
    dependsOn("generateMetadata")
}

publishing {
    publications {
        create<MavenPublication>("publication") {
            artifact("metadataJar")
            artifact("bootJar")
        }
    }
}

tasks.getByName("publish").dependsOn("metadataJar")

configurations.create("compileJava").apply {
    extendsFrom(configurations.annotationProcessor.get())
}

dependencies {

    implementation(libs.spring.cloud.stream.binder.kafka)
    implementation(project(":spring-cloud-app-gemfire-source"))
    implementation(project(":gemfire-apps-parent"))

    testImplementation(libs.kafka.test)
    testImplementation(libs.testcontainers.kafka)
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation(libs.testcontainers)
    testImplementation(libs.testcontainers.junit.jupiter)
    testImplementation(libs.gemfire.core)
    testImplementation(libs.gemfire.cq)
    testImplementation("org.testcontainers:testcontainers")
    testImplementation(libs.awaitility)
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

tasks.named<BootBuildImage>("bootBuildImage") {
    builder = "paketobuildpacks/builder-jammy-base:latest"
}
