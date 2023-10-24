/*
 * Copyright (c) VMware, Inc. 2023. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package com.vmware.gemfire.spring.cloud.fn.common;

import org.apache.geode.cache.client.ClientCache;
import org.apache.geode.cache.client.ClientCacheFactory;
import org.apache.geode.pdx.ReflectionBasedAutoSerializer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.Resource;
import org.springframework.data.gemfire.util.PropertiesBuilder;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Properties;

/**
 * @author David Turanski
 * @author Christian Tzolov
 */
@EnableConfigurationProperties({ GemFireClientCacheProperties.class, GemFireSecurityProperties.class,
		GemFireSslProperties.class, GemFirePoolProperties.class })
@Import(InetSocketAddressConverterConfiguration.class)
public class GemFireClientCacheConfiguration {

	private static final String SECURITY_CLIENT = "security-client-auth-init";

	private static final String SECURITY_USERNAME = "security-username";

	private static final String SECURITY_PASSWORD = "security-password";

	@Bean
	public ClientCache clientCache(GemFireClientCacheProperties clientCacheProperties,
																 GemFireSecurityProperties securityProperties, GemFireSslProperties sslProperties,
																 GemFirePoolProperties gemfirePoolProperties) {

		Properties properties = new Properties();
		PropertiesBuilder pb = new PropertiesBuilder();
		if (StringUtils.hasText(securityProperties.getUsername())
				&& StringUtils.hasText(securityProperties.getPassword())) {

			properties
					.setProperty(SECURITY_CLIENT,
							GemFireSecurityProperties.UserAuthInitialize.class.getName() + ".create");
			properties.setProperty(SECURITY_USERNAME, securityProperties.getUsername());
			properties.setProperty(SECURITY_PASSWORD, securityProperties.getPassword());

		}

		if (sslProperties.isSslEnabled()) {
			pb.add(properties);
			pb.add(this.toGemFireSslProperties(sslProperties));
		}

		ClientCacheFactory clientCacheFactory = new ClientCacheFactory(pb.build());

		if (clientCacheProperties.isPdxReadSerialized()) {
			clientCacheFactory.setPdxSerializer(new ReflectionBasedAutoSerializer(".*"));
			clientCacheFactory.setPdxReadSerialized(true);
		}

		if (gemfirePoolProperties.getConnectType().equals(GemFirePoolProperties.ConnectType.locator)) {
			for (InetSocketAddress address : gemfirePoolProperties.getHostAddresses()) {
				clientCacheFactory.addPoolLocator(address.getHostName(), address.getPort());
			}
		}
		else {
			for (InetSocketAddress address : gemfirePoolProperties.getHostAddresses()) {
				clientCacheFactory.addPoolServer(address.getHostName(), address.getPort());
			}
		}

		clientCacheFactory.setPoolSubscriptionEnabled(gemfirePoolProperties.isSubscriptionEnabled());
		ClientCache clientCache = clientCacheFactory.create();
		clientCache.readyForEvents();
		return clientCache;
	}

	/**
	 * Converts the App Starter properties into Geode native SSL properties.
	 * @param sslProperties App starter properties.
	 * @return Returns the geode native SSL properties.
	 */
	private Properties toGemFireSslProperties(GemFireSslProperties sslProperties) {

		PropertiesBuilder pb = new PropertiesBuilder();

		// locator - SSL communication with and between locators
		// server - SSL communication between clients and servers
		pb.setProperty("ssl-enabled-components", "server,locator");

		pb.setProperty("ssl-keystore", this.resolveRemoteStore(sslProperties.getKeystoreUri(),
				sslProperties.getUserHomeDirectory(), GemFireSslProperties.LOCAL_KEYSTORE_FILE_NAME));
		pb.setProperty("ssl-keystore-password", sslProperties.getSslKeystorePassword());
		pb.setProperty("ssl-keystore-type", sslProperties.getKeystoreType());

		pb.setProperty("ssl-truststore", this.resolveRemoteStore(sslProperties.getTruststoreUri(),
				sslProperties.getUserHomeDirectory(), GemFireSslProperties.LOCAL_TRUSTSTORE_FILE_NAME));
		pb.setProperty("ssl-truststore-password", sslProperties.getSslTruststorePassword());
		pb.setProperty("ssl-truststore-type", sslProperties.getTruststoreType());

		pb.setProperty("ssl-ciphers", sslProperties.getCiphers());

		return pb.build();
	}

	/**
	 * Copy the Trust store specified in the URI into a local accessible file.
	 *
	 * @param storeUri Either Keystore or Truststore remote resource URI
	 * @param userHomeDirectory local root directory to store the keystore and localsore files
	 * @param localStoreFileName local keystore or truststore file name
	 * @return Returns the absolute path of the local trust or keys store file copy
	 */
	private String resolveRemoteStore(Resource storeUri, String userHomeDirectory, String localStoreFileName) {

		File localStoreFile = new File(userHomeDirectory, localStoreFileName);
		try {
			FileCopyUtils.copy(storeUri.getInputStream(), new FileOutputStream(localStoreFile));
			return localStoreFile.getAbsolutePath();
		}
		catch (IOException e) {
			throw new IllegalStateException(String.format("Failed to copy the store from [%s] into %s",
					storeUri.getDescription(), localStoreFile.getAbsolutePath()), e);
		}
	}
}
