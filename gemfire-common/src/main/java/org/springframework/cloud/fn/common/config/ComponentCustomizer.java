/*
 * Copyright (c) VMware, Inc. 2023. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package org.springframework.cloud.fn.common.config;

/**
 * The customizer contract to apply to beans in the application context which
 * type is matching to generic type of the instance of this interface.
 *
 * @param <T> the target component (bean) type in the application context to customize.
 *
 * @author Artem Bilan
 *
 * @since 1.2.1
 */
@FunctionalInterface
public interface ComponentCustomizer<T> {

	void customize(T component, String beanName);

}
