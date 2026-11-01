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

import com.verificationgentleman.gradle.hdvl.TestProjectDir

import spock.lang.Specification
import spock.lang.TempDir

abstract class AbstractDVTPluginSpec extends Specification {
    @TempDir File testProjectDir
    File buildFile

    def setup() {
        File sv = TestProjectDir.newFolder(testProjectDir, 'src', 'main', 'sv')
        new File(sv, 'dummy_main.sv').createNewFile()

        buildFile = TestProjectDir.newFile(testProjectDir, 'build.gradle')
        buildFile << """
            plugins {
                id 'com.verificationgentleman.gradle.hdvl.dvt'
            }
        """
    }
}
