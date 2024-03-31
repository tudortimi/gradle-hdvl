package com.verificationgentleman.gradle.hdvl.internal;

import org.gradle.api.Action;
import org.gradle.api.artifacts.transform.InputArtifact;
import org.gradle.api.artifacts.transform.TransformAction;
import org.gradle.api.artifacts.transform.TransformOutputs;
import org.gradle.api.artifacts.transform.TransformParameters;
import org.gradle.api.file.*;
import org.gradle.api.provider.Provider;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import javax.inject.Inject;

public abstract class UnzipAndWriteXrunArgsFile implements TransformAction<TransformParameters.None> {
    private final FileSystemOperations files;
    private final ArchiveOperations archives;

    @Inject
    public UnzipAndWriteXrunArgsFile(FileSystemOperations files, ArchiveOperations archives) {
        this.files = files;
        this.archives = archives;
    }

    @InputArtifact
    public abstract Provider<FileSystemLocation> getInputArtifact();

    @Override
    public void transform(TransformOutputs outputs) {
        File input = getInputArtifact().get().getAsFile();
        File xrunArgsFile = outputs.file(input.getName() + ".xrun_args.f");
        File unzipDir = xrunArgsFile.getParentFile();
        unzipTo(input, unzipDir);
        writeXrunArgsFile(xrunArgsFile);
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

    private static void writeXrunArgsFile(File xrunArgsFile) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(xrunArgsFile, true))) {
            writer.write("-makelib worklib\n");
            writer.write("  " + xrunArgsFile.getParent() + "/src/main/sv/*.sv\n");  // FIXME Assumes source in conventional location
            writer.write("-endlib\n");
        }
        catch (IOException ex) {
            ex.printStackTrace();  // TODO Implement better exception handling
        }
    }
}
