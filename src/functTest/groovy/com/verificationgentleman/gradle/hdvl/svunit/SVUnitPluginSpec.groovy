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

package com.verificationgentleman.gradle.hdvl.svunit

import com.verificationgentleman.gradle.hdvl.systemverilog.SystemVerilogPlugin
import org.gradle.testkit.runner.GradleRunner
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

import java.nio.file.Files

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS

class SVUnitPluginSpec extends Specification  {
    @Rule TemporaryFolder testProjectDir = new TemporaryFolder()
    File buildFile
    File settingsFile

    def setup() {
        File sv = testProjectDir.newFolder('src', 'main', 'sv')
        new File(sv, 'dummy_main.sv').createNewFile()

        buildFile = testProjectDir.newFile('build.gradle')
        buildFile << """
            plugins {
                id 'com.verificationgentleman.gradle.hdvl.svunit'
            }
            dependencies {
                testCompile "org.svunit:svunit:v3.34.2"
            }
        """
        settingsFile = testProjectDir.newFile('settings.gradle')
        settingsFile << """
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

    def "can successfully import the plugin"() {
        when:
        def result = GradleRunner.create()
            .withProjectDir(testProjectDir.root)
            .withPluginClasspath()
            .build()

        then:
        result.task(":help").outcome == SUCCESS
    }

    def "importing the plugin applies the 'systemverilog' plugin"() {
        when:
        def result = GradleRunner.create()
            .withProjectDir(testProjectDir.root)
            .withPluginClasspath()
            .withArguments(':properties')
            .build()

        then:
        result.task(":properties").outcome == SUCCESS
        def pluginsLine = result.output.split('\n').find { it.startsWith('plugins:') }
        pluginsLine.contains(SystemVerilogPlugin.class.name)
    }

    def "'test' source set is added by the plugin"() {
        File sv = testProjectDir.newFolder('src', 'test', 'sv')
        new File(sv, 'dummy.sv').createNewFile()

        buildFile << """
            task copy(type: Copy) {
                from sourceSets.test.sv.files
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

    def "'testWithXrun' task creates link in build directory to tests"() {
        File sv = testProjectDir.newFolder('src', 'test', 'sv')
        new File(sv, 'dummy.sv').createNewFile()

        when:
        def result = newGradleRunnerWithFakeRunSVunit()
            .withProjectDir(testProjectDir.root)
            .withPluginClasspath()
            .withArguments('testWithXrun')
            .build()

        then:
        result.task(":testWithXrun").outcome == SUCCESS
        def testsLink = new File(testProjectDir.root, 'build/svunit/tests')
        testsLink.exists()
        Files.isSymbolicLink(testsLink.toPath())
        testsLink.toPath().toRealPath() == sv.toPath()
    }

    def "'testWithXrun' task executes 'runSVUnit'"() {
        File sv = testProjectDir.newFolder('src', 'test', 'sv')
        new File(sv, 'dummy.sv').createNewFile()

        when:
        def result = newGradleRunnerWithFakeRunSVunit()
            .withProjectDir(testProjectDir.root)
            .withPluginClasspath()
            .withArguments('testWithXrun')
            .build()

        then:
        result.task(":testWithXrun").outcome == SUCCESS
        def dummyLog = new File(testProjectDir.root, 'build/svunit/runSVUnit.log')
        dummyLog.exists()
    }

    def "'testWithXrun' task passes args file to 'runSVUnit'"() {
        File testSv = testProjectDir.newFolder('src', 'test', 'sv')
        new File(testSv, 'dummy_test.sv').createNewFile()

        when:
        def result = newGradleRunnerWithFakeRunSVunit()
            .withProjectDir(testProjectDir.root)
            .withPluginClasspath()
            .withArguments('testWithXrun')
            .build()

        then:
        result.task(":testWithXrun").outcome == SUCCESS
        def dummyLog = new File(testProjectDir.root, 'build/svunit/runSVUnit.log')
        dummyLog.text.contains "-f ${testProjectDir.root}/build/full_xrun_args.f"
    }

    def "'testWithQrun' task passes args file to 'runSVUnit'"() {
        File testSv = testProjectDir.newFolder('src', 'test', 'sv')
        new File(testSv, 'dummy_test.sv').createNewFile()

        when:
        def result = newGradleRunnerWithFakeRunSVunit()
            .withProjectDir(testProjectDir.root)
            .withPluginClasspath()
            .withArguments('testWithQrun')
            .build()

        then:
        result.task(":testWithQrun").outcome == SUCCESS
        def dummyLog = new File(testProjectDir.root, 'build/svunit/runSVUnit.log')
        dummyLog.text.contains "-f ${testProjectDir.root}/build/full_qrun_args.f"
    }

    def "'testWithXrun' task passes simulator option to 'runSVUnit'"() {
        File testSv = testProjectDir.newFolder('src', 'test', 'sv')
        new File(testSv, 'dummy_test.sv').createNewFile()

        when:
        def result = newGradleRunnerWithFakeRunSVunit()
            .withProjectDir(testProjectDir.root)
            .withPluginClasspath()
            .withArguments('testWithXrun')
            .build()

        then:
        result.task(":testWithXrun").outcome == SUCCESS
        def dummyLog = new File(testProjectDir.root, 'build/svunit/runSVUnit.log')
        dummyLog.text.contains "-sim xrun"
    }

    def "'testWithQrun' task passes simulator option to 'runSVUnit'"() {
        File testSv = testProjectDir.newFolder('src', 'test', 'sv')
        new File(testSv, 'dummy_test.sv').createNewFile()

        when:
        def result = newGradleRunnerWithFakeRunSVunit()
            .withProjectDir(testProjectDir.root)
            .withPluginClasspath()
            .withArguments('testWithQrun')
            .build()

        then:
        result.task(":testWithQrun").outcome == SUCCESS
        def dummyLog = new File(testProjectDir.root, 'build/svunit/runSVUnit.log')
        dummyLog.text.contains "-sim qrun"
    }

    def "'toolChains' are added by the plugin"() {
        buildFile << """
            toolChains {
            }
        """

        when:
        def result = newGradleRunnerWithFakeRunSVunit()
            .withProjectDir(testProjectDir.root)
            .withPluginClasspath()
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
            .withProjectDir(testProjectDir.root)
            .withPluginClasspath()
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
            .withProjectDir(testProjectDir.root)
            .withPluginClasspath()
            .withArguments('help')
            .build()

        then:
        result.task(":help").outcome == SUCCESS
        result.output.contains 'some_arg'
        result.output.contains 'some_other_arg'
    }

    def "'testWithXrun' task passes custom args to 'runSVUnit'"() {
        File testSv = testProjectDir.newFolder('src', 'test', 'sv')
        new File(testSv, 'dummy_test.sv').createNewFile()

        buildFile << """
            toolChains {
                runSVUnit {
                    args '--uvm'
                }
            }
        """

        when:
        def result = newGradleRunnerWithFakeRunSVunit()
            .withProjectDir(testProjectDir.root)
            .withPluginClasspath()
            .withArguments('testWithXrun')
            .build()

        then:
        result.task(":testWithXrun").outcome == SUCCESS
        def dummyLog = new File(testProjectDir.root, 'build/svunit/runSVUnit.log')
        dummyLog.text.contains "--uvm"
    }

    def "sources in 'src/test/sv' are written to args file"() {
        given:
        File testSv = testProjectDir.newFolder('src', 'test', 'sv')
        new File(testSv, 'some_test_package.sv').createNewFile()

        when:
        def result = GradleRunner.create()
            .withProjectDir(testProjectDir.root)
            .withPluginClasspath()
            .withArguments('genTestXrunArgsFile')
            .build()

        then:
        result.task(":genTestXrunArgsFile").outcome == SUCCESS
        def testXrunArgs = new File(testProjectDir.root, 'build/test_xrun_args.f')
        testXrunArgs.exists()
        def lines = testXrunArgs.text.split("\n")
        lines.any { it.contains('some_test_package.sv') }
    }

    def "'test' source set excludes SVUnit unit tests"() {
        given:
        File testSv = testProjectDir.newFolder('src', 'test', 'sv')
        new File(testSv, 'some_test_package.sv').createNewFile()
        new File(testSv, 'some_unit_test.sv').createNewFile()
        new File(testSv, 'some_dir').mkdir()
        new File(testSv, 'some_dir/some_other_unit_test.sv').createNewFile()


        when:
        def result = GradleRunner.create()
            .withProjectDir(testProjectDir.root)
            .withPluginClasspath()
            .withArguments('genTestXrunArgsFile')
            .build()

        then:
        result.task(":genTestXrunArgsFile").outcome == SUCCESS
        def testXrunArgs = new File(testProjectDir.root, 'build/test_xrun_args.f')
        testXrunArgs.exists()
        def lines = testXrunArgs.text.split("\n")
        !lines.any { it.contains('some_unit_test.sv') }
        !lines.any { it.contains('some_other_unit_test.sv') }
    }

    def "'testWithXrun' task passes full args file for 'test' source set to 'runSVUnit'"() {
        File testSv = testProjectDir.newFolder('src', 'test', 'sv')
        new File(testSv, 'dummy_test.sv').createNewFile()

        when:
        def result = newGradleRunnerWithFakeRunSVunit()
            .withProjectDir(testProjectDir.root)
            .withPluginClasspath()
            .withArguments('testWithXrun')
            .build()

        then:
        result.task(":testWithXrun").outcome == SUCCESS
        def dummyLog = new File(testProjectDir.root, 'build/svunit/runSVUnit.log')
        dummyLog.text.contains "-f ${testProjectDir.root}/build/full_test_xrun_args.f"
    }

    def "'testWithXrun' uses args file for source set dependency"() {
        File testSv = testProjectDir.newFolder('src', 'test', 'sv')

        File mainSv = testProjectDir.newFolder('src', 'mocks', 'sv')
        new File(mainSv, 'dummy_mocks.sv').createNewFile()

        buildFile << """
            sourceSets.register('mocks')

            dependencies {
                testCompile files(genMocksXrunArgsFile.destination)
            }
        """

        when:
        def result = newGradleRunnerWithFakeRunSVunit()
            .withProjectDir(testProjectDir.root)
            .withPluginClasspath()
            .withArguments('testWithXrun')
            .build()

        then:
        result.task(":testWithXrun").outcome == SUCCESS
        def testFullXrunArgsFile = new File(testProjectDir.root, 'build/full_test_xrun_args.f')
        testFullXrunArgsFile.text.contains "-f ${testProjectDir.root}/build/mocks_xrun_args.f"
    }

    def "'check' task executes test tasks"() {
        File testSv = testProjectDir.newFolder('src', 'test', 'sv')
        new File(testSv, 'dummy_test.sv').createNewFile()

        when:
        def result = newGradleRunnerWithFakeRunSVunit()
            .withProjectDir(testProjectDir.root)
            .withPluginClasspath()
            .withArguments('check')
            .build()

        then:
        result.task(":check").outcome == SUCCESS
        result.task(":testWithXrun").outcome == SUCCESS
        result.task(":testWithQrun").outcome == SUCCESS
    }

    def newGradleRunnerWithFakeRunSVunit() {
        def runSVUnitFake = new File(getClass().getResource('/runSVUnit').toURI())
        def env = System.getenv()

        return GradleRunner.create()
            .withEnvironment(["PATH": [runSVUnitFake.parent, env.PATH].join(':')])
    }
}
