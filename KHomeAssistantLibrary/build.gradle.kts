plugins {
    kotlin("multiplatform") version "1.4.10"
    kotlin("plugin.serialization") version "1.4.10"
    id("org.jetbrains.dokka") version "1.4.10"
    `maven-publish`
}

repositories {
    jcenter()
    mavenCentral()
    maven(url = "https://kotlin.bintray.com/ktor")
    maven(url = "https://kotlin.bintray.com/kotlinx")
    maven(url = "https://dl.bintray.com/korlibs/korlibs")
}

group = "nl.jolanrensen.kHomeAssistant"
version = "0.0.3"

//apply plugin: "kotlinx-serialization"


dependencies {
    dokkaHtmlPlugin("org.jetbrains.dokka:kotlin-as-java-plugin:1.4.10")
}

//dokkaH
//
//dokkaHtml {
//
//
//    outputFormat = "html"
//    outputDirectory = "$buildDir/dokka"
//
//    multiplatform {
//
//
//    }
//}


kotlin {

    explicitApiWarning()

    jvm {
        compilations["main"].kotlinOptions {
//            jvmTarget = "1.8"
        }

        compilations["test"].kotlinOptions {
            // Setup the Kotlin compiler options for the "main" compilation:
//            jvmTarget = "1.8"
//            freeCompilerArgs += "-Xopt-in=kotlin.RequiresOptIn"
        }
    }
//    js(BOTH) {
//        browser {
//        }
//        nodejs {
//        }
//    }
    // For ARM, should be changed to iosArm32 or iosArm64
    // For Linux, should be changed to e.g. linuxX64
    // For MacOS, should be changed to e.g. macosX64
    // For Windows, should be changed to e.g. mingwX64
//    linuxX64("linux") {
//        binaries {
//            executable()
//            test([RELEASE])
//        }
//    }

    sourceSets {
        val commonMain by getting {
            dependencies {
//                implementation(kotlin("stdlib-common"))
                implementation("io.ktor:ktor-client-core:1.3.2")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.9")
//                implementation("com.soywiz.korlibs.klock:klock:1.12.1")
                implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.1.0")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:1.0.0-RC")
                implementation("org.jetbrains.kotlin:kotlin-reflect:1.4.10")
                implementation("com.soywiz.korlibs.kds:kds:1.11.0")
                implementation("com.soywiz.korlibs.korio:korio:1.12.0")
                implementation("com.soywiz.korlibs.korim:korim:1.12.29") // still kotlin 1.3, based on korma, also 1.3
                implementation("io.ktor:ktor-client-cio:1.4.0")

//                implementation( "com.soywiz.korlibs.kmem:kmem:1.11.0"

            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
            }
        }

        val jvmMain by getting {
            dependencies {
//                implementation(kotlin("stdlib-jdk8"))
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.9")
                implementation("io.ktor:ktor-client-cio:1.4.0")

//                implementation(("io.ktor:ktor-client-apache:$ktor_version")) // https://ktor.io/clients/http-client/engines.html#artifact-4
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:1.0.0-RC")

            }
        }

        val jvmTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation(kotlin("test-junit"))
            }
        }
        all {
            languageSettings.enableLanguageFeature("InlineClasses")
            languageSettings.useExperimentalAnnotation("kotlin.RequiresOptIn")
        }
//        val jsMain by getting {
//            dependencies {
//                implementation( kotlin("stdlib-js"))
////                implementation( "org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.5")
//                implementation( "org.jetbrains.kotlinx:kotlinx-coroutines-core-js:1.3.5")
//                implementation( "io.ktor:ktor-client-js:1.3.2")
//                implementation( "org.jetbrains.kotlinx:kotlinx-serialization-runtime-js:0.20.0")
//
//            }
//        }
//        val jsTest by getting {
//            dependencies {
//                implementation( kotlin("test-js"))
//            }
//        }
//        linuxMain {
//            dependencies {
//                implementation( kotlin("stdlib"))
//                implementation( "io.ktor:ktor-client-cio:1.4.0")
////                implementation( "com.soywiz.korlibs.korim:korim:1.12.29")
//
////                compile( "io.ktor:ktor-client-core-native:$ktor_version")
//            }
//        }
//        linuxTest {
//            dependencies {
//                implementation( kotlin("test"))
//            }
//        }
    }
}