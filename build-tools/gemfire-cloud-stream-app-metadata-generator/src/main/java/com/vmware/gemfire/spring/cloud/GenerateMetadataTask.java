/*
 * Copyright 2023-2024 Broadcom. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package com.vmware.gemfire.spring.cloud;

import org.apache.commons.lang3.StringEscapeUtils;
import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.file.Directory;
import org.gradle.api.file.FileSystemLocation;
import org.gradle.api.logging.Logger;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.boot.configurationprocessor.metadata.ConfigurationMetadata;
import org.springframework.boot.configurationprocessor.metadata.ItemHint;
import org.springframework.boot.configurationprocessor.metadata.ItemMetadata;
import org.springframework.boot.configurationprocessor.metadata.JsonMarshaller;
import org.springframework.core.io.FileSystemResource;
import org.springframework.util.ClassUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.inject.Inject;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static com.vmware.gemfire.spring.cloud.MetadataGeneratorPlugin.CONFIGURATION_PROPERTIES_CLASSES;
import static com.vmware.gemfire.spring.cloud.MetadataGeneratorPlugin.CONFIGURATION_PROPERTIES_INBOUND_PORTS;
import static com.vmware.gemfire.spring.cloud.MetadataGeneratorPlugin.CONFIGURATION_PROPERTIES_NAMES;
import static com.vmware.gemfire.spring.cloud.MetadataGeneratorPlugin.CONFIGURATION_PROPERTIES_OUTBOUND_PORTS;
import static com.vmware.gemfire.spring.cloud.MetadataGeneratorPlugin.DEPRECATED_BACKUP_WHITELIST_PATH;
import static com.vmware.gemfire.spring.cloud.MetadataGeneratorPlugin.DEPRECATED_WHITELIST_PATH;
import static com.vmware.gemfire.spring.cloud.MetadataGeneratorPlugin.METADATA_PATH;
import static com.vmware.gemfire.spring.cloud.MetadataGeneratorPlugin.SPRING_CLOUD_DATAFLOW_OPTION_GROUPS_PROPERTIES;
import static com.vmware.gemfire.spring.cloud.MetadataGeneratorPlugin.SPRING_CLOUD_DATAFLOW_PORT_MAPPING_PROPERTIES;
import static com.vmware.gemfire.spring.cloud.MetadataGeneratorPlugin.SPRING_CLOUD_FUNCTION_DEFINITION;
import static com.vmware.gemfire.spring.cloud.MetadataGeneratorPlugin.SPRING_CLOUD_STREAM_FUNCTION_BINDINGS;
import static com.vmware.gemfire.spring.cloud.MetadataGeneratorPlugin.SPRING_CLOUD_STREAM_FUNCTION_DEFINITION;
import static com.vmware.gemfire.spring.cloud.MetadataGeneratorPlugin.VISIBLE_PROPERTIES_PATH;

public abstract class GenerateMetadataTask extends DefaultTask {

  private final JsonMarshaller jsonMarshaller = new JsonMarshaller();
  private final Logger logger = getProject().getLogger();

  @OutputDirectory
  @Inject
  public Provider<Directory> metadataJarPath;

  @InputFiles
  public Provider<Set<FileSystemLocation>> getInputFiles() {
    Configuration configuration = getProject().getConfigurations().getByName("runtimeClasspath");
    return configuration.getElements();
  }


  @TaskAction
  public void doWork() {

    Set<FileSystemLocation> fileSystemLocations = getInputFiles().get();
    Set<File> inputFiles = fileSystemLocations.stream()
        .filter(fileSystemLocation -> {
          String fileString = fileSystemLocation.getAsFile().toString();
          return
              (fileString.contains("spring-cloud-app-gemfire") ||
                  fileString.contains("spring-cloud-consumer-gemfire") ||
                  fileString.contains("spring-cloud-supplier-gemfire") ||
                  fileString.contains("spring-cloud-app-gemfire-sink") ||
                  fileString.contains("spring-cloud-app-gemfire-source") ||
                  fileString.contains("spring-cloud-common-gemfire"));
        })
        .map(fileSystemLocation -> fileSystemLocation.getAsFile()).collect(Collectors.toSet());

    inputFiles.addAll(getProject().getTasks().getByName("jar").getOutputs().getFiles().getFiles());

    ConfigurationMetadata configurationMetadata = gatherConfigurationMetadata(inputFiles, null);

    JavaPluginExtension javaPluginExtension = getProject().getExtensions().getByType(JavaPluginExtension.class);
    inputFiles.addAll(javaPluginExtension.getSourceSets().getByName("main").getResources().getSrcDirs());

    Properties properties = gatherVisibleMetadata(inputFiles);
    produceArtifact(new MetadataGeneratorPlugin.Result(configurationMetadata, properties), getProject());
  }


  ConfigurationMetadata gatherConfigurationMetadata(Set<File> files, MetadataGeneratorPluginExtension.MetadataFilter metadataFilter) {
    ConfigurationMetadata metadata = new ConfigurationMetadata();
    try {
      ClassLoader classLoader = getClassLoader(files);
      for (File inputFile : files) {
        if (inputFile.isDirectory()) {
          File localMetadata = new File(inputFile, METADATA_PATH);
          if (localMetadata.canRead()) {
            try (InputStream is = new FileInputStream(localMetadata)) {
              ConfigurationMetadata depMetadata = jsonMarshaller.read(is);
              depMetadata = filterMetadata(depMetadata, metadataFilter);
              addEnumHints(depMetadata, classLoader);
              metadata.merge(depMetadata);
            }
          }
        } else {
          try (ZipFile zipFile = new ZipFile(inputFile)) {
            ZipEntry entry = zipFile.getEntry(METADATA_PATH);
            if (entry != null) {
              try (InputStream inputStream = zipFile.getInputStream(entry)) {
                ConfigurationMetadata depMetadata = jsonMarshaller.read(inputStream);
                depMetadata = filterMetadata(depMetadata, metadataFilter);
                addEnumHints(depMetadata, classLoader);
                metadata.merge(depMetadata);
              }
            }
          }
        }

        // Replace all escaped double quotes by a single one.
        metadata.getItems().stream().forEach(itemMetadata -> {
          if (!StringUtils.isEmpty(itemMetadata.getDescription()) && itemMetadata.getDescription()
              .contains("\"")) {
            itemMetadata.setDescription(itemMetadata.getDescription().replaceAll("\"", "'"));
          }
        });
      }
    } catch (Exception e) {
      throw new RuntimeException("Exception trying to read metadata from dependencies of project", e);
    }
    return metadata;
  }

  private ClassLoader getClassLoader(Set<File> jarPaths) {
    List<URL> urls = new LinkedList<>();

    jarPaths.forEach(jarPath -> {
      try {
        urls.add(new URL("file://" + jarPath.getPath()));
      } catch (MalformedURLException e) {
        //do nothing... just log out the path
        logger.warn("Could not find url: file://" + jarPath);
      }
    });

    URL[] urlArray = new URL[]{};
    urlArray = urls.toArray(urlArray);
    return new URLClassLoader(urlArray);
  }

  private ConfigurationMetadata filterMetadata(ConfigurationMetadata metadata, MetadataGeneratorPluginExtension.MetadataFilter metadataFilters) {
    if (metadataFilters == null
        || (CollectionUtils.isEmpty(metadataFilters.getNames()) && CollectionUtils
        .isEmpty(metadataFilters.getSourceTypes()))) {
      return metadata; // nothing to filter by so take all;
    }

    List<String> sourceTypeFilters = CollectionUtils.isEmpty(metadataFilters.getSourceTypes()) ?
        Collections.EMPTY_LIST : metadataFilters.getSourceTypes();

    List<String> nameFilters = CollectionUtils.isEmpty(metadataFilters.getNames()) ?
        Collections.EMPTY_LIST : metadataFilters.getNames();

    ConfigurationMetadata filteredMetadata = new ConfigurationMetadata();
    List<String> visibleNames = new ArrayList<>();
    for (ItemMetadata itemMetadata : metadata.getItems()) {
      String metadataName = itemMetadata.getName();
      String metadataSourceType = itemMetadata.getSourceType();
      if (StringUtils.hasText(metadataSourceType) && sourceTypeFilters.contains(metadataSourceType.trim())) {
        filteredMetadata.add(itemMetadata);
        visibleNames.add(itemMetadata.getName());
      }
      if (StringUtils.hasText(metadataName) && nameFilters.contains(metadataName.trim())) {
        filteredMetadata.add(itemMetadata);
        visibleNames.add(itemMetadata.getName());
      }

    }

    // copy the hits only for the visible metadata.
    for (ItemHint itemHint : metadata.getHints()) {
      if (itemHint != null && visibleNames.contains(itemHint.getName())) {
        filteredMetadata.add(itemHint);
      }
    }

    return filteredMetadata;
  }

  void addEnumHints(ConfigurationMetadata configurationMetadata, ClassLoader classLoader) {

    Map<String, List<ItemHint.ValueProvider>> providers = new HashMap<>();

    Map<String, ItemHint> itemHints = new HashMap<>();

    for (ItemMetadata property : configurationMetadata.getItems()) {

      if (property.isOfItemType(ItemMetadata.ItemType.PROPERTY)) {

        try {
          if (ClassUtils.isPresent(property.getType(), classLoader)) {
            Class<?> clazz = ClassUtils.resolveClassName(property.getType(), classLoader);

            if (clazz.isEnum()) {
              List<ItemHint.ValueHint> valueHints = new ArrayList<>();
              for (Object o : clazz.getEnumConstants()) {
                valueHints.add(new ItemHint.ValueHint(o, null));
              }

              if (!providers.containsKey(property.getType())) {
                providers.put(property.getType(), new ArrayList<ItemHint.ValueProvider>());
              }

              //Equals is not correct for ValueProvider

              boolean found = false;
              for (ItemHint.ValueProvider valueProvider : providers.get(property.getType())) {
                if (valueProvider.getName().equals(property.getType())) {
                  found = true;
                }
              }

              if (!found) {
                providers.get(property.getType()).add(new ItemHint.ValueProvider(property.getType(), null));
              }

              itemHints.put(property.getType(), new ItemHint(property.getName(), valueHints,
                  new ArrayList<>(providers.get(property.getType()))));

            }
          }
        } catch (Throwable e) {
          logger.warn("error working with: " + property.getType());
        }
      }
    }
    if (!CollectionUtils.isEmpty(itemHints)) {
      for (ItemHint itemHint : itemHints.values()) {
        configurationMetadata.add(itemHint);
      }
    }
  }

  Properties merge(Properties visible, InputStream is) throws IOException {
    Properties mergedProperties = new Properties();
    mergedProperties.load(is);

    if (!mergedProperties.containsKey(CONFIGURATION_PROPERTIES_CLASSES) && !mergedProperties
        .containsKey(CONFIGURATION_PROPERTIES_NAMES)) {
      logger.info(String.format("Visible properties does not contain any required keys: %s",
          StringUtils.arrayToCommaDelimitedString(new String[]{
              CONFIGURATION_PROPERTIES_CLASSES,
              CONFIGURATION_PROPERTIES_NAMES
          })));
      return visible;
    }

    if (!CollectionUtils.isEmpty(visible)) {
      mergeCommaDelimitedValue(visible, mergedProperties, CONFIGURATION_PROPERTIES_CLASSES);
      mergeCommaDelimitedValue(visible, mergedProperties, CONFIGURATION_PROPERTIES_NAMES);
    }

    return mergedProperties;
  }

  private void mergeCommaDelimitedValue(Properties currentProperties, Properties newProperties, String key) {
    if (currentProperties.containsKey(key) || newProperties.containsKey(key)) {
      Collection<String> values = StringUtils.commaDelimitedListToSet(currentProperties.getProperty(key));
      values.addAll(StringUtils.commaDelimitedListToSet(newProperties.getProperty(key)));
      if (newProperties.containsKey(key)) {
        logger.info(String.format("Merging visible property %s=%s", key, newProperties.getProperty(key)));
      }
      newProperties.setProperty(key, StringUtils.collectionToCommaDelimitedString(values));

    }
  }

  private Optional<Properties> getVisibleFromFile(Path visiblePropertiesPath) throws IOException {
    File localVisible = visiblePropertiesPath.toFile();
    if (localVisible.canRead()) {
      Properties visible = new Properties();
      try (InputStream is = new FileInputStream(localVisible)) {
        logger.info("!!!! Merging visible metadata from " + visiblePropertiesPath.toString());
        visible = merge(visible, is);
        return Optional.of(visible);
      }
    }
    return Optional.empty();
  }

  private Properties getVisibleFromZipFile(Properties visible, String path, ZipFile zipFile, ZipEntry entry)
      throws IOException {
    try (InputStream inputStream = zipFile.getInputStream(entry)) {
      logger.info("Merging visible metadata from " + path);
      visible = merge(visible, inputStream);
    }
    return visible;
  }

  Properties gatherVisibleMetadata(Set<File> inputFiles) {
    Properties visible = new Properties();
    List<String> inboundPorts = new ArrayList<>();
    List<String> outboundPorts = new ArrayList<>();
    try {
      for (File inputFile : inputFiles) {
        if (inputFile.isDirectory()) {
          String path = inputFile.getPath();
          for (String visibleProperties : new String[]{VISIBLE_PROPERTIES_PATH, DEPRECATED_WHITELIST_PATH,
              DEPRECATED_BACKUP_WHITELIST_PATH}) {
            Optional<Properties> properties;
            properties = getVisibleFromFile(Paths.get(path, visibleProperties));
            if (properties.isPresent()) {
              if (!visibleProperties.equals(VISIBLE_PROPERTIES_PATH)) {
                logger.info("Use of " + visibleProperties + " is deprecated." +
                    " Please use " + VISIBLE_PROPERTIES_PATH);

              }
              visible = properties.get();
              break;
            }
          }
          File dir = new File(path);
          for (File file : dir.listFiles()) {
            Properties properties = new Properties();

            if (file.isFile() && file.canRead() && file.getName().endsWith(".properties")) {
              try (InputStream is = new FileInputStream(file)) {
                properties.load(is);
              }
            }
            if (file.isFile() && file.canRead() && (file.getName().endsWith(".yaml") || file.getName()
                .endsWith(".yml"))) {
              YamlPropertiesFactoryBean yamlPropertiesFactoryBean = new YamlPropertiesFactoryBean();
              yamlPropertiesFactoryBean.setResources(new FileSystemResource(file));
              properties = yamlPropertiesFactoryBean.getObject();
            }

            if (!properties.isEmpty()) {
              String functionDefinitions = null;
              if (properties.containsKey(SPRING_CLOUD_FUNCTION_DEFINITION)) {
                functionDefinitions = properties.getProperty(SPRING_CLOUD_FUNCTION_DEFINITION);
              } else if (properties.containsKey(SPRING_CLOUD_STREAM_FUNCTION_DEFINITION)) {
                functionDefinitions = properties
                    .getProperty(SPRING_CLOUD_STREAM_FUNCTION_DEFINITION);
              }
              for (String functionDefinition : StringUtils
                  .delimitedListToStringArray(functionDefinitions, ";")) {
                if (functionDefinition != null) {
                  for (Object propertyKey : properties.keySet()) {
                    if (((String) propertyKey).startsWith(
                        String.format("%s.%s-in-", SPRING_CLOUD_STREAM_FUNCTION_BINDINGS,
                            functionDefinition))) {
                      inboundPorts.add(properties.getProperty((String) propertyKey));
                    }
                    if (((String) propertyKey).startsWith(
                        String.format("%s.%s-out-", SPRING_CLOUD_STREAM_FUNCTION_BINDINGS,
                            functionDefinition))) {
                      outboundPorts.add(properties.getProperty((String) propertyKey));
                    }
                  }
                }
              }
            }
          }
        } else {
          try (ZipFile zipFile = new ZipFile(new File(inputFile.getPath()))) {
            ZipEntry entry;
            for (String zipEntry : new String[]{VISIBLE_PROPERTIES_PATH, DEPRECATED_WHITELIST_PATH,
                DEPRECATED_BACKUP_WHITELIST_PATH}) {
              entry = zipFile.getEntry(zipEntry);
              if (entry != null) {
                if (!zipEntry.equals(VISIBLE_PROPERTIES_PATH)) {
                  logger.info("Use of " + zipEntry + " is deprecated." +
                      " Please use " + VISIBLE_PROPERTIES_PATH);
                }
                visible = getVisibleFromZipFile(visible, inputFile.getPath(), zipFile, entry);
                break;
              }
            }
          }
        }
      }
    } catch (Exception e) {
      throw new RuntimeException("Exception trying to read metadata from dependencies of project", e);
    }
    if (!inboundPorts.isEmpty()) {
      visible.put(CONFIGURATION_PROPERTIES_INBOUND_PORTS,
          StringUtils.arrayToCommaDelimitedString(inboundPorts.toArray(new String[0])));
    }
    if (!outboundPorts.isEmpty()) {
      visible.put(CONFIGURATION_PROPERTIES_OUTBOUND_PORTS,
          StringUtils.arrayToCommaDelimitedString(outboundPorts.toArray(new String[0])));
    }
    return visible;
  }

  void produceArtifact(MetadataGeneratorPlugin.Result result, Project project) {

    Path metadataBasePath = metadataJarPath.get().getAsFile().toPath();
    Path metadataMetaInfPath = metadataBasePath.resolve("META-INF");

    try {
      if (!metadataMetaInfPath.toFile().exists()) {
        Files.createDirectories(metadataMetaInfPath);
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    writePropertiesToFile(result.visible, metadataBasePath.resolve(VISIBLE_PROPERTIES_PATH).toFile());
    writePropertiesToFile(result.visible, metadataBasePath.resolve(DEPRECATED_WHITELIST_PATH).toFile());
    writePropertiesToFile(result.visible, metadataBasePath.resolve(DEPRECATED_BACKUP_WHITELIST_PATH).toFile());
    writePropertiesToFile(result.getPortMappingProperties(), metadataMetaInfPath.resolve(SPRING_CLOUD_DATAFLOW_OPTION_GROUPS_PROPERTIES).toFile());
    writePropertiesToFile(new Properties(), metadataMetaInfPath.resolve(SPRING_CLOUD_DATAFLOW_PORT_MAPPING_PROPERTIES).toFile());

    String metadataPropertiesPath = project.getLayout().getBuildDirectory().getAsFile().get().toPath().resolve("resources/main/META-INF").resolve("spring-configuration-metadata-encoded.properties").toString();

    try (FileWriter fileWriter = new FileWriter(metadataPropertiesPath)) {
      try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
        jsonMarshaller.write(result.metadata, byteArrayOutputStream);
        String json = byteArrayOutputStream.toString();
        json = json.replaceAll("\\$\\{", "{");
        fileWriter.write("org.springframework.cloud.dataflow.spring.configuration.metadata.json=" + StringEscapeUtils.escapeJson(json));
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private static void writePropertiesToFile(Properties properties, File file) {
    try (FileOutputStream outputStream = new FileOutputStream(file)) {
      properties.store(outputStream, null);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
