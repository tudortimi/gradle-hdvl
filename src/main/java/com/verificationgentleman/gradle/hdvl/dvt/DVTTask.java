/*
 * Copyright 2021-2023 the original author or authors.
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
package com.verificationgentleman.gradle.hdvl.dvt;

import org.gradle.api.DefaultTask;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class DVTTask extends DefaultTask {
    private RegularFileProperty argsFile;
    private RegularFileProperty defaultBuild;

    public DVTTask() {
        argsFile = getProject().getObjects().fileProperty();
        defaultBuild = getProject().getObjects().fileProperty().convention(
                getProject().getLayout().getProjectDirectory().dir(".dvt").file("default.build"));
    }

    @InputFile
    public RegularFileProperty getArgsFile() {
        return argsFile;
    }

    @OutputFile
    public RegularFileProperty getDefaultBuild() {
        return defaultBuild;
    }

    @TaskAction
    public void generate() throws IOException {
        FileWriter fw = new FileWriter(defaultBuild.get().getAsFile());
        fw.write("+dvt_init+xcelium.xrun\n");
        fw.write("-f " + argsFile.getAsFile().get().getAbsolutePath() + "\n");
        fw.close();
    }
}
