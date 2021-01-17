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

package com.verificationgentleman.gradle.hdvl.dvt

import org.gradle.testkit.runner.GradleRunner
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS

class DVTPluginSpec extends Specification {
    @Rule TemporaryFolder testProjectDir = new TemporaryFolder()
    File buildFile

    def setup() {
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

    def "'dvt' task calls 'dvt_cli.sh createProject'"() {
        when:
        def result = newGradleRunnerWithFakeDvtCli()
            .withProjectDir(testProjectDir.root)
            .withPluginClasspath()
            .withArguments(':dvt')
            .build()

        then:
        result.task(":dvt").outcome == SUCCESS
        def dummyLog = new File(testProjectDir.root, 'dvt_cli.sh.log')
        dummyLog.exists()
        dummyLog.text.contains('createProject')
    }

    def "'dvt' task passes project path"() {
        when:
        def result = newGradleRunnerWithFakeDvtCli()
            .withProjectDir(testProjectDir.root)
            .withPluginClasspath()
            .withArguments(':dvt')
            .build()

        then:
        result.task(":dvt").outcome == SUCCESS
        def dummyLog = new File(testProjectDir.root, 'dvt_cli.sh.log')
        dummyLog.exists()
        dummyLog.text.contains(testProjectDir.root.absolutePath)
    }

    def "'dvt' task adds '-lang vlog' when 'systemverilog' plugin is imported"() {
        buildFile << """
            plugins {
                id 'com.verificationgentleman.gradle.hdvl.systemverilog'
            }
        """

        when:
        def result = newGradleRunnerWithFakeDvtCli()
            .withProjectDir(testProjectDir.root)
            .withPluginClasspath()
            .withArguments(':dvt')
            .build()

        then:
        result.task(":dvt").outcome == SUCCESS
        def dummyLog = new File(testProjectDir.root, 'dvt_cli.sh.log')
        dummyLog.exists()
        dummyLog.text.contains('-lang vlog')
    }

    def "'dvt' task adds single '-lang' when only 'systemverilog' plugin is imported"() {
        buildFile << """
            plugins {
                id 'com.verificationgentleman.gradle.hdvl.systemverilog'
            }
        """

        when:
        def result = newGradleRunnerWithFakeDvtCli()
            .withProjectDir(testProjectDir.root)
            .withPluginClasspath()
            .withArguments(':dvt')
            .build()

        then:
        result.task(":dvt").outcome == SUCCESS
        def dummyLog = new File(testProjectDir.root, 'dvt_cli.sh.log')
        dummyLog.exists()
        dummyLog.text.count('-lang') == 1
    }

    def "'dvt' task adds '-lang cpp' when 'c' plugin is imported"() {
        buildFile << """
            plugins {
                id 'com.verificationgentleman.gradle.hdvl.c'
            }
        """

        when:
        def result = newGradleRunnerWithFakeDvtCli()
            .withProjectDir(testProjectDir.root)
            .withPluginClasspath()
            .withArguments(':dvt')
            .build()

        then:
        result.task(":dvt").outcome == SUCCESS
        def dummyLog = new File(testProjectDir.root, 'dvt_cli.sh.log')
        dummyLog.exists()
        dummyLog.text.contains('-lang c')
    }

    def "'dvt' task adds two '-lang' options when both 'systemverilog' and 'c' plugins are imported"() {
        buildFile << """
            plugins {
                id 'com.verificationgentleman.gradle.hdvl.systemverilog'
                id 'com.verificationgentleman.gradle.hdvl.c'
            }
        """

        when:
        def result = newGradleRunnerWithFakeDvtCli()
            .withProjectDir(testProjectDir.root)
            .withPluginClasspath()
            .withArguments(':dvt')
            .build()

        then:
        result.task(":dvt").outcome == SUCCESS
        def dummyLog = new File(testProjectDir.root, 'dvt_cli.sh.log')
        dummyLog.exists()
        dummyLog.text.count('-lang') == 2
    }

    def newGradleRunnerWithFakeDvtCli() {
        def dvtCliFake = new File(getClass().getResource('/dvt_cli.sh').toURI())
        def env = System.getenv()

        return GradleRunner.create()
            .withEnvironment(["PATH": [dvtCliFake.parent, env.PATH].join(':')])
    }
}
