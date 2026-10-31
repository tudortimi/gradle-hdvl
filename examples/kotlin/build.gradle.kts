import org.gradle.api.file.SourceDirectorySet

plugins {
    id("com.verificationgentleman.gradle.hdvl.systemverilog")
}

tasks.register<Copy>("copy") {
    from((sourceSets.main.get().extensions.getByName("sv") as SourceDirectorySet).files)
    into("build")
}
