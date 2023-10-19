/*
 * Copyright (c) VMware, Inc. 2023. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package org.springframework.cloud.fn.common.geode;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Geode client pool configuration properties.
 *
 * @author Christian Tzolov
 */
@ConfigurationProperties("gemfire.client")
@Validated
public class GeodeClientCacheProperties {

	/**
	 * Deserialize the Geode objects into PdxInstance instead of the domain class.
	 */
	private boolean pdxReadSerialized = false;

	public boolean isPdxReadSerialized() {
		return pdxReadSerialized;
	}

	public void setPdxReadSerialized(boolean pdxReadSerialized) {
		this.pdxReadSerialized = pdxReadSerialized;
	}
}
