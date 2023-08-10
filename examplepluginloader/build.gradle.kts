plugins {
    kotlin("jvm")
    id("com.github.johnrengelman.shadow") version "8.1.1"
    // Apply the java-library plugin for API and implementation separation.
    `java-library`
}
repositories {
    mavenCentral()
}
dependencies {
    // This dependency is exported to consumers, that is to say found on their compile classpath.
    api(kotlin("stdlib"))
    api("org.apache.commons:commons-math3:3.6.1")
    // This dependency is used internally, and not exposed to consumers on their own compile classpath.
    implementation("com.google.guava:guava:31.1-jre")
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.8.10")
    implementation("org.reflections:reflections:0.10.2")
    runtimeOnly("org.slf4j:slf4j-nop:1.7.32")
}

// Apply a specific Java toolchain to ease working on different environments.
java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

tasks {
    // java-library plugin wont find main class for us but we want to be able to make kotlin runtime available to plugins so we need it
    jar {
        manifest { attributes( "Main-Class" to "examplepluginloader.MainKt" ) }
    }
    // make it output to the shared output directory to make it easier to run it with the plugin (the example program uses the loader to load from ./ by default unless you specify)
    shadowJar {
        destinationDirectory.set(file("../outputDir/"))
    }
}