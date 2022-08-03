import org.jetbrains.intellij.ideaDir

plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "1.6.20"
    id("org.jetbrains.intellij") version "1.5.2"
}

group = "com.byte_stefan"
version = "1.0-alpha.1"

repositories {
    mavenCentral()
}

// Configure Gradle IntelliJ Plugin - read more: https://github.com/JetBrains/gradle-intellij-plugin
intellij {
    version.set("212.5712.43")
//    localPath.set("/Applications/Android Studio.app/Contents")
    type.set("IC") // Target IDE Platform
    plugins.set(listOf("com.intellij.java", "org.jetbrains.kotlin", "android", "git4idea"))
}

tasks {
    // Set the JVM compatibility versions
    withType<JavaCompile> {
        sourceCompatibility = "11"
        targetCompatibility = "11"
    }
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.jvmTarget = "11"
    }

    patchPluginXml {
        sinceBuild.set("203.7717")
        untilBuild.set("222.*")
    }

    runIde {
        ideaDir("/Applications/Android Studio.app/Contents")
    }
}
