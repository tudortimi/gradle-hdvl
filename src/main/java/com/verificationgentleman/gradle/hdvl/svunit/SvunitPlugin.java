package com.verificationgentleman.gradle.hdvl.svunit;

import com.verificationgentleman.gradle.hdvl.SourceSet;
import com.verificationgentleman.gradle.hdvl.SystemVerilogPlugin;
import org.gradle.api.Action;
import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.reflect.TypeOf;

import java.io.File;

public class SvunitPlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        project.getPluginManager().apply(SystemVerilogPlugin.class);
        NamedDomainObjectContainer<SourceSet> sourceSets = project.getExtensions()
                .getByType(new TypeOf<NamedDomainObjectContainer<SourceSet>>() {});
        final SourceSet testSourceSet = sourceSets.create("test");
        configureTestTask(project, testSourceSet);
    }

    private void configureTestTask(Project project, SourceSet testSourceSet) {
        project.getTasks().register("test", TestTask.class, new Action<TestTask>() {
            @Override
            public void execute(TestTask testTask) {
                testTask.setDescription("Runs the unit tests using SVUnit.");
                testTask.setSource(testSourceSet.getSv());
                testTask.setTestsRoot(testSourceSet.getSv().getSourceDirectories().getSingleFile());
                testTask.getDestination().set(new File(project.getBuildDir(), "svunit/tests"));
            }
        });
    }
}
