/*
 * Copyright (c) VMware, Inc. 2023. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package com.vmware.gemfire.spring.cloud;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.tasks.TaskProvider;
import org.springframework.boot.configurationprocessor.metadata.ConfigurationMetadata;

import java.util.Properties;

public class MetadataGeneratorPlugin implements Plugin<Project> {

  static final String METADATA_PATH = "META-INF/spring-configuration-metadata.json";

  static final String CONFIGURATION_PROPERTIES_INBOUND_PORTS = "configuration-properties.inbound-ports";

  static final String CONFIGURATION_PROPERTIES_OUTBOUND_PORTS = "configuration-properties.outbound-ports";

  static final String VISIBLE_PROPERTIES_PATH = "META-INF/dataflow-configuration-metadata.properties";

  static final String DEPRECATED_WHITELIST_PATH = "META-INF/dataflow-configuration-metadata-whitelist.properties";

  static final String DEPRECATED_BACKUP_WHITELIST_PATH = "META-INF/spring-configuration-metadata-whitelist.properties";

  static final String SPRING_CLOUD_DATAFLOW_PORT_MAPPING_PROPERTIES = "dataflow-configuration-port-mapping.properties";

  static final String SPRING_CLOUD_DATAFLOW_OPTION_GROUPS_PROPERTIES = "dataflow-configuration-option-groups.properties";

  static final String CONFIGURATION_PROPERTIES_CLASSES = "configuration-properties.classes";

  static final String CONFIGURATION_PROPERTIES_NAMES = "configuration-properties.names";

  static final String SPRING_CLOUD_FUNCTION_DEFINITION = "spring.cloud.function.definition";

  static final String SPRING_CLOUD_STREAM_FUNCTION_DEFINITION = "spring.cloud.stream.function.definition";

  static final String SPRING_CLOUD_STREAM_FUNCTION_BINDINGS = "spring.cloud.stream.function.bindings";

  @Override
  public void apply(Project project) {
    MetadataGeneratorPluginExtension metadataGenerator = project.getExtensions().create("metadataGenerator", MetadataGeneratorPluginExtension.class);

    TaskProvider<GenerateMetadataTask> metadataTaskTaskProvider = project.getTasks().register("generateMetadata", GenerateMetadataTask.class, task -> {
      task.dependsOn("assemble");
      project.getTasks().getByName("publish").dependsOn(task);
      task.metadataJarPath = project.getLayout().getBuildDirectory().dir("generated/metadata");
    });

//    TaskProvider<Jar> jarTaskProvider = project.getTasks().register("generateMetadataJar", Jar.class, jar -> {
//      jar.from(project.getLayout().getBuildDirectory().dir("generated/metadata"));
//      jar.getArchiveClassifier().set("metadata");
//
//    });

//    jarTaskProvider.configure(jar -> jar.dependsOn("generateMetadata"));
  }

  static final class Result {
    final ConfigurationMetadata metadata;

    final Properties visible;

    Properties getPortMappingProperties() {
      Properties portMappingProperties = new Properties();
      visible.entrySet().stream()
          .filter(e -> e.getKey().equals(CONFIGURATION_PROPERTIES_OUTBOUND_PORTS) || e.getKey().equals(CONFIGURATION_PROPERTIES_INBOUND_PORTS))
          .forEach(e -> portMappingProperties.put(e.getKey(), e.getValue()));
      return portMappingProperties;
    }

    Result(ConfigurationMetadata metadata, Properties visible) {
      this.metadata = metadata;
      this.visible = visible;
    }
  }

}
