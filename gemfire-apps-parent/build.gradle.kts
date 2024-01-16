plugins {
    id("java-library")
    id("idea")
    id("eclipse")
    alias(libs.plugins.lombok)
    id("gemfire-repo-artifact-publishing")
    id("gemfire-repos-plugin")
}

group = "com.vmware.gemfire.spring.cloud.stream.app"

configurations.create("compileJava").apply {
    extendsFrom(configurations.annotationProcessor.get())
}

java {
    withJavadocJar()
    withSourcesJar()
    toolchain{ languageVersion.set(JavaLanguageVersion.of(8))}
}

tasks.named<Javadoc>("javadoc") {
    title = "Spring Cloud Dataflow Sink for VMware GemFire Java API Reference"
    isFailOnError=false
}

publishingDetails {
    artifactName.set("gemfire-apps-parent")
    longName.set("Spring Cloud Dataflow Sink for VMware GemFire Parent")
    description.set("Spring Cloud Dataflow Sink for VMware GemFire Parent")
}

dependencies {

    api(platform(libs.spring.cloud.dependencies.bom))
    api(platform(libs.spring.boot.dependencies.bom))
    api(platform(libs.wavefront.spring.boot.bom))
    api(platform(libs.spring.cloud.stream.applications.core))
    api(platform(libs.testcontainers.dependencies.bom))
    api(platform(libs.spring.framework.bom))

    annotationProcessor(libs.spring.boot.configuration.processor)
    api(libs.stream.applications.postprocessor.common)
    api(libs.java.cfenv.boot)
    api(libs.stream.applications.security.common)
    api(libs.spring.cloud.services.starter.config.client)
    api(libs.stream.applications.micrometer.common)
    api("org.springframework.cloud:spring-cloud-sleuth-zipkin")
    api("io.micrometer:micrometer-registry-wavefront")
    api("io.pivotal.spring.cloud:spring-cloud-services-starter-config-client")
    api("io.micrometer:micrometer-registry-influx")
    api(libs.spring.boot.configuration.processor)
    api("org.springframework.boot:spring-boot-starter-security")
    api("org.springframework.boot:spring-boot-starter-web")
    api("org.springframework.boot:spring-boot-starter")
    api(libs.stream.applications.security.common)
    api("io.micrometer:micrometer-registry-datadog")
    api("org.springframework.cloud:spring-cloud-starter-sleuth")
    api(libs.prometheus.rsocket.spring)
    api("org.springframework.boot:spring-boot-starter-logging")
    api(libs.stream.applications.postprocessor.common)
    api("com.wavefront:wavefront-spring-boot-starter")
    api("org.springframework.cloud:spring-cloud-starter-config")
    api("io.micrometer:micrometer-registry-prometheus")
    api("org.springframework.boot:spring-boot-starter-actuator")
    api("io.pivotal.cfenv:java-cfenv-boot")
}
