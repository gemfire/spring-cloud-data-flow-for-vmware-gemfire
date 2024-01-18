// Copyright (c) VMware, Inc. 2024. All rights reserved.
// SPDX-License-Identifier: Apache-2.0

dependencyResolutionManagement {
  versionCatalogs {
    create("libs") {
      from(files("../../gradle/publishing.versions.toml"))
    }
  }
}