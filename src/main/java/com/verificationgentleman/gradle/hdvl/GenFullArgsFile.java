package com.verificationgentleman.gradle.hdvl;

import org.gradle.api.DefaultTask;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;

import javax.inject.Inject;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class GenFullArgsFile extends DefaultTask {

    private File destination;
    private final RegularFileProperty source;

    @Inject
    public GenFullArgsFile(ObjectFactory objectFactory) {
        source = objectFactory.fileProperty();
    }

    @OutputFile
    public File getDestination() {
        return destination;
    }

    public void setDestination(File destination) {
        this.destination = destination;
    }

    @InputFile
    public RegularFileProperty getSource() {
        return source;
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
        FileWriter writer = new FileWriter(destination);
        writer.write("-f " + source.get().getAsFile().getAbsolutePath() + "\n");
        writer.close();
    }

}
