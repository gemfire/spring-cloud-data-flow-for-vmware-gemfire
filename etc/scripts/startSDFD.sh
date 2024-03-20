#!/usr/bin/env bash

#
# Copyright 2024 Broadcom. All rights reserved.
# SPDX-License-Identifier: Apache-2.0
#

./startPostGres.sh

wget https://repo.maven.apache.org/maven2/org/springframework/cloud/spring-cloud-dataflow-server/2.11.1/spring-cloud-dataflow-server-2.11.1.jar
wget https://repo.maven.apache.org/maven2/org/springframework/cloud/spring-cloud-dataflow-shell/2.11.1/spring-cloud-dataflow-shell-2.11.1.jar
wget https://repo.maven.apache.org/maven2/org/springframework/cloud/spring-cloud-skipper-server/2.11.1/spring-cloud-skipper-server-2.11.1.jar

java -agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=8000 -jar spring-cloud-dataflow-server-2.11.1.jar \
      --spring.datasource.url='jdbc:postgresql://docker.internal.host:5432/dataflow' \
      --spring.datasource.username=dataflow \
      --spring.datasource.password=dataflow123 \
      --spring.datasource.driverClassName=org.postgresql.Driver \
      --logging.level.root=INFO \
      --spring.config.additional-location=/Users/udo/projects/spring-cloud-data-flow-for-vmware-gemfire/etc/scripts/maven.yaml

java -jar spring-cloud-skipper-server-2.11.1.jar \
      --spring.datasource.url='jdbc:postgresql://docker.internal.host:5432/dataflow' \
      --spring.datasource.username=dataflow \
      --spring.datasource.password=dataflow123 \
      --spring.datasource.driverClassName=org.postgresql.Driver
