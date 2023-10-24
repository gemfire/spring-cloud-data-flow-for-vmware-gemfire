## Spring Cloud Stream for VMware GemFire

This [Spring Cloud Stream](https://spring.io/projects/spring-cloud-stream) project provides `java.util.function.Consumer` and `java.util.function.Supplier` implementations for VMware GemFire.

You need to include this dependency into your project:

**Maven**
```xml
<dependency>
    <groupId>com.vmware.gemfire</groupId>
    <artifactId>spring-cloud-starter-2022.0-gemfire-10.0</artifactId>
    <version>{project-version}</version>
</dependency>
<dependency>
    <groupId>com.vmware.gemfire</groupId>
    <artifactId>gemfire-core</artifactId>
    <version>10.0.1</version>
</dependency>
<dependency>
    <groupId>com.vmware.gemfire</groupId>
    <artifactId>gemfire-cq</artifactId>
    <version>10.0.1</version>
</dependency>
```

**Gradle**
```groovy
implementation "com.vmware.gemfire:spring-cloud-starter-2022.0-gemfire-10.0:{project-version}"
implementation "com.vmware.gemfire:gemfire-core:10.0.1"
implementation "com.vmware.gemfire:gemfire-cq:10.0.1"
```

For more information on how to configure the project please refer to:
* [Spring Cloud Supplier](spring-cloud-supplier-gemfire/README.md)
* [Spring Cloud Consumer](spring-cloud-consumer-gemfire/README.md)
