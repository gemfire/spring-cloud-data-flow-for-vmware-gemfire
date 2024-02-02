/*
 * Copyright (c) VMware, Inc. 2024. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

/*
 * Copyright (c) VMware, Inc. 2024. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

tasks.withType<Test> {
  useJUnitPlatform()
}

group = "com.vmware.gemfire"

repositories {
  mavenCentral()
  maven {
    credentials {
      username = property("gemfireRepoUsername") as String
      password = property("gemfireRepoPassword") as String
    }
    url = uri("https://commercial-repo.pivotal.io/data3/gemfire-release-repo/gemfire")
  }
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
