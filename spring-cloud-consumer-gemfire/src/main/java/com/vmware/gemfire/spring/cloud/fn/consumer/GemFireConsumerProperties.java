/*
 * Copyright (c) VMware, Inc. 2023. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package com.vmware.gemfire.spring.cloud.fn.consumer;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotEmpty;

/**
 * @author David Turanski
 */
@ConfigurationProperties("gemfire.consumer")
@Validated
public class GemFireConsumerProperties {

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
