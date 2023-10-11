/*
 * Copyright 2020-2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.fn.consumer.geode;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vmware.gemfire.testcontainers.GemFireClusterContainer;
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
import org.springframework.messaging.Message;
import org.springframework.messaging.support.GenericMessage;

import java.io.IOException;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("integration")
public class GeodeConsumerApplicationTests {
	private static ApplicationContextRunner applicationContextRunner;

	private static GemFireClusterContainer gemFireClusterContainer;

	private ObjectMapper objectMapper = new ObjectMapper();

	@BeforeAll
	static void setup() throws IOException {
		gemFireClusterContainer = new GemFireClusterContainer(1, "gemfire/gemfire:9.15.8");

		gemFireClusterContainer.acceptLicense().start();

		gemFireClusterContainer.gfsh(
				false,
				"create region --name=Stocks --type=REPLICATE");

		System.setProperty("gemfire.locator.port",String.valueOf(gemFireClusterContainer.getLocatorPort()));

		applicationContextRunner = new ApplicationContextRunner()
				.withUserConfiguration(GeodeConsumerTestApplication.class);
	}

	@AfterAll
	static void stopServer() {
		gemFireClusterContainer.stop();
	}

	@Test
	void consumeWithJsonEnabled() {
		applicationContextRunner
				.withPropertyValues(
						"gemfire.region.regionName=Stocks",
						"gemfire.consumer.json=true",
						"gemfire.consumer.key-expression=payload.getField('symbol')",
						"gemfire.pool.connectType=locator",
						"gemfire.pool.hostAddresses=localhost:" + gemFireClusterContainer.getLocatorPort())
				.run(context -> {
					Consumer<Message<?>> geodeConsumer = context.getBean("geodeConsumer", Consumer.class);

					String json = objectMapper.writeValueAsString(new Stock("XXX", 100.00));
					geodeConsumer.accept(new GenericMessage<>(json));

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
						"gemfire.region.regionName=Stocks",
						"gemfire.consumer.key-expression='key'",
						"gemfire.pool.connectType=locator",
						"gemfire.pool.hostAddresses=localhost:" + gemFireClusterContainer.getLocatorPort())
				.run(context -> {
					Consumer<Message<?>> geodeConsumer = context.getBean("geodeConsumer", Consumer.class);

					geodeConsumer.accept(new GenericMessage<>("value"));

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
	static class GeodeConsumerTestApplication {
	}

}
