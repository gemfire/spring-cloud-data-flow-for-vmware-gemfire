/*
 * Copyright 2025 Broadcom. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

plugins {
    id("java-gradle-plugin")
    alias(libs.plugins.ben.manes.versions)
    `kotlin-dsl`
}

repositories {
    mavenCentral()
    gradlePluginPortal()
    maven { url = uri("https://repo.spring.io/plugins-release") }
    val repositoryConfigFilePath = providers.gradleProperty("spring.gemfire.repositories").getOrElse(
        providers.environmentVariable("HOME").get() + "/.gradle/gradleRepositories.json"
    )
    val jsonString = File(repositoryConfigFilePath).readText(Charsets.UTF_8)
    val repositories = groovy.json.JsonSlurper().parseText(jsonString) as Map<*, *>
    (repositories["repositories"] as List<*>).filterNotNull().map { entry -> entry as Map<*, *> }
        .forEach { entry ->
            entry.apply {
                maven {
                    url = uri(entry["url"]!! as String)
                    if (!entry["username"]?.toString().isNullOrBlank()) {
                        credentials {
                            username = entry["username"] as String
                            password = entry["password"] as String
                        }
                    }
                }
            }
        }
}

dependencies {
    implementation(gradleApi())
    implementation(libs.spring.cloud.dataflow.configuration.metadata)
    implementation(libs.spring.boot.configuration.processor)
    implementation(libs.spring.core)
    implementation(libs.spring.beans)
}

gradlePlugin {
    plugins {
        create("metadata-docs") {
            id = "gemfire.spring.cloud.metadata-docs"
            implementationClass = "com.vmware.gemfire.spring.cloud.MetadataDocsGeneratorPlugin"
        }
    }
}
