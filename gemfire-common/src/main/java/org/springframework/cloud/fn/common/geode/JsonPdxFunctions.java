/*
 * Copyright (c) VMware, Inc. 2023. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package org.springframework.cloud.fn.common.geode;

import java.util.function.Function;

import org.apache.geode.pdx.JSONFormatter;
import org.apache.geode.pdx.PdxInstance;

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
