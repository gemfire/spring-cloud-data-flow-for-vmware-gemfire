/*
 * Copyright (c) VMware, Inc. 2023. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package com.vmware.gemfire.spring.cloud

import org.gradle.api.Project
import org.springframework.boot.loader.archive.Archive
import org.springframework.boot.loader.archive.ExplodedArchive
import org.springframework.boot.loader.archive.JarFileArchive
import java.io.IOException
import java.net.MalformedURLException
import java.net.URL
import java.util.*
import java.util.jar.Manifest

/**
 * An adapter to boot [Archive] that satisfies just enough of the API to craft a
 * ClassLoader that "sees" all the properties that this Mojo tries to document.
 *
 * @author Eric Bottard
 */
class ScatteredArchive(private val project: Project) : Archive {
  @Throws(MalformedURLException::class)
  override fun getUrl(): URL {
    return project.tasks.getByName("jar").outputs.files.singleFile.toURI().toURL()
  }

  override fun getManifest(): Manifest {
    throw UnsupportedOperationException()
  }

  @Throws(IOException::class)
  override fun getNestedArchives(ignored: Archive.EntryFilter): List<Archive> {
    val runtimeClasspath = project.configurations.getByName("runtimeClasspath").files
    val archives: MutableList<Archive> = ArrayList(runtimeClasspath.size)
    for (file in runtimeClasspath) {
      archives.add(if (file.isDirectory()) ExplodedArchive(file) else JarFileArchive(file))
    }
    return archives
  }

  override fun iterator(): MutableIterator<Archive.Entry> {
    // BootClassLoaderFactory.createClassLoader (which uses this iterator call) is not
    // actually
    // used here. Returning the simples thing that works.
    return Collections.emptyIterator()
  }
}
