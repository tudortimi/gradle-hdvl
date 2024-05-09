package com.verificationgentleman.gradle.hdvl.internal;

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

public abstract class WriteXrunArgsFile implements TransformAction<TransformParameters.None> {
    @InputArtifact
    public abstract Provider<FileSystemLocation> getInputArtifact();

    @Override
    public void transform(TransformOutputs outputs) {
        File input = getInputArtifact().get().getAsFile();
        File xrunArgsFile = outputs.file(input.getName() + ".xrun_args.f");
        File[] svSourceFiles = getSvSourceFiles(input);
        writeXrunArgsFile(input, xrunArgsFile, svSourceFiles);
    }

    private static File[] getSvSourceFiles(File input) {
        // FIXME Should get list of files from compile spec
        return new File(input, "src/main/sv").listFiles();
    }

    private static void writeXrunArgsFile(File input, File xrunArgsFile, File[] svSourceFiles) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(xrunArgsFile, true))) {
            writer.write("-makelib worklib\n");
            for (File svSourceFile : svSourceFiles)
                writer.write("  " + svSourceFile + "\n");
            writer.write("-endlib\n");
        }
        catch (IOException ex) {
            ex.printStackTrace();  // TODO Implement better exception handling
        }
    }
}
