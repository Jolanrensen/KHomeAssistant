import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.4.10"
    kotlin("plugin.serialization") version "1.4.10"
    id("com.github.johnrengelman.shadow") version "6.0.0"
    application
}

//apply plugin: "war"
//apply from: "https://raw.github.com/akhikhl/gretty/master/pluginScripts/gretty.plugin"

group = "nl.jolanrensen.KHomeAssistantJVMExample"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    mavenLocal()
    jcenter()
    maven(url = "https://dl.bintray.com/korlibs/korlibs")
}


dependencies {
//    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.9")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:1.0.0-RC")
    implementation("com.soywiz.korlibs.klock:klock-jvm:1.12.1")
    implementation("com.soywiz.korlibs.korim:korim:1.12.29")
//    implementation( "com.soywiz.korlibs.klock:klock-jvm:1.10.5")
//    implementation( "com.soywiz:klock:0.5.0")

//    implementation( "nl.jolanrensen.kHomeAssistant:KHomeAssistantLibrary:0.0.3")
    implementation(project(":KHomeAssistantLibrary"))

//    implementation( "org.jetbrains.kotlin:kotlin-reflect:1.3.72")
}

application {
    mainClassName = "MainKt"
}

//

tasks.withType<KotlinCompile>().all {
    kotlinOptions.freeCompilerArgs += "-Xopt-in=kotlin.RequiresOptIn"
}
//compileTestKotlin {
//    kotlinOptions {
//        jvmTarget = "1.8"
//        freeCompilerArgs += "-Xopt-in=kotlin.RequiresOptIn"
//    }
//}

//val shadowJar by tasks.getting(ShadowJar::class) {
//
//    this.mainClassName = mainClassName
//}

val jar by tasks.getting(Jar::class) {
    manifest {
        attributes["Main-Class"] = "MainKt"
    }
}
