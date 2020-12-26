plugins {
    id("java-gradle-plugin")
    id("groovy")
}

apply(from = "$rootDir/gradle/functional-test.gradle")

gradlePlugin {
    plugins {
        create("systemverilog") {
            id = "com.verificationgentleman.gradle.hdvl.systemverilog"
            implementationClass = "com.verificationgentleman.gradle.hdvl.SystemVerilogPlugin"
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
