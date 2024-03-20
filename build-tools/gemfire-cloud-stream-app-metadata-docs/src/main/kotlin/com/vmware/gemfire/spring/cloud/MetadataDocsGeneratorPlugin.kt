/*
 * Copyright 2023-2024 Broadcom. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vmware.gemfire.spring.cloud

import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.Project

class MetadataDocsGeneratorPlugin : Plugin<Project> {
  override fun apply(project: Project) {
    val generateDocs = project.tasks.register(
      "generateDocs",
      MetadataDocsGeneratorTask::class.java
    )

    generateDocs.configure(Action {
      val file = project.layout.projectDirectory.file("README.md")
      val resolvedFile = file.asFile
      if(resolvedFile.exists())
      {
        resolvedFile.delete()
      }
      docFile.set(file)
      dependsOn("jar")
    })
  }
}
