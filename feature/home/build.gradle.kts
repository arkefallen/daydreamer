plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.jetbrains.kotlin.android)
    id("com.google.dagger.hilt.android")
    id("io.realm.kotlin")
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.android.ark.home"
    compileSdk = ProjectConfig.COMPILE_SDK_VERSION

    defaultConfig {
        minSdk = ProjectConfig.MINIMUM_SDK_VERSION

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = ProjectConfig.KOTLIN_COMPILER_EXTENSION_VERSION
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(libs.androidx.material3)
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)

    // Dagger Hilt DI
    implementation(libs.google.dagger.hilt.android)
    ksp(libs.google.dagger.hilt.compiler)
    ksp(libs.androidx.hilt.compiler)

    // Google Firebase Service
    implementation(libs.google.firebase.auth)
    implementation(libs.google.firebase.storage)
    implementation(libs.google.android.play.services.auth)

    // Mongo DB - Realm
    implementation(libs.jetbrains.kotlinx.coroutines.core)
    implementation(libs.realm.kotlin.library.sync)
    implementation(libs.realm.kotlin.library.base)

    implementation(libs.coil.kotlin)
    implementation(libs.androidx.core.splash)

    implementation(project(":data:model"))
    implementation(project(":core:util"))
    implementation(project(":core:ui"))
    implementation(project(":data:room"))
    implementation(project(":data:mongo"))
    implementation(project(":domain"))

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}