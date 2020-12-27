package com.verificationgentleman.gradle.hdvl;

import org.gradle.api.provider.Property;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.SourceTask;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.io.IOException;

public class GenArgsFile extends SourceTask {

    private File destinationDir;

    @OutputDirectory
    public File getDestinationDir() {
        return destinationDir;
    }

    public void setDestinationDir(File destinationDir) {
        this.destinationDir = destinationDir;
    }

    @TaskAction
    protected void generate() {
        try {
            new File(destinationDir, "args.f").createNewFile();
        } catch (IOException e) {
            System.out.println("Could not create args file");
        }
    }

}
