plugins {
 id("com.android.application")
 id("org.jetbrains.kotlin.android")
}
android {
 namespace = "com.josxtm.traductorlive"
 compileSdk = 35
 defaultConfig {
  applicationId = "com.josxtm.traductorlive"
  minSdk = 26
  targetSdk = 35
  versionCode = 2
  versionName = "0.2.0"
 }
 compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
 kotlinOptions { jvmTarget = "17" }
}
dependencies {
 implementation("com.google.mlkit:translate:17.0.2")
 implementation("com.google.mlkit:language-id:17.0.6")
}