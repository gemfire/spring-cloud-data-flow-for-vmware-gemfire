/*
 * Copyright (c) VMware, Inc. 2023. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package org.springframework.cloud.stream.app.gemfire.sink.kafka;

import com.vmware.gemfire.testcontainers.GemFireClusterContainer;
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
public class GemfireSinkKafkaApplicationTests {

	private static GemFireClusterContainer gemFireClusterContainer;

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
		gemFireClusterContainer = new GemFireClusterContainer(System.getProperty("spring.test.gemfire.docker.image"));

		gemFireClusterContainer.acceptLicense().start();

		gemFireClusterContainer.gfsh(
				false,
				"create region --name=Test --type=REPLICATE");

		System.setProperty("gemfire.pool.hostAddresses",gemFireClusterContainer.getHost()+":"+gemFireClusterContainer.getLocatorPort());

		kafka.start();
	}

	@AfterAll
	static void stopServer() {
		Awaitility.await().pollDelay(Duration.of(5, ChronoUnit.SECONDS)).until(() -> true);
		if (gemFireClusterContainer != null) {
			gemFireClusterContainer.close();
		}

		kafka.stop();
	}

	@Test
	public void contextLoads() {
	}

}
