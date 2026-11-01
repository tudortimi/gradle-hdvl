/*
 * Copyright 2021-2026 the original author or authors.
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

package com.verificationgentleman.gradle.hdvl.c

import com.verificationgentleman.gradle.hdvl.FunctionalTestRunner
import com.verificationgentleman.gradle.hdvl.TestProjectDir
import spock.lang.Specification
import spock.lang.TempDir

import java.nio.file.Files
import java.util.zip.ZipFile

import static org.gradle.testkit.runner.TaskOutcome.NO_SOURCE
import static org.gradle.testkit.runner.TaskOutcome.SUCCESS

class CPluginSpec extends Specification {
    @TempDir File testProjectDir
    File buildFile

    def setup() {
        buildFile = TestProjectDir.newFile(testProjectDir, 'build.gradle')
        buildFile << """
            plugins {
                id 'com.verificationgentleman.gradle.hdvl.c'
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
        File folder = TestProjectDir.newFolder(testProjectDir, name)

        File sv = TestProjectDir.newFolder(testProjectDir, name,'src', 'main', 'c')
        new File(sv, "${name}.c").createNewFile()

        File buildFile = new File(folder, "build.gradle")
        buildFile << """
            plugins {
                id 'com.verificationgentleman.gradle.hdvl.c'
            }
        """

        return buildFile
    }

    def "can successfully import the plugin"() {
        when:
        def result = FunctionalTestRunner.create()
                .withProjectDir(testProjectDir)
                .build()

        then:
        result.task(":help").outcome == SUCCESS
    }

    def "can specify a source set C source directory using a closure"() {
        File sv = TestProjectDir.newFolder(testProjectDir, 'c')
        new File(sv, 'dummy.c').createNewFile()

        buildFile << """
            sourceSets {
                main {
                    c {
                        srcDirs = ['c']
                    }
                }
            }

            task copy(type: Copy) {
                from sourceSets.main.c.files
                into 'build'
            }
        """

        when:
        def result = FunctionalTestRunner.create()
            .withProjectDir(testProjectDir)
            .withArguments('copy')
            .build()

        then:
        result.task(":copy").outcome == SUCCESS
        new File(testProjectDir, 'build/dummy.c').exists()
    }

    def "can specify a source set C source exclude using an action"() {
        // XXX Most tests use 'build.gradle', but in this test we want to use a Kotlin build script. It seems like
        // overkill to create a new test class just fo this.
        setup:
        new File(testProjectDir, 'build.gradle').delete()

        File sv = TestProjectDir.newFolder(testProjectDir, 'src', 'main', 'c')
        new File(sv, 'dummy.c').createNewFile()

        File buildFile = TestProjectDir.newFile(testProjectDir, 'build.gradle.kts')
        buildFile << """
            import com.verificationgentleman.gradle.hdvl.c.c

            plugins {
                id("com.verificationgentleman.gradle.hdvl.c")
            }

            sourceSets {
                main {
                    c.exclude("**/dummy.c")
                }
            }

            tasks.register<Copy>("copy") {
                from(sourceSets.main.get().c)
                include("*")
                into("build")
            }
        """

        when:
        def result = FunctionalTestRunner.create()
            .withProjectDir(testProjectDir)
            .withArguments('copy')
            .build()

        then:
        result.task(":copy").outcome == NO_SOURCE
    }

    def "'genXrunArgsFile' task writes C files to args file"() {
        File c = TestProjectDir.newFolder(testProjectDir, 'src', 'main', 'c')
        new File(c, 'dummy.c').createNewFile()

        when:
        def result = FunctionalTestRunner.create()
            .withProjectDir(testProjectDir)
            .withArguments('genXrunArgsFile')
            .build()

        then:
        new File(testProjectDir, 'build/xrun_args.f').text.contains('src/main/c/dummy.c')
    }

    def "'genQrunArgsFile' task writes C files to args file"() {
        File c = TestProjectDir.newFolder(testProjectDir, 'src', 'main', 'c')
        new File(c, 'dummy.c').createNewFile()

        when:
        def result = FunctionalTestRunner.create()
            .withProjectDir(testProjectDir)
            .withArguments('genQrunArgsFile')
            .build()

        then:
        new File(testProjectDir, 'build/qrun_args.f').text.contains('src/main/c/dummy.c')
    }

    def "'genXrunArgsFile' task indents entries in makelib block"() {
        File c = TestProjectDir.newFolder(testProjectDir, 'src', 'main', 'c')
        new File(c, 'dummy.c').createNewFile()

        when:
        def result = FunctionalTestRunner.create()
            .withProjectDir(testProjectDir)
            .withArguments('genXrunArgsFile')
            .build()

        then:
        def lines = new File(testProjectDir, 'build/xrun_args.f').text.split('\n')
        lines.findAll { !it.contains('-makelib') && !it.contains('-endlib') }.each {
            assert it.startsWith('  ')
        }
    }

    def "'genXrunArgsFile' task for custom source set produces args file"() {
        File sv = TestProjectDir.newFolder(testProjectDir, 'src', 'dummy', 'c')
        new File(sv, "dummy.c").createNewFile()

        buildFile << """
            sourceSets {
                dummy {
                   c {
                       srcDir 'src/dummy/c'
                   }
                }
            }
        """

        when:
        def result = FunctionalTestRunner.create()
            .withProjectDir(testProjectDir)
            .withArguments('genDummyXrunArgsFile')
            .build()

        then:
        result.task(":genDummyXrunArgsFile").outcome == SUCCESS
        new File(testProjectDir, "build/dummy_xrun_args.f").exists()
        new File(testProjectDir, "build/dummy_xrun_args.f").text.contains('dummy.c')
    }

    def "can produce archive with source file"() {
        File mainSv = TestProjectDir.newFolder(testProjectDir, 'src', 'main', 'c')
        new File(mainSv, "main.c").createNewFile()

        when:
        def result = FunctionalTestRunner.create()
            .withProjectDir(testProjectDir)
            .withArguments(':hdvlSourcesArchive')
            .build()

        then:
        new File(testProjectDir, 'build/hdvl-sources.zip').exists()
        def zipFile = new ZipFile(new File(testProjectDir, 'build/hdvl-sources.zip'))
        def entries = zipFile.entries().findAll { !it.directory }
        entries.size() == 2
        entries[1].name == 'src/main/c/main.c'
    }

    def "can produce archive with source file in custom location"() {
        File mainSv = TestProjectDir.newFolder(testProjectDir, 'c')
        new File(mainSv, "main.c").createNewFile()

        buildFile << """
        sourceSets.main.c.srcDirs = ['c']
        """

        when:
        def result = FunctionalTestRunner.create()
            .withProjectDir(testProjectDir)
            .withArguments(':hdvlSourcesArchive')
            .build()

        then:
        new File(testProjectDir, 'build/hdvl-sources.zip').exists()
        def zipFile = new ZipFile(new File(testProjectDir, 'build/hdvl-sources.zip'))
        def entries = zipFile.entries().findAll { !it.directory }
        entries.size() == 2
        entries[1].name == 'c/main.c'
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

        FunctionalTestRunner.create()
            .withProjectDir(dependencyProjectBuildFile.parentFile)
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
        def result = FunctionalTestRunner.create()
            .withProjectDir(mainProjectBuildFile.parentFile)
            .withDebug(true)
            .withArguments(':genFullXrunArgsFile')
            .build()

        then:
        def lines = new File(mainProjectBuildFile.parentFile, 'build/full_xrun_args.f').text.split("\n")
        def xrunArgsForDependencyProject = new File(lines[0].split(/\s+/)[1])
        Files.lines(xrunArgsForDependencyProject.toPath()).anyMatch { line ->
            line.endsWith 'src/main/c/dependency-project.c'
        }
    }
}
