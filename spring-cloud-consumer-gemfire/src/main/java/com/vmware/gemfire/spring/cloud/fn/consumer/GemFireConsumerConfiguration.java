/*
 * Copyright 2023-2024 Broadcom. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package com.vmware.gemfire.spring.cloud.fn.consumer;

import com.vmware.gemfire.spring.cloud.fn.common.GemFireClientRegionConfiguration;
import org.apache.geode.cache.Region;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.integration.gemfire.outbound.CacheWritingMessageHandler;
import org.springframework.messaging.Message;

import java.util.Collections;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Configuration for Geode consumer that writes an entry to a {@link Region} for a
 * Message, using a SpEL expression for a key, and the payload for the value.
 * @author David Turanski
 */
@Configuration
@EnableConfigurationProperties(GemFireConsumerProperties.class)
@Import(GemFireClientRegionConfiguration.class)
public class GemFireConsumerConfiguration {

	@Bean
	public Consumer<Message<?>> gemfireConsumer(Function<Message<?>, Message<?>> gemfireConsumerHandler,
			CacheWritingMessageHandler cacheWriter) {
		return message -> cacheWriter.handleMessage(gemfireConsumerHandler.apply(message));
	}

	@Bean
	Function<Message<?>, Message<?>> gemfireConsumerHandler(GemFireConsumerProperties properties) {
		return new GemFireConsumerHandler(properties.isJson());
	}

	@Bean
	CacheWritingMessageHandler cacheWriter(Region<?, ?> region, GemFireConsumerProperties properties) {
		CacheWritingMessageHandler messageHandler = new CacheWritingMessageHandler(region);
		messageHandler.setCacheEntries(
				Collections.singletonMap(properties.getKeyExpression(), "payload"));
		return messageHandler;
	}
}
