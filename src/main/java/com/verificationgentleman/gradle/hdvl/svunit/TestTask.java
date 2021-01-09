package com.verificationgentleman.gradle.hdvl.svunit;

import org.gradle.api.Action;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.FileCollection;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.tasks.*;
import org.gradle.process.ExecSpec;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class TestTask extends SourceTask {
    private RegularFileProperty mainArgsFile;
    private File testsRoot;
    private FileCollection svunitRoot;
    private DirectoryProperty workingDir;

    @Inject
    public TestTask(ObjectFactory objectFactory) {
        mainArgsFile = objectFactory.fileProperty();
        workingDir = objectFactory.directoryProperty();
    }

    @InputFile
    public RegularFileProperty getMainArgsFile() {
        return mainArgsFile;
    }

    @Input
    public File getTestsRoot() {
        return testsRoot;
    }

    public void setTestsRoot(File testsRoot) {
        this.testsRoot = testsRoot;
    }

    @Input
    public FileCollection getSvunitRoot() {
        return svunitRoot;
    }

    public void setSvunitRoot(FileCollection svunitRoot) {
        this.svunitRoot = svunitRoot;
    }

    @OutputDirectory
    public DirectoryProperty getWorkingDir() {
        return workingDir;
    }

    @TaskAction
    protected void run() {
        createLinkToTests();
        runTests();
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
                execSpec.executable("bash");
                String sourceCommands = String.join("; ",
                        "cd " + svunitRoot.getSingleFile(),
                        "source Setup.bsh",
                        "cd -");
                String runSVUnitCommand = String.join(" ",
                        "runSVUnit",
                        "-f", mainArgsFile.getAsFile().get().getAbsolutePath());
                String cArg = String.join("; ", sourceCommands, runSVUnitCommand);
                execSpec.args("-c", cArg);
                execSpec.workingDir(workingDir.get().getAsFile());
            }
        });
    }
}
