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
package com.verificationgentleman.gradle.hdvl;

import com.verificationgentleman.gradle.hdvl.internal.DefaultHDVLPluginExtension;
import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.ConfigurablePublishArtifact;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.attributes.Attribute;

import java.io.File;

public class HDVLBasePlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        final DefaultHDVLPluginExtension extension = new DefaultHDVLPluginExtension(project);
        project.getExtensions().add("hdvl", extension);
        project.getExtensions().add("sourceSets", extension.getSourceSets());
        extension.getSourceSets().create("main");

        extension.getSourceSets().all(new Action<SourceSet>() {
            @Override
            public void execute(SourceSet sourceSet) {
                configureGenXrunArgsFile(project, sourceSet);
                configureGenQrunArgsFile(project, sourceSet);
            }
        });

        configureGenFullArgsFile(project);
        configureGenFullQrunArgsFile(project);
        configureConfigurations(project);
        configureXrunCompileArtifact(project);
        configureQrunCompileArtifact(project);
    }

    private void configureGenXrunArgsFile(Project project, SourceSet sourceSet) {
        String taskName = sourceSet.getGenXrunArgsFileTaskName();
        project.getTasks().register(taskName, GenXrunArgsFile.class, new Action<GenXrunArgsFile>() {
            @Override
            public void execute(GenXrunArgsFile genXrunArgsFile) {
                genXrunArgsFile.setDescription("Generates an argument file for the " + sourceSet.getName()
                        + " source code.");
                genXrunArgsFile.setSource(project.files().getAsFileTree());
                genXrunArgsFile.setPrivateIncludeDirs(project.files().getAsFileTree());
                genXrunArgsFile.setExportedIncludeDirs(project.files().getAsFileTree());
                genXrunArgsFile.setCSource(project.files().getAsFileTree());
                genXrunArgsFile.getDestination().set(new File(project.getBuildDir(), sourceSet.getArgsFileName()));
            }
        });
    }

    private void configureGenFullArgsFile(Project project) {
        GenXrunArgsFile genXrunArgsFile = (GenXrunArgsFile) project.getTasks().getByName("genXrunArgsFile");
        project.getTasks().register("genFullArgsFile", GenFullArgsFile.class, new Action<GenFullArgsFile>() {
            @Override
            public void execute(GenFullArgsFile genFullArgsFile) {
                genFullArgsFile.setDescription("Generates an argument file for the main source code and its dependencies.");
                genFullArgsFile.getSource().set(genXrunArgsFile.getDestination());
                genFullArgsFile.getDestination().set(new File(project.getBuildDir(), "full_args.f"));
                genFullArgsFile.setArgsFiles(project.getConfigurations().getByName("xrunArgsFiles"));
            }
        });
    }

    private void configureGenQrunArgsFile(Project project, SourceSet sourceSet) {
        String taskName = sourceSet.getGenQrunArgsFileTaskName();
        project.getTasks().register(taskName, GenQrunArgsFile.class, new Action<GenQrunArgsFile>() {
            @Override
            public void execute(GenQrunArgsFile genQrunArgsFile) {
                genQrunArgsFile.setDescription("Generates a 'qrun' argument file for the " + sourceSet.getName()
                        + " source code.");
                genQrunArgsFile.setSource(project.files().getAsFileTree());
                genQrunArgsFile.setPrivateIncludeDirs(project.files().getAsFileTree());
                genQrunArgsFile.setExportedIncludeDirs(project.files().getAsFileTree());
                genQrunArgsFile.setCSource(project.files().getAsFileTree());
                genQrunArgsFile.getDestination().set(new File(project.getBuildDir(), sourceSet.getQrunArgsFileName()));
            }
        });
    }

    private void configureGenFullQrunArgsFile(Project project) {
        AbstractGenArgsFile genQrunArgsFile = (AbstractGenArgsFile) project.getTasks().getByName("genQrunArgsFile");
        project.getTasks().register("genFullQrunArgsFile", GenFullArgsFile.class, new Action<GenFullArgsFile>() {
            @Override
            public void execute(GenFullArgsFile genFullArgsFile) {
                genFullArgsFile.setDescription("Generates a qrun argument file for the main source code and its dependencies.");
                genFullArgsFile.getSource().set(genQrunArgsFile.getDestination());
                genFullArgsFile.getDestination().set(new File(project.getBuildDir(), "full_qrun_args.f"));
                genFullArgsFile.setArgsFiles(project.getConfigurations().getByName("qrunArgsFiles"));
            }
        });
    }

    private void configureConfigurations(Project project) {
        Configuration compileConfiguration = project.getConfigurations().create("compile");
        compileConfiguration.setCanBeConsumed(false);
        compileConfiguration.setCanBeResolved(false);

        Attribute<String> tool = Attribute.of("com.verificationgentlenan.gradle.hdvl.tool", String.class);
        project.getDependencies().getAttributesSchema().attribute(tool);

        Configuration xrunArgsFiles = project.getConfigurations().create("xrunArgsFiles");
        xrunArgsFiles.extendsFrom(compileConfiguration);
        xrunArgsFiles.setCanBeConsumed(true);
        xrunArgsFiles.setCanBeResolved(true);
        xrunArgsFiles.getAttributes().attribute(tool, "Xrun");

        Configuration qrunArgsFiles = project.getConfigurations().create("qrunArgsFiles");
        qrunArgsFiles.extendsFrom(compileConfiguration);
        qrunArgsFiles.setCanBeConsumed(true);
        qrunArgsFiles.setCanBeResolved(true);
        qrunArgsFiles.getAttributes().attribute(tool, "Qrun");
    }

    private void configureXrunCompileArtifact(Project project) {
        GenXrunArgsFile genXrunArgsFile = (GenXrunArgsFile) project.getTasks().getByName("genXrunArgsFile");
        Action<ConfigurablePublishArtifact> configureAction = new Action<ConfigurablePublishArtifact>() {
            @Override
            public void execute(ConfigurablePublishArtifact configurablePublishArtifact) {
                configurablePublishArtifact.builtBy(genXrunArgsFile);
            }
        };
        project.getArtifacts().add("xrunArgsFiles", genXrunArgsFile.getDestination(), configureAction);
    }

    private void configureQrunCompileArtifact(Project project) {
        AbstractGenArgsFile genArgsFile = (AbstractGenArgsFile) project.getTasks().getByName("genQrunArgsFile");
        Action<ConfigurablePublishArtifact> configureAction = new Action<ConfigurablePublishArtifact>() {
            @Override
            public void execute(ConfigurablePublishArtifact configurablePublishArtifact) {
                configurablePublishArtifact.builtBy(genArgsFile);
            }
        };
        project.getArtifacts().add("qrunArgsFiles", genArgsFile.getDestination(), configureAction);
    }
}
