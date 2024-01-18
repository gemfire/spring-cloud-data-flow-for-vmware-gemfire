plugins {
    id("java-gradle-plugin")
    alias(libs.plugins.ben.manes.versions)
    `kotlin-dsl`
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    implementation(gradleApi())
    implementation(libs.spring.cloud.dataflow.configuration.metadata)
    implementation(libs.spring.boot.configuration.processor)
    implementation(libs.spring.core)
    implementation(libs.spring.beans)
}

gradlePlugin {
    plugins {
        create("metadata-docs") {
            id = "gemfire.spring.cloud.metadata-docs"
            implementationClass = "com.vmware.gemfire.spring.cloud.MetadataDocsGeneratorPlugin"
        }
    }
}
