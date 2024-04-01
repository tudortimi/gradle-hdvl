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
