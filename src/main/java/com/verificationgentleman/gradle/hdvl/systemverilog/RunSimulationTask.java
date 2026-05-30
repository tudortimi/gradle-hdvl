package com.verificationgentleman.gradle.hdvl.systemverilog;

import org.gradle.api.Action;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;
import org.gradle.process.ExecSpec;

import javax.inject.Inject;

public class RunSimulationTask extends DefaultTask {

    private final Property<String> simulator;
    private final RegularFileProperty argsFile;
    private final ListProperty<String> extraArgs;
    private final DirectoryProperty workingDir;

    @Inject
    public RunSimulationTask(ObjectFactory objectFactory) {
        simulator = objectFactory.property(String.class);
        argsFile = objectFactory.fileProperty();
        extraArgs = objectFactory.listProperty(String.class);
        workingDir = objectFactory.directoryProperty();
    }

    @Input
    public Property<String> getSimulator() {
        return simulator;
    }

    @InputFile
    public RegularFileProperty getArgsFile() {
        return argsFile;
    }

    @Input
    public ListProperty<String> getExtraArgs() {
        return extraArgs;
    }

    @OutputDirectory
    public DirectoryProperty getWorkingDir() {
        return workingDir;
    }

    @TaskAction
    protected void runSimulation() {
        getProject().mkdir(getWorkingDir());
        getProject().exec(new Action<ExecSpec>() {
            @Override
            public void execute(ExecSpec execSpec) {
                execSpec.executable(simulator.get());
                execSpec.args("-f", argsFile.getAsFile().get().getAbsolutePath());
                execSpec.args(extraArgs.get());
                execSpec.workingDir(getWorkingDir().get().getAsFile());
            }
        });
    }
}
