/*
 * Copyright (c) VMware, Inc. 2023. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package org.springframework.cloud.fn.common.geode;

import javax.validation.constraints.NotBlank;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Region configuration properties.
 * @author David Turanski
 */
@ConfigurationProperties("gemfire.region")
@Validated
public class GeodeRegionProperties {
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
