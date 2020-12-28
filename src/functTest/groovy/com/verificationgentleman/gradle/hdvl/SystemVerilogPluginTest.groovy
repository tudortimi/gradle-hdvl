/*
 * Copyright 2020 the original author or authors.
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
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

import static org.gradle.testkit.runner.TaskOutcome.NO_SOURCE
import static org.gradle.testkit.runner.TaskOutcome.SUCCESS

class SystemVerilogPluginFunctionalTest extends Specification {
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

    def "can successfully import the plugin"() {
        when:
        def result = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withPluginClasspath()
                .build()

        then:
        result.task(":help").outcome == SUCCESS
    }

    def "can access 'sourceSets' property"() {
        buildFile << """
            task assertProps {
                doLast {
                    assert project.sourceSets != null
                }
            }
        """

        when:
        def result = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withPluginClasspath()
                .withArguments('assertProps')
                .build()

        then:
        result.task(":assertProps").outcome == SUCCESS
    }

    def "can configure a source set"() {
        File sv = testProjectDir.newFolder('src', 'main', 'sv')
        new File(sv, 'dummy.sv').createNewFile()

        buildFile << """
            sourceSets {
                main
            }
            
            task copy(type: Copy) {
                from sourceSets.main.sv.files
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

    def "source set ignores 'svh' files"() {
        File sv = testProjectDir.newFolder('src', 'main', 'sv')
        new File(sv, 'dummy.sv').createNewFile()
        new File(sv, 'dummy.svh').createNewFile()

        buildFile << """
            sourceSets {
                main
            }
            
            task copy(type: Copy) {
                from sourceSets.main.sv.files
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
        !(new File(testProjectDir.root, 'build/dummy.svh').exists())
    }

    def "can specify a source set source directory using a closure"() {
        File sv = testProjectDir.newFolder('sv')
        new File(sv, 'dummy.sv').createNewFile()

        buildFile << """
            sourceSets {
                main {
                    sv {
                        srcDirs = ['sv']
                    }
                }
            }
            
            task copy(type: Copy) {
                from sourceSets.main.sv.files
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

    def "'main' source set is added by the plugin"() {
        File sv = testProjectDir.newFolder('src', 'main', 'sv')
        new File(sv, 'dummy.sv').createNewFile()

        buildFile << """            
            task copy(type: Copy) {
                from sourceSets.main.sv.files
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

    def "can specify a source set source exclude using an action"() {
        // XXX Most tests use 'build.gradle', but in this test we want to use a Kotlin build script. It seems like
        // overkill to create a new test class just fo this.
        setup:
        new File(testProjectDir.root, 'build.gradle').delete()

        File sv = testProjectDir.newFolder('src', 'main', 'sv')
        new File(sv, 'dummy.sv').createNewFile()

        File buildFile = testProjectDir.newFile('build.gradle.kts')
        buildFile << """
            plugins {
                id("com.verificationgentleman.gradle.hdvl.systemverilog")
            }
            
            sourceSets {
                main {
                    sv {
                        exclude("**/dummy.sv")
                    }
                }
            }
            
            tasks.register<Copy>("copy") {
                // XXX Not clear why we can't just do 'sourceSets.main.sv'.
                // 'sourceSets.main' doesn't return an object of type 'SourceSet', but a
                // 'NamedDomainObjectProvider<SourceSet'. The Java plugin has the same issue.
                from(sourceSets.main.get().sv.files)
                into("build")
            }
        """

        when:
        def result = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withPluginClasspath()
                .withArguments('copy')
                .build()

        then:
        result.task(":copy").outcome == NO_SOURCE
    }

    def "'getArgsFile' task is added by the plugin"() {
        buildFile << """
            task assertTasks {
                doLast {
                    assert project.tasks.genArgsFile != null
                }
            }
        """

        when:
        def result = GradleRunner.create()
            .withProjectDir(testProjectDir.root)
            .withPluginClasspath()
            .withArguments('assertTasks')
            .build()

        then:
        result.task(":assertTasks").outcome == SUCCESS
    }

    def "'getArgsFile' task produces output"() {
        File sv = testProjectDir.newFolder('src', 'main', 'sv')
        new File(sv, 'dummy.sv').createNewFile()

        when:
        def result = GradleRunner.create()
            .withProjectDir(testProjectDir.root)
            .withPluginClasspath()
            .withArguments('genArgsFile')
            .build()

        then:
        result.task(":genArgsFile").outcome == SUCCESS
        new File(testProjectDir.root, 'build/args.f').exists()
        new File(testProjectDir.root, 'build/args.f').text.contains('src/main/sv/dummy.sv')
    }

    def "'argsFiles' configuration is added by the plugin"() {
        buildFile << """
            task assertTasks {
                doLast {
                    assert project.configurations.argsFiles != null
                }
            }
        """

        when:
        def result = GradleRunner.create()
            .withProjectDir(testProjectDir.root)
            .withPluginClasspath()
            .withArguments('assertTasks')
            .build()

        then:
        result.task(":assertTasks").outcome == SUCCESS
    }

    def "'argsFiles' artifacts produced by producer are consumed by consumer"() {
        setup:
        buildFile.delete()

        File settingsFile = testProjectDir.newFile('settings.gradle')
        settingsFile << """
            include 'producer'
            include 'consumer'
        """

        File producer = testProjectDir.newFolder('producer')

        File producerSv = testProjectDir.newFolder('producer','src', 'main', 'sv')
        new File(producerSv, 'dummy.sv').createNewFile()

        File producerBuildFile = new File(producer, "build.gradle")
        producerBuildFile << """
            plugins {
                id 'com.verificationgentleman.gradle.hdvl.systemverilog'
            }
        """

        File consumer = testProjectDir.newFolder('consumer')

        File consumerBuildFile = new File(consumer, "build.gradle")
        consumerBuildFile << """
            plugins {
                id 'com.verificationgentleman.gradle.hdvl.systemverilog'
            }
            
            dependencies {
                argsFiles(project(path: ':producer', configuration: 'argsFiles'))
            }
        """

        consumerBuildFile << """
            task assertConfigurations {
                dependsOn project.configurations.incomingArgsFiles
                doLast {
                    assert !project.configurations.incomingArgsFiles.files.empty
                }
            }
        """

        when:
        def result = GradleRunner.create()
            .withProjectDir(testProjectDir.root)
            .withPluginClasspath()
            .withArguments(':consumer:assertConfigurations')
            .build()

        then:
        result.task(":producer:genArgsFile").outcome == SUCCESS
        result.task(":consumer:assertConfigurations").outcome == SUCCESS
    }

    def "'genFullArgsFile' task consumes output of 'genArgsFile"() {
        File sv = testProjectDir.newFolder('src', 'main', 'sv')
        new File(sv, 'dummy.sv').createNewFile()

        when:
        def result = GradleRunner.create()
            .withProjectDir(testProjectDir.root)
            .withPluginClasspath()
            .withArguments('genFullArgsFile')
            .build()

        then:
        result.task(":genArgsFile").outcome == SUCCESS
        result.task(":genFullArgsFile").outcome == SUCCESS
        new File(testProjectDir.root, 'build/full_args.f').exists()
        new File(testProjectDir.root, 'build/full_args.f').text.contains('build/args.f')
    }

    def "'argsFiles' artifacts produced by producer are consumed by consumer in 'getFullArgsFile'"() {
        setup:
        buildFile.delete()

        File settingsFile = testProjectDir.newFile('settings.gradle')
        settingsFile << """
            include 'producer'
            include 'consumer'
        """

        File producer = testProjectDir.newFolder('producer')

        File producerSv = testProjectDir.newFolder('producer','src', 'main', 'sv')
        new File(producerSv, 'producer.sv').createNewFile()

        File producerBuildFile = new File(producer, "build.gradle")
        producerBuildFile << """
            plugins {
                id 'com.verificationgentleman.gradle.hdvl.systemverilog'
            }
        """

        File consumer = testProjectDir.newFolder('consumer')

        File consumerSv = testProjectDir.newFolder('consumer','src', 'main', 'sv')
        new File(consumerSv, 'consumer.sv').createNewFile()

        File consumerBuildFile = new File(consumer, "build.gradle")
        consumerBuildFile << """
            plugins {
                id 'com.verificationgentleman.gradle.hdvl.systemverilog'
            }
            
            dependencies {
                argsFiles(project(path: ':producer', configuration: 'argsFiles'))
            }
        """

        when:
        def result = GradleRunner.create()
            .withProjectDir(testProjectDir.root)
            .withPluginClasspath()
            .withArguments(':consumer:genFullArgsFile')
            .build()

        then:
        result.task(":producer:genArgsFile").outcome == SUCCESS
        result.task(":consumer:genArgsFile").outcome == SUCCESS
        result.task(":consumer:genFullArgsFile").outcome == SUCCESS
        new File(testProjectDir.root, 'consumer/build/full_args.f').text.contains('producer/build/args.f')
        new File(testProjectDir.root, 'consumer/build/full_args.f').text.contains('consumer/build/args.f')
    }

    def "'argsFiles' artifacts produced by transitive dependencies are consumed in 'genFullArgsFile'"() {
        setup:
        buildFile.delete()

        File settingsFile = testProjectDir.newFile('settings.gradle')
        settingsFile << """
            include 'transitive'
            include 'producer'
            include 'consumer'
        """

        File transitive = testProjectDir.newFolder('transitive')

        File transitiveSv = testProjectDir.newFolder('transitive','src', 'main', 'sv')
        new File(transitiveSv, 'transitive.sv').createNewFile()

        File transitiveBuildFile = new File(transitive, "build.gradle")
        transitiveBuildFile << """
            plugins {
                id 'com.verificationgentleman.gradle.hdvl.systemverilog'
            }
        """

        File producer = testProjectDir.newFolder('producer')

        File producerSv = testProjectDir.newFolder('producer','src', 'main', 'sv')
        new File(producerSv, 'producer.sv').createNewFile()

        File producerBuildFile = new File(producer, "build.gradle")
        producerBuildFile << """
            plugins {
                id 'com.verificationgentleman.gradle.hdvl.systemverilog'
            }
            
            dependencies {
                argsFiles(project(path: ':transitive', configuration: 'argsFiles'))
            }
        """

        File consumer = testProjectDir.newFolder('consumer')

        File consumerSv = testProjectDir.newFolder('consumer','src', 'main', 'sv')
        new File(consumerSv, 'consumer.sv').createNewFile()

        File consumerBuildFile = new File(consumer, "build.gradle")
        consumerBuildFile << """
            plugins {
                id 'com.verificationgentleman.gradle.hdvl.systemverilog'
            }
            
            dependencies {
                argsFiles(project(path: ':producer', configuration: 'argsFiles'))
            }
        """

        when:
        def result = GradleRunner.create()
            .withProjectDir(testProjectDir.root)
            .withPluginClasspath()
            .withArguments(':consumer:genFullArgsFile')
            .build()

        then:
        result.task(":transitive:genArgsFile").outcome == SUCCESS
        result.task(":producer:genArgsFile").outcome == SUCCESS
        result.task(":consumer:genArgsFile").outcome == SUCCESS
        result.task(":consumer:genFullArgsFile").outcome == SUCCESS
        new File(testProjectDir.root, 'consumer/build/full_args.f').text.contains('transitive/build/args.f')
        new File(testProjectDir.root, 'consumer/build/full_args.f').text.contains('producer/build/args.f')
        new File(testProjectDir.root, 'consumer/build/full_args.f').text.contains('consumer/build/args.f')
    }

    def "'argsFiles' are consumed in dependency order"() {
        setup:
        buildFile.delete()

        File settingsFile = testProjectDir.newFile('settings.gradle')
        settingsFile << """
            include 'transitive'
            include 'producer'
            include 'consumer'
        """

        File transitive = testProjectDir.newFolder('transitive')

        File transitiveSv = testProjectDir.newFolder('transitive','src', 'main', 'sv')
        new File(transitiveSv, 'transitive.sv').createNewFile()

        File transitiveBuildFile = new File(transitive, "build.gradle")
        transitiveBuildFile << """
            plugins {
                id 'com.verificationgentleman.gradle.hdvl.systemverilog'
            }
        """

        File producer = testProjectDir.newFolder('producer')

        File producerSv = testProjectDir.newFolder('producer','src', 'main', 'sv')
        new File(producerSv, 'producer.sv').createNewFile()

        File producerBuildFile = new File(producer, "build.gradle")
        producerBuildFile << """
            plugins {
                id 'com.verificationgentleman.gradle.hdvl.systemverilog'
            }
            
            dependencies {
                argsFiles(project(path: ':transitive', configuration: 'argsFiles'))
            }
        """

        File consumer = testProjectDir.newFolder('consumer')

        File consumerSv = testProjectDir.newFolder('consumer','src', 'main', 'sv')
        new File(consumerSv, 'consumer.sv').createNewFile()

        File consumerBuildFile = new File(consumer, "build.gradle")
        consumerBuildFile << """
            plugins {
                id 'com.verificationgentleman.gradle.hdvl.systemverilog'
            }
            
            dependencies {
                argsFiles(project(path: ':producer', configuration: 'argsFiles'))
            }
        """

        when:
        def result = GradleRunner.create()
            .withProjectDir(testProjectDir.root)
            .withPluginClasspath()
            .withArguments(':consumer:genFullArgsFile')
            .build()

        then:
        def lines = new File(testProjectDir.root, 'consumer/build/full_args.f').text.split('\n')
        def transitiveIdx = lines.findIndexOf {
            it.contains('transitive/build/args.f')
        }
        transitiveIdx != -1
        def producerIdx = lines.findIndexOf(transitiveIdx) {
            it.contains('producer/build/args.f')
        }
        producerIdx != -1
        def consumerIdx = lines.findIndexOf(transitiveIdx) {
            it.contains('consumer/build/args.f')
        }
        consumerIdx != -1
    }
}
