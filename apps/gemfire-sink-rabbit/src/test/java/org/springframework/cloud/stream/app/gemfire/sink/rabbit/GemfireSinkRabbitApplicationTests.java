/*
 * Copyright (c) VMware, Inc. 2023. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package org.springframework.cloud.stream.app.gemfire.sink.rabbit;

import com.vmware.gemfire.testcontainers.GemFireClusterContainer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;

@SpringBootTest
public class GemfireSinkRabbitApplicationTests {
	private static GemFireClusterContainer gemFireClusterContainer;

	@BeforeAll
	static void setup() throws IOException {
		gemFireClusterContainer = new GemFireClusterContainer("gemfire/gemfire:9.15.8");

		gemFireClusterContainer.acceptLicense().start();

		gemFireClusterContainer.gfsh(
				false,
				"create region --name=Test --type=REPLICATE");

		System.setProperty("gemfire.pool.hostAddresses",gemFireClusterContainer.getHost()+":"+gemFireClusterContainer.getLocatorPort());
	}

	@AfterAll
	static void stopServer() {
		if (gemFireClusterContainer != null) {
			gemFireClusterContainer.close();
		}
	}


	@Test
	public void contextLoads() {
	}

}
