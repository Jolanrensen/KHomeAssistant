rootProject.name = "KHomeAssistant"
include(":KHomeAssistantLibrary", ":KHomeAssistantJVMExample")
enableFeaturePreview("GRADLE_METADATA")

pluginManagement {
    repositories {
        gradlePluginPortal()
        jcenter()
    }
}