/*
 * Copyright 2023-2024 Broadcom. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package com.vmware.gemfire.spring.cloud.fn.supplier;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vmware.gemfire.spring.cloud.fn.common.JsonPdxFunctions;
import com.vmware.gemfire.testcontainers.GemFireCluster;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.geode.cache.EntryEvent;
import org.apache.geode.cache.Region;
import org.apache.geode.pdx.PdxInstance;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.time.Duration;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

@Tag("integration")
public class GemFireSupplierApplicationTests {

	private static ApplicationContextRunner applicationContextRunner;

	private static GemFireCluster gemFireCluster;

	private final ObjectMapper objectMapper = new ObjectMapper();

	@BeforeAll
	static void setup() throws IOException {
		gemFireCluster = new GemFireCluster( "gemfire/gemfire:9.15.11",1,1);

		gemFireCluster.acceptLicense().start();

		gemFireCluster.gfsh(
				false,
				"create region --name=myRegion --type=REPLICATE");

		System.setProperty("gemfire.locator.port",String.valueOf(gemFireCluster.getLocatorPort()));

		applicationContextRunner = new ApplicationContextRunner()
				.withUserConfiguration(GemFireSupplierTestApplication.class);
	}

	@AfterAll
	static void stopServer() {
		gemFireCluster.close();
	}

	@Test
	void getServerEntryEvents() {
		applicationContextRunner
				.withPropertyValues("gemfire.region.regionName=myRegion",
						"gemfire.supplier.event-expression=#root",
						"gemfire.pool.connectType=locator",
						"gemfire.pool.hostAddresses=" + "localhost:" + gemFireCluster.getLocatorPort())
				.run(context -> {
					Region region = context.getBean(Region.class);
					region.put("hello", "world");
					region.put("foo", "bar");
					region.replace("hello", "dave");

					Supplier<Flux<EntryEvent>> geodeSupplier = context.getBean("gemfireSupplier", Supplier.class);

					StepVerifier.create(geodeSupplier.get()).assertNext(cacheEvent -> {
						assertThat(cacheEvent.getOperation().isCreate()).isTrue();
						assertThat(cacheEvent.getKey()).isEqualTo("hello");
						assertThat(cacheEvent.getNewValue()).isEqualTo("world");
					}).assertNext(cacheEvent -> {
						assertThat(cacheEvent.getOperation().isCreate()).isTrue();
						assertThat(cacheEvent.getKey()).isEqualTo("foo");
						assertThat(cacheEvent.getNewValue()).isEqualTo("bar");
					}).assertNext(cacheEvent -> {
						assertThat(cacheEvent.getOperation().isUpdate()).isTrue();
						assertThat(cacheEvent.getKey()).isEqualTo("hello");
						assertThat(cacheEvent.getNewValue()).isEqualTo("dave");

					}).thenCancel().verify(Duration.ofSeconds(10));
				});
	}

	@Test
	void pdxReadSerialized() {
		applicationContextRunner
				.withPropertyValues(
						"gemfire.region.regionName=myRegion",
						"gemfire.client.pdx-read-serialized=true",
						"gemfire.pool.connectType=locator",
						"gemfire.pool.hostAddresses=" + "localhost:" + gemFireCluster.getLocatorPort())
				.run(context -> {
					Supplier<Flux<String>> geodeSupplier = context.getBean("gemfireSupplier", Supplier.class);
					// Using local region here
					Region<String, PdxInstance> region = context.getBean(Region.class);
					Stock stock = new Stock("XXX", 140.00);
					ObjectMapper objectMapper = new ObjectMapper();
					String json = objectMapper.writeValueAsString(stock);
					region.put(stock.getSymbol(), JsonPdxFunctions.jsonToPdx().apply(json));

					StepVerifier.create(geodeSupplier.get()).assertNext(val -> {
						try {
							assertThat(objectMapper.readValue(val, Stock.class)).isEqualTo(stock);
						}
						catch (JsonProcessingException e) {
							fail(e.getMessage());
						}
					}).thenCancel().verify(Duration.ofSeconds(10));
				});
	}

	@Test
	void continuousQuery() {
		applicationContextRunner
				.withPropertyValues(
						"gemfire.region.regionName=myRegion",
						"gemfire.client.pdx-read-serialized=true",
						"gemfire.supplier.query=Select * from /myRegion where symbol='XXX' and price > 140",
						"gemfire.pool.connectType=locator",
						"gemfire.pool.hostAddresses=" + "localhost:" + gemFireCluster.getLocatorPort())
				.run(context -> {
					Supplier<Flux<String>> geodeCqSupplier = context.getBean("gemfireSupplier", Supplier.class);
					// Using local region here
					Region<String, PdxInstance> region = context.getBean(Region.class);
					putStockEvent(region, new Stock("XXX", 140.00));
					putStockEvent(region, new Stock("XXX", 140.20));
					putStockEvent(region, new Stock("YYY", 110.00));
					putStockEvent(region, new Stock("YYY", 110.01));
					putStockEvent(region, new Stock("XXX", 139.80));

					StepVerifier.create(geodeCqSupplier.get()).assertNext(val -> {
						try {
							assertThat(objectMapper.readValue(val, Stock.class)).isEqualTo(new Stock("XXX", 140.20));
						}
						catch (JsonProcessingException e) {
							fail(e.getMessage());
						}
					}).thenCancel().verify(Duration.ofSeconds(10));
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
	static class GemFireSupplierTestApplication {
	}
}
