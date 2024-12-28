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
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS

class DVTPluginInMultiProjectBuildSpec extends Specification {
    @Rule TemporaryFolder testProjectDir = new TemporaryFolder()

    def setup() {
        File sv0 = testProjectDir.newFolder('proj0', 'src', 'main', 'sv')
        new File(sv0, 'proj0.sv').createNewFile()

        File sv1 = testProjectDir.newFolder('proj1', 'src', 'main', 'sv')
        new File(sv0, 'proj1.sv').createNewFile()

        File buildFile = testProjectDir.newFile('build.gradle')
        buildFile << """
            plugins {
                id 'com.verificationgentleman.gradle.hdvl.systemverilog' apply false
                id 'com.verificationgentleman.gradle.hdvl.dvt'
            }
            subprojects {
                apply plugin: 'com.verificationgentleman.gradle.hdvl.systemverilog'
            }
        """

        File settingsFile = testProjectDir.newFile('settings.gradle')
        settingsFile << """
            include 'proj0'
            include 'proj1'
        """
    }

    def "'dvt' task includes args files of both projects in 'default.build'"() {
        when:
        def result = GradleRunner.create()
            .withProjectDir(testProjectDir.root)
            .withPluginClasspath()
            .withArguments('dvt')
            .build()

        then:
        result.task(":dvt").outcome == SUCCESS
        def defaultBuild = new File(testProjectDir.root, '.dvt/default.build')
        defaultBuild.text.contains "proj0"
        defaultBuild.text.contains "proj1"
    }
}
