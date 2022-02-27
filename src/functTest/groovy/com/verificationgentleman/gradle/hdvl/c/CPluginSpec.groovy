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
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Ignore
import spock.lang.Specification

import static org.gradle.testkit.runner.TaskOutcome.NO_SOURCE
import static org.gradle.testkit.runner.TaskOutcome.SUCCESS

class CPluginSpec extends Specification {
    @Rule TemporaryFolder testProjectDir = new TemporaryFolder()
    File buildFile

    def setup() {
        buildFile = testProjectDir.newFile('build.gradle')
        buildFile << """
            plugins {
                id 'com.verificationgentleman.gradle.hdvl.c'
            }
        """
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

    def "can specify a source set C source directory using a closure"() {
        File sv = testProjectDir.newFolder('c')
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
        def result = GradleRunner.create()
            .withProjectDir(testProjectDir.root)
            .withPluginClasspath()
            .withArguments('copy')
            .build()

        then:
        result.task(":copy").outcome == SUCCESS
        new File(testProjectDir.root, 'build/dummy.c').exists()
    }

    @Ignore("Complains that the source set doesn't support conventions")
    def "can specify a source set C source exclude using an action"() {
        // XXX Most tests use 'build.gradle', but in this test we want to use a Kotlin build script. It seems like
        // overkill to create a new test class just fo this.
        setup:
        new File(testProjectDir.root, 'build.gradle').delete()

        File sv = testProjectDir.newFolder('src', 'main', 'c')
        new File(sv, 'dummy.c').createNewFile()

        File buildFile = testProjectDir.newFile('build.gradle.kts')
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
            .withProjectDir(testProjectDir.root)
            .withPluginClasspath()
            .withArguments('copy')
            .build()

        then:
        result.task(":copy").outcome == NO_SOURCE
    }

    def "'genXrunArgsFile' task writes C files to args file"() {
        File c = testProjectDir.newFolder('src', 'main', 'c')
        new File(c, 'dummy.c').createNewFile()

        when:
        def result = GradleRunner.create()
            .withProjectDir(testProjectDir.root)
            .withPluginClasspath()
            .withArguments('genXrunArgsFile')
            .build()

        then:
        new File(testProjectDir.root, 'build/args.f').text.contains('src/main/c/dummy.c')
    }

    def "'genXrunArgsFile' task indents entries in makelib block"() {
        File c = testProjectDir.newFolder('src', 'main', 'c')
        new File(c, 'dummy.c').createNewFile()

        when:
        def result = GradleRunner.create()
            .withProjectDir(testProjectDir.root)
            .withPluginClasspath()
            .withArguments('genXrunArgsFile')
            .build()

        then:
        def lines = new File(testProjectDir.root, 'build/args.f').text.split('\n')
        lines.findAll { !it.contains('-makelib') && !it.contains('-endlib') }.each {
            assert it.startsWith('  ')
        }
    }

    def "'genXrunArgsFile' task for custom source set produces args file"() {
        File sv = testProjectDir.newFolder('src', 'dummy', 'c')
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
        def result = GradleRunner.create()
            .withProjectDir(testProjectDir.root)
            .withPluginClasspath()
            .withArguments('genDummyXrunArgsFile')
            .build()

        then:
        result.task(":genDummyXrunArgsFile").outcome == SUCCESS
        new File(testProjectDir.root, "build/dummy_args.f").exists()
        new File(testProjectDir.root, "build/dummy_args.f").text.contains('dummy.c')
    }
}
