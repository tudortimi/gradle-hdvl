package com.verificationgentleman.gradle.hdvl.svunit

import com.verificationgentleman.gradle.hdvl.SystemVerilogPlugin
import org.gradle.testkit.runner.GradleRunner
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS

class SvunitPluginSpec extends Specification  {
    @Rule TemporaryFolder testProjectDir = new TemporaryFolder()

    def "can successfully import the plugin"() {
        File buildFile = new File(testProjectDir.root, "build.gradle")
        buildFile << """
            plugins {
                id 'com.verificationgentleman.gradle.hdvl.svunit'
            }
        """

        when:
        def result = GradleRunner.create()
            .withProjectDir(testProjectDir.root)
            .withPluginClasspath()
            .build()

        then:
        result.task(":help").outcome == SUCCESS
    }

    def "importing the plugin applies the 'systemverilog' plugin"() {
        File buildFile = new File(testProjectDir.root, "build.gradle")
        buildFile << """
            plugins {
                id 'com.verificationgentleman.gradle.hdvl.svunit'
            }
        """

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

        File buildFile = new File(testProjectDir.root, "build.gradle")
        buildFile << """
            plugins {
                id 'com.verificationgentleman.gradle.hdvl.svunit'
            }
        """

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
}
