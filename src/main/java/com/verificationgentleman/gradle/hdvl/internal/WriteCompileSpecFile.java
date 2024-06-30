/*
 * Copyright 2024 the original author or authors.
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

package com.verificationgentleman.gradle.hdvl.internal;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.*;

import java.io.File;
import java.io.IOException;

public class WriteCompileSpecFile extends DefaultTask {
    private final RegularFileProperty destination;

    private final ConfigurableFileCollection svSourceFiles;
    private final ConfigurableFileCollection svPrivateIncludeDirs;
    private final ConfigurableFileCollection svExportedHeaderDirs;
    private final ConfigurableFileCollection cSourceFiles;


    public WriteCompileSpecFile() {
        destination = getProject().getObjects().fileProperty();
        svSourceFiles = getProject().getObjects().fileCollection();
        svPrivateIncludeDirs = getProject().getObjects().fileCollection();
        svExportedHeaderDirs = getProject().getObjects().fileCollection();
        cSourceFiles = getProject().getObjects().fileCollection();
    }

    @OutputFile
    public RegularFileProperty getDestination() {
        return destination;
    }

    @InputFiles
    @SkipWhenEmpty
    @PathSensitive(PathSensitivity.ABSOLUTE)
    public ConfigurableFileCollection getSvSource() {
        return svSourceFiles;
    }

    @InputFiles
    @SkipWhenEmpty
    @PathSensitive(PathSensitivity.ABSOLUTE)
    public ConfigurableFileCollection getSvPrivateIncludeDirs() {
        return svPrivateIncludeDirs;
    }

    @InputFiles
    @SkipWhenEmpty
    @PathSensitive(PathSensitivity.ABSOLUTE)
    public ConfigurableFileCollection getSvExportedHeaderDirs() {
        return svExportedHeaderDirs;
    }

    @InputFiles
    @SkipWhenEmpty
    @PathSensitive(PathSensitivity.ABSOLUTE)
    public ConfigurableFileCollection getCSource() {
        return cSourceFiles;
    }

    @TaskAction
    protected void generateJson() {
        DefaultHDVLCompileSpec compileSpec = new DefaultHDVLCompileSpec(getSvSource().getFiles(),
                svPrivateIncludeDirs.getFiles(), svExportedHeaderDirs.getFiles(), cSourceFiles.getFiles());
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            SimpleModule module = new SimpleModule();
            module.addSerializer(File.class, new FileSerializer(getProject().getProjectDir()));
            objectMapper.registerModule(module);

            objectMapper.writerWithDefaultPrettyPrinter().writeValue(destination.get().getAsFile(), compileSpec);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
