/*
 * Copyright 2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.verificationgentleman.gradle.hdvl.systemverilog

import org.gradle.testkit.runner.GradleRunner
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Ignore
import spock.lang.Specification

import static org.gradle.testkit.runner.TaskOutcome.NO_SOURCE
import static org.gradle.testkit.runner.TaskOutcome.SUCCESS

class SystemVerilogPluginSpec extends Specification {
    @Rule TemporaryFolder testProjectDir = new TemporaryFolder()
    File buildFile

    def setup() {
        buildFile = testProjectDir.newFile('build.gradle')
        buildFile << """
            plugins {
                id 'com.verificationgentleman.gradle.hdvl.systemverilog'
            }
        """
    }

    /**
     * Creates a new project in a new directory, with a standard layout.
     *
     * @param name The project name
     * @return The build file of the project
     */
    def newStandardProject(String name) {
        File folder = testProjectDir.newFolder(name)

        File sv = testProjectDir.newFolder(name,'src', 'main', 'sv')
        new File(sv, "${name}.sv").createNewFile()

        File buildFile = new File(folder, "build.gradle")
        buildFile << """
            plugins {
                id 'com.verificationgentleman.gradle.hdvl.systemverilog'
            }
        """

        return buildFile
    }

    def "can successfully import the plugin"() {
        when:
        def result = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withPluginClasspath()
                .build()

        then:
        result.task(":help").outcome == SUCCESS
    }

    def "can access 'sourceSets' property"() {
        buildFile << """
            task assertProps {
                doLast {
                    assert project.sourceSets != null
                }
            }
        """

        when:
        def result = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withPluginClasspath()
                .withArguments('assertProps')
                .build()

        then:
        result.task(":assertProps").outcome == SUCCESS
    }

    def "can configure a source set"() {
        File sv = testProjectDir.newFolder('src', 'main', 'sv')
        new File(sv, 'dummy.sv').createNewFile()

        buildFile << """
            sourceSets {
                main
            }
            
            task copy(type: Copy) {
                from sourceSets.main.sv.files
                into 'build'
            }
        """

        when:
        def result = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withPluginClasspath()
                .withArguments('copy')
                .build()

        then:
        result.task(":copy").outcome == SUCCESS
        new File(testProjectDir.root, 'build/dummy.sv').exists()
    }

    def "source set ignores 'svh' files"() {
        File sv = testProjectDir.newFolder('src', 'main', 'sv')
        new File(sv, 'dummy.sv').createNewFile()
        new File(sv, 'dummy.svh').createNewFile()

        buildFile << """
            sourceSets {
                main
            }
            
            task copy(type: Copy) {
                from sourceSets.main.sv.files
                into 'build'
            }
        """

        when:
        def result = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withPluginClasspath()
                .withArguments('copy')
                .build()

        then:
        result.task(":copy").outcome == SUCCESS
        !(new File(testProjectDir.root, 'build/dummy.svh').exists())
    }

    def "can specify a source set source directory using a closure"() {
        File sv = testProjectDir.newFolder('sv')
        new File(sv, 'dummy.sv').createNewFile()

        buildFile << """
            sourceSets {
                main {
                    sv {
                        srcDirs = ['sv']
                    }
                }
            }
            
            task copy(type: Copy) {
                from sourceSets.main.sv.files
                into 'build'
            }
        """

        when:
        def result = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withPluginClasspath()
                .withArguments('copy')
                .build()

        then:
        result.task(":copy").outcome == SUCCESS
        new File(testProjectDir.root, 'build/dummy.sv').exists()
    }

    def "can specify a source set source exported header directory using a closure"() {
        File sv = testProjectDir.newFolder('sv')
        new File(sv, 'dummy.svh').createNewFile()

        buildFile << """
            sourceSets {
                main {
                    svHeaders {
                        srcDirs = ['sv']
                    }
                }
            }
            
            task copy(type: Copy) {
                from sourceSets.main.svHeaders.files
                into 'build'
            }
        """

        when:
        def result = GradleRunner.create()
            .withProjectDir(testProjectDir.root)
            .withPluginClasspath()
            .withArguments('copy')
            .build()

        then:
        result.task(":copy").outcome == SUCCESS
        new File(testProjectDir.root, 'build/dummy.svh').exists()
    }

    def "'main' source set is added by the plugin"() {
        File sv = testProjectDir.newFolder('src', 'main', 'sv')
        new File(sv, 'dummy.sv').createNewFile()

        buildFile << """            
            task copy(type: Copy) {
                from sourceSets.main.sv.files
                into 'build'
            }
        """

        when:
        def result = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withPluginClasspath()
                .withArguments('copy')
                .build()

        then:
        result.task(":copy").outcome == SUCCESS
        new File(testProjectDir.root, 'build/dummy.sv').exists()
    }

    @Ignore("Complains that the source set doesn't support conventions")
    def "can specify a source set source exclude using an action"() {
        // XXX Most tests use 'build.gradle', but in this test we want to use a Kotlin build script. It seems like
        // overkill to create a new test class just fo this.
        setup:
        new File(testProjectDir.root, 'build.gradle').delete()

        File sv = testProjectDir.newFolder('src', 'main', 'sv')
        new File(sv, 'dummy.sv').createNewFile()

        File buildFile = testProjectDir.newFile('build.gradle.kts')
        buildFile << """
            import com.verificationgentleman.gradle.hdvl.systemverilog.SystemVerilogSourceSet
            
            plugins {
                id("com.verificationgentleman.gradle.hdvl.systemverilog")
            }
            
            sourceSets {
                main {
                    withConvention(SystemVerilogSourceSet::class) {
                        sv.exclude("**/dummy.sv")
                    }
                }
            }
            
            tasks.register<Copy>("copy") {
                // XXX Not clear why we can't just do 'sourceSets.main.sv'.
                // 'sourceSets.main' doesn't return an object of type 'SourceSet', but a
                // 'NamedDomainObjectProvider<SourceSet'. The Java plugin has the same issue.
                from(sourceSets.main.withConvention(SystemVerilogSourceSet::class) { sv }.files)
                include("*")
                into("build")
            }
        """

        when:
        def result = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withPluginClasspath()
                .withArguments('copy')
                .build()

        then:
        result.task(":copy").outcome == NO_SOURCE
    }

    @Ignore("Complains that the source set doesn't support conventions")
    def "can specify a source set exported header source exclude using an action"() {
        // XXX Most tests use 'build.gradle', but in this test we want to use a Kotlin build script. It seems like
        // overkill to create a new test class just fo this.
        setup:
        new File(testProjectDir.root, 'build.gradle').delete()

        File sv = testProjectDir.newFolder('src', 'main', 'sv_headers')
        new File(sv, 'dummy.svh').createNewFile()

        File buildFile = testProjectDir.newFile('build.gradle.kts')
        buildFile << """
            import com.verificationgentleman.gradle.hdvl.systemverilog.SystemVerilogSourceSet
            
            plugins {
                id("com.verificationgentleman.gradle.hdvl.systemverilog")
            }
            
            sourceSets {
                main {
                    withConvention(SystemVerilogSourceSet::class) {
                        svHeaders.exclude("**/dummy.svh")
                    }
                }
            }
            
            tasks.register<Copy>("copy") {
                // XXX Not clear why we can't just do 'sourceSets.main.sv'.
                // 'sourceSets.main' doesn't return an object of type 'SourceSet', but a
                // 'NamedDomainObjectProvider<SourceSet'. The Java plugin has the same issue.
                from(sourceSets.main.withConvention(SystemVerilogSourceSet::class) { svHeaders }.files)
                include("*")
                into("build")
            }
        """

        when:
        def result = GradleRunner.create()
            .withProjectDir(testProjectDir.root)
            .withPluginClasspath()
            .withArguments('copy')
            .build()

        then:
        result.task(":copy").outcome == NO_SOURCE
    }

    def "'genArgsFile' task produces output"() {
        File sv = testProjectDir.newFolder('src', 'main', 'sv')
        new File(sv, 'dummy.sv').createNewFile()

        when:
        def result = GradleRunner.create()
            .withProjectDir(testProjectDir.root)
            .withPluginClasspath()
            .withArguments('genArgsFile')
            .build()

        then:
        result.task(":genArgsFile").outcome == SUCCESS
        new File(testProjectDir.root, 'build/args.f').exists()
    }

    def "'genArgsFile' task writes compile files to args file"() {
        File sv = testProjectDir.newFolder('src', 'main', 'sv')
        new File(sv, 'dummy.sv').createNewFile()

        when:
        def result = GradleRunner.create()
            .withProjectDir(testProjectDir.root)
            .withPluginClasspath()
            .withArguments('genArgsFile')
            .build()

        then:
        new File(testProjectDir.root, 'build/args.f').text.contains('src/main/sv/dummy.sv')
    }

    def "'genArgsFile' task writes private include directories to args file"() {
        File sv = testProjectDir.newFolder('src', 'main', 'sv')
        new File(sv, 'dummy.sv').createNewFile()

        when:
        def result = GradleRunner.create()
            .withProjectDir(testProjectDir.root)
            .withPluginClasspath()
            .withArguments('genArgsFile')
            .build()

        then:
        def lines = new File(testProjectDir.root, 'build/args.f').text.split("\n")
        def lineWithIncdir = lines.find { it.contains('-incdir') }
        lineWithIncdir != null
        lineWithIncdir.endsWith("src/main/sv")
    }

    def "'genArgsFile' task writes private include directories to args file after re-configure of source dirs"() {
        File sv = testProjectDir.newFolder('sv')
        new File(sv, 'dummy.sv').createNewFile()

        buildFile << """
            sourceSets {
                main {
                    sv {
                        srcDirs = ['sv']
                    }
                }
            }
        """

        when:
        def result = GradleRunner.create()
            .withProjectDir(testProjectDir.root)
            .withPluginClasspath()
            .withArguments('genArgsFile')
            .build()

        then:
        def lines = new File(testProjectDir.root, 'build/args.f').text.split("\n")
        def lineWithIncdir = lines.find { it.contains('-incdir') }
        lineWithIncdir != null
        !lineWithIncdir.contains("src")
        !lineWithIncdir.contains("main")
        lineWithIncdir.endsWith("sv")
    }

    def "'genArgsFile' task writes exported header directories to args file"() {
        File svHeaders = testProjectDir.newFolder('src', 'main', 'sv_headers')

        when:
        def result = GradleRunner.create()
            .withProjectDir(testProjectDir.root)
            .withPluginClasspath()
            .withArguments('genArgsFile')
            .build()

        then:
        def lines = new File(testProjectDir.root, 'build/args.f').text.split("\n")
        def linesWithIncdir = lines.findAll { it.contains('-incdir') }
        !linesWithIncdir.isEmpty()
        linesWithIncdir.any { it.endsWith("src/main/sv_headers") }
    }

    def "'genArgsFile' task doesn't write exported header directories to args file if none exist"() {
        // No 'sv_headers' directory

        when:
        def result = GradleRunner.create()
            .withProjectDir(testProjectDir.root)
            .withPluginClasspath()
            .withArguments('genArgsFile')
            .build()

        then:
        def lines = new File(testProjectDir.root, 'build/args.f').text.split("\n")
        def linesWithIncdir = lines.findAll { it.contains('-incdir') }
        !linesWithIncdir.isEmpty()
        linesWithIncdir.each { assert !it.endsWith("src/main/sv_headers") }
    }

    def "'genArgsFile' task indents entries in makelib block"() {
        File sv = testProjectDir.newFolder('src', 'main', 'sv')
        new File(sv, 'dummy.sv').createNewFile()

        when:
        def result = GradleRunner.create()
            .withProjectDir(testProjectDir.root)
            .withPluginClasspath()
            .withArguments('genArgsFile')
            .build()

        then:
        def lines = new File(testProjectDir.root, 'build/args.f').text.split('\n')
        lines.findAll { !it.contains('-makelib') && !it.contains('-endlib') }.each {
            assert it.startsWith('  ')
        }
    }

    def "'genFullArgsFile' task consumes output of 'genArgsFile"() {
        File sv = testProjectDir.newFolder('src', 'main', 'sv')
        new File(sv, 'dummy.sv').createNewFile()

        when:
        def result = GradleRunner.create()
            .withProjectDir(testProjectDir.root)
            .withPluginClasspath()
            .withArguments('genFullArgsFile')
            .build()

        then:
        result.task(":genArgsFile").outcome == SUCCESS
        result.task(":genFullArgsFile").outcome == SUCCESS
        new File(testProjectDir.root, 'build/full_args.f').exists()
        new File(testProjectDir.root, 'build/full_args.f').text.contains('build/args.f')
    }

    def "'argsFiles' artifacts produced by direct dependencies are consumed by main project in 'genFullArgsFile'"() {
        setup:
        buildFile.delete()

        File settingsFile = testProjectDir.newFile('settings.gradle')
        settingsFile << """
            include 'directDependency'
            include 'mainProject'
        """

        File directDependencyBuildFile = newStandardProject('directDependency')

        File mainProjectBuildFile = newStandardProject('mainProject')
        mainProjectBuildFile << """
            dependencies {
                compile project(':directDependency')
            }
        """

        when:
        def result = GradleRunner.create()
            .withProjectDir(testProjectDir.root)
            .withPluginClasspath()
            .withArguments(':mainProject:genFullArgsFile')
            .build()

        then:
        result.task(":directDependency:genArgsFile").outcome == SUCCESS
        result.task(":mainProject:genArgsFile").outcome == SUCCESS
        result.task(":mainProject:genFullArgsFile").outcome == SUCCESS
        new File(testProjectDir.root, 'mainProject/build/full_args.f').text.contains('directDependency/build/args.f')
        new File(testProjectDir.root, 'mainProject/build/full_args.f').text.contains('mainProject/build/args.f')
    }

    def "'argsFiles' artifacts produced by transitive dependencies are consumed in 'genFullArgsFile'"() {
        setup:
        buildFile.delete()

        File settingsFile = testProjectDir.newFile('settings.gradle')
        settingsFile << """
            include 'transitiveDependency'
            include 'directDependency'
            include 'mainProject'
        """

        File transitiveDependencyBuildFile = newStandardProject('transitiveDependency')

        File directDependencyBuildFile = newStandardProject('directDependency')
        directDependencyBuildFile << """
            dependencies {
                compile project(':transitiveDependency')
            }
        """

        File mainProjectBuildFile = newStandardProject('mainProject')
        mainProjectBuildFile << """
            dependencies {
                compile project(':directDependency')
            }
        """

        when:
        def result = GradleRunner.create()
            .withProjectDir(testProjectDir.root)
            .withPluginClasspath()
            .withArguments(':mainProject:genFullArgsFile')
            .build()

        then:
        result.task(":transitiveDependency:genArgsFile").outcome == SUCCESS
        result.task(":directDependency:genArgsFile").outcome == SUCCESS
        result.task(":mainProject:genArgsFile").outcome == SUCCESS
        result.task(":mainProject:genFullArgsFile").outcome == SUCCESS
        new File(testProjectDir.root, 'mainProject/build/full_args.f').text.contains('transitiveDependency/build/args.f')
        new File(testProjectDir.root, 'mainProject/build/full_args.f').text.contains('directDependency/build/args.f')
        new File(testProjectDir.root, 'mainProject/build/full_args.f').text.contains('mainProject/build/args.f')
    }

    def "'argsFiles' are consumed in dependency order"() {
        setup:
        buildFile.delete()

        File settingsFile = testProjectDir.newFile('settings.gradle')
        settingsFile << """
            include 'transitiveDependency'
            include 'directDependency'
            include 'mainProject'
        """

        File transitiveDependencyBuildFile = newStandardProject('transitiveDependency')

        File directDependencyBuildFile = newStandardProject('directDependency')
        directDependencyBuildFile << """
            dependencies {
                compile project(':transitiveDependency')
            }
        """

        File mainProjectBuildFile = newStandardProject('mainProject')
        mainProjectBuildFile << """
            dependencies {
                compile project(':directDependency')
            }
        """

        when:
        def result = GradleRunner.create()
            .withProjectDir(testProjectDir.root)
            .withPluginClasspath()
            .withArguments(':mainProject:genFullArgsFile')
            .build()

        then:
        def lines = new File(testProjectDir.root, 'mainProject/build/full_args.f').text.split('\n')
        def transitiveDependencyIdx = lines.findIndexOf {
            it.contains('transitiveDependency/build/args.f')
        }
        transitiveDependencyIdx != -1
        def directDependencyIdx = lines.findIndexOf(transitiveDependencyIdx) {
            it.contains('directDependency/build/args.f')
        }
        directDependencyIdx != -1
        def mainProjectIdx = lines.findIndexOf(transitiveDependencyIdx) {
            it.contains('mainProject/build/args.f')
        }
        mainProjectIdx != -1
    }

    def "custom source set has own 'genArgsFile' task"() {
        buildFile << """
            sourceSets {
                dummy
            }
        """

        when:
        def result = GradleRunner.create()
            .withProjectDir(testProjectDir.root)
            .withPluginClasspath()
            .withArguments('tasks', '--all')
            .build()

        then:
        result.task(":tasks").outcome == SUCCESS
        result.output.contains('genDummyArgsFile')
    }

    def "'genArgsFileTask' for custom source set produces args file"() {
        File sv = testProjectDir.newFolder('src', 'dummy', 'sv')
        new File(sv, "dummy.sv").createNewFile()

        buildFile << """
            sourceSets {
                dummy {
                   sv {
                       srcDir 'src/dummy/sv'
                   }
                }
            }
        """

        when:
        def result = GradleRunner.create()
            .withProjectDir(testProjectDir.root)
            .withPluginClasspath()
            .withArguments('genDummyArgsFile')
            .build()

        then:
        result.task(":genDummyArgsFile").outcome == SUCCESS
        new File(testProjectDir.root, "build/dummy_args.f").exists()
        new File(testProjectDir.root, "build/dummy_args.f").text.contains('dummy.sv')
    }
}
