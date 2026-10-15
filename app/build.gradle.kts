plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
}

import java.util.Properties

val localProperties = Properties().apply {
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        localPropertiesFile.inputStream().use { load(it) }
    }
}

val geminiKeyList = mutableListOf<String>()
for (i in 1..5) {
    val key = localProperties.getProperty("gemini.key$i", "")?.trim()
        ?.ifEmpty { System.getenv("GEMINI_KEY_$i")?.trim() } ?: ""
    if (key.isNotBlank()) {
        geminiKeyList.add(key)
    }
}
if (geminiKeyList.isEmpty()) {
    val commaKeys = localProperties.getProperty("gemini.keys", "")?.trim()
        ?.ifEmpty { System.getenv("GEMINI_KEYS")?.trim() } ?: ""
    if (commaKeys.isNotBlank()) {
        geminiKeyList.addAll(commaKeys.split(",").map { it.trim() }.filter { it.isNotBlank() })
    }
}
if (geminiKeyList.isEmpty()) {
    val singleEnv = System.getenv("GEMINI_API_KEY")?.trim() ?: ""
    if (singleEnv.isNotBlank()) {
        geminiKeyList.add(singleEnv)
    }
}
val geminiKeys = geminiKeyList.joinToString(",")
val nvidiaApiKey = localProperties.getProperty("nvidia.api.key", "")?.trim()
    ?.ifEmpty { System.getenv("NVIDIA_API_KEY")?.trim() } ?: ""

android {
    namespace = "com.nmiet.app"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.nmiet.app"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0.0"

        buildConfigField("String", "GEMINI_KEYS", "\"$geminiKeys\"")
        buildConfigField("String", "NVIDIA_API_KEY", "\"$nvidiaApiKey\"")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    testOptions {
        unitTests.isReturnDefaultValues = true
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    compilerOptions {
        freeCompilerArgs.add("-Xskip-metadata-version-check")
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.navigation.compose)

    // Coil Image Loading
    implementation("io.coil-kt:coil-compose:2.6.0")

    // Room
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // DataStore Preferences
    implementation("androidx.datastore:datastore-preferences:1.1.1")

    // Firebase Authentication & Google Sign-In
    implementation(platform("com.google.firebase:firebase-bom:33.9.0"))
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.android.gms:play-services-auth:21.3.0")

    // Google Play Services Nearby Connections
    implementation(libs.play.services.nearby)

    // Google Generative AI Client SDK (Gemini Multimodal 1.5 Flash)
    implementation("com.google.ai.client.generativeai:generativeai:0.9.0")

    // LiteRT-LM On-Device LLM Runtime
    implementation(libs.litertlm.android) {
        exclude(group = "org.jetbrains.kotlin")
    }

    // Media3 ExoPlayer
    implementation("androidx.media3:media3-exoplayer:1.3.1")
    implementation("androidx.media3:media3-ui:1.3.1")

    // Testing
    testImplementation(libs.junit)
    testImplementation(libs.json)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
