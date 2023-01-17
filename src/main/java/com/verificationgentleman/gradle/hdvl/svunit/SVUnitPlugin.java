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

        configureConfiguration(project);
        configureSVUnitRootConfiguration(project);

        String[] toolNames = {"Xrun", "Qrun"};
        for (String toolName: toolNames) {
            configureArgsFilesConfiguration(project, toolName);
            configureGenFullArgsFile(project, toolName);
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
                    configureTestTask(project, svSourceSet, toolName);
                }
            }
        });
    }

    private void configureConfiguration(Project project) {
        Configuration testCompileConfiguration = project.getConfigurations().create("testCompile");
        testCompileConfiguration.setCanBeConsumed(false);
        testCompileConfiguration.setCanBeResolved(false);
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

    private void configureArgsFilesConfiguration(Project project, String toolName) {
        Configuration argsFiles = project.getConfigurations().create(GUtil.toLowerCamelCase(toolName + "TestArgsFiles"));
        argsFiles.extendsFrom(project.getConfigurations().getByName("testCompile"));
        argsFiles.exclude(getExcludeForSVUnit());
        argsFiles.setCanBeConsumed(true);
        argsFiles.setCanBeResolved(true);
        argsFiles.getAttributes().attribute(HDVLBasePlugin.TOOL_ATTRIBUTE, toolName);
    }

    private Map<String, String> getExcludeForSVUnit() {
        HashMap<String, String> exclude = new HashMap<>();
        exclude.put("group", "org.svunit");
        exclude.put("module", "svunit");
        return exclude;
    }

    private void configureGenFullArgsFile(Project project, String toolName) {
        AbstractGenArgsFile genArgsFile = (AbstractGenArgsFile) project.getTasks()
            .getByName(Names.getGenArgsFileTaskName("test", toolName));
        project.getTasks().register("genFullTest" + toolName + "ArgsFile", GenFullArgsFile.class, new Action<GenFullArgsFile>() {
            @Override
            public void execute(GenFullArgsFile genFullArgsFile) {
                genFullArgsFile.setDescription("Generates an argument file for the test source code and its dependencies.");
                genFullArgsFile.getSource().set(genArgsFile.getDestination());
                genFullArgsFile.getDestination().set(new File(project.getBuildDir(), "full_test_" + toolName.toLowerCase() + "_args.f"));
                genFullArgsFile.setArgsFiles(project.getConfigurations().getByName(GUtil.toLowerCamelCase(toolName + "TestArgsFiles")));
            }
        });
    }

    private void configureToolChain(Project project) {
        toolChains = project.getExtensions().create(ToolChains.class, "toolChains", DefaultToolChains.class);
    }

    private void ignoreUnitTests(SystemVerilogSourceSet svSourceSet) {
        svSourceSet.getSv().exclude("**/*_unit_test.sv");
    }

    private void configureTestTask(Project project, SystemVerilogSourceSet testSourceSet, String toolName) {
        GenFullArgsFile genFullArgsFile
                = (GenFullArgsFile) project.getTasks().getByName(Names.getGenFullArgsFileTaskName(toolName));
        AbstractGenArgsFile genTestArgsFile
                = (AbstractGenArgsFile) project.getTasks().getByName(Names.getGenArgsFileTaskName("test", toolName));
        Configuration svUnitRoot = project.getConfigurations().getByName("svUnitRoot");
        project.getTasks().register(Names.getTestTaskName(toolName), TestTask.class, new Action<TestTask>() {
            @Override
            public void execute(TestTask testTask) {
                testTask.setDescription("Runs the unit tests using SVUnit.");
                testTask.getToolName().set(toolName.toLowerCase());
                testTask.getMainArgsFile().set(genFullArgsFile.getDestination());
                testTask.getTestArgsFile().set(genTestArgsFile.getDestination());
                testTask.setTestsRoot(testSourceSet.getSv().getSourceDirectories().getSingleFile());
                testTask.setSvunitRoot(svUnitRoot);
                testTask.getWorkingDir().set(new File(project.getBuildDir(), "svunit"));
                testTask.getExtraArgs().set(toolChains.getRunSVUnit().getArgs());
            }
        });
    }

}
