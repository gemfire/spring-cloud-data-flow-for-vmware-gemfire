import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask
import org.springframework.boot.gradle.tasks.bundling.BootJar

buildscript {
  repositories {
    mavenCentral()
    gradlePluginPortal()
    maven { url = uri("https://repo.spring.io/plugins-release") }
  }
}

plugins {
  alias(libs.plugins.spring.boot)
  alias(libs.plugins.version.catalog.update)
  alias(libs.plugins.ben.manes.versions)
  id("gemfire-repos-plugin")
  id("idea")
  id("eclipse")
  id("java")
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

  if (candidateSplit.size == currentSplit.size && currentSplit.size == 3) {
    if (candidateSplit[0] != currentSplit[0]) {
      return false
    }
    if (candidateSplit[1] != currentSplit[1]) {
      return false
    }
  }
  return true
}