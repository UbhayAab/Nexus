import java.util.Properties

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    // If you add Room + KSP later, also:
    // alias(libs.plugins.ksp)
}

val localProps = Properties().apply {
    val f = rootProject.file("local.properties")
    if (f.exists()) f.inputStream().use { load(it) }
}

fun reqProp(key: String): String =
    (localProps.getProperty(key) ?: System.getenv(key.replace('.', '_')))
        ?: error("Missing required property: $key (add it to local.properties)")

android {
    namespace = "com.nexus.data"

    // Use a stable SDK you have installed; 36 requires preview SDK.
    compileSdk = 35

    defaultConfig {
        minSdk = 24
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")

        // ✅ Correct quoting for Kotlin DSL buildConfigField:
        buildConfigField("String", "SUPABASE_URL", "\"${reqProp("supabase.url")}\"")
        buildConfigField("String", "SUPABASE_ANON_KEY", "\"${reqProp("supabase.anonKey")}\"")
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

    // AGP 8.x expects JDK 17. Bump these or you’ll hit toolchain issues later.
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }

    // Library modules need this explicitly to generate BuildConfig
    buildFeatures { buildConfig = true }
}

dependencies {
    implementation(libs.androidx.core.ktx)

    // (UI libs usually aren’t needed in a pure data module; keep only if you really use them)
    // implementation(libs.androidx.appcompat)
    // implementation(libs.material)

    // Supabase (jan-tennert supabase-kt). Make sure these exist in your versions catalog.
    implementation(platform(libs.supabase.bom))
    implementation(libs.supabase.gotrue)
    implementation(libs.supabase.postgrest)
    implementation(libs.supabase.realtime)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
