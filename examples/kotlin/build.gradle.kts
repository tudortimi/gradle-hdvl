import com.verificationgentleman.gradle.hdvl.systemverilog.sv

plugins {
    id("com.verificationgentleman.gradle.hdvl.systemverilog")
}

tasks.register<Copy>("copy") {
    from(sourceSets.main.get().sv.files)
    into("build")
}
