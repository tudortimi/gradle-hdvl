package com.verificationgentleman.gradle.hdvl.svunit;

import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.SourceTask;
import org.gradle.api.tasks.TaskAction;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class TestTask extends SourceTask {
    private File testsRoot;
    private RegularFileProperty destination;

    @Inject
    public TestTask(ObjectFactory objectFactory) {
        destination = objectFactory.fileProperty();
    }

    @Input
    public File getTestsRoot() {
        return testsRoot;
    }

    public void setTestsRoot(File testsRoot) {
        this.testsRoot = testsRoot;
    }

    @OutputFile
    public RegularFileProperty getDestination() {
        return destination;
    }

    @TaskAction
    protected void run() {
        try {
            Files.createSymbolicLink(destination.get().getAsFile().toPath(), getTestsRoot().toPath());
        } catch (IOException e) {
            System.out.println("Could not create 'tests' link");
        }
    }
}
