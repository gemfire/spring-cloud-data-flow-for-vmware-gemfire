/*
 * Copyright 2023-2024 Broadcom. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package com.vmware.gemfire.spring.cloud.fn.common;

import org.apache.geode.pdx.JSONFormatter;
import org.apache.geode.pdx.PdxInstance;

import java.util.function.Function;

/**
 * @author David Turanski
 * @author Christian Tzolov
 *
 */
public abstract class JsonPdxFunctions {

	public static Function<String, PdxInstance> jsonToPdx() {
		return JSONFormatter::fromJSON;
	}

	public static Function<PdxInstance, String> pdxToJson() {
		return obj -> {
			if (obj == null) {
				return null;
			}
			if (obj instanceof PdxInstance) {
				String json = JSONFormatter.toJSON(obj);
				// de-pretty
				return json.replaceAll("\\r\\n\\s*", "").replaceAll("\\n\\s*", "")
						.replaceAll("\\s*:\\s*", ":").trim();
			}
			return obj.toString();
		};
	}
}
