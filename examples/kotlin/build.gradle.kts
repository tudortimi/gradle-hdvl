import org.gradle.kotlin.dsl.accessors.runtime.extensionOf

plugins {
    id("com.verificationgentleman.gradle.hdvl.systemverilog")
}

println(sourceSets)
println(sourceSets.main)

sourceSets {
    main {
        println(this)
        println(name)
        println(extensions)
        println(getExtensions())
        //println(sv)
        println(getExtensions().getByName("sv"))
        //println(extensions.sv)
    }
}
sourceSets {
    main {
        extensions.configure<SourceDirectorySet>("sv") {
            println(this)
            println(this.files)
        }

        extensions.configure<SourceDirectorySet>() {
            println(this)
            println(this.files)
        }

        with(extensionOf(this, "svHeaders") as SourceDirectorySet) {
            println(this)
            println(this.files)
        }

        configure<SourceDirectorySet> {
            println(this)
        }

        with(the<SourceDirectorySet>()) {
            println(this)
        }
    }
}

//println(sourceSets.main.getExtensions())
//println(sourceSets.main.sv)
//sourceSets {
//    main {
//        sv {
//            exclude("**/dummy.sv")
//        }
//    }
//}

//tasks.register<Copy>("copy") {
//    from(sourceSets.main.sv.files)
//    include("*")
//    into("build")
//}

//tasks.register<Copy>("copy") {
//    // XXX Not clear why we can't just do 'sourceSets.main.sv'. This is probably because when the extension
//    // is added, the element type of 'sourceSets' is lost.
//    from(sourceSets.main.get().sv.files)
//    into("build")
//}
