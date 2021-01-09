package com.verificationgentleman.gradle.hdvl.svunit;

import com.verificationgentleman.gradle.hdvl.GenFullArgsFile;
import com.verificationgentleman.gradle.hdvl.SourceSet;
import com.verificationgentleman.gradle.hdvl.SystemVerilogPlugin;
import org.gradle.api.Action;
import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.reflect.TypeOf;

import java.io.File;

public class SvunitPlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        project.getPluginManager().apply(SystemVerilogPlugin.class);
        NamedDomainObjectContainer<SourceSet> sourceSets = project.getExtensions()
                .getByType(new TypeOf<NamedDomainObjectContainer<SourceSet>>() {});
        final SourceSet testSourceSet = sourceSets.create("test");
        configureConfiguration(project);
        configureTestTask(project, testSourceSet);
    }

    private void configureConfiguration(Project project) {
        Configuration testCompileConfiguration = project.getConfigurations().create("testCompile");
    }

    private void configureTestTask(Project project, SourceSet testSourceSet) {
        GenFullArgsFile genFullArgsFile = (GenFullArgsFile) project.getTasks().getByName("genFullArgsFile");
        Configuration testCompileConfiguration = project.getConfigurations().getByName("testCompile");
        project.getTasks().register("test", TestTask.class, new Action<TestTask>() {
            @Override
            public void execute(TestTask testTask) {
                testTask.setDescription("Runs the unit tests using SVUnit.");
                testTask.getMainArgsFile().set(genFullArgsFile.getDestination());
                testTask.setSource(testSourceSet.getSv());
                testTask.setTestsRoot(testSourceSet.getSv().getSourceDirectories().getSingleFile());
                testTask.setSvunitRoot(testCompileConfiguration);
                testTask.getWorkingDir().set(new File(project.getBuildDir(), "svunit"));
            }
        });
    }
}
