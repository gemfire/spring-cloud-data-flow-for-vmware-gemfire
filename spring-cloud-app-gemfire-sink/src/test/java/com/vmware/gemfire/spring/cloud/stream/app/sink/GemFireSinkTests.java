/*
 * Copyright 2023-2024 Broadcom. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package com.vmware.gemfire.spring.cloud.stream.app.sink;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vmware.gemfire.spring.cloud.fn.consumer.GemFireConsumerConfiguration;
import com.vmware.gemfire.testcontainers.GemFireCluster;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.geode.cache.Region;
import org.apache.geode.pdx.PdxInstance;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.cloud.stream.binder.test.InputDestination;
import org.springframework.cloud.stream.binder.test.TestChannelBinderConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.messaging.support.GenericMessage;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("integration")
public class GemFireSinkTests {

	private static ApplicationContextRunner applicationContextRunner;

	private static GemFireCluster gemFireCluster;

	private ObjectMapper objectMapper = new ObjectMapper();

	@BeforeAll
	static void setup() throws IOException {
		gemFireCluster = new GemFireCluster( "gemfire/gemfire:9.15.11",1,1);

		gemFireCluster.acceptLicense().start();

		gemFireCluster.gfsh(
				false,
				"create region --name=Stocks --type=REPLICATE");

		applicationContextRunner = new ApplicationContextRunner()
				.withUserConfiguration(
						TestChannelBinderConfiguration.getCompleteConfiguration(GemFireSinkTestApplication.class));
	}

	@AfterAll
	static void stopServer() {
		gemFireCluster.close();
	}

	@Test
	void consumeWithJsonEnabled() {
		applicationContextRunner
				.withPropertyValues(
						"spring.cloud.function.definition=gemfireConsumer",
						"gemfire.region.regionName=Stocks",
						"gemfire.consumer.json=true",
						"gemfire.consumer.key-expression=payload.getField('symbol')",
						"gemfire.pool.connectType=locator",
						"gemfire.pool.hostAddresses=localhost:" + gemFireCluster.getLocatorPort())
				.run(context -> {
					InputDestination inputDestination = context.getBean(InputDestination.class);

					byte[] json = objectMapper.writeValueAsBytes(new Stock("XXX", 100.00));
					inputDestination.send(new GenericMessage<>(json));

					Region<String, PdxInstance> region = context.getBean(Region.class);
					PdxInstance instance = region.get("XXX");
					assertThat(instance.getField("price")).isEqualTo(100.00);
					region.close();
				});
	}

	@Test
	void consumeWithoutJsonEnabled() {
		applicationContextRunner
				.withPropertyValues(
						"spring.cloud.function.definition=gemfireConsumer",
						"gemfire.region.regionName=Stocks",
						"gemfire.consumer.key-expression='key'",
						"gemfire.pool.connectType=locator",
						"gemfire.pool.hostAddresses=localhost:" + gemFireCluster.getLocatorPort())
				.run(context -> {
					InputDestination inputDestination = context.getBean(InputDestination.class);
					inputDestination.send(new GenericMessage<>("value"));

					Region<String, String> region = context.getBean(Region.class);
					String value = region.get("key");
					assertThat(value).isEqualTo("value");
					region.close();
				});
	}

	@Data
	@AllArgsConstructor
	@NoArgsConstructor
	static class Stock {
		private String symbol;

		private double price;
	}

	@SpringBootApplication
	@Import(GemFireConsumerConfiguration.class)
	static class GemFireSinkTestApplication {

	}
}
