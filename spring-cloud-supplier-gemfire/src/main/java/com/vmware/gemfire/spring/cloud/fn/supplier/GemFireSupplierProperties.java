/*
 * Copyright (c) VMware, Inc. 2023. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package com.vmware.gemfire.spring.cloud.fn.supplier;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;

/**
 * This represents the configuration properties for the GemFire Supplier.
 *
 * @author David Turanski
 */
@ConfigurationProperties(prefix = "gemfire.supplier")
public class GemFireSupplierProperties {

	private static final String DEFAULT_EXPRESSION = "newValue";

	private final SpelExpressionParser parser = new SpelExpressionParser();

	/**
	 * SpEL expression to extract data from an {@link org.apache.geode.cache.EntryEvent} or
	 * {@link org.apache.geode.cache.query.CqEvent}.
	 */
	private Expression eventExpression = parser.parseExpression(DEFAULT_EXPRESSION);

	/**
	 * An OQL query. This will enable continuous query if provided.
	 */
	private String query;

	public Expression getEventExpression() {
		return eventExpression;
	}

	public void setEventExpression(Expression eventExpression) {
		this.eventExpression = eventExpression;
	}

	public String getQuery() {
		return query;
	}

	public void setQuery(String query) {
		this.query = query;
	}
}
