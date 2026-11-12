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
        versionCode = 9
        versionName = "0.9"
    }

    // Chave fixa: assim cada APK novo instala por cima do anterior, sem desinstalar.
    signingConfigs {
        getByName("debug") {
            storeFile = rootProject.file("resposta-ia.jks")
            storePassword = "respostaia"
            keyAlias = "respostaia"
            keyPassword = "respostaia"
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
