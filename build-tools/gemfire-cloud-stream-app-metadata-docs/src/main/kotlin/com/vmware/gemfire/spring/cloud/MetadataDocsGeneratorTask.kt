/*
 * Copyright (c) VMware, Inc. 2023. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vmware.gemfire.spring.cloud

import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.springframework.boot.configurationmetadata.ConfigurationMetadataProperty
import org.springframework.cloud.dataflow.configuration.metadata.BootApplicationConfigurationMetadataResolver
import org.springframework.cloud.dataflow.configuration.metadata.BootClassLoaderFactory
import org.springframework.cloud.dataflow.configuration.metadata.container.ContainerImageMetadataResolver
import org.springframework.util.ClassUtils
import org.springframework.util.StringUtils
import java.io.*
import java.net.URLClassLoader
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.util.*
import java.util.function.BiConsumer
import java.util.function.Consumer
import java.util.function.Function
import kotlin.math.max

@CacheableTask
abstract class MetadataDocsGeneratorTask : DefaultTask() {
  private val metadataResolver =
    BootApplicationConfigurationMetadataResolver(
      ContainerImageMetadataResolver { _: String? -> null })
  private val failOnMissingDescription = false
  private var grouped = true
  private val log = project.logger

  @get:OutputFile
  abstract val docFile: RegularFileProperty

  @TaskAction
  fun execute() {
    val readme = docFile.get().asFile

    readme.createNewFile()
    val tmp = File(readme.path + ".tmp")
    try {
      PrintWriter(tmp).use { out ->
        BufferedReader(FileReader(readme)).use { _ ->
          val archive = ScatteredArchive(project)
          val bootClassLoaderFactory = BootClassLoaderFactory(archive, null)
          bootClassLoaderFactory.createClassLoader().use { classLoader ->
            debug(classLoader)
            val properties = metadataResolver.listProperties(archive, false)
            Collections.sort(
              properties, Comparator.comparing(
                Function { obj: ConfigurationMetadataProperty -> obj.id })
            )
            val groupedProperties = groupProperties(properties)
            grouped = grouped && groupedProperties.size > 1
            if (grouped) {
              out.print("# Property definitions for **")
              project.name.replace("-", " ").split(" ").map { it.capitalize() }.forEachIndexed { index, s ->
                out.print(
                  if (index == 0) {
                    s
                  } else {
                    " $s"
                  }
                )
              }
              out.println("**")
              out.println("## Properties:")
              groupedProperties.forEach(BiConsumer { group: String, props: List<ConfigurationMetadataProperty> ->
                log.debug(
                  " Documenting group $group"
                )
                out.println(markdownForGroup(group))
                listProperties(props, out, classLoader, Function { prop: ConfigurationMetadataProperty -> prop.name })
              })
            } else {
              listProperties(properties, out, classLoader, Function { prop: ConfigurationMetadataProperty -> prop.id })
            }
            log.info(String.format("Documented %d configuration properties", properties.size))
          }
        }
      }
    } catch (e: Exception) {
      tmp.delete()
      throw RuntimeException("Error generating documentation", e)
    }
    try {
      Files.move(tmp.toPath(), readme.toPath(), StandardCopyOption.REPLACE_EXISTING)
    } catch (e: IOException) {
      throw RuntimeException("Error moving tmp file to README.md", e)
    }
  }

  private fun listProperties(
    properties: List<ConfigurationMetadataProperty>, out: PrintWriter,
    classLoader: ClassLoader,
    propertyValue: Function<ConfigurationMetadataProperty, String>
  ) {
    for (property in properties) {
      log.debug("Documenting " + property.id)
      out.println(markdownFor(property, classLoader, propertyValue))
    }
  }

  private fun groupProperties(
    properties: List<ConfigurationMetadataProperty>
  ): Map<String, MutableList<ConfigurationMetadataProperty>> {
    val groupedProperties: MutableMap<String, MutableList<ConfigurationMetadataProperty>> = LinkedHashMap()
    properties.forEach(Consumer { property: ConfigurationMetadataProperty ->
      val group = group(property.id)
      if (!groupedProperties.containsKey(group)) {
        groupedProperties[group] = LinkedList()
      }
      groupedProperties[group]!!.add(property)
    })
    return groupedProperties
  }

  private fun group(id: String): String {
    return if (id.lastIndexOf('.') > 0) id.substring(0, id.lastIndexOf('.')) else ""
  }

  private fun debug(classLoader: ClassLoader) {
    if (classLoader is URLClassLoader) {
      val urls = listOf(*classLoader.urLs)
      log.debug(
        """
  Classloader has the following URLs:
  ${urls.toString().replace(',', '\n')}
  """.trimIndent()
      )
    }
  }

  private fun markdownFor(
    property: ConfigurationMetadataProperty, classLoader: ClassLoader,
    propertyValue: Function<ConfigurationMetadataProperty, String>
  ): String {
    return String.format(
      "> **_%s_** \n >> _%s_ **( _%s_ , Default: _%s_ )** \n \n",
      propertyValue.apply(property),
      niceDescription(property),
      niceType(property),
      niceDefault(property),
      maybeHints(property, classLoader)
    )
  }

  private fun markdownForGroup(group: String): String {
    return "\n### $group\n"
  }

  private fun niceDescription(property: ConfigurationMetadataProperty): String {
    return if (property.description == null) {
      if (failOnMissingDescription) {
        throw RuntimeException("Missing description for property " + property.id)
      } else {
        "<documentation missing>"
      }
    } else property.description
  }

  private fun maybeHints(property: ConfigurationMetadataProperty, classLoader: ClassLoader): CharSequence {
    var type = property.type ?: return ""
    type = type.replace('$', '.')
    if (ClassUtils.isPresent(type, classLoader)) {
      val clazz = ClassUtils.resolveClassName(type, classLoader)
      if (clazz.isEnum) {
        return (", possible values: `" + StringUtils.arrayToDelimitedString(clazz.getEnumConstants(), "`,`")
            + "`")
      }
    }
    return ""
  }

  private fun niceDefault(property: ConfigurationMetadataProperty): String {
    return when (property.defaultValue) {
      null -> {
        "<none>"
      }

      "" -> {
        "<empty string>"
      }

      else -> {
        stringify(property.defaultValue)
      }
    }
  }

  private fun stringify(element: Any): String {
    val clazz: Class<*> = element.javaClass
    return if (clazz == ByteArray::class.java) {
      (element as ByteArray).contentToString()
    } else if (clazz == ShortArray::class.java) {
      (element as ShortArray).contentToString()
    } else if (clazz == IntArray::class.java) {
      (element as IntArray).contentToString()
    } else if (clazz == LongArray::class.java) {
      (element as LongArray).contentToString()
    } else if (clazz == CharArray::class.java) {
      (element as CharArray).contentToString()
    } else if (clazz == FloatArray::class.java) {
      (element as FloatArray).contentToString()
    } else if (clazz == DoubleArray::class.java) {
      (element as DoubleArray).contentToString()
    } else if (clazz == BooleanArray::class.java) {
      (element as BooleanArray).contentToString()
    } else if (element is Array<*> && element.isArrayOf<Any>()) {
      (element).contentDeepToString()
    } else {
      element.toString()
    }
  }

  private fun niceType(property: ConfigurationMetadataProperty): String {
    val type = property.type ?: return "<unknown>"
    return niceType(type)
  }

  private fun niceType(type: String): String {
    val parts: MutableList<String> = ArrayList()
    var openBrackets = 0
    var lastGenericPart = 0
    for (i in type.indices) {
      when (type[i]) {
        '<' -> if (openBrackets++ == 0) {
          parts.add(type.substring(0, i))
          lastGenericPart = i + 1
        }

        '>' -> if (--openBrackets == 0) {
          parts.add(type.substring(lastGenericPart, i))
        }

        ',' -> if (openBrackets == 1) {
          parts.add(type.substring(lastGenericPart, i))
          lastGenericPart = i + 1
        }

        ' ' -> if (openBrackets == 1) {
          lastGenericPart++
        }
      }
    }
    return if (parts.isEmpty()) {
      unqualify(type) // simple type
    } else { // type with generics
      val sb = StringBuilder(unqualify(parts[0]))
      for (i in 1 until parts.size) {
        if (i == 1) {
          sb.append('<')
        }
        sb.append(unqualify(niceType(parts[i])))
        if (i == parts.size - 1) {
          sb.append('>')
        } else {
          sb.append(", ")
        }
      }
      sb.toString()
    }
  }

  private fun unqualify(type: String): String {
    val lastDot = type.lastIndexOf('.')
    val lastDollar = type.lastIndexOf('$')
    return type.substring(max(lastDot, lastDollar) + 1)
  }
}
