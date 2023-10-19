/*
 * Copyright (c) VMware, Inc. 2023. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package org.springframework.cloud.fn.common.geode;

import java.net.InetSocketAddress;

import javax.validation.constraints.NotEmpty;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Geode client pool configuration properties.
 *
 * @author David Turanski
 */
@ConfigurationProperties("gemfire.pool")
@Validated
public class GeodePoolProperties {

	/**
	 * Connection Type locator or server.
	 */
	public enum ConnectType {
		/**
		 * use locator.
		 */
		locator,
		/**
		 * use server.
		 */
		server
	}

	/**
	 * Specifies one or more GemFire locator or server addresses formatted as [host]:[port].
	 */
	private InetSocketAddress[] hostAddresses = { new InetSocketAddress("localhost", 10334) };

	/**
	 * Specifies connection type: 'server' or 'locator'.
	 */
	private ConnectType connectType = ConnectType.locator;

	/**
	 * Set to true to enable subscriptions for the client pool. Required to sync updates to
	 * the client cache.
	 */
	private boolean subscriptionEnabled;

	@NotEmpty
	public InetSocketAddress[] getHostAddresses() {
		return hostAddresses;
	}

	public void setHostAddresses(InetSocketAddress[] hostAddresses) {
		this.hostAddresses = hostAddresses;
	}

	public ConnectType getConnectType() {
		return connectType;
	}

	public void setConnectType(ConnectType connectType) {
		this.connectType = connectType;
	}

	public boolean isSubscriptionEnabled() {
		return subscriptionEnabled;
	}

	public void setSubscriptionEnabled(boolean subscriptionEnabled) {
		this.subscriptionEnabled = subscriptionEnabled;
	}

}
