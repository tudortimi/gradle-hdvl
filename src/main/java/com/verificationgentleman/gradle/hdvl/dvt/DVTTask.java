/*
 * Copyright 2021-2024 the original author or authors.
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
package com.verificationgentleman.gradle.hdvl.dvt;

import com.verificationgentleman.gradle.hdvl.dvt.internal.SVUnitSetup;
import org.gradle.api.Action;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.tasks.*;
import org.gradle.process.ExecSpec;

import javax.inject.Inject;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;

public abstract class DVTTask extends DefaultTask {
    // TODO Fix duplication with SVUnit plugin w.r.t. executing SVUnit scripts

    private ConfigurableFileCollection argsFiles;
    private RegularFileProperty defaultBuild;

    @Inject
    public DVTTask(ObjectFactory objectFactory) {
        argsFiles = getProject().getObjects().fileCollection();
        defaultBuild = getProject().getObjects().fileProperty().convention(
                getProject().getLayout().getProjectDirectory().dir(".dvt").file("default.build"));
    }

    @InputFiles
    public ConfigurableFileCollection getArgsFiles() {
        return argsFiles;
    }

    @OutputFile
    public RegularFileProperty getDefaultBuild() {
        return defaultBuild;
    }

    @Nested
    public abstract SVUnitSetup getSvUnitSetup();

    @TaskAction
    public void generate() throws IOException {
        FileWriter fw = new FileWriter(defaultBuild.get().getAsFile());
        fw.write("+dvt_init+xcelium.xrun\n");
        for (File argsFile : argsFiles.getFiles())
            fw.write("-f " + argsFile.getAbsolutePath() + "\n");

        if (getSvUnitSetup().getTestsRoot().isPresent()) {
            createLinkToTests();
            buildTestInfrastructure();
            fw.write("-F " + getSvUnitSetup().getWorkingDir().file(".svunit.f").get().getAsFile().getAbsolutePath());
        }

        fw.close();
    }

    private void createLinkToTests() {
        try {
            File testsLink = new File(getSvUnitSetup().getWorkingDir().get().getAsFile(), "tests");
            Files.deleteIfExists(testsLink.toPath());
            Files.createSymbolicLink(testsLink.toPath(), getSvUnitSetup().getTestsRoot().get().getAsFile().toPath());
        } catch (IOException e) {
            throw new RuntimeException("Could not create 'tests' link.\n\n" + e.toString());
        }
    }

    private void buildTestInfrastructure() {
        getProject().exec(new Action<ExecSpec>() {
            @Override
            public void execute(ExecSpec execSpec) {
                execSpec.executable("bash");
                String sourceCommands = String.join("; ",
                    "cd " + getSvUnitSetup().getSvunitRoot().getSingleFile(),
                    "source Setup.bsh",
                    "cd -");
                String buildSVUnitCommand = String.join(" ",
                    "buildSVUnit");
                String cArg = String.join("; ", sourceCommands, buildSVUnitCommand);
                execSpec.args("-c", cArg);
                execSpec.workingDir(getSvUnitSetup().getWorkingDir().get().getAsFile());
            }
        });
    }
}
