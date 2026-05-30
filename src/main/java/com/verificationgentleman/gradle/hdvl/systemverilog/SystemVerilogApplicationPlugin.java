package com.verificationgentleman.gradle.hdvl.systemverilog;

import com.verificationgentleman.gradle.hdvl.GenFullArgsFile;
import com.verificationgentleman.gradle.hdvl.HDVLPluginExtension;
import com.verificationgentleman.gradle.hdvl.SourceSet;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.ApplicationPlugin;
import org.gradle.api.tasks.TaskProvider;

import java.util.Collections;

public class SystemVerilogApplicationPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        project.getPluginManager().apply(SystemVerilogPlugin.class);

        SourceSet mainSourceSet = project.getExtensions()
                .getByType(HDVLPluginExtension.class)
                .getSourceSets()
                .getByName("main");

        String[] toolNames = {"Xrun", "Qrun"};
        for (String toolName : toolNames) {
            configureSimulationTask(project, mainSourceSet, toolName);
        }
    }

    private void configureSimulationTask(Project project, SourceSet mainSourceSet, String toolName) {
        GenFullArgsFile genFullArgsFile = (GenFullArgsFile) project.getTasks()
                .getByName(mainSourceSet.getGenFullArgsFileTaskName(toolName));

        TaskProvider<RunSimulationTask> runSimulationTask = project.getTasks().register(
                "simulateWith" + toolName,
                RunSimulationTask.class,
                task -> {
                    task.setGroup(ApplicationPlugin.APPLICATION_GROUP);
                    task.setDescription("Runs a simulation using " + toolName + ".");
                    task.getSimulator().set(toolName.toLowerCase());
                    task.getArgsFile().set(genFullArgsFile.getDestination());
                    task.getExtraArgs().set(Collections.emptyList());
                    task.getWorkingDir().set(project.getLayout().getBuildDirectory().dir(toolName.toLowerCase()));
                    task.getOutputs().upToDateWhen(spec -> false);
                }
        );

        runSimulationTask.configure(task -> task.dependsOn(genFullArgsFile));
    }
}
