package com.verificationgentleman.gradle.hdvl.systemverilog

import org.gradle.testkit.runner.GradleRunner
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS

class SystemVerilogApplicationPluginSpec extends Specification {
    @Rule TemporaryFolder testProjectDir = new TemporaryFolder()
    File buildFile

    def setup() {
        buildFile = testProjectDir.newFile('build.gradle')
        buildFile << """
            plugins {
                id 'com.verificationgentleman.gradle.hdvl.systemverilog-application'
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
        result.task(':help').outcome == SUCCESS
    }

    def "'simulateWithXrun' runs xrun with full args file"() {
        File sv = testProjectDir.newFolder('src', 'main', 'sv')
        new File(sv, 'dummy.sv').createNewFile()

        when:
        def result = newGradleRunnerWithFakeSimulators()
            .withProjectDir(testProjectDir.root)
            .withPluginClasspath()
            .withArguments('simulateWithXrun')
            .build()

        then:
        result.task(':genFullXrunArgsFile').outcome == SUCCESS
        result.task(':simulateWithXrun').outcome == SUCCESS
        def xrunLog = new File(testProjectDir.root, 'build/xrun/xrun.log')
        xrunLog.exists()
        xrunLog.text.contains("-f ${testProjectDir.root}/build/full_xrun_args.f")
    }

    def "'simulateWithQrun' runs qrun with full args file"() {
        File sv = testProjectDir.newFolder('src', 'main', 'sv')
        new File(sv, 'dummy.sv').createNewFile()

        when:
        def result = newGradleRunnerWithFakeSimulators()
            .withProjectDir(testProjectDir.root)
            .withPluginClasspath()
            .withArguments('simulateWithQrun')
            .build()

        then:
        result.task(':genFullQrunArgsFile').outcome == SUCCESS
        result.task(':simulateWithQrun').outcome == SUCCESS
        def qrunLog = new File(testProjectDir.root, 'build/qrun/qrun.log')
        qrunLog.exists()
        qrunLog.text.contains("-f ${testProjectDir.root}/build/full_qrun_args.f")
    }

    def "'simulateWithXrun' passes dependency args files through full args file"() {
        setup:
        buildFile.delete()

        File settingsFile = testProjectDir.newFile('settings.gradle')
        settingsFile << """
            include 'directDependency'
            include 'mainProject'
        """

        File directDependencyBuildFile = newStandardProject('directDependency', 'com.verificationgentleman.gradle.hdvl.systemverilog')

        File mainProjectBuildFile = newStandardProject('mainProject', 'com.verificationgentleman.gradle.hdvl.systemverilog-application')
        mainProjectBuildFile << """
            dependencies {
                compile project(':directDependency')
            }
        """

        when:
        def result = newGradleRunnerWithFakeSimulators()
            .withProjectDir(testProjectDir.root)
            .withPluginClasspath()
            .withArguments(':mainProject:simulateWithXrun')
            .build()

        then:
        result.task(':directDependency:genXrunArgsFile').outcome == SUCCESS
        result.task(':mainProject:genFullXrunArgsFile').outcome == SUCCESS
        result.task(':mainProject:simulateWithXrun').outcome == SUCCESS

        def fullArgsFile = new File(testProjectDir.root, 'mainProject/build/full_xrun_args.f')
        fullArgsFile.text.contains('directDependency/build/xrun_args.f')
        fullArgsFile.text.contains('mainProject/build/xrun_args.f')
    }

    def newStandardProject(String name, String pluginId) {
        File folder = testProjectDir.newFolder(name)

        File sv = testProjectDir.newFolder(name, 'src', 'main', 'sv')
        new File(sv, "${name}.sv").createNewFile()

        File nestedBuildFile = new File(folder, 'build.gradle')
        nestedBuildFile << """
            plugins {
                id '${pluginId}'
            }
        """

        return nestedBuildFile
    }

    def newGradleRunnerWithFakeSimulators() {
        def xrunFake = new File(getClass().getResource('/xrun').toURI())
        def env = System.getenv()

        return GradleRunner.create()
            .withEnvironment(['PATH': [xrunFake.parent, env.PATH].join(':')])
    }
}
