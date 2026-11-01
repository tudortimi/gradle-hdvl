/*
 * Copyright 2024-2026 the original author or authors.
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

import spock.lang.Specification
import spock.lang.TempDir
import spock.util.io.FileSystemFixture

abstract class AbstractDVTPluginSpec extends Specification {
    @TempDir FileSystemFixture testProjectDir
    File buildFile

    def setup() {
        testProjectDir.create {
            dir('src/main/sv') {
                file('dummy_main.sv') << ''
            }
            file('build.gradle') << """
                plugins {
                    id 'com.verificationgentleman.gradle.hdvl.dvt'
                }
            """
        }
        buildFile = testProjectDir.resolve('build.gradle').toFile()
    }
}
