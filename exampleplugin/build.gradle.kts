plugins {
    kotlin("jvm")
    // Apply the java-library plugin because we dont need a Main-Class (plugin loader loads the correct class.)
    `java-library`
}
repositories {
    mavenCentral()
}
dependencies {
    implementation(kotlin("stdlib"))
    implementation(project(":examplepluginloader"))
    // This dependency is exported to consumers, that is to say found on their compile classpath.
    api("org.apache.commons:commons-math3:3.6.1")
    // This dependency is used internally, and not exposed to consumers on their own compile classpath.
    implementation("com.google.guava:guava:31.1-jre")
}

// Apply a specific Java toolchain to ease working on different environments.
java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}
tasks {
    // make it output to the shared output directory to make it easier to run it with the plugin (the example program uses the loader to load from ./ by default unless you specify)
    jar {
        destinationDirectory.set(file("../outputDir/plugins/"))
    }
}