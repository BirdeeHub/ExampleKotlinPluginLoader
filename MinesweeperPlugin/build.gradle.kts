plugins {
    // Apply the java-library plugin because we dont need a Main-Class (plugin loader loads the correct class.)
    `java-library`
}
repositories {
    mavenCentral()
    gradlePluginPortal()
//    flatDir {
//        dirs("../outputDir/API/")
//    }
}
dependencies {
    compileOnly(project(":exampleAPI"))
    compileOnly("org.apache.commons:commons-math3:3.6.1")
    compileOnly("com.google.guava:guava:31.1-jre")
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
        manifest { attributes( "Main-Class" to "MySweep.MineSweeper" ) }
        destinationDirectory.set(file("../plugins/"))
        archiveFileName.set("minesweeper.jar")
    }
}