plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

import java.util.Properties

android {
    namespace = "com.cralert.app"
    compileSdk = 34

    val coinCapToken = project.findProperty("COINCAP_TOKEN") as String? ?: ""
    val localProps = Properties().apply {
        val propsFile = rootProject.file("local.properties")
        if (propsFile.exists()) {
            propsFile.inputStream().use { load(it) }
        }
    }
    val keystorePath = localProps.getProperty("cralert.storeFile") ?: ""
    val keystorePassword = localProps.getProperty("cralert.storePassword") ?: ""
    val keyAliasValue = localProps.getProperty("cralert.keyAlias") ?: ""
    val keyPasswordValue = localProps.getProperty("cralert.keyPassword") ?: ""
    val cryptoCompareKey = localProps.getProperty("cralert.cryptoCompareApiKey") ?: ""
    val resendApiKey = localProps.getProperty("cralert.resendApiKey") ?: ""

    defaultConfig {
        applicationId = "com.cralert.app"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        buildConfigField("String", "COINCAP_TOKEN", "\"$coinCapToken\"")
        buildConfigField("String", "CRYPTOCOMPARE_API_KEY", "\"$cryptoCompareKey\"")
        buildConfigField("String", "RESEND_API_KEY", "\"$resendApiKey\"")
    }

    signingConfigs {
        create("release") {
            if (keystorePath.isNotBlank()) {
                storeFile = file(keystorePath)
            }
            storePassword = keystorePassword
            keyAlias = keyAliasValue
            keyPassword = keyPasswordValue
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            isMinifyEnabled = false
        }
    }

    buildFeatures {
        buildConfig = true
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.11"
    }

    lint {
        abortOnError = true
        warningsAsErrors = false
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

tasks.matching { it.name == "assembleDebug" }.configureEach {
    dependsOn("lintDebug", "testDebugUnitTest")
}

dependencies {
    implementation(platform("androidx.compose:compose-bom:2024.02.02"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.material:material")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.activity:activity-compose:1.9.1")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.4")
    implementation("androidx.compose.runtime:runtime-livedata")

    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")

    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.4")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.8.4")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")

    testImplementation("junit:junit:4.13.2")
    testImplementation("org.json:json:20240303")

    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")

    debugImplementation("androidx.compose.ui:ui-tooling")
}
