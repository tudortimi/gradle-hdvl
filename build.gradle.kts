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
            displayName = "Plugin for SystemVerilog support in HDL simulators"
            description = "A plugin that adds support for compiling and running SystemVerilog code in HDL simulators"
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
            displayName = "Plugin for SVUnit support"
            description = "A plugin that adds testing of HDVL projects with SVUnit"
            implementationClass = "com.verificationgentleman.gradle.hdvl.svunit.SVUnitPlugin"
        }
        create("svunit-build") {
            id = "com.verificationgentleman.gradle.hdvl.svunit-build"
            displayName = "Plugin for building SVUnit itself as an HDVL project"
            description = "A plugin that builds SVUnit as an HDVL project"
            implementationClass = "com.verificationgentleman.gradle.hdvl.svunit.SVUnitBuildPlugin"
        }
        create("svunit-build-injector") {
            id = "com.verificationgentleman.gradle.hdvl.svunit-build-injector"
            displayName = "Plugin for injecting build Gradle into SVUnit itself"
            description = "A plugin that injects a Gradle build into SVUnit"
            implementationClass = "com.verificationgentleman.gradle.hdvl.svunit.SVUnitBuildInjectorPlugin"
        }
        create("dvt") {
            id = "com.verificationgentleman.gradle.hdvl.dvt"
            displayName = "Plugin for DVT IDE support"
            description = "A plugin that creates DVT projects from HDVL sources"
            implementationClass = "com.verificationgentleman.gradle.hdvl.dvt.DVTPlugin"
        }
    }
}

dependencies {
    testImplementation("org.spockframework:spock-core:2.0-groovy-3.0") {
        exclude(group = "org.codehaus.groovy")
    }
    testImplementation("org.spockframework:spock-junit4:2.0-groovy-3.0")
}

repositories {
    jcenter()
}

tasks.withType(JavaCompile::class) {
  options.compilerArgs.addAll(listOf("-Xlint:unchecked", "-Werror"))
}
