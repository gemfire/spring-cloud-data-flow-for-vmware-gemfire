import java.io.FileInputStream
import java.util.*

pluginManagement {
    includeBuild("build-tools/gemfire-cloud-stream-app-metadata-generator")
    includeBuild("build-tools/gemfire-cloud-stream-app-metadata-docs")
    includeBuild("build-tools/publishing")
}

rootProject.name = "spring-cloud-data-flow-for-vmware-gemfire"
include("spring-cloud-common-gemfire")
include("spring-cloud-consumer-gemfire")
include("spring-cloud-supplier-gemfire")

include("spring-cloud-app-gemfire-source")
include("spring-cloud-app-gemfire-sink")

include("gemfire-apps-parent")
include("gemfire-apps-parent:gemfire-sink-kafka")
include("gemfire-apps-parent:gemfire-sink-rabbit")
include("gemfire-apps-parent:gemfire-source-rabbit")
include("gemfire-apps-parent:gemfire-source-kafka")

dependencyResolutionManagement {
  versionCatalogs {
    create("libs") {
      val properties = Properties()
      properties.load(FileInputStream(layout.rootDirectory.asFile.toPath().resolve("gradle.properties").toFile()))
      versionOverrideFromProperties(this, properties)
    }
  }
}

private fun versionOverrideFromProperty(versionCatalogBuilder: VersionCatalogBuilder, propertyName: String, propertiesFile: Properties): String {
  val propertyValue = providers.systemProperty(propertyName).getOrElse(propertiesFile.getProperty(propertyName))
  return versionCatalogBuilder.version(propertyName, propertyValue)
}

private fun versionOverrideFromProperties(versionCatalogBuilder: VersionCatalogBuilder, properties: Properties) {
  versionOverrideFromProperty(versionCatalogBuilder, "gemfireVersion", properties)
  versionOverrideFromProperty(versionCatalogBuilder, "springIntegrationGemFireVersion", properties)
  versionOverrideFromProperty(versionCatalogBuilder, "springBootGemFireVersion", properties)
  versionOverrideFromProperty(versionCatalogBuilder, "springDataGemFireVersion", properties)
}
