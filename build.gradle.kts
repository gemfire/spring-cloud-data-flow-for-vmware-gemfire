/*
 * Copyright 2025 Broadcom. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask
import okhttp3.CertificatePinner.Companion.pin
import org.gradle.api.internal.artifacts.ivyservice.resolveengine.graph.conflicts.DefaultCapabilitiesConflictHandler.candidate
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.kotlin.dsl.libs
import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
  alias(libs.plugins.spring.boot)
  alias(libs.plugins.version.catalog.update)
  alias(libs.plugins.ben.manes.versions)
  id("commercial-repositories")
  id("gemfire-repos-plugin")
  id("idea")
  id("eclipse")
  id("java")
  id("gemfire-artifactory")
}

// Suppress warning from gemfire-artifactory plugin. We need the module to be on this project in order to get buildInfo
// uploaded, but there is no artifact on the root project, so we skip that part.
tasks.artifactoryPublish {
  skip = true
}

java {
  toolchain { languageVersion.set(JavaLanguageVersion.of(8)) }
}

tasks.named<BootJar>("bootJar") {
  enabled = false
}

versionCatalogUpdate {
  // These options will be set as default for all version catalogs
  sortByKey = true
  // Referenced that are pinned are not automatically updated.
  // They are also not automatically kept however (use keep for that).
  pin {
  }
  keep {
    keepUnusedVersions = true
    // keep all libraries that aren't used in the project
    keepUnusedLibraries = true
    // keep all plugins that aren't used in the project
    keepUnusedPlugins = true
  }
  versionCatalogs{
    create("publish"){
      catalogFile = file("gradle/publish.versions.toml")
    }
  }
}

tasks.withType<DependencyUpdatesTask> {
  rejectVersionIf {
    !isPatch(candidate.version, currentVersion)
  }
}

fun isPatch(candidateVersion: String, currentVersion: String): Boolean {
  val candidateSplit = candidateVersion.split(".")
  val currentSplit = currentVersion.split(".")

  if (currentSplit.size == 3) {
    if (candidateSplit.size == currentSplit.size) {
      if (candidateSplit[0] != currentSplit[0]) {
        return false
      }
      if (candidateSplit[1] != currentSplit[1]) {
        return false
      }
      return true
    }
  } else {
    return false
  }
  return false
}
