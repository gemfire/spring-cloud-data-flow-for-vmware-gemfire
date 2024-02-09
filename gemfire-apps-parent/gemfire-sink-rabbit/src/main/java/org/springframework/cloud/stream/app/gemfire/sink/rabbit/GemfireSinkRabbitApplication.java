/*
 * Copyright (c) VMware, Inc. 2023. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package org.springframework.cloud.stream.app.gemfire.sink.rabbit;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;


@SpringBootApplication
@Import({ com.vmware.gemfire.spring.cloud.fn.consumer.GemFireConsumerConfiguration.class })
public class GemfireSinkRabbitApplication {

	public static void main(String[] args) {
		SpringApplication springApplication = new SpringApplication();
		springApplication.setWebApplicationType(WebApplicationType.NONE);
		springApplication.run(GemfireSinkRabbitApplication.class,args);
	}
}
