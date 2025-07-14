/*
 * Copyright 2025 Broadcom. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

import groovyjarjarantlr.build.ANTLR.jarName

plugins {
  id("java-library")
  id("idea")
  id("eclipse")
  alias(libs.plugins.lombok)
  id("org.springframework.boot")
  id("gemfire-repo-artifact-publishing")
  id("gemfire.spring.cloud.metadata-generator")
  id("gemfire.spring.cloud.metadata-docs")
  id("commercial-repositories")
  id("gemfire-repos-plugin")
  id("gemfire-artifactory")
}

group = "com.vmware.gemfire.spring.cloud.stream.app"

tasks.named<Javadoc>("javadoc") {
  title = "Spring Cloud Dataflow Sink for VMware GemFire Java API Reference"
  isFailOnError = false
}

val projectArchiveName = "gemfire-sink-kafka"

publishingDetails {
  artifactName.set(projectArchiveName)
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
    getByName<MavenPublication>("maven") {
      artifact(tasks.named("metadataJar"))
      artifact(tasks.named("bootJar"))
    }
  }
}

tasks.getByName("publish").dependsOn(tasks.named("metadataJar"))
tasks.getByName("publish").dependsOn(tasks.named("bootJar"))
tasks.getByName("bootJar").dependsOn(tasks.named("metadataJar"))

configurations.create("compileJava").apply {
  extendsFrom(configurations.annotationProcessor.get())
}

dependencies {
  implementation(enforcedPlatform(libs.jackson.dependencies.bom))
  implementation(libs.spring.cloud.stream.binder.kafka)
  implementation(project(":spring-cloud-app-gemfire-sink"))
  implementation(project(":gemfire-apps-parent"))

  testImplementation(libs.kafka.test)
  testImplementation(libs.testcontainers.kafka)
  testImplementation("org.springframework.boot:spring-boot-starter-test")
  testImplementation(libs.testcontainers.gemfire)
  testImplementation(libs.testcontainers.junit.jupiter)
  testImplementation(libs.gemfire.core)
  testImplementation(libs.gemfire.cq)
  testImplementation("org.testcontainers:testcontainers")
  testImplementation(libs.awaitility)
  testImplementation("org.springframework.boot:spring-boot-starter-test")
}

tasks.register<Exec>("buildApplicationImage") {
  dependsOn(tasks.getByName("bootJar"))
  workingDir = file("$rootDir/scripts")
  jarName = tasks.getByName("bootJar").outputs.files.singleFile.absolutePath
  commandLine("bash", "./build-apps.sh", "gemfire/$projectArchiveName", "${project.version}", "$jarName")
}
