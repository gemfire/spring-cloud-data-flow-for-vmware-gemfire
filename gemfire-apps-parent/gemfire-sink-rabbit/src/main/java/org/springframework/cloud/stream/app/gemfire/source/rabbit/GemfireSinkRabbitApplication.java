/*
 * Copyright (c) VMware, Inc. 2023. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package org.springframework.cloud.stream.app.gemfire.source.rabbit;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;


@SpringBootApplication
@Import({com.vmware.gemfire.spring.cloud.fn.consumer.GemFireConsumerConfiguration.class})
//@EnableConfigurationProperties(GemFireSupplierProperties.class)
public class GemfireSinkRabbitApplication {

  public static void main(String[] args) {
    SpringApplication.run(GemfireSinkRabbitApplication.class, args);
  }
}
