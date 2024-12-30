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

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import org.gradle.testkit.runner.GradleRunner
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS

class SystemVerilogPluginCompileOrderSpec extends Specification {
    @Rule TemporaryFolder testProjectDir = new TemporaryFolder()
    File buildFile
    File mainSv

    def setup() {
        buildFile = testProjectDir.newFile('build.gradle')
        buildFile << """
            plugins {
                id 'com.verificationgentleman.gradle.hdvl.systemverilog'
            }
        """

        mainSv = testProjectDir.newFolder('src', 'main', 'sv')
    }

    def "can compile a given sv source file first"() {
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

    def "can compile matching source files first"() {
        new File(mainSv, 'another_file.sv').createNewFile()
        new File(mainSv, 'file0.sv').createNewFile()
        new File(mainSv, 'file1.sv').createNewFile()

        buildFile << """
            sourceSets.main.sv.order.first 'file*.sv'
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

        def lineWithAnotherFile = lines.findIndexOf { it.contains('another_file.sv') }
        lineWithFile1 != -1

        lineWithFile0 < lineWithAnotherFile
        lineWithFile1 < lineWithAnotherFile
    }

    def "can compile a given source file last"() {
        new File(mainSv, "file0.sv").createNewFile()
        new File(mainSv, "file1.sv").createNewFile()

        buildFile << """
            sourceSets.main.sv.order.last 'file0.sv'
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

        lineWithFile0 > lineWithFile1
    }

    def "can specify both first and last for compile order"() {
        new File(mainSv, "file0.sv").createNewFile()
        new File(mainSv, "file1.sv").createNewFile()
        new File(mainSv, "file2.sv").createNewFile()

        buildFile << """
            sourceSets.main.sv.order.first 'file1.sv'
            sourceSets.main.sv.order.last 'file0.sv'
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

        def lineWithFile2 = lines.findIndexOf { it.contains('file2.sv') }
        lineWithFile2 != -1

        lineWithFile1 < lineWithFile0
        lineWithFile1 < lineWithFile2
        lineWithFile0 > lineWithFile1
        lineWithFile0 > lineWithFile2
    }

    def "compile spec considers source file order"() {
        new File(mainSv, "file0.sv").createNewFile()
        new File(mainSv, "file1.sv").createNewFile()
        new File(mainSv, "file2.sv").createNewFile()

        buildFile << """
            sourceSets.main.sv.order.first 'file1.sv'
            sourceSets.main.sv.order.last 'file0.sv'
        """

        when:
        def result = GradleRunner.create()
            .withProjectDir(testProjectDir.root)
            .withPluginClasspath()
            .withArguments(':writeCompileSpecFile')
            .build()

        then:
        result.task(":writeCompileSpecFile").outcome == SUCCESS
        def compileSpecFile = new File(testProjectDir.root, 'build/compile-spec.json')
        JsonNode compileSpec = new ObjectMapper().readTree(compileSpecFile)
        JsonNode svSourceFiles = compileSpec.get("svSourceFiles")
        svSourceFiles.get(0).asText().contains 'file1.sv'
        svSourceFiles.get(1).asText().contains 'file2.sv'
        svSourceFiles.get(2).asText().contains 'file0.sv'
    }
}
