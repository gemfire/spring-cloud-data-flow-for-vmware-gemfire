/*
 * Copyright (c) VMware, Inc. 2023. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package org.springframework.cloud.stream.app.gemfire.source.rabbit;

import com.vmware.gemfire.testcontainers.GemFireClusterContainer;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;

@SpringBootTest
public class GemfireSourceRabbitApplicationTests {

  private static GemFireClusterContainer gemFireClusterContainer;

  @BeforeAll
  static void setup() throws IOException {
    gemFireClusterContainer = new GemFireClusterContainer(1, "gemfire/gemfire:9.15.9");

    gemFireClusterContainer.acceptLicense().start();

    gemFireClusterContainer.gfsh(
        false,
        "create region --name=Test --type=REPLICATE");

    System.setProperty("gemfire.pool.subscriptionEnabled", "true");
    System.setProperty("gemfire.pool.hostAddresses", gemFireClusterContainer.getHost() + ":" + gemFireClusterContainer.getLocatorPort());
  }

  @AfterAll
  static void stopServer() {
    Awaitility.await().pollDelay(Duration.of(10, ChronoUnit.SECONDS)).timeout(15, TimeUnit.SECONDS).until(() -> true);
    if (gemFireClusterContainer != null) {
      gemFireClusterContainer.close();
    }
  }

  @Test
  public void contextLoads() {
  }

}
