buildscript {
//    ext.ktor_version = "1.3.2"
//    ext.serialization_version = "0.20.0"

    repositories { jcenter() }

    dependencies {
//        classpath "org.jetbrains.kotlin:kotlin-serialization:1.3.70"
    }
}



allprojects {
    repositories {
        jcenter()
        mavenCentral()    
    }
}

project(":KHomeAssistantJVMExample") {
    dependencies {
//        implementation(project(":KHomeAssistant-library"))
    }
}