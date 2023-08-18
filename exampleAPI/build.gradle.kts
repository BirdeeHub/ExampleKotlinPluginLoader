plugins {
    kotlin("jvm")
    //id("com.github.johnrengelman.shadow") version "8.1.1"
    // Apply the java-library plugin for API and implementation separation.
    `java-library`
}
repositories {
    mavenCentral()
    gradlePluginPortal()
}
dependencies {
    compileOnly(kotlin("stdlib"))
    compileOnly("org.apache.commons:commons-math3:3.6.1")
    //api("org.ow2.asm:asm:9.5") //<-- this kicks reflection's butt!
    //api("org.jetbrains.kotlin:kotlin-reflect:1.8.10")
    //api("org.reflections:reflections:0.10.2")
    compileOnly("com.google.guava:guava:31.1-jre")
    //runtimeOnly("org.slf4j:slf4j-nop:1.7.32")
}
// Apply a specific Java toolchain to ease working on different environments.
java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}
tasks {
    // make it output to the shared output directory to make it easier to run it with the plugin
    jar {
        destinationDirectory.set(file("../outputDir/API/"))
        archiveFileName.set("exampleAPI.jar")
    }
}