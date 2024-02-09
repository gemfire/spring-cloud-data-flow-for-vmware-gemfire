/*
 * Copyright (c) VMware, Inc. 2023. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package org.springframework.cloud.stream.app.gemfire.sink.rabbit;

import com.vmware.gemfire.testcontainers.GemFireCluster;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.time.Duration;
import java.time.temporal.ChronoUnit;

@SpringBootTest
public class GemfireSinkRabbitApplicationTests {
	private static GemFireCluster gemFireCluster;

	@BeforeAll
	static void setup() throws IOException {

		gemFireCluster = new GemFireCluster("gemfire/gemfire:9.15.10",1,1);

		gemFireCluster.acceptLicense().start();
		gemFireCluster.gfsh(
				false,
				"create region --name=Test --type=REPLICATE");

		System.setProperty("gemfire.pool.subscriptionEnabled", "true");
		System.setProperty("gemfire.pool.hostAddresses", "localhost:" + gemFireCluster.getLocatorPort());
	}

	@AfterAll
	static void stopServer() {
		Awaitility.await().pollDelay(Duration.of(5, ChronoUnit.SECONDS)).until(() -> true);
		if (gemFireCluster != null) {
			gemFireCluster.close();
		}
	}

	@Test
	public void contextLoads() {
	}

}
