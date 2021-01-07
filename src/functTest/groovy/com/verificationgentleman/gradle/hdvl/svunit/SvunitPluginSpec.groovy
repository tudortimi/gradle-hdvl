package com.verificationgentleman.gradle.hdvl.svunit

import com.verificationgentleman.gradle.hdvl.SystemVerilogPlugin
import org.gradle.testkit.runner.GradleRunner
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

import java.nio.file.Files

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS

class SvunitPluginSpec extends Specification  {
    @Rule TemporaryFolder testProjectDir = new TemporaryFolder()
    File buildFile

    def setup() {
        buildFile = testProjectDir.newFile('build.gradle')
        buildFile << """
            plugins {
                id 'com.verificationgentleman.gradle.hdvl.svunit'
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

    def "importing the plugin applies the 'systemverilog' plugin"() {
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

    def "'test' task creates link in build directory to tests"() {
        File sv = testProjectDir.newFolder('src', 'test', 'sv')
        new File(sv, 'dummy.sv').createNewFile()

        def runSVUnitFake = new File(getClass().getResource('/runSVUnit').toURI())
        def env = System.getenv()

        when:
        def result = GradleRunner.create()
            .withProjectDir(testProjectDir.root)
            .withPluginClasspath()
            .withEnvironment(["PATH": [runSVUnitFake.parent, env.PATH].join(':')])
            .withArguments('test')
            .build()

        then:
        result.task(":test").outcome == SUCCESS
        def testsLink = new File(testProjectDir.root, 'build/svunit/tests')
        testsLink.exists()
        Files.isSymbolicLink(testsLink.toPath())
        testsLink.toPath().toRealPath() == sv.toPath()
    }

    def "'test' task executes 'runSVUnit'"() {
        File sv = testProjectDir.newFolder('src', 'test', 'sv')
        new File(sv, 'dummy.sv').createNewFile()

        def runSVUnitFake = new File(getClass().getResource('/runSVUnit').toURI())
        def env = System.getenv()

        when:
        def result = GradleRunner.create()
            .withProjectDir(testProjectDir.root)
            .withPluginClasspath()
            .withEnvironment(["PATH": [runSVUnitFake.parent, env.PATH].join(':')])
            .withArguments('test')
            .build()

        then:
        result.task(":test").outcome == SUCCESS
        def dummyLog = new File(testProjectDir.root, 'build/svunit/runSVUnit.log')
        dummyLog.exists()
    }
}
