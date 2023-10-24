/*
 * Copyright (c) VMware, Inc. 2023. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package com.vmware.gemfire.spring.cloud.fn.common;

import org.springframework.boot.context.properties.ConfigurationPropertiesBinding;
import org.springframework.context.annotation.Bean;
import org.springframework.core.convert.converter.Converter;

import java.net.InetSocketAddress;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class InetSocketAddressConverterConfiguration {
	@Bean
	@ConfigurationPropertiesBinding
	public Converter<String, InetSocketAddress> inetSocketAddressConverter() {
		return new InetSocketAddressConverter();
	}

	public static class InetSocketAddressConverter implements Converter<String, InetSocketAddress> {

		private static final Pattern HOST_AND_PORT_PATTERN = Pattern.compile("^\\s*(.*?):(\\d+)\\s*$");

		@Override
		public InetSocketAddress convert(String hostAddress) {
			Matcher m = HOST_AND_PORT_PATTERN.matcher(hostAddress);
			if (m.matches()) {
				String host = m.group(1);
				int port = Integer.parseInt(m.group(2));
				return new InetSocketAddress(host, port);
			}
			throw new IllegalArgumentException(String.format("%s is not a valid [host]:[port] value.", hostAddress));
		}
	}
}
