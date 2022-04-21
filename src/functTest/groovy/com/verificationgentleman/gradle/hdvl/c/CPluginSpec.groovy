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

import static org.gradle.testkit.runner.TaskOutcome.NO_SOURCE
import static org.gradle.testkit.runner.TaskOutcome.SUCCESS

class CPluginSpec extends Specification {
    @TempDir File testProjectDir
    File buildFile

    def setup() {
        buildFile = new File(testProjectDir,'build.gradle')
        buildFile << """
            plugins {
                id 'com.verificationgentleman.gradle.hdvl.c'
            }
        """
    }

    def "can successfully import the plugin"() {
        when:
        def result = GradleRunner.create()
                .withProjectDir(testProjectDir)
                .withPluginClasspath()
                .build()

        then:
        result.task(":help").outcome == SUCCESS
    }

    def "can specify a source set C source directory using a closure"() {
        File c = new File([testProjectDir, 'c'].join(File.separator))
        c.mkdirs()
        new File(c, 'dummy.c').createNewFile()

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
            .withProjectDir(testProjectDir)
            .withPluginClasspath()
            .withArguments('copy')
            .build()

        then:
        result.task(":copy").outcome == SUCCESS
        new File(testProjectDir, 'build/dummy.c').exists()
    }

    @Ignore("Complains that the source set doesn't support conventions")
    def "can specify a source set C source exclude using an action"() {
        // XXX Most tests use 'build.gradle', but in this test we want to use a Kotlin build script. It seems like
        // overkill to create a new test class just fo this.
        setup:
        new File(testProjectDir, 'build.gradle').delete()

        File c = new File([testProjectDir, 'src', 'main', 'c'].join(File.separator))
        c.mkdirs()
        new File(c, 'dummy.c').createNewFile()

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
        File c = new File([testProjectDir, 'src', 'main', 'c'].join(File.separator))
        c.mkdirs()
        new File(c, 'dummy.c').createNewFile()

        when:
        def result = GradleRunner.create()
            .withProjectDir(testProjectDir)
            .withPluginClasspath()
            .withArguments('genXrunArgsFile')
            .build()

        then:
        new File(testProjectDir, 'build/xrun_args.f').text.contains('src/main/c/dummy.c')
    }

    def "'genQrunArgsFile' task writes C files to args file"() {
        File c = new File([testProjectDir, 'src', 'main', 'c'].join(File.separator))
        c.mkdirs()
        new File(c, 'dummy.c').createNewFile()

        when:
        def result = GradleRunner.create()
            .withProjectDir(testProjectDir)
            .withPluginClasspath()
            .withArguments('genQrunArgsFile')
            .build()

        then:
        new File(testProjectDir, 'build/qrun_args.f').text.contains('src/main/c/dummy.c')
    }

    def "'genXrunArgsFile' task indents entries in makelib block"() {
        File c = new File([testProjectDir, 'src', 'main', 'c'].join(File.separator))
        c.mkdirs()
        new File(c, 'dummy.c').createNewFile()

        when:
        def result = GradleRunner.create()
            .withProjectDir(testProjectDir)
            .withPluginClasspath()
            .withArguments('genXrunArgsFile')
            .build()

        then:
        def lines = new File(testProjectDir, 'build/xrun_args.f').text.split('\n')
        lines.findAll { !it.contains('-makelib') && !it.contains('-endlib') }.each {
            assert it.startsWith('  ')
        }
    }

    def "'genXrunArgsFile' task for custom source set produces args file"() {
        File sv = new File([testProjectDir, 'src', 'dummy', 'c'].join(File.separator))
        sv.mkdirs()
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
            .withProjectDir(testProjectDir)
            .withPluginClasspath()
            .withArguments('genDummyXrunArgsFile')
            .build()

        then:
        result.task(":genDummyXrunArgsFile").outcome == SUCCESS
        new File(testProjectDir, "build/dummy_xrun_args.f").exists()
        new File(testProjectDir, "build/dummy_xrun_args.f").text.contains('dummy.c')
    }
}
