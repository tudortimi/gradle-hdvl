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

import com.verificationgentleman.gradle.hdvl.SystemVerilogPlugin
import org.gradle.testkit.runner.GradleRunner
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Ignore
import spock.lang.Specification

import java.nio.file.Files

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS

class SVUnitPluginSpec extends Specification  {
    @Rule TemporaryFolder testProjectDir = new TemporaryFolder()
    File buildFile
    File settingsFile

    def setup() {
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

    def "'test' task creates link in build directory to tests"() {
        File sv = testProjectDir.newFolder('src', 'test', 'sv')
        new File(sv, 'dummy.sv').createNewFile()

        when:
        def result = newGradleRunnerWithFakeRunSVunit()
            .withProjectDir(testProjectDir.root)
            .withPluginClasspath()
            .withArguments('test')
            .build()

        then:
        result.task(":test").outcome == SUCCESS
        def testsLink = new File(testProjectDir.root, 'build/svunit/tests')
        testsLink.exists()
        Files.isSymbolicLink(testsLink.toPath())
        testsLink.toPath().toRealPath() == sv.toPath()
    }

    def "'test' task executes 'runSVUnit'"() {
        File sv = testProjectDir.newFolder('src', 'test', 'sv')
        new File(sv, 'dummy.sv').createNewFile()

        when:
        def result = newGradleRunnerWithFakeRunSVunit()
            .withProjectDir(testProjectDir.root)
            .withPluginClasspath()
            .withArguments('test')
            .build()

        then:
        result.task(":test").outcome == SUCCESS
        def dummyLog = new File(testProjectDir.root, 'build/svunit/runSVUnit.log')
        dummyLog.exists()
    }

    def "'test' task passes args file to 'runSVUnit'"() {
        File mainSv = testProjectDir.newFolder('src', 'main', 'sv')
        new File(mainSv, 'dummy_main.sv').createNewFile()

        File testSv = testProjectDir.newFolder('src', 'test', 'sv')
        new File(testSv, 'dummy_test.sv').createNewFile()

        when:
        def result = newGradleRunnerWithFakeRunSVunit()
            .withProjectDir(testProjectDir.root)
            .withPluginClasspath()
            .withArguments('test')
            .build()

        then:
        result.task(":test").outcome == SUCCESS
        def dummyLog = new File(testProjectDir.root, 'build/svunit/runSVUnit.log')
        dummyLog.text.contains "-f ${testProjectDir.root}/build/full_args.f"
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

    // FIXME Re-add
    @Ignore
    def "'test' task passes custom args to 'runSVUnit'"() {
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
            .withArguments('test')
            .build()

        then:
        result.task(":test").outcome == SUCCESS
        def dummyLog = new File(testProjectDir.root, 'build/svunit/runSVUnit.log')
        dummyLog.text.contains "--uvm"
    }

    def newGradleRunnerWithFakeRunSVunit() {
        def runSVUnitFake = new File(getClass().getResource('/runSVUnit').toURI())
        def env = System.getenv()

        return GradleRunner.create()
            .withEnvironment(["PATH": [runSVUnitFake.parent, env.PATH].join(':')])
    }
}
