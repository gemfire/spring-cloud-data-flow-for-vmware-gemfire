/*
 * Copyright (c) VMware, Inc. 2023. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package com.vmware.gemfire.spring.cloud.fn.common;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotBlank;

/**
 * Region configuration properties.
 * @author David Turanski
 */
@ConfigurationProperties(prefix = "gemfire.region")
@Validated
public class GemFireRegionProperties {
	/**
	 * The region name.
	 */
	private String regionName;

	@NotBlank(message = "Region name is required")
	public String getRegionName() {
		return regionName;
	}

	public void setRegionName(String regionName) {
		this.regionName = regionName;
	}

}
