/*
 * Copyright 2023-2024 Broadcom. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package com.vmware.gemfire.spring.cloud.fn.common;

import org.apache.geode.cache.DataPolicy;
import org.apache.geode.cache.client.ClientCache;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.gemfire.client.ClientRegionFactoryBean;
import org.springframework.data.gemfire.client.Interest;
import org.springframework.lang.Nullable;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * Client region configuration common to Geode functions. This configures the
 * 'regionName', 'spring.application.name' by default and injects the pool. Also, any
 * beans of type {@link Interest} will be registered to the client region to control which
 * keys will be automatically synced to the client. At least one of these is required for
 * Geode Suppliers.
 *
 * @author David Turanski
 */
@Configuration
@Import(GemFireClientCacheConfiguration.class)
@EnableConfigurationProperties(GemFireRegionProperties.class)
public class GemFireClientRegionConfiguration {

	@Bean(name = "clientRegion")
	@SuppressWarnings({ "rawtype", "unchecked" })
	public ClientRegionFactoryBean clientRegionFactoryBean(ClientCache clientCache,
			@Nullable List<Interest> keyInterests, GemFireRegionProperties properties) {
		ClientRegionFactoryBean clientRegionFactoryBean = new ClientRegionFactoryBean();
		clientRegionFactoryBean.setRegionName(properties.getRegionName());
		clientRegionFactoryBean.setDataPolicy(DataPolicy.EMPTY);
		if (!CollectionUtils.isEmpty(keyInterests)) {
			clientRegionFactoryBean.setInterests(keyInterests.toArray(new Interest[keyInterests.size()]));
		}

		try {
			clientRegionFactoryBean.setCache(clientCache);
		}
		catch (Exception e) {
			throw new BeanCreationException(e.getMessage(), e);
		}
		return clientRegionFactoryBean;
	}

}
