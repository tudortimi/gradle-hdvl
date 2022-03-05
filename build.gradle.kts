plugins {
    id("java-gradle-plugin")
    id("groovy")
    id("maven-publish")
    id("com.gradle.plugin-publish") version "0.20.0"
}

pluginBundle {
    website = "https://github.com/tudortimi/gradle-hdvl/blob/master/README.md"
    vcsUrl = "https://github.com/tudortimi/gradle-hdvl"
    tags = listOf("SystemVerilog", "HDL", "hardware", "verification", "Xcelium", "QuestaSim", "SVUnit")
}

apply(from = "$rootDir/gradle/functional-test.gradle")

group = "com.verificationgentleman.gradle"
version = "0.2.0"

gradlePlugin {
    plugins {
        create("systemverilog") {
            id = "com.verificationgentleman.gradle.hdvl.systemverilog"
            implementationClass = "com.verificationgentleman.gradle.hdvl.systemverilog.SystemVerilogPlugin"
        }
        create("c") {
            id = "com.verificationgentleman.gradle.hdvl.c"
            displayName = "Plugin for C support in HDL simulators"
            description = "A plugin that adds support for compiling and running C code in HDL simulators through the DPI"
            implementationClass = "com.verificationgentleman.gradle.hdvl.c.CPlugin"
        }
        create("svunit") {
            id = "com.verificationgentleman.gradle.hdvl.svunit"
            implementationClass = "com.verificationgentleman.gradle.hdvl.svunit.SVUnitPlugin"
        }
        create("svunit-build") {
            id = "com.verificationgentleman.gradle.hdvl.svunit-build"
            implementationClass = "com.verificationgentleman.gradle.hdvl.svunit.SVUnitBuildPlugin"
        }
        create("svunit-build-injector") {
            id = "com.verificationgentleman.gradle.hdvl.svunit-build-injector"
            implementationClass = "com.verificationgentleman.gradle.hdvl.svunit.SVUnitBuildInjectorPlugin"
        }
        create("dvt") {
            id = "com.verificationgentleman.gradle.hdvl.dvt"
            implementationClass = "com.verificationgentleman.gradle.hdvl.dvt.DVTPlugin"
        }
    }
}

dependencies {
    testCompile("org.spockframework:spock-core:1.1-groovy-2.4") {
        exclude(group = "org.codehaus.groovy")
    }
}

repositories {
    jcenter()
}

tasks.withType(JavaCompile::class) {
  options.compilerArgs.addAll(listOf("-Xlint:unchecked", "-Werror"))
}
