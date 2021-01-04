plugins {
    id("java-gradle-plugin")
    id("groovy")
    id("maven-publish")
}

apply(from = "$rootDir/gradle/functional-test.gradle")

group = "com.verificationgentleman.gradle"
version = "0.1.0-SNAPSHOT"

gradlePlugin {
    plugins {
        create("systemverilog") {
            id = "com.verificationgentleman.gradle.hdvl.systemverilog"
            implementationClass = "com.verificationgentleman.gradle.hdvl.SystemVerilogPlugin"
        }
        create("svunit") {
            id = "com.verificationgentleman.gradle.hdvl.svunit"
            implementationClass = "com.verificationgentleman.gradle.hdvl.svunit.SvunitPlugin"
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
