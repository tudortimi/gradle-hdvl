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

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import org.gradle.testkit.runner.GradleRunner
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Ignore
import spock.lang.Specification

import java.nio.file.Files
import java.util.zip.ZipFile

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

    def "'genXrunArgsFile' task produces output"() {
        File sv = testProjectDir.newFolder('src', 'main', 'sv')
        new File(sv, 'dummy.sv').createNewFile()

        when:
        def result = GradleRunner.create()
            .withProjectDir(testProjectDir.root)
            .withPluginClasspath()
            .withArguments('genXrunArgsFile')
            .build()

        then:
        result.task(":genXrunArgsFile").outcome == SUCCESS
        new File(testProjectDir.root, 'build/xrun_args.f').exists()
    }

    def "'genXrunArgsFile' task writes compile files to args file"() {
        File sv = testProjectDir.newFolder('src', 'main', 'sv')
        new File(sv, 'dummy.sv').createNewFile()

        when:
        def result = GradleRunner.create()
            .withProjectDir(testProjectDir.root)
            .withPluginClasspath()
            .withArguments('genXrunArgsFile')
            .build()

        then:
        new File(testProjectDir.root, 'build/xrun_args.f').text.contains('src/main/sv/dummy.sv')
    }

    def "'genXrunArgsFile' task writes private include directories to args file"() {
        File sv = testProjectDir.newFolder('src', 'main', 'sv')
        new File(sv, 'dummy.sv').createNewFile()

        when:
        def result = GradleRunner.create()
            .withProjectDir(testProjectDir.root)
            .withPluginClasspath()
            .withArguments('genXrunArgsFile')
            .build()

        then:
        def lines = new File(testProjectDir.root, 'build/xrun_args.f').text.split("\n")
        def lineWithIncdir = lines.find { it.contains('-incdir') }
        lineWithIncdir != null
        lineWithIncdir.endsWith("src/main/sv")
    }

    def "'genXrunArgsFile' task writes private include directories to args file after re-configure of source dirs"() {
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
            .withArguments('genXrunArgsFile')
            .build()

        then:
        def lines = new File(testProjectDir.root, 'build/xrun_args.f').text.split("\n")
        def lineWithIncdir = lines.find { it.contains('-incdir') }
        lineWithIncdir != null
        !lineWithIncdir.contains("src")
        !lineWithIncdir.contains("main")
        lineWithIncdir.endsWith("sv")
    }

    def "'genXrunArgsFile' task skips empty private include directories"() {
        File sv = testProjectDir.newFolder('sv')
        new File(sv, 'dummy.sv').createNewFile()

        buildFile << """
            sourceSets {
                main {
                    sv {
                        srcDirs 'sv'
                    }
                }
            }
        """

        // No `src/main/sv` directory, but it still is a `srcDir`

        when:
        def result = GradleRunner.create()
            .withProjectDir(testProjectDir.root)
            .withPluginClasspath()
            .withArguments('genXrunArgsFile')
            .build()

        then:
        def lines = new File(testProjectDir.root, 'build/xrun_args.f').text.split("\n")
        !lines.any { it.contains('src/main/sv') }
    }

    def "'genXrunArgsFile' task writes exported header directories to args file"() {
        File svHeaders = testProjectDir.newFolder('src', 'main', 'sv_headers')

        when:
        def result = GradleRunner.create()
            .withProjectDir(testProjectDir.root)
            .withPluginClasspath()
            .withArguments('genXrunArgsFile')
            .build()

        then:
        def lines = new File(testProjectDir.root, 'build/xrun_args.f').text.split("\n")
        def linesWithIncdir = lines.findAll { it.contains('-incdir') }
        !linesWithIncdir.isEmpty()
        linesWithIncdir.any { it.endsWith("src/main/sv_headers") }
    }

    def "'genXrunArgsFile' task doesn't write exported header directories to args file if none exist"() {
        File sv = testProjectDir.newFolder('src/main/sv')
        new File(sv, 'dummy.sv').createNewFile()

        // No 'src/main/sv_headers' directory

        when:
        def result = GradleRunner.create()
            .withProjectDir(testProjectDir.root)
            .withPluginClasspath()
            .withArguments('genXrunArgsFile')
            .build()

        then:
        def lines = new File(testProjectDir.root, 'build/xrun_args.f').text.split("\n")
        !lines.any { it.contains('src/main/sv_headers') }
    }

    def "'genXrunArgsFile' task indents entries in makelib block"() {
        File sv = testProjectDir.newFolder('src', 'main', 'sv')
        new File(sv, 'dummy.sv').createNewFile()

        when:
        def result = GradleRunner.create()
            .withProjectDir(testProjectDir.root)
            .withPluginClasspath()
            .withArguments('genXrunArgsFile')
            .build()

        then:
        def lines = new File(testProjectDir.root, 'build/xrun_args.f').text.split('\n')
        lines.findAll { !it.contains('-makelib') && !it.contains('-endlib') }.each {
            assert it.startsWith('  ')
        }
    }

    def "'genFullXrunArgsFile' task consumes output of 'genXrunArgsFile"() {
        File sv = testProjectDir.newFolder('src', 'main', 'sv')
        new File(sv, 'dummy.sv').createNewFile()

        when:
        def result = GradleRunner.create()
            .withProjectDir(testProjectDir.root)
            .withPluginClasspath()
            .withArguments('genFullXrunArgsFile')
            .build()

        then:
        result.task(":genXrunArgsFile").outcome == SUCCESS
        result.task(":genFullXrunArgsFile").outcome == SUCCESS
        new File(testProjectDir.root, 'build/full_xrun_args.f').exists()
        new File(testProjectDir.root, 'build/full_xrun_args.f').text.contains('build/xrun_args.f')
    }

    def "'argsFiles' artifacts produced by direct dependencies are consumed by main project in 'genFullXrunArgsFile'"() {
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
            .withArguments(':mainProject:genFullXrunArgsFile')
            .build()

        then:
        result.task(":directDependency:genXrunArgsFile").outcome == SUCCESS
        result.task(":mainProject:genXrunArgsFile").outcome == SUCCESS
        result.task(":mainProject:genFullXrunArgsFile").outcome == SUCCESS
        new File(testProjectDir.root, 'mainProject/build/full_xrun_args.f').text.contains('directDependency/build/xrun_args.f')
        new File(testProjectDir.root, 'mainProject/build/full_xrun_args.f').text.contains('mainProject/build/xrun_args.f')
    }

    def "'argsFiles' artifacts produced by transitive dependencies are consumed in 'genFullXrunArgsFile'"() {
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
            .withArguments(':mainProject:genFullXrunArgsFile')
            .build()

        then:
        result.task(":transitiveDependency:genXrunArgsFile").outcome == SUCCESS
        result.task(":directDependency:genXrunArgsFile").outcome == SUCCESS
        result.task(":mainProject:genXrunArgsFile").outcome == SUCCESS
        result.task(":mainProject:genFullXrunArgsFile").outcome == SUCCESS
        new File(testProjectDir.root, 'mainProject/build/full_xrun_args.f').text.contains('transitiveDependency/build/xrun_args.f')
        new File(testProjectDir.root, 'mainProject/build/full_xrun_args.f').text.contains('directDependency/build/xrun_args.f')
        new File(testProjectDir.root, 'mainProject/build/full_xrun_args.f').text.contains('mainProject/build/xrun_args.f')
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
            .withArguments(':mainProject:genFullXrunArgsFile')
            .build()

        then:
        def lines = new File(testProjectDir.root, 'mainProject/build/full_xrun_args.f').text.split('\n')
        def transitiveDependencyIdx = lines.findIndexOf {
            it.contains('transitiveDependency/build/xrun_args.f')
        }
        transitiveDependencyIdx != -1
        def directDependencyIdx = lines.findIndexOf(transitiveDependencyIdx) {
            it.contains('directDependency/build/xrun_args.f')
        }
        directDependencyIdx != -1
        def mainProjectIdx = lines.findIndexOf(transitiveDependencyIdx) {
            it.contains('mainProject/build/xrun_args.f')
        }
        mainProjectIdx != -1
    }

    def "custom source set has own 'genXrunArgsFile' task"() {
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
        result.output.contains('genDummyXrunArgsFile')
    }

    def "'genXrunArgsFile' task for custom source set produces args file"() {
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
            .withArguments('genDummyXrunArgsFile')
            .build()

        then:
        result.task(":genDummyXrunArgsFile").outcome == SUCCESS
        new File(testProjectDir.root, "build/dummy_xrun_args.f").exists()
        new File(testProjectDir.root, "build/dummy_xrun_args.f").text.contains('dummy.sv')
    }

    def "'genXrunArgsFile' tasks when custom source set present produce correct args file"() {
        File mainSv = testProjectDir.newFolder('src', 'main', 'sv')
        new File(mainSv, "main.sv").createNewFile()
        File dummySv = testProjectDir.newFolder('src', 'dummy', 'sv')
        new File(dummySv, "dummy.sv").createNewFile()

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
            .withArguments('genDummyXrunArgsFile', 'genXrunArgsFile')
            .build()

        then:
        new File(testProjectDir.root, "build/xrun_args.f").text.contains('main.sv')
        new File(testProjectDir.root, "build/dummy_xrun_args.f").text.contains('dummy.sv')
    }

    def "'genQrunArgsFile' task produces output"() {
        File sv = testProjectDir.newFolder('src', 'main', 'sv')
        new File(sv, 'dummy.sv').createNewFile()

        when:
        def result = GradleRunner.create()
            .withProjectDir(testProjectDir.root)
            .withPluginClasspath()
            .withArguments('genQrunArgsFile')
            .build()

        then:
        result.task(":genQrunArgsFile").outcome == SUCCESS
        new File(testProjectDir.root, 'build/qrun_args.f').exists()
    }

    def "'genQrunArgsFile' task uses 'work' as name for library"() {
        File sv = testProjectDir.newFolder('src', 'main', 'sv')
        new File(sv, 'dummy.sv').createNewFile()

        when:
        def result = GradleRunner.create()
            .withProjectDir(testProjectDir.root)
            .withPluginClasspath()
            .withArguments('genQrunArgsFile')
            .build()

        then:
        new File(testProjectDir.root, 'build/qrun_args.f').text.contains('-makelib work\n')
    }

    def "'genQrunArgsFile' task writes private include directories to args file"() {
        File sv = testProjectDir.newFolder('src', 'main', 'sv')
        new File(sv, 'dummy.sv').createNewFile()

        when:
        def result = GradleRunner.create()
            .withProjectDir(testProjectDir.root)
            .withPluginClasspath()
            .withArguments('genQrunArgsFile')
            .build()

        then:
        def lines = new File(testProjectDir.root, 'build/qrun_args.f').text.split("\n")
        def lineWithIncdir = lines.find { it.contains('+incdir+') }
        lineWithIncdir != null
        lineWithIncdir.endsWith("src/main/sv")
        !lineWithIncdir.any { it.contains('+ ') }
    }

    def "'genQrunArgsFile' task writes exported header directories to args file"() {
        File svHeaders = testProjectDir.newFolder('src', 'main', 'sv_headers')

        when:
        def result = GradleRunner.create()
            .withProjectDir(testProjectDir.root)
            .withPluginClasspath()
            .withArguments('genQrunArgsFile')
            .build()

        then:
        def lines = new File(testProjectDir.root, 'build/qrun_args.f').text.split("\n")
        def linesWithIncdir = lines.findAll { it.contains('+incdir+') }
        !linesWithIncdir.isEmpty()
        linesWithIncdir.any { it.endsWith("src/main/sv_headers") }
        !linesWithIncdir.any { it.contains('+ ') }
    }

    def "'genFullQrunArgsFile' task consumes output of 'genQrunArgsFile"() {
        File sv = testProjectDir.newFolder('src', 'main', 'sv')
        new File(sv, 'dummy.sv').createNewFile()

        when:
        def result = GradleRunner.create()
            .withProjectDir(testProjectDir.root)
            .withPluginClasspath()
            .withArguments('genFullQrunArgsFile')
            .build()

        then:
        result.task(":genQrunArgsFile").outcome == SUCCESS
        result.task(":genFullQrunArgsFile").outcome == SUCCESS
        new File(testProjectDir.root, 'build/full_qrun_args.f').exists()
        new File(testProjectDir.root, 'build/full_qrun_args.f').text.contains('build/qrun_args.f')
    }

    def "'argsFiles' artifacts produced by direct dependencies are consumed by main project in 'genFullQrunArgsFile'"() {
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
            .withArguments(':mainProject:genFullQrunArgsFile')
            .build()

        then:
        result.task(":directDependency:genQrunArgsFile").outcome == SUCCESS
        result.task(":mainProject:genQrunArgsFile").outcome == SUCCESS
        result.task(":mainProject:genFullQrunArgsFile").outcome == SUCCESS
        new File(testProjectDir.root, 'mainProject/build/full_qrun_args.f').text
                .contains('directDependency/build/qrun_args.f')
        new File(testProjectDir.root, 'mainProject/build/full_qrun_args.f').text
                .contains('mainProject/build/qrun_args.f')
    }

    def "only qrun 'argsFiles' artifacts are consumed by main project in 'genFullQrunArgsFile'"() {
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
            .withArguments(':mainProject:genFullQrunArgsFile')
            .build()

        then:
        result.task(":directDependency:genQrunArgsFile").outcome == SUCCESS
        result.task(":mainProject:genQrunArgsFile").outcome == SUCCESS
        result.task(":mainProject:genFullQrunArgsFile").outcome == SUCCESS
        new File(testProjectDir.root, 'mainProject/build/full_qrun_args.f').readLines().each { String line ->
            if (line.contains('args.f')) {
                assert line.contains('qrun_args.f')
            }
        }
    }

    def "can write sv source to compile spec file"() {
        File mainSv = testProjectDir.newFolder('src', 'main', 'sv')
        new File(mainSv, "main.sv").createNewFile()

        when:
        def result = GradleRunner.create()
            .withProjectDir(testProjectDir.root)
            .withPluginClasspath()
            .withArguments(':writeCompileSpecFile')
            .build()

        then:
        def compileSpecFile = new File(testProjectDir.root, 'build/compile-spec.json')
        compileSpecFile.exists()
        JsonNode compileSpec = new ObjectMapper().readTree(compileSpecFile)
        JsonNode svSourceFiles = compileSpec.get("svSourceFiles")
        svSourceFiles.isArray()
        svSourceFiles.size() == 1
        svSourceFiles.get(0).asText() == 'src/main/sv/main.sv'
    }

    def "archive with source file contains compile spec"() {
        File mainSv = testProjectDir.newFolder('src', 'main', 'sv')
        new File(mainSv, "main.sv").createNewFile()

        when:
        def result = GradleRunner.create()
            .withProjectDir(testProjectDir.root)
            .withPluginClasspath()
            .withArguments(':hdvlSourcesArchive')
            .build()

        then:
        new File(testProjectDir.root, 'build/hdvl-sources.zip').exists()
        def zipFile = new ZipFile(new File(testProjectDir.root, 'build/hdvl-sources.zip'))
        def entries = zipFile.entries().findAll { !it.directory }
        entries.size() == 2
        entries[0].name == '.gradle-hdvl/compile-spec.json'
    }

    def "can produce archive with source file"() {
        File mainSv = testProjectDir.newFolder('src', 'main', 'sv')
        new File(mainSv, "main.sv").createNewFile()

        when:
        def result = GradleRunner.create()
            .withProjectDir(testProjectDir.root)
            .withPluginClasspath()
            .withArguments(':hdvlSourcesArchive')
            .build()

        then:
        new File(testProjectDir.root, 'build/hdvl-sources.zip').exists()
        def zipFile = new ZipFile(new File(testProjectDir.root, 'build/hdvl-sources.zip'))
        def entries = zipFile.entries().findAll { !it.directory }
        entries.size() == 2
        entries[1].name == 'src/main/sv/main.sv'
    }

    def "can produce archive with source file in custom location"() {
        File mainSv = testProjectDir.newFolder('sv')
        new File(mainSv, "main.sv").createNewFile()

        buildFile << """
        sourceSets.main.sv.srcDirs = ['sv']
        """

        when:
        def result = GradleRunner.create()
            .withProjectDir(testProjectDir.root)
            .withPluginClasspath()
            .withArguments(':hdvlSourcesArchive')
            .build()

        then:
        new File(testProjectDir.root, 'build/hdvl-sources.zip').exists()
        def zipFile = new ZipFile(new File(testProjectDir.root, 'build/hdvl-sources.zip'))
        def entries = zipFile.entries().findAll { !it.directory }
        entries.size() == 2
        entries[1].name == 'sv/main.sv'
    }

    def "can produce archive with private header"() {
        File mainSv = testProjectDir.newFolder('src', 'main', 'sv')
        new File(mainSv, "main.sv").createNewFile()
        new File(mainSv, "private_header.svh").createNewFile()

        when:
        def result = GradleRunner.create()
            .withProjectDir(testProjectDir.root)
            .withPluginClasspath()
            .withArguments(':hdvlSourcesArchive')
            .build()

        then:
        new File(testProjectDir.root, 'build/hdvl-sources.zip').exists()
        def zipFile = new ZipFile(new File(testProjectDir.root, 'build/hdvl-sources.zip'))
        def entries = zipFile.entries().findAll { !it.directory }
        entries.size() == 3
        entries[1].name == 'src/main/sv/main.sv'
        entries[2].name == 'src/main/sv/private_header.svh'
    }

    def "can produce archive with private header in custom location"() {
        File mainSv = testProjectDir.newFolder('sv')
        new File(mainSv, "main.sv").createNewFile()
        new File(mainSv, "private_header.svh").createNewFile()

        buildFile << """
        sourceSets.main.sv.srcDirs = ['sv']
        """

        when:
        def result = GradleRunner.create()
            .withProjectDir(testProjectDir.root)
            .withPluginClasspath()
            .withArguments(':hdvlSourcesArchive')
            .build()

        then:
        new File(testProjectDir.root, 'build/hdvl-sources.zip').exists()
        def zipFile = new ZipFile(new File(testProjectDir.root, 'build/hdvl-sources.zip'))
        def entries = zipFile.entries().findAll { !it.directory }
        entries.size() == 3
        entries[1].name == 'sv/main.sv'
        entries[2].name == 'sv/private_header.svh'
    }

    def "can produce archive with exported header"() {
        File mainSvHeaders = testProjectDir.newFolder('src', 'main', 'sv_headers')
        new File(mainSvHeaders, "exported_header.svh").createNewFile()

        when:
        def result = GradleRunner.create()
            .withProjectDir(testProjectDir.root)
            .withPluginClasspath()
            .withArguments(':hdvlSourcesArchive')
            .build()

        then:
        new File(testProjectDir.root, 'build/hdvl-sources.zip').exists()
        def zipFile = new ZipFile(new File(testProjectDir.root, 'build/hdvl-sources.zip'))
        def entries = zipFile.entries().findAll { !it.directory }
        entries.size() == 2
        entries[1].name == 'src/main/sv_headers/exported_header.svh'
    }

    def "can produce archive with exported header in custom location"() {
        File mainSvHeaders = testProjectDir.newFolder('sv')
        new File(mainSvHeaders, "exported_header.svh").createNewFile()

        buildFile << """
        sourceSets.main.svHeaders.srcDirs = ['sv']
        """

        when:
        def result = GradleRunner.create()
            .withProjectDir(testProjectDir.root)
            .withPluginClasspath()
            .withArguments(':hdvlSourcesArchive')
            .build()

        then:
        new File(testProjectDir.root, 'build/hdvl-sources.zip').exists()
        def zipFile = new ZipFile(new File(testProjectDir.root, 'build/hdvl-sources.zip'))
        def entries = zipFile.entries().findAll { !it.directory }
        entries.size() == 2
        entries[1].name == 'sv/exported_header.svh'
    }

    def "can publishing metadata for archive"() {
        File mainSv = testProjectDir.newFolder('src', 'main', 'sv')
        new File(mainSv, "main.sv").createNewFile()

        buildFile.text = """
            plugins {
                id("com.verificationgentleman.gradle.hdvl.systemverilog")
                id("maven-publish")
            }

            group = "org.example"
            version = "1.0.0"
        """

        when:
        def result = GradleRunner.create()
            .withProjectDir(testProjectDir.root)
            .withPluginClasspath()
            .withArguments(':generateMetadataFileForHdvlLibraryPublication')
            .build()

        then:
        def gradleMetadata = new File(testProjectDir.root, "build/publications/hdvlLibrary/module.json")
        gradleMetadata.exists()
        JsonNode metadata = new ObjectMapper().readTree(gradleMetadata);

        JsonNode component = metadata.get("component")
        component.get("group").asText() == "org.example"
        component.get("module").asText() == testProjectDir.root.name
        component.get("version").asText() == "1.0.0"

        JsonNode variants = metadata.get("variants")
        variants.size() == 1
        variants[0].get("name").asText() == "hdvlSourcesArchiveElements"
        variants[0].get("attributes").size() == 1
        variants[0].get("attributes").has("com.verificationgentlenan.gradle.hdvl.usage")
        variants[0].get("attributes").get("com.verificationgentlenan.gradle.hdvl.usage").asText() == "HdvlSourcesArchive"
    }

    def "can consume source archive"() {
        File dependencyProjectBuildFile = newStandardProject('dependency-project')
        dependencyProjectBuildFile << """
            plugins {
                id 'maven-publish'
            }

            group = "org.example"
            version = "1.0.0"

            publishing {
                repositories {
                    maven {
                        name = 'dummy'
                        url = layout.buildDirectory.dir('dummy-repo')
                    }
                }
            }
        """

        GradleRunner.create()
                .withProjectDir(dependencyProjectBuildFile.parentFile)
                .withPluginClasspath()
                .withArguments(':publish')
                .build()

        File mainProjectBuildFile = newStandardProject('main-project')
        mainProjectBuildFile << """
            dependencies {
                compile 'org.example:dependency-project:1.0.0'
            }

            repositories {
                maven {
                    url = layout.projectDirectory.dir('../dependency-project/build/dummy-repo')
                }
            }
        """

        when:
        def result = GradleRunner.create()
                .withProjectDir(mainProjectBuildFile.parentFile)
                .withPluginClasspath()
                .withDebug(true)
                .withArguments(':genFullXrunArgsFile')
                .build()

        then:
        def lines = new File(mainProjectBuildFile.parentFile, 'build/full_xrun_args.f').text.split("\n")
        def xrunArgsForDependencyProject = new File(lines[0].split(/\s+/)[1])
        Files.lines(xrunArgsForDependencyProject.toPath()).anyMatch { line ->
            line.endsWith 'src/main/sv/dependency-project.sv'
        }
    }

    def "can consume source archive with private include directory"() {
        File dependencyProjectBuildFile = newStandardProject('dependency-project')
        dependencyProjectBuildFile << """
            plugins {
                id 'maven-publish'
            }

            group = "org.example"
            version = "1.0.0"

            publishing {
                repositories {
                    maven {
                        name = 'dummy'
                        url = layout.buildDirectory.dir('dummy-repo')
                    }
                }
            }
        """

        File dependencyProjectPrivateHeader = new File(dependencyProjectBuildFile.parentFile, 'src/main/sv/dependency-project-private-header.svh')
        dependencyProjectPrivateHeader.text = "dummy"

        GradleRunner.create()
            .withProjectDir(dependencyProjectBuildFile.parentFile)
            .withPluginClasspath()
            .withArguments(':publish')
            .build()

        File mainProjectBuildFile = newStandardProject('main-project')
        mainProjectBuildFile << """
            dependencies {
                compile 'org.example:dependency-project:1.0.0'
            }

            repositories {
                maven {
                    url = layout.projectDirectory.dir('../dependency-project/build/dummy-repo')
                }
            }
        """

        when:
        def result = GradleRunner.create()
            .withProjectDir(mainProjectBuildFile.parentFile)
            .withPluginClasspath()
            .withDebug(true)
            .withArguments(':genFullXrunArgsFile')
            .build()

        then:
        def lines = new File(mainProjectBuildFile.parentFile, 'build/full_xrun_args.f').text.split("\n")
        def xrunArgsForDependencyProject = new File(lines[0].split(/\s+/)[1])
        Files.lines(xrunArgsForDependencyProject.toPath()).anyMatch { line ->
            line.contains('-incdir') && line.endsWith('src/main/sv')
        }
    }

    def "can consume source archive with exported header directory"() {
        File dependencyProjectBuildFile = newStandardProject('dependency-project')
        dependencyProjectBuildFile << """
            plugins {
                id 'maven-publish'
            }

            group = "org.example"
            version = "1.0.0"

            publishing {
                repositories {
                    maven {
                        name = 'dummy'
                        url = layout.buildDirectory.dir('dummy-repo')
                    }
                }
            }
        """

        File dependencyProjectExportedHeaderDir = new File(dependencyProjectBuildFile.parentFile, 'src/main/sv_headers')
        dependencyProjectExportedHeaderDir.mkdir()
        File dependencyProjectPrivateHeader = new File(dependencyProjectExportedHeaderDir, 'dependency-project-exported-header.svh')
        dependencyProjectPrivateHeader.text = "dummy"

        GradleRunner.create()
            .withProjectDir(dependencyProjectBuildFile.parentFile)
            .withPluginClasspath()
            .withArguments(':publish')
            .build()

        File mainProjectBuildFile = newStandardProject('main-project')
        mainProjectBuildFile << """
            dependencies {
                compile 'org.example:dependency-project:1.0.0'
            }

            repositories {
                maven {
                    url = layout.projectDirectory.dir('../dependency-project/build/dummy-repo')
                }
            }
        """

        when:
        def result = GradleRunner.create()
            .withProjectDir(mainProjectBuildFile.parentFile)
            .withPluginClasspath()
            .withDebug(true)
            .withArguments(':genFullXrunArgsFile')
            .build()

        then:
        def lines = new File(mainProjectBuildFile.parentFile, 'build/full_xrun_args.f').text.split("\n")
        def xrunArgsForDependencyProject = new File(lines[0].split(/\s+/)[1])
        Files.lines(xrunArgsForDependencyProject.toPath()).anyMatch { line ->
            line.contains('-incdir') && line.endsWith('src/main/sv_headers')
        }
    }

    def "can consume source archive with only exported header directory"() {
        File dependencyProjectBuildFile = newStandardProject('dependency-project')
        dependencyProjectBuildFile << """
            plugins {
                id 'maven-publish'
            }

            group = "org.example"
            version = "1.0.0"

            publishing {
                repositories {
                    maven {
                        name = 'dummy'
                        url = layout.buildDirectory.dir('dummy-repo')
                    }
                }
            }
        """

        File dependencyProjectSvDir = new File(dependencyProjectBuildFile.parentFile, 'src/main/sv')
        dependencyProjectSvDir.deleteDir()

        File dependencyProjectExportedHeaderDir = new File(dependencyProjectBuildFile.parentFile, 'src/main/sv_headers')
        dependencyProjectExportedHeaderDir.mkdir()
        File dependencyProjectPrivateHeader = new File(dependencyProjectExportedHeaderDir, 'dependency-project-exported-header.svh')
        dependencyProjectPrivateHeader.text = "dummy"

        GradleRunner.create()
            .withProjectDir(dependencyProjectBuildFile.parentFile)
            .withPluginClasspath()
            .withArguments(':publish')
            .build()

        File mainProjectBuildFile = newStandardProject('main-project')
        mainProjectBuildFile << """
            dependencies {
                compile 'org.example:dependency-project:1.0.0'
            }

            repositories {
                maven {
                    url = layout.projectDirectory.dir('../dependency-project/build/dummy-repo')
                }
            }
        """

        when:
        def result = GradleRunner.create()
            .withProjectDir(mainProjectBuildFile.parentFile)
            .withPluginClasspath()
            .withDebug(true)
            .withArguments(':genFullXrunArgsFile')
            .build()

        then:
        def lines = new File(mainProjectBuildFile.parentFile, 'build/full_xrun_args.f').text.split("\n")
        def xrunArgsForDependencyProject = new File(lines[0].split(/\s+/)[1])
        Files.lines(xrunArgsForDependencyProject.toPath()).anyMatch { line ->
            line.contains('-incdir') && line.endsWith('src/main/sv_headers')
        }
    }

    def "can publish dependencies for consumed archives"() {
        File dependencyProjectBuildFile = newStandardProject('dependency-project')
        dependencyProjectBuildFile << """
            plugins {
                id 'maven-publish'
            }

            group = "org.example"
            version = "1.0.0"

            publishing {
                repositories {
                    maven {
                        name = 'dummy'
                        url = layout.buildDirectory.dir('dummy-repo')
                    }
                }
            }
        """

        GradleRunner.create()
            .withProjectDir(dependencyProjectBuildFile.parentFile)
            .withPluginClasspath()
            .withArguments(':publish')
            .build()

        File dependentProjectBuildFile = newStandardProject('dependent-project')
        dependentProjectBuildFile << """
            plugins {
                id 'maven-publish'
            }

            group = "org.example"
            version = "1.0.0"

            dependencies {
                compile 'org.example:dependency-project:1.0.0'
            }

            repositories {
                maven {
                    url = layout.projectDirectory.dir('../dependency-project/build/dummy-repo')
                }
            }
        """

        when:
        def result = GradleRunner.create()
            .withProjectDir(dependentProjectBuildFile.parentFile)
            .withPluginClasspath()
            .withArguments(':generateMetadataFileForHdvlLibraryPublication')
            .build()

        then:
        def gradleMetadata = new File(dependentProjectBuildFile.parentFile, "build/publications/hdvlLibrary/module.json")
        gradleMetadata.exists()
        JsonNode metadata = new ObjectMapper().readTree(gradleMetadata)
        def dependencies = metadata.get("variants")[0].get("dependencies")
        dependencies != null
        dependencies.size() == 1
    }
}
