/*
 * Copyright 2024 the original author or authors.
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

import java.nio.file.Files

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS

class DVTWithSVUnitPluginSpec extends AbstractDVTPluginSpec {
    File testSv

    def setup() {
        testSv = testProjectDir.newFolder('src', 'test', 'sv')
        new File(testSv, 'dummy_unit_test.sv').createNewFile()

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
    }

    def "'dvt' task task creates link in build directory to tests"() {
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

    def "'dvt' task executes 'buildSVUnit'"() {
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

    def "'dvt' task includes SVUnit args file in 'default.build'"() {
        when:
        def result = GradleRunner.create()
            .withProjectDir(testProjectDir.root)
            .withPluginClasspath()
            .withArguments('dvt')
            .build()

        then:
        result.task(":dvt").outcome == SUCCESS
        def svunitArgsFile = new File(testProjectDir.root, 'build/dvt/svunit/.svunit.f')
        def defaultBuild = new File(testProjectDir.root, '.dvt/default.build')
        defaultBuild.text.contains "-F $svunitArgsFile"
    }
}
