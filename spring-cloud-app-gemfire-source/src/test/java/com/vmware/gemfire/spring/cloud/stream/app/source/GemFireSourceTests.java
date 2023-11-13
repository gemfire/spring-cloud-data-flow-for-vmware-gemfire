/*
 * Copyright (c) VMware, Inc. 2023. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vmware.gemfire.spring.cloud.stream.app.source;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vmware.gemfire.spring.cloud.fn.common.JsonPdxFunctions;
import com.vmware.gemfire.spring.cloud.fn.supplier.GemFireSupplierConfiguration;
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
import org.springframework.cloud.stream.binder.test.OutputDestination;
import org.springframework.cloud.stream.binder.test.TestChannelBinderConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.messaging.Message;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("integration")
public class GemFireSourceTests {

	private static ApplicationContextRunner applicationContextRunner;

	private static GemFireClusterContainer gemFireClusterContainer;


	private ObjectMapper objectMapper = new ObjectMapper();

	@BeforeAll
	static void setup() throws IOException {

		gemFireClusterContainer = new GemFireClusterContainer(1, "gemfire/gemfire:9.15.8");

		gemFireClusterContainer.acceptLicense().start();

		gemFireClusterContainer.gfsh(
				false,
				"create region --name=myRegion --type=REPLICATE");

		applicationContextRunner = new ApplicationContextRunner()
				.withUserConfiguration(
						TestChannelBinderConfiguration.getCompleteConfiguration(GemFireSourceTestApplication.class));

	}

	@AfterAll
	static void stopServer() {
		gemFireClusterContainer.close();
	}

	@Test
	void getCacheEvents() {
		applicationContextRunner
				.withPropertyValues("gemfire.region.regionName=myRegion",
						"gemfire.supplier.event-expression=key+':'+newValue",
						"gemfire.pool.connectType=locator",
						"gemfire.pool.hostAddresses=" + gemFireClusterContainer.getHost()+":" + gemFireClusterContainer.getLocatorPort(),
						"spring.cloud.function.definition=gemfireSupplier")
				.run(context -> {

					// Using local region here since it's faster
					Region<String, String> region = context.getBean(Region.class);

					region.put("foo", "bar");
					region.put("name", "dave");
					region.put("hello", "world");
					OutputDestination outputDestination = context.getBean(OutputDestination.class);

					List<String> values = new ArrayList();
					for (int i = 0; i < 3; i++) {
						Message<byte[]> message = outputDestination.receive(Duration.ofSeconds(3).toMillis(), "gemfireSupplier-out-0");
						assertThat(message).isNotNull();
						values.add(new String(message.getPayload()));
					}

					assertThat(values).containsExactly("foo:bar", "name:dave", "hello:world");
				});
	}

	@Test
	void pdxReadSerialized() {
		applicationContextRunner
				.withPropertyValues(
						"spring.cloud.function.definition=gemfireSupplier",
						"gemfire.region.regionName=myRegion",
						"gemfire.client.pdx-read-serialized=true",
						"gemfire.supplier.query=Select * from /myRegion where symbol='XXX' and price > 140",
						"gemfire.pool.connectType=locator",
						"gemfire.pool.hostAddresses=" + gemFireClusterContainer.getHost()+":" + gemFireClusterContainer.getLocatorPort())
				.run(context -> {
					OutputDestination outputDestination = context.getBean(OutputDestination.class);
					// Using local region here
					Region<String, PdxInstance> region = context.getBean(Region.class);
					putStockEvent(region, new Stock("XXX", 140.00));
					putStockEvent(region, new Stock("XXX", 140.20));
					putStockEvent(region, new Stock("YYY", 110.00));
					putStockEvent(region, new Stock("YYY", 110.01));
					putStockEvent(region, new Stock("XXX", 139.80));

					Message<byte[]> message = outputDestination.receive(Duration.ofSeconds(3).toMillis(), "gemfireSupplier-out-0");
					assertThat(message).isNotNull();
					Stock result = objectMapper.readValue(message.getPayload(), Stock.class);
					assertThat(result).isEqualTo(new Stock("XXX", 140.20));
				});
	}

	private void putStockEvent(Region<String, PdxInstance> region, Stock stock) throws JsonProcessingException {
		String json = objectMapper.writeValueAsString(stock);
		region.put(stock.getSymbol(), JsonPdxFunctions.jsonToPdx().apply(json));
	}

	@Data
	@AllArgsConstructor
	@NoArgsConstructor
	static class Stock {
		private String symbol;

		private double price;
	}

	@SpringBootApplication
	@Import(GemFireSupplierConfiguration.class)
	static class GemFireSourceTestApplication {

	}
}
