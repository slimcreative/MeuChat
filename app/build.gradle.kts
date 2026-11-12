plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.meuteclado"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.meuteclado"
        minSdk = 29
        targetSdk = 35
        versionCode = 10
        versionName = "1.0"
    }

    // Chave fixa: cada APK novo instala por cima do anterior, sem desinstalar.
    signingConfigs {
        create("fixed") {
            storeFile = rootProject.file("resposta-ia.jks")
            storePassword = "respostaia"
            keyAlias = "respostaia"
            keyPassword = "respostaia"
        }
    }

    buildTypes {
        // Build de release (não "debug"): tira a flag debuggable, que os antivírus marcam.
        getByName("release") {
            isDebuggable = false
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("fixed")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")
}
