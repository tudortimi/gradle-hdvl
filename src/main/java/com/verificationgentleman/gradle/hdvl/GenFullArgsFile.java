package com.verificationgentleman.gradle.hdvl;

import org.gradle.api.DefaultTask;
import org.gradle.api.file.FileCollection;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;

import javax.inject.Inject;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class GenFullArgsFile extends DefaultTask {

    private final RegularFileProperty destination;
    private final RegularFileProperty source;
    private FileCollection argsFiles;

    @Inject
    public GenFullArgsFile(ObjectFactory objectFactory) {
        destination = objectFactory.fileProperty();
        source = objectFactory.fileProperty();
    }

    @OutputFile
    public RegularFileProperty getDestination() {
        return destination;
    }

    @InputFile
    public RegularFileProperty getSource() {
        return source;
    }

    @InputFiles
    public FileCollection getArgsFiles() {
        return argsFiles;
    }

    public void setArgsFiles(FileCollection argsFiles) {
        this.argsFiles = argsFiles;
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
        for (File argsFile: argsFiles) {
            writer.write("-f " + argsFile.getAbsolutePath() + "\n");
        }
        writer.write("-f " + source.get().getAsFile().getAbsolutePath() + "\n");
        writer.close();
    }

}
