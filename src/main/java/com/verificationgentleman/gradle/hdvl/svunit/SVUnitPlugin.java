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

import com.verificationgentleman.gradle.hdvl.GenFullArgsFile;
import com.verificationgentleman.gradle.hdvl.SourceSet;
import com.verificationgentleman.gradle.hdvl.svunit.internal.DefaultToolChains;
import com.verificationgentleman.gradle.hdvl.systemverilog.SystemVerilogPlugin;
import com.verificationgentleman.gradle.hdvl.systemverilog.SystemVerilogSourceSet;
import org.gradle.api.Action;
import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.internal.HasConvention;
import org.gradle.api.reflect.TypeOf;

import java.io.File;

public class SVUnitPlugin implements Plugin<Project> {

    private ToolChains toolChains;

    @Override
    public void apply(Project project) {
        project.getPluginManager().apply(SystemVerilogPlugin.class);
        NamedDomainObjectContainer<SourceSet> sourceSets = project.getExtensions()
                .getByType(new TypeOf<NamedDomainObjectContainer<SourceSet>>() {});
        final SourceSet testSourceSet = sourceSets.create("test");

        configureConfiguration(project);
        configureToolChain(project);

        sourceSets.named("test", new Action<SourceSet>() {
            @Override
            public void execute(SourceSet sourceSet) {
                HasConvention sourceSetWithConvention = (HasConvention) sourceSet;
                SystemVerilogSourceSet svSourceSet
                        = (SystemVerilogSourceSet) sourceSetWithConvention.getConvention().getPlugins().get("sv");
                configureTestWithXrunTask(project, svSourceSet);
            }
        });
    }

    private void configureConfiguration(Project project) {
        Configuration testCompileConfiguration = project.getConfigurations().create("testCompile");
    }

    private void configureToolChain(Project project) {
        toolChains = project.getExtensions().create(ToolChains.class, "toolChains", DefaultToolChains.class);
    }

    private void configureTestWithXrunTask(Project project, SystemVerilogSourceSet testSourceSet) {
        GenFullArgsFile genFullArgsFile = (GenFullArgsFile) project.getTasks().getByName("genFullXrunArgsFile");
        Configuration testCompileConfiguration = project.getConfigurations().getByName("testCompile");
        project.getTasks().register("testWithXrun", TestTask.class, new Action<TestTask>() {
            @Override
            public void execute(TestTask testTask) {
                testTask.setDescription("Runs the unit tests using SVUnit.");
                testTask.getMainArgsFile().set(genFullArgsFile.getDestination());
                testTask.setTestsRoot(testSourceSet.getSv().getSourceDirectories().getSingleFile());
                testTask.setSvunitRoot(testCompileConfiguration);
                testTask.getWorkingDir().set(new File(project.getBuildDir(), "svunit"));
                testTask.getExtraArgs().set(toolChains.getRunSVUnit().getArgs());
            }
        });
    }
}
