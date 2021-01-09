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

package com.verificationgentleman.gradle.hdvl.svunit

import org.gradle.api.Plugin
import org.gradle.api.initialization.Settings

class SvunitBuildPlugin implements Plugin<Settings> {
    void apply(Settings settings) {
        settings.with {
            rootProject.name = 'svunit'
            gradle.rootProject {
                group = 'org.svunit'

                configurations.create('default')

                artifacts {
                    'default'(projectDir)
                }
            }
        }
    }
}
