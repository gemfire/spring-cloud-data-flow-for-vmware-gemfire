# VMware GemFire Supplier

This module provides a `java.util.function.Supplier` that can be reused and composed in other applications.
The `Supplier` configures an VMware GemFire client that connects to an external VMware GemFire cache server or locator to monitor an existing region.
If `gemfire.supplier.query` is provided, the supplier will create a continuous query on the region and publish any events that meet the select criteria.
If no query is provided, the supplier will publish all create and update events on the region.

## Continuous Query
If a query is provided, the supplier uses the `ContinuousQueryMessageProducer` from `Spring Integration` which wraps a [ContinuousQueryListenerContainer](https://developer.vmware.com/apis/1737/)
and emits a reactive stream of objects, extracted from a VMware GemFire [CqEvent](https://developer.vmware.com/apis/1657/), which holds all of the event details.
A SpEl Expression, given by the property `gemfire.cq.supplier.event-expression` is used to extract desired fields from the CQEvent payload.
The default expression is `newValue` which returns the current value from the configured Region.

## Cache Listener

If no query is provided, the supplier uses the `CacheListeningMessageProducer` from `Spring Integration` which wraps a [CacheListener](https://developer.vmware.com/apis/1657/)
and emits a reactive stream of objects, extracted from a VMware GemFire [EntryEvent](https://developer.vmware.com/apis/1657/), which holds all of the
event details.
A SpEl Expression, given by the property `gemfire.supplier.entry-event-expression` is used to extract desired fields from the EntryEvent payload.
The default expression is `newValue` which returns the current value from the configured Region.

NOTE: Retrieving the value by itself is not always sufficient, especially if it does not contain the key value, or any additional context.
The key is referenced by the field `key`.
If the cached key and value types are primitives, a simple expression like `key + ':' +newValue` may be useful.
To access the entire EntryEvent, set the expression to `#root` or `#this`.

The configured MessageProducer emits objects to the supplier implemented as `Supplier<Flux<?>>`.
Users have to subscribe to the returned `Flux` to receive the data.

## PDX Serialization

The supplier works with PDX serialized cache objects of type [PdxInstance](https://developer.vmware.com/apis/1657/), which VMware GemFire uses to store objects that can be represented as JSON.
If the target region uses PDX serialization and you set  `gemfire.client.pdx-read-serialized` to `true`, PdxInstance objects will be returned as JSON strings.

## Beans for injection

You can import the `GemFireSupplierConfiguration` configuration in a Spring Boot application and then inject the `gemfireSupplier` bean as type `Supplier<Flux<T>>`, where `T` is the expected return type.
If necessary, can use the bean name `gemfireSupplier` as a qualifier.

Once injected, you can invoke the `get` method of the `Supplier` and then subscribe to the returned `Flux` to initiate the stream.

## Configuration Options

Required properties:

* `gemfire.region.region-name` - The name of the existing remote region.
* `gemfire.pool.host-addresses` - A comma delimited list of `host:port` pairs. By default these are locator addresses but are cache server addresses if you set `gemfire.pool.connect-type=server`.

For more information on the various options available, please see:

* [GemFireSupplierProperties.java](src/main/java/com/vmware/gemfire/spring/cloud/fn/supplier/GemFireSupplierProperties.java) (`gemfire.supplier`)

Many of the options, common to functions that use VMware GemFire, are configured by several `@ConfigurationProperties` classes which are included as needed:

* [GemFireClientCacheProperties.java](../gemfire-common/src/main/java/com/vmware/gemfire/spring/cloud/fn/common/GemFireClientCacheProperties.java) (`gemfire.client`)
* [GemFireRegionProperties.java](../gemfire-common/src/main/java/com/vmware/gemfire/spring/cloud/fn/common/GemFireRegionProperties.java) (`gemfire.region`)
* [GemFirePoolProperties.java](../gemfire-common/src/main/java/com/vmware/gemfire/spring/cloud/fn/common/GemFirePoolProperties.java) (`gemfire.pool`)
* [GemFireSecurityProperties.java](../gemfire-common/src/main/java/com/vmware/gemfire/spring/cloud/fn/common/GemFireSecurityProperties.java) (`gemfire.security`)
* [GemFireSslProperties.java](../gemfire-common/src/main/java/com/vmware/gemfire/spring/cloud/fn/common/GemFireSslProperties.java) (`gemfire.security.ssl`)

## Examples

See this [test suite](src/test/java/com/vmware/gemfire/spring/cloud/fn/supplier/GemFireSupplierApplicationTests.java) for examples of how this supplier is used.
