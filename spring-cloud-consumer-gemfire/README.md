# VMware GemFire Consumer

This module provides a `java.util.function.Consumer` that can be reused and composed in other applications.
The `Consumer` configures a VMware GemFire client that connects to an external VMware GemFire cache server or locator to write Message contents to an existing region.
The consumer uses the `CacheWritingMessageHandler` from `Spring Integration` which writes entries, as key-value pairs to the given region.
A SpEl Expression, given by the property `gemfire.consumer.key-expression` is used to derive the desired key from the inbound Message.
The value is the message payload.

## PDX Serialization

The supplier works with PDX serialized cache objects of type [PdxInstance](https://developer.vmware.com/apis/1657/vmware-gemfire-java-api-reference/), which VMware GemFire uses to store objects that can be represented as JSON.
If the target region uses PDX serialization ,you should set `gemfire.consumer.json` to `true`.
In this case, the expected payload is Json, serialized as a byte[] or String, and will be converted to PdxInstance before writing to the region.
The root object for evaluating `key-expression` will be of type PDXInstance so values are referenced using the `getField(...)` method.


## Beans for injection

You can import the `GemFireConsumerConfiguration` configuration in a Spring Boot application and then inject the `gemfireConsumer` bean as type `Consumer<Message<?>>`.
If necessary, can use the bean name `gemfireCqSupplier` as a qualifier.

Once injected, you can invoke the `accept` method of the `Consumer`.

## Configuration Options

Required properties:

* `gemfire.region.region-name` - The name of the existing remote region.
* `gemfire.pool.host-addresses` - A comma delimited list of `host:port` pairs. By default these are locator addresses but are cache server addresses if you set `gemfire.pool.connect-type=server`.

For more information on the various options available, please see:

* [GemFireConsumerProperties.java](src/main/java/com/vmware/gemfire/spring/cloud/fn/consumer/GemFireConsumerProperties.java) (`gemfire.consumer`)

Many of the options, common to functions that use  VMware GemFire, are configured by several `@ConfigurationProperties` classes which are included as needed:

* [GemFireRegionProperties.java](../gemfire-common/src/main/java/com/vmware/gemfire/spring/cloud/fn/common/GemFireRegionProperties.java) (`gemfire.region`)
* [GemFirePoolProperties.java](../gemfire-common/src/main/java/com/vmware/gemfire/spring/cloud/fn/common/GemFirePoolProperties.java) (`gemfire.pool`)
* [GemFireSecurityProperties.java](../gemfire-common/src/main/java/com/vmware/gemfire/spring/cloud/fn/common/GemFireSecurityProperties.java) (`gemfire.security`)
* [GemFireSslProperties.java](../gemfire-common/src/main/java/com/vmware/gemfire/spring/cloud/fn/common/GemFireSslProperties.java) (`gemfire.security.ssl`)

## Examples

See this [test suite](src/test/java/com/vmware/gemfire/spring/cloud/fn/consumer/GemFireConsumerApplicationTests.java) for examples of how this consumer is used.


