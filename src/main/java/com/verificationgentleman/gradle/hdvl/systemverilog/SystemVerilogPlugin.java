/*
 * Copyright 2020 the original author or authors.
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
package com.verificationgentleman.gradle.hdvl.systemverilog;

import com.verificationgentleman.gradle.hdvl.HDVLBasePlugin;
import com.verificationgentleman.gradle.hdvl.SourceSet;
import org.gradle.api.Action;
import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.ConfigurablePublishArtifact;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.Dependency;

import java.io.File;

public class SystemVerilogPlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        project.getPluginManager().apply(HDVLBasePlugin.class);
        NamedDomainObjectContainer<SourceSet> sourceSets
                = (NamedDomainObjectContainer<SourceSet>) project.getExtensions().getByName("sourceSets");
	    final SourceSet mainSourceSet = sourceSets.getByName("main");
	    configureGenArgsFile(project, mainSourceSet);
        configureGenFullArgsFile(project);
	    configureConfigurations(project);
	    configureCompileArtifact(project);
    }

    private void configureGenArgsFile(Project project, SourceSet mainSourceSet) {
        project.getTasks().register("genArgsFile", GenArgsFile.class, new Action<GenArgsFile>() {
            @Override
            public void execute(GenArgsFile genArgsFile) {
                genArgsFile.setDescription("Generates an argument file for the main source code.");
                genArgsFile.setSource(mainSourceSet.getSv());
                genArgsFile.setPrivateIncludeDirs(mainSourceSet.getSv().getSourceDirectories());
                genArgsFile.setExportedIncludeDirs(mainSourceSet.getSvHeaders().getSourceDirectories());
                genArgsFile.setCSource(project.files().getAsFileTree());
                genArgsFile.getDestination().set(new File(project.getBuildDir(), "args.f"));
            }
        });
    }

    private void configureGenFullArgsFile(Project project) {
        GenArgsFile genArgsFile = (GenArgsFile) project.getTasks().getByName("genArgsFile");
        project.getTasks().register("genFullArgsFile", GenFullArgsFile.class, new Action<GenFullArgsFile>() {
            @Override
            public void execute(GenFullArgsFile genFullArgsFile) {
                genFullArgsFile.setDescription("Generates an argument file for the main source code and its dependencies.");
                genFullArgsFile.getSource().set(genArgsFile.getDestination());
                genFullArgsFile.getDestination().set(new File(project.getBuildDir(), "full_args.f"));
                genFullArgsFile.setArgsFiles(project.getConfigurations().getByName("compile"));
            }
        });
    }

    private void configureConfigurations(Project project) {
        Configuration compileConfiguration = project.getConfigurations().create("compile");

        Configuration defaultConfiguration = project.getConfigurations().create(Dependency.DEFAULT_CONFIGURATION);
        defaultConfiguration.extendsFrom(compileConfiguration);
    }

    private void configureCompileArtifact(Project project) {
        GenArgsFile genArgsFile = (GenArgsFile) project.getTasks().getByName("genArgsFile");
        Action<ConfigurablePublishArtifact> configureAction = new Action<>() {
            @Override
            public void execute(ConfigurablePublishArtifact configurablePublishArtifact) {
                configurablePublishArtifact.builtBy(genArgsFile);
            }
        };
        project.getArtifacts().add("default", genArgsFile.getDestination(), configureAction);
    }
}