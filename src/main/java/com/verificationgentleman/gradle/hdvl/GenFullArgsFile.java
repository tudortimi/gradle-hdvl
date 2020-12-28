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
import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.reverse;

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
        for (File argsFile: getArgsFilesInDependencyOrder()) {
            writer.write("-f " + argsFile.getAbsolutePath() + "\n");
        }
        writer.write("-f " + source.get().getAsFile().getAbsolutePath() + "\n");
        writer.close();
    }

    // XXX When looping over the configuration, the args files are returned in top/down order. Direct dependencies come
    // first, followed by transitive dependencies. We need them in the other order.
    // This is probably an implementation detail, so it's probably not so good to rely on this order.
    private List<File> getArgsFilesInDependencyOrder() {
        List<File> result = new ArrayList<>();
        for (File argsFile: argsFiles) {
            result.add(argsFile);
        }
        reverse(result);
        return result;
    }

}
