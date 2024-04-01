package com.verificationgentleman.gradle.hdvl.internal;

import org.gradle.api.DefaultTask;
import org.gradle.api.file.FileCollection;
import org.gradle.api.file.FileTree;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.*;

import java.io.FileWriter;
import java.io.IOException;

public class WriteCompileSpecFile extends DefaultTask {
    private final RegularFileProperty destination;

    public WriteCompileSpecFile() {
        destination = getProject().getObjects().fileProperty();
    }

    @OutputFile
    public RegularFileProperty getDestination() {
        return destination;
    }

    @TaskAction
    protected void generate() {
        try {
            writeFile();
        } catch (IOException e) {
            System.out.println("Could not create compile spec file");
        }
    }

    private void writeFile() throws IOException {
        FileWriter writer = new FileWriter(destination.get().getAsFile());
        writer.write("");  // FIXME Implement
        writer.close();
    }
}
