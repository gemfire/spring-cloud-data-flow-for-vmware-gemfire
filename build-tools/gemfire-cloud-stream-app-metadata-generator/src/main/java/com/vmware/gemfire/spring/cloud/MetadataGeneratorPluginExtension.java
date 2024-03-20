/*
 * Copyright 2023-2024 Broadcom. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package com.vmware.gemfire.spring.cloud;

import java.util.List;

public class MetadataGeneratorPluginExtension {
  private boolean storeFilteredMetadata;

  private MetadataFilter metadataFilter;

  public static class MetadataFilter {
    private List<String> names;

    private List<String> sourceTypes;

    public List<String> getNames() {
      return names;
    }

    public void setNames(List<String> names) {
      this.names = names;
    }

    public List<String> getSourceTypes() {
      return sourceTypes;
    }

    public void setSourceTypes(List<String> sourceTypes) {
      this.sourceTypes = sourceTypes;
    }

    @Override
    public String toString() {
      return "MetadataFilter{" +
          "name=" + names +
          ", sourceType=" + sourceTypes +
          '}';
    }
  }
}
