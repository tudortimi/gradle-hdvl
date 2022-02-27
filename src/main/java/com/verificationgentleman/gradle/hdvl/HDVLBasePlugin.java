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
import com.verificationgentleman.gradle.hdvl.internal.Names;
import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.ConfigurablePublishArtifact;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.attributes.Attribute;

import java.io.File;

public class HDVLBasePlugin implements Plugin<Project> {

    public static final Attribute<String> TOOL_ATTRIBUTE
            = Attribute.of("com.verificationgentlenan.gradle.hdvl.tool", String.class);

    @Override
    public void apply(Project project) {
        final DefaultHDVLPluginExtension extension = new DefaultHDVLPluginExtension(project);
        project.getExtensions().add("hdvl", extension);
        project.getExtensions().add("sourceSets", extension.getSourceSets());
        extension.getSourceSets().create("main");

        extension.getSourceSets().all(new Action<SourceSet>() {
            @Override
            public void execute(SourceSet sourceSet) {
                configureGenArgsFile(project, sourceSet, "Xrun");
                configureGenArgsFile(project, sourceSet, "Qrun");
            }
        });

        configureGenFullXrunArgsFile(project);
        configureGenFullQrunArgsFile(project);
        configureConfigurations(project);
        configureXrunCompileArtifact(project);
        configureQrunCompileArtifact(project);
    }

    private void configureGenArgsFile(Project project, SourceSet sourceSet, String toolName) {
        String taskName = sourceSet.getGenArgsFileTaskName(toolName);
        project.getTasks().register(taskName, getGenArgsFileClass(toolName), new Action<AbstractGenArgsFile>() {
            @Override
            public void execute(AbstractGenArgsFile genArgsFile) {
                genArgsFile.setDescription("Generates an argument file for the " + sourceSet.getName()
                        + " source code.");
                genArgsFile.setSource(project.files().getAsFileTree());
                genArgsFile.setPrivateIncludeDirs(project.files().getAsFileTree());
                genArgsFile.setExportedIncludeDirs(project.files().getAsFileTree());
                genArgsFile.setCSource(project.files().getAsFileTree());
                genArgsFile.getDestination().set(new File(project.getBuildDir(), sourceSet.getArgsFileName(toolName)));
            }
        });
    }

    // TODO Handle in a generic way eventually
    private Class<? extends AbstractGenArgsFile> getGenArgsFileClass(String toolName) {
        switch (toolName) {
            case "Xrun":
                return GenXrunArgsFile.class;
            case "Qrun":
                return GenQrunArgsFile.class;
        }
        throw new IllegalArgumentException("Unexpected tool name: " + toolName);
    }

    private void configureGenFullXrunArgsFile(Project project) {
        AbstractGenArgsFile genXrunArgsFile = (AbstractGenArgsFile) project.getTasks().getByName("genXrunArgsFile");
        project.getTasks().register("genFullXrunArgsFile", GenFullArgsFile.class, new Action<GenFullArgsFile>() {
            @Override
            public void execute(GenFullArgsFile genFullArgsFile) {
                genFullArgsFile.setDescription("Generates an argument file for the main source code and its dependencies.");
                genFullArgsFile.getSource().set(genXrunArgsFile.getDestination());
                genFullArgsFile.getDestination().set(new File(project.getBuildDir(), "full_xrun_args.f"));
                genFullArgsFile.setArgsFiles(project.getConfigurations().getByName("xrunArgsFiles"));
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

        project.getDependencies().getAttributesSchema().attribute(TOOL_ATTRIBUTE);

        configureArgsFilesConfiguration(project, compileConfiguration, "Xrun");
        configureArgsFilesConfiguration(project, compileConfiguration, "Qrun");
    }

    private void configureArgsFilesConfiguration(Project project, Configuration compileConfiguration, String toolName) {
        Configuration argsFiles = project.getConfigurations().create(Names.getArgsFilesConfigurationName(toolName));
        argsFiles.extendsFrom(compileConfiguration);
        argsFiles.setCanBeConsumed(true);
        argsFiles.setCanBeResolved(true);
        argsFiles.getAttributes().attribute(HDVLBasePlugin.TOOL_ATTRIBUTE, toolName);
    }

    private void configureXrunCompileArtifact(Project project) {
        AbstractGenArgsFile genXrunArgsFile = (AbstractGenArgsFile) project.getTasks().getByName("genXrunArgsFile");
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
