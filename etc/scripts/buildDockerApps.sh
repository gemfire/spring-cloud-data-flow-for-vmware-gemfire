#!/usr/bin/env bash

declare -a arr=("gemfire-sink-rabbit" "gemfire-source-rabbit" "gemfire-sink-kafka" "gemfire-source-kafka")

## now loop through the above array
for i in "${arr[@]}"
do
   echo "$i"
   pack build --path "/Users/udo/projects/spring-cloud-data-flow-for-vmware-gemfire/gemfire-apps-parent/$i/build/libs/$i-1.0.0-build.9999.jar" --builder docker.io/paketobuildpacks/builder-jammy-base:latest --env BP_JVM_VERSION=8 --env BPE_APPEND_JDK_JAVA_OPTIONS=-Dfile.encoding=UTF-8 --env BPE_APPEND_JDK_JAVA_OPTIONS=-Dsun.jnu.encoding --env BPE_LC_ALL=en_US.utf8 --env BPE_LANG=en_US.utf8 "gemfire/$i:1.0.0"

#   docker push "udo774/$i:1.0.0"
done


