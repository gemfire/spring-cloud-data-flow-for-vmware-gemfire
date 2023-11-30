/*
 * Copyright (c) VMware, Inc. 2023. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package org.springframework.cloud.stream.app.gemfire.source.rabbit;

//@ConfigurationProperties(prefix = "dude.whereis.my.car")
public class TestProperties {

  public String getFoo() {
    return foo;
  }

  public void setFoo(String foo) {
    this.foo = foo;
  }

  private String foo;
}
