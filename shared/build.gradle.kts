plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.sqldelight)
    alias(libs.plugins.kotlin.serialization)
    id("com.android.library")
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
            freeCompilerArgs.add("-Xexpect-actual-classes")
        }
    }

    iosX64()
    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        val commonMain by getting {
            dependencies {
                // Coroutines
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${libs.versions.kotlinxCoroutines.get()}")
                
                // Serialization
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:${libs.versions.kotlinxSerialization.get()}")
                
                // DateTime
                implementation("org.jetbrains.kotlinx:kotlinx-datetime:${libs.versions.kotlinxDateTime.get()}")
                
                // Ktor Client
                implementation("io.ktor:ktor-client-core:${libs.versions.ktor.get()}")
                implementation("io.ktor:ktor-client-content-negotiation:${libs.versions.ktor.get()}")
                implementation("io.ktor:ktor-serialization-kotlinx-json:${libs.versions.ktor.get()}")
                implementation("io.ktor:ktor-client-logging:${libs.versions.ktor.get()}")
                
                // SQLDelight
                implementation("app.cash.sqldelight:runtime:${libs.versions.sqldelight.get()}")
                implementation("app.cash.sqldelight:coroutines-extensions:${libs.versions.sqldelight.get()}")
                
                // Settings
                implementation("com.russhwolf:multiplatform-settings:${libs.versions.multiplatformSettings.get()}")
            }
        }
        
        val androidMain by getting {
            dependencies {
                // SQLDelight Android Driver
                implementation("app.cash.sqldelight:android-driver:${libs.versions.sqldelight.get()}")
                
                // Ktor Android
                implementation("io.ktor:ktor-client-android:${libs.versions.ktor.get()}")
            }
        }
        
        val iosMain by creating {
            dependsOn(commonMain)
            dependencies {
                // SQLDelight iOS Driver
                implementation("app.cash.sqldelight:native-driver:${libs.versions.sqldelight.get()}")
                
                // Ktor iOS
                implementation("io.ktor:ktor-client-darwin:${libs.versions.ktor.get()}")
            }
        }
        
        val iosX64Main by getting {
            dependsOn(iosMain)
        }
        
        val iosArm64Main by getting {
            dependsOn(iosMain)
        }
        
        val iosSimulatorArm64Main by getting {
            dependsOn(iosMain)
        }
        
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:${libs.versions.kotlinxCoroutines.get()}")
            }
        }
    }
}

sqldelight {
    databases {
        create("PetSchedulingDatabase") {
            packageName.set("com.hfad.pet_scheduling.database")
            generateAsync.set(false)
        }
    }
}

android {
    namespace = "com.hfad.pet_scheduling.shared"
    compileSdk = 34
    
    defaultConfig {
        minSdk = 26
    }
    
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

