plugins {
    id("java-gradle-plugin")
    `kotlin-dsl`
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    implementation(gradleApi())
    implementation("org.springframework.cloud:spring-cloud-dataflow-configuration-metadata:2.7.0")
    implementation("org.springframework.boot:spring-boot-configuration-processor:2.7.18")
    implementation("org.springframework:spring-core:5.3.31")
    implementation("org.springframework:spring-beans:5.3.31")
}

gradlePlugin {
    plugins {
        create("simplePlugin") {
            id = "gemfire.spring.cloud.metadata-docs"
            implementationClass = "com.vmware.gemfire.spring.cloud.MetadataDocsGeneratorPlugin"
        }
    }
}
