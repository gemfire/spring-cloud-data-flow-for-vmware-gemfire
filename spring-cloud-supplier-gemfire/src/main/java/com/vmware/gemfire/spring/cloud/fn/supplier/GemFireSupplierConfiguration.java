/*
 * Copyright 2023-2024 Broadcom. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package com.vmware.gemfire.spring.cloud.fn.supplier;

import com.vmware.gemfire.spring.cloud.fn.common.GemFireClientRegionConfiguration;
import com.vmware.gemfire.spring.cloud.fn.common.JsonPdxFunctions;
import org.apache.geode.cache.Region;
import org.apache.geode.cache.client.ClientCache;
import org.apache.geode.pdx.PdxInstance;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.data.gemfire.client.Interest;
import org.springframework.data.gemfire.listener.ContinuousQueryListenerContainer;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.channel.FluxMessageChannel;
import org.springframework.integration.core.MessageProducer;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.gemfire.inbound.CacheListeningMessageProducer;
import org.springframework.integration.gemfire.inbound.ContinuousQueryMessageProducer;
import org.springframework.integration.router.PayloadTypeRouter;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.util.StringUtils;
import reactor.core.publisher.EmitterProcessor;
import reactor.core.publisher.Flux;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 * The Geode Supplier which publishes a message for each event matching the criteria for a
 * configured Region. If a query is provided, this will use a
 * {@link ContinuousQueryMessageProducer} to publish
 * {@link org.apache.geode.cache.query.CqEvent}s that match the query selection criteria.
 * If no query is provided, this will use a {@link CacheListeningMessageProducer} to
 * publsh {@link org.apache.geode.cache.EntryEvent}s.
 *
 * A SpEl Expression, given by the property 'eventExpression' is evaluated to extract
 * desired information from the payload. The default expression is 'newValue' which
 * returns the current object value. This may not be ideal for every use case especially
 * if it does not provide the key value. The key is referenced by the field 'key'. If the
 * cached key and value types are primitives, an simple expression like "key + ':' +
 * newValue" will work. Additional available depend on the specific event type.
 *
 * More complex transformations, such as Json, will require customization. To access the
 * original object, set 'entryExpression' to '#root' or "#this'.
 *
 * This converts payloads of type {@link PdxInstance}, which Geode uses to store JSON
 * content (the type of 'newValue' for instance), to a JSON String.
 *
 *
 * @author David Turanski
 */

@Import(GemFireClientRegionConfiguration.class)
@Configuration
@PropertySource("classpath:gemfire-client.properties")
@EnableConfigurationProperties(GemFireSupplierProperties.class)
public class GemFireSupplierConfiguration {

	private EmitterProcessor<Message<?>> cacheEvents = EmitterProcessor.create();

	@Bean
	public Supplier<Flux<?>> gemfireSupplier() {
		return () -> cacheEvents.map(Message::getPayload);
	}

	@Bean
	FluxMessageChannel fluxChannel() {
		FluxMessageChannel fluxChannel = new FluxMessageChannel();
		fluxChannel.subscribe(cacheEvents);
		return fluxChannel;
	}

	@Bean
	MessageChannel convertToStringChannel() {
		return new DirectChannel();
	}

	@Bean
	MessageChannel routerChannel() {
		return new DirectChannel();
	}

	@Bean
	PayloadTypeRouter payloadTypeRouter(FluxMessageChannel fluxChannel) {
		PayloadTypeRouter router = new PayloadTypeRouter();
		router.setDefaultOutputChannel(fluxChannel);
		router.setChannelMapping(PdxInstance.class.getName(), "convertToStringChannel");

		return router;
	}

	@ConditionalOnMissingBean(Interest.class)
	@Bean
	public Interest<?> allKeysInterest() {
		return new Interest<>(Interest.ALL_KEYS);
	}

	@Bean
	IntegrationFlow startFlow(MessageChannel routerChannel, PayloadTypeRouter payloadTypeRouter) {
		return IntegrationFlows.from(routerChannel)
				.route(payloadTypeRouter)
				.get();
	}

	@Bean
	Function<PdxInstance, String> pdxToJson() {
		return JsonPdxFunctions.pdxToJson();
	}

	@Bean
	IntegrationFlow convertToString(MessageChannel convertToStringChannel, FluxMessageChannel fluxChannel,
			Function<PdxInstance, String> pdxToJson) {
		return IntegrationFlows.from(convertToStringChannel)
				.transform(pdxToJson)
				.channel(fluxChannel)
				.get();
	}

	@Bean
	@Conditional({ CQNotEnabled.class }) // ConditionalOnExpression causing SpEL errors.
	public MessageProducer cacheListeningMessageProducer(
			MessageChannel routerChannel,
			GemFireSupplierProperties properties, Region<?, ?> region) {
		CacheListeningMessageProducer cacheListeningMessageProducer = new CacheListeningMessageProducer(region);
		cacheListeningMessageProducer.setOutputChannel(routerChannel);
		cacheListeningMessageProducer.setPayloadExpression(
				properties.getEventExpression());
		return cacheListeningMessageProducer;
	}

	@ConditionalOnProperty(prefix = "gemfire.supplier", name = "query")
	static class CQConfiguration {
		@Bean
		MessageProducer continuousQueryListener(
				MessageChannel routerChannel,
				ContinuousQueryListenerContainer continuousQueryListenerContainer,
				GemFireSupplierProperties properties) {
			ContinuousQueryMessageProducer continuousQueryMessageProducer = new ContinuousQueryMessageProducer(
					continuousQueryListenerContainer,
					properties.getQuery());
			continuousQueryMessageProducer.setPayloadExpression(properties.getEventExpression());
			continuousQueryMessageProducer.setOutputChannel(routerChannel);
			return continuousQueryMessageProducer;
		}

		@Bean
		ContinuousQueryListenerContainer continuousQueryListenerContainer(ClientCache clientCache) {
			ContinuousQueryListenerContainer continuousQueryListenerContainer = new ContinuousQueryListenerContainer();
			try {
				continuousQueryListenerContainer.setCache(clientCache);
			}
			catch (Exception e) {
				throw new BeanCreationException(e.getLocalizedMessage(), e);
			}
			return continuousQueryListenerContainer;
		}
	}

	static class CQNotEnabled implements Condition {

		@Override
		public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
			return StringUtils.isEmpty(context.getEnvironment().getProperty("gemfire.supplier.query"));
		}
	}
}
