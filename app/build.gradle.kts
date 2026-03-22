plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.play.publisher)
    jacoco
}

android {
    namespace = "dev.lanthoor.spendly"
    compileSdk = 36

    defaultConfig {
        applicationId = "dev.lanthoor.spendly"
        minSdk = 31
        targetSdk = 36
        ndkVersion = "29.0.14206865"
        versionCode = 90
        versionName = "0.9.0-beta"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    testOptions {
        // Disable orchestrator on CI due to snapshot caching conflicts
        if (System.getenv("CI") != "true") {
            execution = "ANDROIDX_TEST_ORCHESTRATOR"
        }
        animationsDisabled = true
    }

    signingConfigs {
        create("release") {
            // Only require signing on CI with proper env vars
            val keystoreFile = System.getenv("KEYSTORE_FILE")
            if (keystoreFile != null && File(keystoreFile).exists()) {
                storeFile = file(keystoreFile)
                storePassword = System.getenv("KEYSTORE_PASSWORD")
                keyAlias = System.getenv("KEY_ALIAS")
                keyPassword = System.getenv("KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            // Only apply signing config if it's properly configured
            signingConfig = signingConfigs.getByName("release")
        }
        debug {
            enableUnitTestCoverage = true
            enableAndroidTestCoverage = true
            signingConfig = signingConfigs.getByName("debug")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    kotlinOptions {
        jvmTarget = "21"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

// Room schema export configuration
ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

// Play Store publishing configuration
play {
    val credentialsPath = System.getenv("PLAY_STORE_CREDENTIALS_FILE")
        ?: "$projectDir/play-store-credentials.json"
    serviceAccountCredentials.set(file(credentialsPath))

    track.set("internal")
    releaseStatus.set(com.github.triplet.gradle.androidpublisher.ReleaseStatus.COMPLETED)
    defaultToAppBundles.set(true)
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.fragment)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material3.adaptive.navigation.suite)

    // Room
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // DataStore
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.datastore.core)

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)
    implementation(libs.androidx.hilt.navigation.compose)

    // Phosphor Icons
    implementation(libs.phosphor.compose)

    // CameraX
    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)

    // Coil for image loading
    implementation(libs.coil.compose)
    implementation(libs.androidx.exifinterface)

    // WorkManager for recurring transactions
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.androidx.hilt.work)
    ksp(libs.androidx.hilt.work.compiler)

    // Biometric authentication
    implementation(libs.androidx.biometric)

    // Kotlinx Serialization
    implementation(libs.kotlinx.serialization.json)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.turbine)
    androidTestImplementation(libs.kotlinx.coroutines.test)
    androidTestUtil(libs.androidx.orchestrator)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}

// Jacoco configuration
jacoco {
    toolVersion = "0.8.12"
}

// Task to generate coverage report from unit tests only (no AVD required)
tasks.register<JacocoReport>("jacocoTestReport") {
    dependsOn("testDebugUnitTest")
    
    reports {
        xml.required.set(true)
        html.required.set(true)
        csv.required.set(false)
    }

    val fileFilter = listOf(
        "**/R.class",
        "**/R$*.class",
        "**/BuildConfig.*",
        "**/Manifest*.*",
        "**/*Test*.*",
        "android/**/*.*",
        "**/*\$ViewInjector*.*",
        "**/*\$ViewBinder*.*",
        "**/databinding/*",
        "**/android/databinding/*",
        "**/androidx/databinding/*",
        "**/BR.*",
        "**/di/*",
        "**/*_HiltModules*",
        "**/*_Hilt*",
        "**/*_Factory*",
        "**/*_MembersInjector*",
        "**/Hilt_*"
    )

    val javaTree = fileTree(layout.buildDirectory.dir("intermediates/javac/debug")) {
        exclude(fileFilter)
    }

    val kotlinTree = fileTree(layout.buildDirectory.dir("tmp/kotlin-classes/debug")) {
        exclude(fileFilter)
    }

    classDirectories.setFrom(files(listOf(javaTree, kotlinTree)))
    
    val mainSrc = "${project.projectDir}/src/main/java"
    sourceDirectories.setFrom(files(listOf(mainSrc)))
    
    executionData.setFrom(fileTree(layout.buildDirectory) {
        include(
            "outputs/unit_test_code_coverage/debugUnitTest/testDebugUnitTest.exec"
        )
    })
}
