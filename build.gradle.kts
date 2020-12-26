plugins {
    id("java-gradle-plugin")
}

gradlePlugin {
    plugins {
        create("systemverilog") {
            id = "com.verificationgentleman.gradle.hdvl.systemverilog"
            implementationClass = "com.verificationgentleman.gradle.hdvl.SystemVerilogPlugin"
        }
    }
}
