/*
 * Copyright 2024 Broadcom. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

tasks.withType<Test> {
  useJUnitPlatform()
}

group = "com.vmware.gemfire"

repositories {
  mavenCentral()
}

repositories {
  val additionalMavenRepoURLs = project.ext.get("additionalMavenRepoURLs") as String
  if (additionalMavenRepoURLs.isNotEmpty() && additionalMavenRepoURLs.isNotBlank()) {
    additionalMavenRepoURLs.split(",").forEach {
      project.repositories.maven {
        this.url = uri(it)
      }
    }
  }
}

configurations.all {
  resolutionStrategy.cacheChangingModulesFor(0, "minutes")
}
