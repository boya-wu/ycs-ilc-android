import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.hilt.android)
}

/* ===== 版本邏輯：SemVer + CI Build Number ===== */
val major = 1
val minor = 0
val patch = 0
val buildNumber = System.getenv("BUILD_NUMBER")?.toIntOrNull() ?: 0

val versionNameComputed = "$major.$minor.$patch"
val versionCodeComputed = (major * 10000 + minor * 100 + patch) * 100 + buildNumber

check(versionCodeComputed > 0) {
    "versionCode must be > 0. Computed=$versionCodeComputed (major=$major, minor=$minor, patch=$patch, build=$buildNumber)"
}
/* ===================================================================== */

android {
    namespace = "com.yuchens.equipinspectandroid"

    compileSdk = 35
    defaultConfig {
        applicationId = "com.yuchens.equipinspectandroid"
        minSdk = 26
        targetSdk = 35

        versionCode = versionCodeComputed
        versionName = versionNameComputed

        buildConfigField("String", "APP_VERSION_NAME", "\"$versionNameComputed\"")
        buildConfigField("int",    "APP_VERSION_CODE", "$versionCodeComputed")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables.useSupportLibrary = true
    }

    // 讀取 keystore.properties
    val keystoreProps = Properties().apply {
        val f = rootProject.file("keystore.properties")
        if (f.exists()) f.inputStream().use { load(it) }
    }
    signingConfigs {
        create("release") {
            val storePath = keystoreProps["storeFile"] as String?
            if (!storePath.isNullOrBlank()) {
                storeFile = when {
                    storePath.startsWith("./") -> rootProject.file(storePath.removePrefix("./"))
                    storePath.startsWith("../") -> rootProject.file(storePath)
                    Regex("^[A-Za-z]:\\\\.*|^/.*").matches(storePath) -> file(storePath)
                    else -> rootProject.file(storePath)
                }
            }
            storePassword = keystoreProps["storePassword"] as String?
            keyAlias = keystoreProps["keyAlias"] as String?
            keyPassword = keystoreProps["keyPassword"] as String?
        }
    }

    // 讀 local.properties
    val localProps = Properties().apply {
        val f = rootProject.file("local.properties")
        if (f.exists()) f.inputStream().use { load(it) }
    }

    val adminPinHash: String = localProps.getProperty("ADMIN_PIN_HASH") ?: ""
    val adminPinPepper: String = localProps.getProperty("ADMIN_PIN_PEPPER") ?: ""

    buildTypes {
        debug {
            buildConfigField("String", "ADMIN_PIN_HASH", "\"$adminPinHash\"")
            buildConfigField("String", "ADMIN_PIN_PEPPER", "\"$adminPinPepper\"")

            buildConfigField("boolean", "SHOW_BUILD_TYPE", "true")
        }

        release {
            // 啟用混淆與資源裁剪（體積小、較難逆向）
            isMinifyEnabled = true
            isShrinkResources = true
            isDebuggable = false
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )

            buildConfigField("String", "ADMIN_PIN_HASH", "\"$adminPinHash\"")
            buildConfigField("String", "ADMIN_PIN_PEPPER", "\"$adminPinPepper\"")

            buildConfigField("boolean", "SHOW_BUILD_TYPE", "false")
        }

        // 做一個不混淆但簽章的 internal 渠道
        create("internal") {
            initWith(getByName("release"))
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = false
            isShrinkResources = false
            applicationIdSuffix = ".internal"
            versionNameSuffix = "-internal"

            buildConfigField("String", "ADMIN_PIN_HASH", "\"$adminPinHash\"")
            buildConfigField("String", "ADMIN_PIN_PEPPER", "\"$adminPinPepper\"")

            buildConfigField("boolean", "SHOW_BUILD_TYPE", "true")
        }
    }
    // AGP 8.x 需要 JDK 17
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
    // 打包時排除常見授權檔以避免重覆
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    // Release 時嚴格一點
    lint {
        checkReleaseBuilds = true
        abortOnError = true
    }
}

dependencies {
    // AndroidX
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)

    // Navigation
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)

    // 網路
    implementation(libs.okhttp)

    // Lifecycle / ViewModel / LiveData
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.process)

    // Coroutines
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)

    // Room（KSP）
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // JSON
    implementation(libs.kotlinx.serialization.json)

    // KTX
    implementation(libs.androidx.fragment.ktx)

    // Hilt（KSP）
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    // 產碼工具
    implementation(libs.javapoet)

    // DataStore
    implementation(libs.androidx.datastore.preferences)

    // 測試
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}