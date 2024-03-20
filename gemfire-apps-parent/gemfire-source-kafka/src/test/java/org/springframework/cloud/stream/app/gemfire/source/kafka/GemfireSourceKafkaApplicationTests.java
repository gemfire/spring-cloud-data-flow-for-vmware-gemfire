/*
 * Copyright 2023-2024 Broadcom. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package org.springframework.cloud.stream.app.gemfire.source.kafka;

import com.vmware.gemfire.testcontainers.GemFireCluster;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.utility.DockerImageName;

import java.io.IOException;
import java.time.Duration;
import java.time.temporal.ChronoUnit;

@SpringBootTest
public class GemfireSourceKafkaApplicationTests {
  private static GemFireCluster gemFireCluster;

  @Container
  static final KafkaContainer kafka = new KafkaContainer(
      DockerImageName.parse("confluentinc/cp-kafka:latest")
  );

  @DynamicPropertySource
  static void overrideProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
  }

  @BeforeAll
  static void setup() throws IOException {

    gemFireCluster = new GemFireCluster( "gemfire/gemfire:9.15.10",1,1);

    gemFireCluster.acceptLicense().start();
    gemFireCluster.gfsh(
        false,
        "create region --name=Test --type=REPLICATE");

    System.setProperty("gemfire.pool.subscriptionEnabled", "true");
    System.setProperty("gemfire.pool.hostAddresses", "localhost:" + gemFireCluster.getLocatorPort());

    kafka.start();
  }

  @AfterAll
  static void stopServer() {
    Awaitility.await().pollDelay(Duration.of(5, ChronoUnit.SECONDS)).until(() -> true);
    if (gemFireCluster != null) {
      gemFireCluster.close();
    }
    kafka.stop();
  }

  @Test
  public void contextLoads() {
  }

}
