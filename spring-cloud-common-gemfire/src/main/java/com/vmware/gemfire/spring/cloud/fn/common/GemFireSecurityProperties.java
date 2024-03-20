/*
 * Copyright 2023-2024 Broadcom. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package com.vmware.gemfire.spring.cloud.fn.common;

import org.apache.geode.LogWriter;
import org.apache.geode.distributed.DistributedMember;
import org.apache.geode.security.AuthInitialize;
import org.apache.geode.security.AuthenticationFailedException;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Properties;

/**
 * Configuration properties for Geode username/password authentication.
 * @author David Turanski
 **/
@ConfigurationProperties(prefix = "gemfire.security")
public class GemFireSecurityProperties {

	/**
	 * The cache username.
	 */
	private String username;

	/**
	 * The cache password.
	 */
	private String password;

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	@SuppressWarnings("unused")
	public static class UserAuthInitialize implements AuthInitialize {

		private LogWriter securitylog;
		private LogWriter systemlog;

		public static AuthInitialize create() {
			return new UserAuthInitialize();
		}

		@Override
		public void init(LogWriter systemLogger, LogWriter securityLogger) throws AuthenticationFailedException {
			this.systemlog = systemLogger;
			this.securitylog = securityLogger;
		}

		@Override
		public Properties getCredentials(Properties props, DistributedMember server, boolean isPeer) throws AuthenticationFailedException {

			String username = props.getProperty(SECURITY_USERNAME);
			if (username == null) {
				throw new AuthenticationFailedException("UserAuthInitialize: username not set.");
			}

			String password = props.getProperty(SECURITY_PASSWORD);
			if (password == null) {
				throw new AuthenticationFailedException("UserAuthInitialize: password not set.");
			}

			Properties properties = new Properties();
			properties.setProperty(SECURITY_USERNAME, username);
			properties.setProperty(SECURITY_PASSWORD, password);
			return properties;
		}

		@Override
		public void close() {
		}
	}
}
