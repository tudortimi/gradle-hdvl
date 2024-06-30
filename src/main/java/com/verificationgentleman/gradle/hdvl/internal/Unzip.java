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

import org.gradle.api.Action;
import org.gradle.api.artifacts.transform.InputArtifact;
import org.gradle.api.artifacts.transform.TransformAction;
import org.gradle.api.artifacts.transform.TransformOutputs;
import org.gradle.api.artifacts.transform.TransformParameters;
import org.gradle.api.file.*;
import org.gradle.api.provider.Provider;

import javax.inject.Inject;
import java.io.File;

public abstract class Unzip implements TransformAction<TransformParameters.None> {
    private final FileSystemOperations files;
    private final ArchiveOperations archives;

    @Inject
    public Unzip(FileSystemOperations files, ArchiveOperations archives) {
        this.files = files;
        this.archives = archives;
    }

    @InputArtifact
    public abstract Provider<FileSystemLocation> getInputArtifact();

    @Override
    public void transform(TransformOutputs outputs) {
        File input = getInputArtifact().get().getAsFile();
        File unzipDir = outputs.dir(input.getName());
        unzipTo(input, unzipDir);
    }

    private void unzipTo(File zipFile, File unzipDir) {
        FileTree zipTree = archives.zipTree(zipFile);
        files.copy(new Action<CopySpec>() {
            @Override
            public void execute(CopySpec copySpec) {
                copySpec.from(zipTree);
                copySpec.into(unzipDir);
            }
        });
    }
}
