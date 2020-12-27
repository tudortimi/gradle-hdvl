package com.verificationgentleman.gradle.hdvl;

import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.SourceTask;
import org.gradle.api.tasks.TaskAction;

import javax.inject.Inject;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class GenArgsFile extends SourceTask {

    private RegularFileProperty destination;

    @Inject
    public GenArgsFile() {
        // XXX 'getProject()' is not part of the public API
        destination = getProject().getObjects().fileProperty();
    }

    @OutputFile
    public RegularFileProperty getDestination() {
        return destination;
    }

    @TaskAction
    protected void generate() {
        try {
            writeArgsFile();
        } catch (IOException e) {
            System.out.println("Could not create args file");
        }
    }

    private void writeArgsFile() throws IOException {
        FileWriter writer = new FileWriter(destination.get().getAsFile());
        for (File f: getSource())
            writer.write(f.getAbsolutePath() + "\n");
        writer.close();
    }

}
