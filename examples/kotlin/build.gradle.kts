plugins {
    id("com.verificationgentleman.gradle.hdvl.systemverilog")
}

tasks.register<Copy>("copy") {
    // XXX Not clear why we can't just do 'sourceSets.main.sv'. This is probably because when the extension
    // is added, the element type of 'sourceSets' is lost.
    from(sourceSets.main.get().sv.files)
    into("build")
}
