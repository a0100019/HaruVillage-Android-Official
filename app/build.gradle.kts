plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("kotlin-kapt")
    id("com.google.dagger.hilt.android")
    alias(libs.plugins.google.gms.google.services)

}

android {
    namespace = "com.a0100019.mypat"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.a0100019.mypat"
        minSdk = 26
        targetSdk = 35
        versionCode = 29
        versionName = "26.11"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    // 1. 추가: 16KB 페이지 크기 지원을 위한 패키징 설정
    packaging {
        jniLibs {
            // jni 라이브러리를 압축하지 않고 저장하여 16KB 정렬을 지원하도록 합니다.
            useLegacyPackaging = false
        }
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    // 2. 추가: 네이티브 라이브러리 정렬 설정 (혹시 모를 네이티브 종속성 대비)
    // 이 코드는 필수는 아니지만, 16KB 오류가 지속될 때 큰 도움이 됩니다.
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
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
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.3"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    kapt {
        correctErrorTypes = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.hilt.common)
    implementation(libs.firebase.auth)
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.googleid)
    implementation(libs.firebase.firestore.ktx)
    implementation(libs.androidx.runtime.android)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    //아래부터 추가한 것

    implementation(libs.orbit.core)
    implementation(libs.orbit.compose)
    implementation(libs.orbit.viewmodel)

    implementation(libs.hilt)
    kapt(libs.hilt.compiler)

    implementation(libs.coil)
    implementation(libs.coil.compose)

    implementation(libs.appcompat)

    implementation(libs.navigation.compose)
    implementation(libs.hilt.navigation.compose)

    // Room
    implementation(libs.androidx.room.runtime)
    //noinspection KaptUsageInsteadOfKsp
    kapt(libs.room.compiler)
    implementation(libs.room.runtime)

    // Coroutine (필수는 아니지만 권장)
    implementation(libs.kotlinx.coroutines.android)

    implementation("com.airbnb.android:lottie-compose:6.4.0")

    //workManager
    implementation(libs.androidx.work.runtime.ktx)

    //chart
    implementation("co.yml:ycharts:2.1.0")

    //웹 열기
    implementation ("androidx.browser:browser:1.7.0")

    //firebase
    implementation(platform("com.google.firebase:firebase-bom:33.12.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.android.gms:play-services-auth:20.7.0")
    implementation ("com.google.firebase:firebase-firestore-ktx")
    implementation ("com.google.firebase:firebase-storage-ktx")

    //JSON → Kotlin 객체 변환 담당
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")

    //communityViewModel
    implementation("org.jetbrains.kotlin:kotlin-reflect")

    //삼성 헬스
    implementation("androidx.health.connect:connect-client:1.1.0-alpha04")

    // 구글 플레이 인앱 리뷰 라이브러리
    implementation("com.google.android.play:review:2.0.1")
    implementation("com.google.android.play:review-ktx:2.0.1") // Kotlin 확장 라이브러리

    //gemini ai
    implementation("com.google.ai.client.generativeai:generativeai:0.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    // Retrofit: 서버와 통신하는 핵심 라이브러리
    implementation("com.squareup.retrofit2:retrofit:2.9.0")

    // Scalars Converter: 서버에서 주는 텍스트를 String으로 바로 받기 위해
    implementation("com.squareup.retrofit2:converter-scalars:2.9.0")

}