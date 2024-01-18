plugins {
    id("java-gradle-plugin")
    alias(libs.plugins.ben.manes.versions)
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    implementation(gradleApi())
    implementation(libs.spring.boot.configuration.processor)
    implementation(libs.spring.core)
    implementation(libs.spring.beans)
}

gradlePlugin {
    plugins {
        create("metadata-generator") {
            id = "gemfire.spring.cloud.metadata-generator"
            implementationClass = "com.vmware.gemfire.spring.cloud.MetadataGeneratorPlugin"
        }
    }
}
