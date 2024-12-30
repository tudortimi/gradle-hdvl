/*
 * Copyright 2024 the original author or authors.
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
package com.verificationgentleman.gradle.hdvl.systemverilog


import org.gradle.testkit.runner.GradleRunner
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS

class SystemVerilogPluginCompileOrderSpec extends Specification {
    @Rule TemporaryFolder testProjectDir = new TemporaryFolder()
    File buildFile

    def setup() {
        buildFile = testProjectDir.newFile('build.gradle')
        buildFile << """
            plugins {
                id 'com.verificationgentleman.gradle.hdvl.systemverilog'
            }
        """
    }

    def "can compile a given sv source file first"() {
        File mainSv = testProjectDir.newFolder('src', 'main', 'sv')
        new File(mainSv, "file0.sv").createNewFile()
        new File(mainSv, "file1.sv").createNewFile()

        buildFile << """
            sourceSets.main.sv.order.first 'file1.sv'
        """

        when:
        def result = GradleRunner.create()
            .withProjectDir(testProjectDir.root)
            .withPluginClasspath()
            .withArguments(':genXrunArgsFile')
            .build()

        then:
        result.task(":genXrunArgsFile").outcome == SUCCESS
        def xrunArgsFile = new File(testProjectDir.root, 'build/xrun_args.f')
        def lines = xrunArgsFile.text.split('\n')

        def lineWithFile0 = lines.findIndexOf { it.contains('file0.sv') }
        lineWithFile0 != -1

        def lineWithFile1 = lines.findIndexOf { it.contains('file1.sv') }
        lineWithFile1 != -1

        lineWithFile1 < lineWithFile0
    }
}
