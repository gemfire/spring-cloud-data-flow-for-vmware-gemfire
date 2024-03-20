/*
 * Copyright 2023-2024 Broadcom. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package com.vmware.gemfire.spring.cloud.fn.consumer;

import com.vmware.gemfire.spring.cloud.fn.common.JsonPdxFunctions;
import org.apache.geode.pdx.PdxInstance;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.messaging.converter.MessageConversionException;

import java.util.function.Function;

/**
 * @author David Turanski
 * @author Christian Tzolov
 **/
class GemFireConsumerHandler implements Function<Message<?>, Message<?>> {

	private final Boolean convertToJson;

	private final Function<String, PdxInstance> transformer = JsonPdxFunctions.jsonToPdx();

	GemFireConsumerHandler(Boolean convertToJson) {
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
