package com.verificationgentleman.gradle.hdvl;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.io.IOException;

public class GenFullArgsFile extends DefaultTask {

    private File destination;

    @OutputFile
    public File getDestination() {
        return destination;
    }

    public void setDestination(File destination) {
        this.destination = destination;
    }

    @TaskAction
    protected void generate() {
        try {
            destination.createNewFile();
        } catch (IOException e) {
            System.out.println("Could not create args file");
        }
    }

}
