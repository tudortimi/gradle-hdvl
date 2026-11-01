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

package com.verificationgentleman.gradle.hdvl.svunit

import com.verificationgentleman.gradle.hdvl.FunctionalTestRunner
import com.verificationgentleman.gradle.hdvl.systemverilog.SystemVerilogPlugin
import spock.lang.Specification
import spock.lang.TempDir
import spock.util.io.FileSystemFixture

import java.nio.file.Files

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS

class SVUnitPluginSpec extends Specification  {
    @TempDir FileSystemFixture testProjectDir
    File buildFile

    def setup() {
        testProjectDir.create {
            dir('src/main/sv') {
                file('dummy_main.sv') << ''
            }
            file('build.gradle') << """
                plugins {
                    id 'com.verificationgentleman.gradle.hdvl.svunit'
                }
                dependencies {
                    testCompile "org.svunit:svunit:v3.34.2"
                }
            """
            file('settings.gradle') << """
                sourceControl {
                    gitRepository("https://github.com/tudortimi/svunit.git") {
                        producesModule("org.svunit:svunit")
                        plugins {
                            id "com.verificationgentleman.gradle.hdvl.svunit-build-injector"
                        }
                    }
                }
            """
        }
        buildFile = testProjectDir.resolve('build.gradle').toFile()
    }

    def "can successfully import the plugin"() {
        when:
        def result = FunctionalTestRunner.create()
            .withProjectDir(testProjectDir.currentPath.toFile())
            .build()

        then:
        result.task(":help").outcome == SUCCESS
    }

    def "importing the plugin applies the 'systemverilog' plugin"() {
        buildFile << """
            task printPlugins {
                doLast {
                    project.plugins.each {
                        println it
                    }
                }
            }
        """

        when:
        def result = FunctionalTestRunner.create()
            .withProjectDir(testProjectDir.currentPath.toFile())
            .withArguments(':printPlugins')
            .build()

        then:
        result.task(":printPlugins").outcome == SUCCESS
        result.output.contains(SystemVerilogPlugin.class.name)
    }

    def "'test' source set is added by the plugin"() {
        testProjectDir.create {
            dir('src/test/sv') {
                file('dummy.sv') << ''
            }
        }

        buildFile << """
            task copy(type: Copy) {
                from sourceSets.test.sv.files
                into 'build'
            }
        """

        when:
        def result = FunctionalTestRunner.create()
            .withProjectDir(testProjectDir.currentPath.toFile())
            .withArguments('copy')
            .build()

        then:
        result.task(":copy").outcome == SUCCESS
        testProjectDir.resolve('build/dummy.sv').toFile().exists()
    }

    def "'testWithXrun' task creates link in build directory to tests"() {
        testProjectDir.create {
            dir('src/test/sv') {
                file('dummy.sv') << ''
            }
        }
        File sv = testProjectDir.resolve('src/test/sv').toFile()

        when:
        def result = newGradleRunnerWithFakeRunSVunit()
            .withProjectDir(testProjectDir.currentPath.toFile())
            .withArguments('testWithXrun')
            .build()

        then:
        result.task(":testWithXrun").outcome == SUCCESS
        def testsLink = testProjectDir.resolve('build/svunit/tests').toFile()
        testsLink.exists()
        Files.isSymbolicLink(testsLink.toPath())
        testsLink.toPath().toRealPath() == sv.toPath()
    }

    def "'testWithXrun' task creates link in build directory to custom tests location"() {
        testProjectDir.create {
            dir('tests') {
                file('dummy.sv') << ''
            }
        }
        File sv = testProjectDir.resolve('tests').toFile()

        buildFile << """
            sourceSets {
                test {
                    sv {
                        srcDirs = ['tests']
                    }
                }
            }
        """

        when:
        def result = newGradleRunnerWithFakeRunSVunit()
            .withProjectDir(testProjectDir.currentPath.toFile())
            .withArguments('testWithXrun')
            .build()

        then:
        result.task(":testWithXrun").outcome == SUCCESS
        def testsLink = testProjectDir.resolve('build/svunit/tests').toFile()
        testsLink.exists()
        Files.isSymbolicLink(testsLink.toPath())
        testsLink.toPath().toRealPath() == sv.toPath()
    }

    def "'testWithXrun' task executes 'runSVUnit'"() {
        testProjectDir.create {
            dir('src/test/sv') {
                file('dummy.sv') << ''
            }
        }

        when:
        def result = newGradleRunnerWithFakeRunSVunit()
            .withProjectDir(testProjectDir.currentPath.toFile())
            .withArguments('testWithXrun')
            .build()

        then:
        result.task(":testWithXrun").outcome == SUCCESS
        def dummyLog = testProjectDir.resolve('build/svunit/runSVUnit.log').toFile()
        dummyLog.exists()
    }

