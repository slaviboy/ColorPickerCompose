plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.compose)
    `maven-publish`
}

android {
    namespace = "com.slaviboy.colorpicker"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        minSdk = 24
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }

    publishing {
        singleVariant("release")
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.foundation)
    debugImplementation(libs.androidx.compose.ui.tooling)
    testImplementation(libs.junit)
}

// JitPack invokes `./gradlew publishToMavenLocal -Pgroup=... -Pversion=...`; this registers the
// "release" AAR variant (declared above) as a Maven publication so that task exists. The release
// component isn't available until the Android plugin finishes configuring variants, hence
// afterEvaluate. groupId/version default to the project's group/version, which Gradle populates
// from the -P command line properties JitPack passes in - no need to hardcode them here.
afterEvaluate {
    publishing {
        publications {
            register<MavenPublication>("release") {
                from(components["release"])
                artifactId = "colorpicker"
            }
        }
    }
}
