/*
 * Copyright 2023 the original author or authors.
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
package com.verificationgentleman.gradle.hdvl

import org.gradle.testkit.runner.GradleRunner
import spock.lang.Specification
import spock.lang.TempDir
import spock.util.io.FileSystemFixture

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS

class HDVLBasePluginSpec extends Specification {
    @TempDir FileSystemFixture fsFixture
    def buildFile

    def setup() {
        fsFixture.create {
            file('build.gradle') << """
                plugins {
                    id 'com.verificationgentleman.gradle.hdvl.base'
                }
            """
        }
        buildFile = fsFixture.resolve('build.gradle')
    }

    def "can successfully import the plugin"() {
        when:
        def result = GradleRunner.create()
                .withProjectDir(fsFixture.currentPath.toFile())
                .withPluginClasspath()
                .build()

        then:
        result.task(":help").outcome == SUCCESS
    }

    def "project with extra source set can be used as dependency"() {
        given:
        fsFixture.create {
            dir('consumer') {
                file('build.gradle') << """
                    plugins {
                        id 'com.verificationgentleman.gradle.hdvl.systemverilog'
                    }
                    dependencies {
                        compile ':producer'
                    }
                """
                file('settings.gradle') << """
                    includeBuild '../producer'
                """
                dir('src/main/sv') {
                    file('main.sv')
                }
            }
            dir('producer') {
                file('build.gradle') << """
                    plugins {
                        id 'com.verificationgentleman.gradle.hdvl.systemverilog'
                    }
                    sourceSets.create('other')
                """
                dir('src/main/sv') {
                    file('main.sv')
                }
                dir('src/other/sv') {
                    file('other.sv')
                }
            }
        }

        when:
        def result = GradleRunner.create()
            .withProjectDir(fsFixture.resolve('consumer').toFile())
            .withPluginClasspath()
            .withArguments('genFullXrunArgsFile')
            .build()
        println result.output

        then:
        result.task(":genFullXrunArgsFile").outcome == SUCCESS
    }
}
