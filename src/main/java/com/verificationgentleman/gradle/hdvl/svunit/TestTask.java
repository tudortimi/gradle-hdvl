/*
 * Copyright 2021-2022 the original author or authors.
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

package com.verificationgentleman.gradle.hdvl.svunit;

import org.gradle.api.Action;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.FileCollection;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.*;
import org.gradle.process.ExecSpec;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class TestTask extends DefaultTask {

    private RegularFileProperty mainArgsFile;
    private RegularFileProperty testArgsFile;
    private File testsRoot;
    private FileCollection svunitRoot;
    private Property<String> toolName;
    private DirectoryProperty workingDir;
    private ListProperty<String> extraArgs;

    @Inject
    public TestTask(ObjectFactory objectFactory) {
        toolName = objectFactory.property(String.class);
        mainArgsFile = objectFactory.fileProperty();
        testArgsFile = objectFactory.fileProperty();
        workingDir = objectFactory.directoryProperty();
        extraArgs = objectFactory.listProperty(String.class);
    }

    @Input
    public Property<String> getToolName() {
        return toolName;
    }

    @InputFile
    public RegularFileProperty getMainArgsFile() {
        return mainArgsFile;
    }

    @InputFile
    public RegularFileProperty getTestArgsFile() {
        return testArgsFile;
    }

    @InputDirectory
    public File getTestsRoot() {
        return testsRoot;
    }

    public void setTestsRoot(File testsRoot) {
        this.testsRoot = testsRoot;
    }

    @InputFiles
    public FileCollection getSvunitRoot() {
        return svunitRoot;
    }

    public void setSvunitRoot(FileCollection svunitRoot) {
        this.svunitRoot = svunitRoot;
    }

    @OutputDirectory
    public DirectoryProperty getWorkingDir() {
        return workingDir;
    }

    @Input
    public ListProperty<String> getExtraArgs() {
        return extraArgs;
    }

    @TaskAction
    protected void run() {
        createLinkToTests();
        runTests();
    }

    private void createLinkToTests() {
        try {
            File testsLink = new File(workingDir.get().getAsFile(), "tests");
            Files.createSymbolicLink(testsLink.toPath(), getTestsRoot().toPath());
        } catch (IOException e) {
            throw new RuntimeException("Could not create 'tests' link.\n\n" + e.toString());
        }

    }

    private void runTests() {
        getProject().exec(new Action<ExecSpec>() {
            @Override
            public void execute(ExecSpec execSpec) {
                execSpec.executable("bash");
                String sourceCommands = String.join("; ",
                        "cd " + svunitRoot.getSingleFile(),
                        "source Setup.bsh",
                        "cd -");
                String runSVUnitCommand = String.join(" ",
                        "runSVUnit",
                        "--sim", toolName.get(),
                        "-f", mainArgsFile.getAsFile().get().getAbsolutePath(),
                        "-f", testArgsFile.getAsFile().get().getAbsolutePath(),
                    String.join(" ", extraArgs.get()));
                String cArg = String.join("; ", sourceCommands, runSVUnitCommand);
                execSpec.args("-c", cArg);
                execSpec.workingDir(workingDir.get().getAsFile());
            }
        });
    }

}
