/*
 * Copyright 2024-2026 the original author or authors.
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

import com.verificationgentleman.gradle.hdvl.FunctionalTestRunner
import spock.lang.Specification
import spock.lang.TempDir
import spock.util.io.FileSystemFixture

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS

class DVTPluginInMultiProjectBuildSpec extends Specification {
    @TempDir FileSystemFixture testProjectDir

    def setup() {
        testProjectDir.create {
            dir('proj0/src/main/sv') {
                file('proj0.sv') << ''
            }
            dir('proj0/src/test/sv') {
                file('proj0_unit_test.sv') << ''
            }
            dir('proj1/src/main/sv') {
                file('proj1.sv') << ''
            }
            dir('proj1/src/test/sv') {
                file('proj1_unit_test.sv') << ''
            }
            file('build.gradle') << """
                plugins {
                    id 'com.verificationgentleman.gradle.hdvl.systemverilog' apply false
                    id 'com.verificationgentleman.gradle.hdvl.svunit' apply false
                    id 'com.verificationgentleman.gradle.hdvl.dvt'
                }
                subprojects {
                    apply plugin: 'com.verificationgentleman.gradle.hdvl.systemverilog'
                    apply plugin: 'com.verificationgentleman.gradle.hdvl.svunit'
                    dependencies {
                        testCompile "org.svunit:svunit:v3.34.2"
                    }
                }
            """
            file('settings.gradle') << """
                include 'proj0'
                include 'proj1'
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
    }

    def "'dvt' task includes args files of both projects in 'default.build'"() {
        when:
        def result = FunctionalTestRunner.create()
            .withProjectDir(testProjectDir.currentPath.toFile())
            .withArguments('dvt')
            .build()

        then:
        result.task(":dvt").outcome == SUCCESS
        def defaultBuild = testProjectDir.resolve('.dvt/default.build').toFile()
        defaultBuild.text.contains "proj0"
        defaultBuild.text.contains "proj1"
    }

    def "'dvt' task includes SVUnit args files of both projects in 'default.build'"() {
        when:
        def result = FunctionalTestRunner.create()
            .withProjectDir(testProjectDir.currentPath.toFile())
            .withArguments('dvt')
            .build()

        then:
        result.task(":dvt").outcome == SUCCESS
        def defaultBuild = testProjectDir.resolve('.dvt/default.build').toFile()
        def svunitLines = defaultBuild.readLines().findAll() { it.contains('.svunit.f') }
        svunitLines.size() == 2
    }
}
