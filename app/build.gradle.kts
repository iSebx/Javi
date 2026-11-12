plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.isebx.javi"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.isebx.javi"
        minSdk = 26
        targetSdk = 34
        versionCode = 5
        versionName = "0.5"
    }

    // "base": la versión normal. "iconos": suma el servicio de accesibilidad
    // (Play Protect bloquea su instalación fuera de Play Store).
    flavorDimensions += "kind"
    productFlavors {
        create("base") { dimension = "kind" }
        create("iconos") { dimension = "kind" }
    }

    signingConfigs {
        getByName("debug") {
            storeFile = file("debug.keystore")
            storePassword = "android"
            keyAlias = "androiddebugkey"
            keyPassword = "android"
        }
    }

    buildTypes {
        getByName("debug") {
            signingConfig = signingConfigs.getByName("debug")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    lint {
        abortOnError = false
        checkReleaseBuilds = false
    }
}
