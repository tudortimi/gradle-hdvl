package com.verificationgentleman.gradle.hdvl.svunit

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
}
