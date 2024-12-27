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

import org.gradle.api.DefaultTask;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.tasks.*;

import javax.inject.Inject;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;

public class DVTTask extends DefaultTask {
    private RegularFileProperty argsFile;
    private RegularFileProperty defaultBuild;
    private File testsRoot;
    private DirectoryProperty workingDir;

    @Inject
    public DVTTask(ObjectFactory objectFactory) {
        argsFile = getProject().getObjects().fileProperty();
        defaultBuild = getProject().getObjects().fileProperty().convention(
                getProject().getLayout().getProjectDirectory().dir(".dvt").file("default.build"));
        workingDir = objectFactory.directoryProperty();
    }

    @InputFile
    public RegularFileProperty getArgsFile() {
        return argsFile;
    }

    @OutputFile
    public RegularFileProperty getDefaultBuild() {
        return defaultBuild;
    }

    @InputDirectory
    @Optional
    public File getTestsRoot() {
        return testsRoot;
    }

    public void setTestsRoot(File testsRoot) {
        this.testsRoot = testsRoot;
    }

    @OutputDirectory
    @Optional
    public DirectoryProperty getWorkingDir() {
        return workingDir;
    }

    @TaskAction
    public void generate() throws IOException {
        FileWriter fw = new FileWriter(defaultBuild.get().getAsFile());
        fw.write("+dvt_init+xcelium.xrun\n");
        fw.write("-f " + argsFile.getAsFile().get().getAbsolutePath() + "\n");

        if (testsRoot != null) {
            createLinkToTests();
        }

        fw.close();
    }

    private void createLinkToTests() {
        try {
            File testsLink = new File(workingDir.get().getAsFile(), "tests");
            Files.deleteIfExists(testsLink.toPath());
            Files.createSymbolicLink(testsLink.toPath(), getTestsRoot().toPath());
        } catch (IOException e) {
            throw new RuntimeException("Could not create 'tests' link.\n\n" + e.toString());
        }
    }
}
