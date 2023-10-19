/*
 * Copyright (c) VMware, Inc. 2023. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package org.springframework.cloud.fn.common.config;

import java.beans.Introspector;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.boot.context.properties.ConfigurationPropertiesBinding;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.convert.converter.Converter;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ParseException;
import org.springframework.expression.spel.standard.SpelExpression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.integration.config.IntegrationConverter;
import org.springframework.integration.expression.SpelPropertyAccessorRegistrar;
import org.springframework.integration.json.JsonPropertyAccessor;

@Configuration
@AutoConfigureAfter(name = "org.springframework.cloud.stream.config.SpelExpressionConverterConfiguration")
@ConditionalOnMissingClass("org.springframework.cloud.stream.config.SpelExpressionConverterConfiguration")
public class SpelExpressionConverterConfiguration {

	/**
	 * Specific Application Context name to be used as Bean qualifier when the {@link EvaluationContext} is injected.
	 */
	public static final String INTEGRATION_EVALUATION_CONTEXT = "integrationEvaluationContext";

	@Bean
	public static SpelPropertyAccessorRegistrar spelPropertyAccessorRegistrar() {
		return (new SpelPropertyAccessorRegistrar())
				.add(Introspector.decapitalize(JsonPropertyAccessor.class.getSimpleName()), new JsonPropertyAccessor());
	}

	@Bean
	@ConfigurationPropertiesBinding
	@IntegrationConverter
	public Converter<String, Expression> spelConverter() {
		return new SpelConverter();
	}

	public static class SpelConverter implements Converter<String, Expression> {
		private SpelExpressionParser parser = new SpelExpressionParser();

		@Autowired
		@Qualifier(INTEGRATION_EVALUATION_CONTEXT)
		@Lazy
		private EvaluationContext evaluationContext;

		public SpelConverter() {
		}

		public Expression convert(String source) {
			try {
				Expression expression = this.parser.parseExpression(source);
				if (expression instanceof SpelExpression) {
					((SpelExpression) expression).setEvaluationContext(this.evaluationContext);
				}

				return expression;
			}
			catch (ParseException var3) {
				throw new IllegalArgumentException(
						String.format("Could not convert '%s' into a SpEL expression", source), var3);
			}
		}
	}
}
