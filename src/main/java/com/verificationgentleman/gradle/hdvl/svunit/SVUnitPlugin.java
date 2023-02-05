/*
 * Copyright 2021-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.verificationgentleman.gradle.hdvl.svunit;

import com.verificationgentleman.gradle.hdvl.AbstractGenArgsFile;
import com.verificationgentleman.gradle.hdvl.GenFullArgsFile;
import com.verificationgentleman.gradle.hdvl.HDVLBasePlugin;
import com.verificationgentleman.gradle.hdvl.SourceSet;
import com.verificationgentleman.gradle.hdvl.internal.Names;
import com.verificationgentleman.gradle.hdvl.svunit.internal.DefaultToolChains;
import com.verificationgentleman.gradle.hdvl.systemverilog.SystemVerilogPlugin;
import com.verificationgentleman.gradle.hdvl.systemverilog.SystemVerilogSourceSet;
import org.gradle.api.Action;
import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.internal.HasConvention;
import org.gradle.api.reflect.TypeOf;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.language.base.plugins.LifecycleBasePlugin;
import org.gradle.util.GUtil;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class SVUnitPlugin implements Plugin<Project> {

    private ToolChains toolChains;

    @Override
    public void apply(Project project) {
        project.getPluginManager().apply(SystemVerilogPlugin.class);
        NamedDomainObjectContainer<SourceSet> sourceSets = project.getExtensions()
                .getByType(new TypeOf<NamedDomainObjectContainer<SourceSet>>() {});
        final SourceSet testSourceSet = sourceSets.create("test");

        configureSVUnitRootConfiguration(project);

        String[] toolNames = {"Xrun", "Qrun"};
        for (String toolName: toolNames) {
            configureArgsFilesConfiguration(project, testSourceSet, toolName);
        }

        configureToolChain(project);

        sourceSets.named("test", new Action<SourceSet>() {
            @Override
            public void execute(SourceSet sourceSet) {
                HasConvention sourceSetWithConvention = (HasConvention) sourceSet;
                SystemVerilogSourceSet svSourceSet
                        = (SystemVerilogSourceSet) sourceSetWithConvention.getConvention().getPlugins().get("sv");

                ignoreUnitTests(svSourceSet);

                String[] toolNames = {"Xrun", "Qrun"};
                for (String toolName: toolNames) {
                    configureTestTask(project, sourceSets.getByName("main"), sourceSet, svSourceSet, toolName);
                }
            }
        });
    }

    private void configureSVUnitRootConfiguration(Project project) {
        Configuration svUnitRoot = project.getConfigurations().create("svUnitRoot");
        svUnitRoot.setCanBeConsumed(false);
        svUnitRoot.setCanBeResolved(true);
        project.getConfigurations().getByName("testCompile").getDependencies().whenObjectAdded(dependency -> {
            if (isSVUnit(dependency)) {
                svUnitRoot.getDependencies().add(dependency);
            }
        });
        project.getConfigurations().getByName("testCompile").getDependencies().whenObjectRemoved(dependency -> {
            if (isSVUnit(dependency)) {
                svUnitRoot.getDependencies().remove(dependency);
            }
        });
    }

    private boolean isSVUnit(Dependency dependency) {
        return dependency.getGroup().equals("org.svunit") && dependency.getName().equals("svunit");
    }

    private void configureArgsFilesConfiguration(Project project, SourceSet testSourceSet, String toolName) {
        Configuration argsFiles = project.getConfigurations().getByName(testSourceSet.getArgsFilesConfigurationName(toolName));
        argsFiles.exclude(getExcludeForSVUnit());
    }

    private Map<String, String> getExcludeForSVUnit() {
        HashMap<String, String> exclude = new HashMap<>();
        exclude.put("group", "org.svunit");
        exclude.put("module", "svunit");
        return exclude;
    }

    private void configureToolChain(Project project) {
        toolChains = project.getExtensions().create(ToolChains.class, "toolChains", DefaultToolChains.class);
    }

    private void ignoreUnitTests(SystemVerilogSourceSet svSourceSet) {
        svSourceSet.getSv().exclude("**/*_unit_test.sv");
    }

    private void configureTestTask(Project project, SourceSet mainSourceSet, SourceSet testSourceSet, SystemVerilogSourceSet testSvSourceSet, String toolName) {
        GenFullArgsFile genFullArgsFile
                = (GenFullArgsFile) project.getTasks().getByName(mainSourceSet.getGenFullArgsFileTaskName(toolName));
        GenFullArgsFile genFullTestArgsFile
                = (GenFullArgsFile) project.getTasks().getByName(testSourceSet.getGenFullArgsFileTaskName(toolName));
        Configuration svUnitRoot = project.getConfigurations().getByName("svUnitRoot");
        TaskProvider<TestTask> testTask = project.getTasks().register(Names.getTestTaskName(toolName), TestTask.class, new Action<TestTask>() {
            @Override
            public void execute(TestTask testTask) {
                testTask.setGroup(LifecycleBasePlugin.VERIFICATION_GROUP);
                testTask.setDescription("Runs the unit tests using SVUnit.");
                testTask.getToolName().set(toolName.toLowerCase());
                testTask.getMainArgsFile().set(genFullArgsFile.getDestination());
                testTask.getTestArgsFile().set(genFullTestArgsFile.getDestination());
                testTask.setTestsRoot(testSvSourceSet.getSv().getSourceDirectories().getSingleFile());
                testTask.setSvunitRoot(svUnitRoot);
                testTask.getWorkingDir().set(new File(project.getBuildDir(), "svunit"));
                testTask.getExtraArgs().set(toolChains.getRunSVUnit().getArgs());
            }
        });
        project.getTasks().getByName("check").dependsOn(testTask);
    }

}
