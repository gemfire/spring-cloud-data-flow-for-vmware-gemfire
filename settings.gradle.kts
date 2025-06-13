/*
 * Copyright 2025 Broadcom. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

import java.io.FileInputStream
import java.util.*

pluginManagement {
  includeBuild("build-tools/gemfire-cloud-stream-app-metadata-generator")
  includeBuild("build-tools/gemfire-cloud-stream-app-metadata-docs")
  includeBuild("build-tools/publishing")
  includeBuild("build-tools/convention-plugins")
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
}

rootProject.name = "spring-cloud-data-flow-for-vmware-gemfire"
include("spring-cloud-common-gemfire")
include("spring-cloud-consumer-gemfire")
include("spring-cloud-supplier-gemfire")

include("spring-cloud-app-gemfire-source")
include("spring-cloud-app-gemfire-sink")

include("gemfire-apps-parent")
include("gemfire-apps-parent:gemfire-sink-kafka")
include("gemfire-apps-parent:gemfire-sink-rabbit")
include("gemfire-apps-parent:gemfire-source-rabbit")
include("gemfire-apps-parent:gemfire-source-kafka")

dependencyResolutionManagement {
  versionCatalogs {
    create("libs") {
      val properties = Properties()
      properties.load(FileInputStream(layout.rootDirectory.asFile.toPath().resolve("gradle.properties").toFile()))
      versionOverrideFromProperties(this, properties)
    }
  }
}

private fun versionOverrideFromProperty(
  versionCatalogBuilder: VersionCatalogBuilder,
  propertyName: String,
  propertiesFile: Properties
): String {
  val propertyValue = providers.systemProperty(propertyName).getOrElse(propertiesFile.getProperty(propertyName))
  return versionCatalogBuilder.version(propertyName, propertyValue)
}

private fun versionOverrideFromProperties(versionCatalogBuilder: VersionCatalogBuilder, properties: Properties) {
  versionOverrideFromProperty(versionCatalogBuilder, "gemfireVersion", properties)
  versionOverrideFromProperty(versionCatalogBuilder, "springIntegrationGemFireVersion", properties)
  versionOverrideFromProperty(versionCatalogBuilder, "springBootGemFireVersion", properties)
  versionOverrideFromProperty(versionCatalogBuilder, "springDataGemFireVersion", properties)
}
