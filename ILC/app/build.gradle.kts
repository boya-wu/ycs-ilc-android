plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

val major = 0
val minor = 1
val patch = 0
val buildNumber = System.getenv("BUILD_NUMBER")?.toIntOrNull() ?: 0
val versionNameComputed = "$major.$minor.$patch"
val versionCodeComputed = (major * 10000 + minor * 100 + patch) * 100 + buildNumber

android {
    namespace = "com.yuchens.ilcandroid"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.yuchens.ilcandroid"
        minSdk = 26
        targetSdk = 35
        versionCode = versionCodeComputed
        versionName = versionNameComputed
        buildConfigField("String", "APP_VERSION_NAME", "\"$versionNameComputed\"")
        buildConfigField("int", "APP_VERSION_CODE", "$versionCodeComputed")
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables.useSupportLibrary = true
    }

    buildTypes {
        debug {
            buildConfigField("boolean", "SHOW_BUILD_TYPE", "true")
        }
        release {
            isMinifyEnabled = false
            buildConfigField("boolean", "SHOW_BUILD_TYPE", "false")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.androidx.fragment.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