    def "'testWithXrun' task passes args file to 'runSVUnit'"() {
        testProjectDir.create {
            dir('src/test/sv') {
                file('dummy_test.sv') << ''
            }
        }

        when:
        def result = newGradleRunnerWithFakeRunSVunit()
            .withProjectDir(testProjectDir.currentPath.toFile())
            .withArguments('testWithXrun')
            .build()

        then:
        result.task(":testWithXrun").outcome == SUCCESS
        def dummyLog = testProjectDir.resolve('build/svunit/runSVUnit.log').toFile()
        dummyLog.text.contains "-f ${testProjectDir.currentPath}/build/full_xrun_args.f"
    }

    def "'testWithQrun' task passes args file to 'runSVUnit'"() {
        testProjectDir.create {
            dir('src/test/sv') {
                file('dummy_test.sv') << ''
            }
        }

        when:
        def result = newGradleRunnerWithFakeRunSVunit()
            .withProjectDir(testProjectDir.currentPath.toFile())
            .withArguments('testWithQrun')
            .build()

        then:
        result.task(":testWithQrun").outcome == SUCCESS
        def dummyLog = testProjectDir.resolve('build/svunit/runSVUnit.log').toFile()
        dummyLog.text.contains "-f ${testProjectDir.currentPath}/build/full_qrun_args.f"
    }

    def "'testWithXrun' task passes simulator option to 'runSVUnit'"() {
        testProjectDir.create {
            dir('src/test/sv') {
                file('dummy_test.sv') << ''
            }
        }

        when:
        def result = newGradleRunnerWithFakeRunSVunit()
            .withProjectDir(testProjectDir.currentPath.toFile())
            .withArguments('testWithXrun')
            .build()

        then:
        result.task(":testWithXrun").outcome == SUCCESS
        def dummyLog = testProjectDir.resolve('build/svunit/runSVUnit.log').toFile()
        dummyLog.text.contains "-sim xrun"
    }

    def "'testWithQrun' task passes simulator option to 'runSVUnit'"() {
        testProjectDir.create {
            dir('src/test/sv') {
                file('dummy_test.sv') << ''
            }
        }

        when:
        def result = newGradleRunnerWithFakeRunSVunit()
            .withProjectDir(testProjectDir.currentPath.toFile())
            .withArguments('testWithQrun')
            .build()

        then:
        result.task(":testWithQrun").outcome == SUCCESS
        def dummyLog = testProjectDir.resolve('build/svunit/runSVUnit.log').toFile()
        dummyLog.text.contains "-sim qrun"
    }

    def "'toolChains' are added by the plugin"() {
        buildFile << """
            toolChains {
            }
        """

        when:
        def result = newGradleRunnerWithFakeRunSVunit()
            .withProjectDir(testProjectDir.currentPath.toFile())
            .withArguments('help')
            .build()

        then:
        result.task(":help").outcome == SUCCESS
    }

    def "'runSVUnit' tool chain is added by the plugin"() {
        buildFile << """
            toolChains {
                runSVUnit
            }
        """

        when:
        def result = newGradleRunnerWithFakeRunSVunit()
            .withProjectDir(testProjectDir.currentPath.toFile())
            .withArguments('help')
            .build()

        then:
        result.task(":help").outcome == SUCCESS
    }

    def "can add args to 'runSVUnit' tool chain"() {
        buildFile << """
            toolChains {
                runSVUnit {
                    args 'some_arg'
                    args 'some_other_arg'
                }
            }

            println toolChains.runSVUnit.args.get()
        """

        when:
        def result = newGradleRunnerWithFakeRunSVunit()
            .withProjectDir(testProjectDir.currentPath.toFile())
            .withArguments('help')
            .build()

        then:
        result.task(":help").outcome == SUCCESS
        result.output.contains 'some_arg'
        result.output.contains 'some_other_arg'
    }

    def "'testWithXrun' task passes custom args to 'runSVUnit'"() {
        testProjectDir.create {
            dir('src/test/sv') {
                file('dummy_test.sv') << ''
            }
        }

        buildFile << """
            toolChains {
                runSVUnit {
                    args '--uvm'
                }
            }
        """

        when:
        def result = newGradleRunnerWithFakeRunSVunit()
            .withProjectDir(testProjectDir.currentPath.toFile())
            .withArguments('testWithXrun')
            .build()

        then:
        result.task(":testWithXrun").outcome == SUCCESS
        def dummyLog = testProjectDir.resolve('build/svunit/runSVUnit.log').toFile()
        dummyLog.text.contains "--uvm"
    }

    def "'testWithXrun' task passes args containing spaces to 'runSVUnit' as single arguments"() {
        testProjectDir.create {
            dir('src/test/sv') {
                file('dummy_test.sv') << ''
            }
        }

        buildFile << """
            toolChains {
                runSVUnit {
                    args.addAll(['-c_arg', '-xmerror RECOMP'])
                }
            }
        """

        when:
        def result = newGradleRunnerWithFakeRunSVunit()
            .withProjectDir(testProjectDir.currentPath.toFile())
            .withArguments('testWithXrun')
            .build()

        then:
        result.task(":testWithXrun").outcome == SUCCESS
        def dummyLog = testProjectDir.resolve('build/svunit/runSVUnit.log').toFile()
        dummyLog.text.contains "-xmerror RECOMP"
    }

