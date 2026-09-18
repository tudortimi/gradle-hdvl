/*
 * Copyright 2021 the original author or authors.
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

import org.gradle.testkit.runner.GradleRunner
import spock.lang.Ignore
import spock.lang.Specification
import spock.lang.TempDir

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

import java.nio.file.Files
import java.util.zip.ZipFile

import static org.gradle.testkit.runner.TaskOutcome.NO_SOURCE
import static org.gradle.testkit.runner.TaskOutcome.SUCCESS

class CPluginSpec extends Specification {
    @TempDir Path testProjectDir
    File buildFile

    def setup() {
        buildFile = Files.createFile(testProjectDir.resolve('build.gradle')).toFile()
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
        File folder = testProjectDir.newFolder(name)

        File sv = testProjectDir.newFolder(name,'src', 'main', 'c')
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
        def result = GradleRunner.create()
                .withProjectDir(testProjectDir.toFile())
                .withPluginClasspath()
                .build()

        then:
        result.task(":help").outcome == SUCCESS
    }

    def "can specify a source set C source directory using a closure"() {
        Path c = Files.createDirectories(testProjectDir.resolve('c'))
        Files.createFile(c.resolve('dummy.c'))

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
        def result = GradleRunner.create()
            .withProjectDir(testProjectDir.toFile())
            .withPluginClasspath()
            .withArguments('copy')
            .build()

        then:
        result.task(":copy").outcome == SUCCESS
        Files.exists(testProjectDir.resolve('build').resolve('dummy.c'))
    }

    @Ignore("Complains that the source set doesn't support conventions")
    def "can specify a source set C source exclude using an action"() {
        // XXX Most tests use 'build.gradle', but in this test we want to use a Kotlin build script. It seems like
        // overkill to create a new test class just fo this.
        setup:
        new File(testProjectDir, 'build.gradle').delete()

        Path c = Files.createDirectories(testProjectdir.resolve('src').resolve('c'))
        Files.createFile(c.resolve('dummy.c'))

        File buildFile = new File(testProjectDir, 'build.gradle.kts')
        buildFile << """
            import com.verificationgentleman.gradle.hdvl.c.CSourceSet

            plugins {
                id("com.verificationgentleman.gradle.hdvl.c")
            }

            sourceSets {
                main {
                    withConvention(CSourceSet::class) {
                        c.exclude("**/dummy.c")
                    }
                }
            }

            tasks.register<Copy>("copy") {
                // XXX Not clear why we can't just do 'sourceSets.main.sv'.
                // 'sourceSets.main' doesn't return an object of type 'SourceSet', but a
                // 'NamedDomainObjectProvider<SourceSet'. The Java plugin has the same issue.
                from(sourceSets.main.withConvention(CSourceSet::class) { c })
                include("*")
                into("build")
            }
        """

        when:
        def result = GradleRunner.create()
            .withProjectDir(testProjectDir)
            .withPluginClasspath()
            .withArguments('copy')
            .build()

        then:
        result.task(":copy").outcome == NO_SOURCE
    }

    def "'genXrunArgsFile' task writes C files to args file"() {
        Path c = Files.createDirectories(testProjectDir.resolve('src').resolve('main').resolve('c'))
        Files.createFile(c.resolve('dummy.c'))

        when:
        def result = GradleRunner.create()
            .withProjectDir(testProjectDir.toFile())
            .withPluginClasspath()
            .withArguments('genXrunArgsFile')
            .build()

        then:
        testProjectDir.resolve('build').resolve('xrun_args.f').toFile().text.contains('src/main/c/dummy.c')
    }

    def "'genQrunArgsFile' task writes C files to args file"() {
        Path c = Files.createDirectories(testProjectDir.resolve('src').resolve('main').resolve('c'))
        Files.createFile(c.resolve('dummy.c'))

        when:
        def result = GradleRunner.create()
            .withProjectDir(testProjectDir.toFile())
            .withPluginClasspath()
            .withArguments('genQrunArgsFile')
            .build()

        then:
        testProjectDir.resolve('build').resolve('qrun_args.f').toFile().text.contains('src/main/c/dummy.c')
    }

    def "'genXrunArgsFile' task indents entries in makelib block"() {
        Path c = Files.createDirectories(testProjectDir.resolve('src').resolve('main').resolve('c'))
        Files.createFile(c.resolve('dummy.c'))

        when:
        def result = GradleRunner.create()
            .withProjectDir(testProjectDir.toFile())
            .withPluginClasspath()
            .withArguments('genXrunArgsFile')
            .build()

        then:
        def lines = testProjectDir.resolve('build').resolve('xrun_args.f').toFile().text.split('\n')
        lines.findAll { !it.contains('-makelib') && !it.contains('-endlib') }.each {
            assert it.startsWith('  ')
        }
    }

    def "'genXrunArgsFile' task for custom source set produces args file"() {
        Path c = Files.createDirectories(testProjectDir.resolve('src').resolve('dummy').resolve('c'))
        Files.createFile(c.resolve('dummy.c'))

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
        def result = GradleRunner.create()
            .withProjectDir(testProjectDir.toFile())
            .withPluginClasspath()
            .withArguments('genDummyXrunArgsFile')
            .build()

        then:
        result.task(":genDummyXrunArgsFile").outcome == SUCCESS
        testProjectDir.resolve('build').resolve('dummy_xrun_args.f').toFile().exists()
        testProjectDir.resolve('build').resolve('dummy_xrun_args.f').toFile().text.contains('dummy.c')
    }

    def "can produce archive with source file"() {
        File mainSv = testProjectDir.newFolder('src', 'main', 'c')
        new File(mainSv, "main.c").createNewFile()

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
        entries[1].name == 'src/main/c/main.c'
    }

    def "can produce archive with source file in custom location"() {
        File mainSv = testProjectDir.newFolder('c')
        new File(mainSv, "main.c").createNewFile()

        buildFile << """
        sourceSets.main.c.srcDirs = ['c']
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
            line.endsWith 'src/main/c/dependency-project.c'
        }
    }
}
