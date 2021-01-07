package com.verificationgentleman.gradle.hdvl.svunit;

import org.gradle.api.Action;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.SourceTask;
import org.gradle.api.tasks.TaskAction;
import org.gradle.process.ExecSpec;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class TestTask extends SourceTask {
    private File testsRoot;
    private RegularFileProperty workingDir;

    @Inject
    public TestTask(ObjectFactory objectFactory) {
        workingDir = objectFactory.fileProperty();
    }

    @Input
    public File getTestsRoot() {
        return testsRoot;
    }

    public void setTestsRoot(File testsRoot) {
        this.testsRoot = testsRoot;
    }

    @OutputFile
    public RegularFileProperty getWorkingDir() {
        return workingDir;
    }

    @TaskAction
    protected void run() {
        createWorkingDir();
        createLinkToTests();
        runTests();
    }

    private void createWorkingDir() {
        getProject().mkdir(workingDir.get().getAsFile());
    }

    private void createLinkToTests() {
        try {
            File testsLink = new File(workingDir.get().getAsFile(), "tests");
            Files.createSymbolicLink(testsLink.toPath(), getTestsRoot().toPath());
        } catch (IOException e) {
            System.out.println("Could not create 'tests' link");
        }

    }

    private void runTests() {
        getProject().exec(new Action<ExecSpec>() {
            @Override
            public void execute(ExecSpec execSpec) {
                execSpec.executable("runSVUnit");
                execSpec.workingDir(workingDir.get().getAsFile());
            }
        });
    }
}
