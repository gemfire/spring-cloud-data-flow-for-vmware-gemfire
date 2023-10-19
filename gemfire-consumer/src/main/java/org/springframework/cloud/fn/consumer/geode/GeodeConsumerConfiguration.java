/*
 * Copyright (c) VMware, Inc. 2023. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package org.springframework.cloud.fn.consumer.geode;

import java.util.Collections;
import java.util.function.Consumer;
import java.util.function.Function;

import org.apache.geode.cache.Region;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.fn.common.geode.GeodeClientRegionConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.integration.gemfire.outbound.CacheWritingMessageHandler;
import org.springframework.messaging.Message;

/**
 * Configuration for Geode consumer that writes an entry to a {@link Region} for a
 * Message, using a SpEL expression for a key, and the payload for the value.
 * @author David Turanski
 */
@Configuration
@EnableConfigurationProperties(GeodeConsumerProperties.class)
@Import(GeodeClientRegionConfiguration.class)
public class GeodeConsumerConfiguration {

	@Bean
	public Consumer<Message<?>> geodeConsumer(Function<Message<?>, Message<?>> geodeConsumerHandler,
			CacheWritingMessageHandler cacheWriter) {
		return message -> cacheWriter.handleMessage(geodeConsumerHandler.apply(message));
	}

	@Bean
	Function<Message<?>, Message<?>> geodeConsumerHandler(GeodeConsumerProperties properties) {
		return new GeodeConsumerHandler(properties.isJson());
	}

	@Bean
	CacheWritingMessageHandler cacheWriter(Region<?, ?> region, GeodeConsumerProperties properties) {
		CacheWritingMessageHandler messageHandler = new CacheWritingMessageHandler(region);
		messageHandler.setCacheEntries(
				Collections.singletonMap(properties.getKeyExpression(), "payload"));
		return messageHandler;
	}
}
