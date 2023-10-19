/*
 * Copyright (c) VMware, Inc. 2023. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package org.springframework.cloud.fn.consumer.geode;

import javax.validation.constraints.NotEmpty;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * @author David Turanski
 */
@ConfigurationProperties("gemfire.consumer")
@Validated
public class GeodeConsumerProperties {

	/**
	 * SpEL expression to use as a cache key.
	 */
	private String keyExpression;

	/**
	 * Indicates if the Geode region stores json objects as PdxInstance.
	 */
	private boolean json;

	@NotEmpty(message = "A valid key expression is required")
	public String getKeyExpression() {
		return keyExpression;
	}

	public void setKeyExpression(String keyExpression) {
		this.keyExpression = keyExpression;
	}

	public boolean isJson() {
		return json;
	}

	public void setJson(boolean json) {
		this.json = json;
	}
}
