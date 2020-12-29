package com.verificationgentleman.gradle.hdvl;

import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.tasks.*;

import javax.inject.Inject;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Set;

public class GenArgsFile extends SourceTask {

    private RegularFileProperty destination;
    private Set<File> privateIncludeDirs;

    @Inject
    public GenArgsFile(ObjectFactory objectFactory) {
        destination = objectFactory.fileProperty();
    }

    @OutputFile
    public RegularFileProperty getDestination() {
        return destination;
    }

    @Input
    public Set<File> getPrivateIncludeDirs() {
        return privateIncludeDirs;
    }

    public void setPrivateIncludeDirs(Set<File> privateIncludeDirs) {
        this.privateIncludeDirs = privateIncludeDirs;
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
        for (File f: getPrivateIncludeDirs())
            writer.write("-incdir " + f.getAbsolutePath() + "\n");
        for (File f: getSource())
            writer.write(f.getAbsolutePath() + "\n");
        writer.close();
    }

}