    def "sources in 'src/test/sv' are written to args file"() {
        given:
        testProjectDir.create {
            dir('src/test/sv') {
                file('some_test_package.sv') << ''
            }
        }

        when:
        def result = FunctionalTestRunner.create()
            .withProjectDir(testProjectDir.currentPath.toFile())
            .withArguments('genTestXrunArgsFile')
            .build()

        then:
        result.task(":genTestXrunArgsFile").outcome == SUCCESS
        def testXrunArgs = testProjectDir.resolve('build/test_xrun_args.f').toFile()
        testXrunArgs.exists()
        def lines = testXrunArgs.text.split("\n")
        lines.any { it.contains('some_test_package.sv') }
    }

    def "'test' source set excludes SVUnit unit tests"() {
        given:
        testProjectDir.create {
            dir('src/test/sv') {
                file('some_test_package.sv') << ''
                file('some_unit_test.sv') << ''
                dir('some_dir') {
                    file('some_other_unit_test.sv') << ''
                }
            }
        }

        when:
        def result = FunctionalTestRunner.create()
            .withProjectDir(testProjectDir.currentPath.toFile())
            .withArguments('genTestXrunArgsFile')
            .build()

        then:
        result.task(":genTestXrunArgsFile").outcome == SUCCESS
        def testXrunArgs = testProjectDir.resolve('build/test_xrun_args.f').toFile()
        testXrunArgs.exists()
        def lines = testXrunArgs.text.split("\n")
        !lines.any { it.contains('some_unit_test.sv') }
        !lines.any { it.contains('some_other_unit_test.sv') }
    }

    def "'testWithXrun' task passes full args file for 'test' source set to 'runSVUnit'"() {
        testProjectDir.create {
            dir('src/test/sv') {
                file('dummy_test.sv') << ''
            }
        }

        when:
        def result = newGradleRunnerWithFakeRunSVunit()
            .withProjectDir(testProjectDir.currentPath.toFile())
            .withArguments('testWithXrun')
            .build()

        then:
        result.task(":testWithXrun").outcome == SUCCESS
        def dummyLog = testProjectDir.resolve('build/svunit/runSVUnit.log').toFile()
        dummyLog.text.contains "-f ${testProjectDir.currentPath}/build/full_test_xrun_args.f"
    }

    def "'testWithXrun' uses args file for source set dependency"() {
        testProjectDir.create {
            dir('src/test/sv')
            dir('src/mocks/sv') {
                file('dummy_mocks.sv') << ''
            }
        }

        buildFile << """
            sourceSets.register('mocks')

            dependencies {
                testCompile files(genMocksXrunArgsFile.destination)
            }
        """

        when:
        def result = newGradleRunnerWithFakeRunSVunit()
            .withProjectDir(testProjectDir.currentPath.toFile())
            .withArguments('testWithXrun')
            .build()

        then:
        result.task(":testWithXrun").outcome == SUCCESS
        def testFullXrunArgsFile = testProjectDir.resolve('build/full_test_xrun_args.f').toFile()
        testFullXrunArgsFile.text.contains "-f ${testProjectDir.currentPath}/build/mocks_xrun_args.f"
    }

    def "'check' task executes test tasks"() {
        testProjectDir.create {
            dir('src/test/sv') {
                file('dummy_test.sv') << ''
            }
        }

        when:
        def result = newGradleRunnerWithFakeRunSVunit()
            .withProjectDir(testProjectDir.currentPath.toFile())
            .withArguments('check')
            .build()

        then:
        result.task(":check").outcome == SUCCESS
        result.task(":testWithXrun").outcome == SUCCESS
        result.task(":testWithQrun").outcome == SUCCESS
    }

    def "can use provider in testCompile for svunit dependency"() {
        setup:
        buildFile.text = """
            plugins {
                id 'com.verificationgentleman.gradle.hdvl.svunit'
            }
            dependencies {
                testCompile providers.provider { "org.svunit:svunit:v3.34.2" }
            }
        """
        testProjectDir.create {
            dir('src/test/sv') {
                file('dummy_test.sv') << ''
            }
        }

        when:
        def result = newGradleRunnerWithFakeRunSVunit()
            .withProjectDir(testProjectDir.currentPath.toFile())
            .withArguments('testWithXrun')
            .build()

        then:
        result.task(":testWithXrun").outcome == SUCCESS
    }

    def newGradleRunnerWithFakeRunSVunit() {
        def runSVUnitFake = new File(getClass().getResource('/runSVUnit').toURI())
        def env = System.getenv()

        return FunctionalTestRunner.create()
            .withEnvironment(["PATH": [runSVUnitFake.parent, env.PATH].join(':')])
    }
}
