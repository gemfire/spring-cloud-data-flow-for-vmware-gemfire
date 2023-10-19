/*
 * Copyright (c) VMware, Inc. 2023. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package org.springframework.cloud.fn.consumer.geode;

import org.apache.geode.pdx.PdxInstance;
import org.springframework.cloud.fn.common.geode.JsonPdxFunctions;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.messaging.converter.MessageConversionException;

import java.util.function.Function;

/**
 * @author David Turanski
 * @author Christian Tzolov
 **/
class GeodeConsumerHandler implements Function<Message<?>, Message<?>> {

	private final Boolean convertToJson;

	private final Function<String, PdxInstance> transformer = JsonPdxFunctions.jsonToPdx();

	GeodeConsumerHandler(Boolean convertToJson) {
		this.convertToJson = convertToJson;
	}

	@Override
	public Message<?> apply(Message<?> message) {
		Message<?> transformedMessage = message;
		Object transformedPayload = message.getPayload();
		if (convertToJson) {

			Object payload = message.getPayload();

			if (payload instanceof byte[]) {
				transformedPayload = transformer.apply(new String((byte[]) payload));
			}
			else if (payload instanceof String) {
				transformedPayload = transformer.apply((String) payload);
			}
			else {
				throw new MessageConversionException(String.format(
						"Cannot convert object of type %s", payload.getClass()
								.getName()));
			}
		}

		return MessageBuilder
				.fromMessage(message)
				.withPayload(transformedPayload)
				.build();
	}
}
