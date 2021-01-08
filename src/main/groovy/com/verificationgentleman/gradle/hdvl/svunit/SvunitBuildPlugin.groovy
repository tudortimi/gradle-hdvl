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
