/*
 * Copyright 2021-2024 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.verificationgentleman.gradle.hdvl.dvt

import org.gradle.testkit.runner.GradleRunner
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

import java.nio.file.Files

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS

class DVTPluginSpec extends Specification {
    @Rule TemporaryFolder testProjectDir = new TemporaryFolder()
    File buildFile

    def setup() {
        File sv = testProjectDir.newFolder('src', 'main', 'sv')
        new File(sv, 'dummy_main.sv').createNewFile()

        buildFile = testProjectDir.newFile('build.gradle')
        buildFile << """
            plugins {
                id 'com.verificationgentleman.gradle.hdvl.dvt'
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

    def "adds a 'dvt' task"() {
        when:
        def result = GradleRunner.create()
            .withProjectDir(testProjectDir.root)
            .withPluginClasspath()
            .withArguments(':tasks', '--all')
            .build()

        then:
        result.task(":tasks").outcome == SUCCESS
        result.output.contains('dvt')
    }

    def "'dvt' task creates 'default.build' in xrun compatibility mode"() {
        buildFile << """
            plugins {
                id 'com.verificationgentleman.gradle.hdvl.systemverilog'
            }
        """

        when:
        def result = GradleRunner.create()
            .withProjectDir(testProjectDir.root)
            .withPluginClasspath()
            .withArguments(':dvt')
            .build()

        then:
        result.task(":dvt").outcome == SUCCESS
        def defaultBuild = new File(testProjectDir.root, '.dvt/default.build')
        defaultBuild.exists()
        defaultBuild.text.contains('+dvt_init+xcelium.xrun')
    }

    def "'dvt' task includes full xrun args file in 'default.build'"() {
        buildFile << """
            plugins {
                id 'com.verificationgentleman.gradle.hdvl.systemverilog'
            }
        """

        when:
        def result = GradleRunner.create()
            .withProjectDir(testProjectDir.root)
            .withPluginClasspath()
            .withArguments(':dvt')
            .build()

        then:
        result.task(":dvt").outcome == SUCCESS
        def defaultBuild = new File(testProjectDir.root, '.dvt/default.build')
        defaultBuild.exists()
        defaultBuild.text.contains('-f')
        defaultBuild.text.contains('full_xrun_args.f')
    }

    def "'dvt' task task creates link in build directory to tests"() {
        File testSv = testProjectDir.newFolder('src', 'test', 'sv')

        buildFile << """
            plugins {
                id 'com.verificationgentleman.gradle.hdvl.svunit'
            }
        """

        when:
        def result = GradleRunner.create()
            .withProjectDir(testProjectDir.root)
            .withPluginClasspath()
            .withArguments(':dvt')
            .build()

        then:
        result.task(":dvt").outcome == SUCCESS
        def testsLink = new File(testProjectDir.root, 'build/dvt/svunit/tests')
        testsLink.exists()
        Files.isSymbolicLink(testsLink.toPath())
        testsLink.toPath().toRealPath() == testSv.toPath()
    }

    def "'testWithXrun' task executes 'buildSVUnit'"() {
        File sv = testProjectDir.newFolder('src', 'test', 'sv')
        new File(sv, 'dummy_unit_test.sv').createNewFile()

        buildFile << """
            plugins {
                id 'com.verificationgentleman.gradle.hdvl.svunit'
            }
            dependencies {
                testCompile "org.svunit:svunit:v3.34.2"
            }
        """

        File settingsFile = testProjectDir.newFile('settings.gradle')
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

        when:
        def result = GradleRunner.create()
            .withProjectDir(testProjectDir.root)
            .withPluginClasspath()
            .withArguments('dvt')
            .build()

        then:
        result.task(":dvt").outcome == SUCCESS
        def svunitArgsFile = new File(testProjectDir.root, 'build/dvt/svunit/.svunit.f')
        svunitArgsFile.exists()
        svunitArgsFile.text.contains 'dummy_unit_test.sv'
    }
}
