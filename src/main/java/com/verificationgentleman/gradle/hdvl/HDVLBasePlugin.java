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
        project.getPluginManager().apply("base");

        final DefaultHDVLPluginExtension extension = new DefaultHDVLPluginExtension(project);
        project.getExtensions().add("hdvl", extension);
        project.getExtensions().add("sourceSets", extension.getSourceSets());
        SourceSet mainSourceSet = extension.getSourceSets().create("main");

        configureCompileConfiguration(project);

        String[] toolNames = {"Xrun", "Qrun"};
        for (String toolName: toolNames) {
            extension.getSourceSets().all(new Action<SourceSet>() {
                @Override
                public void execute(SourceSet sourceSet) {
                    configureGenArgsFile(project, sourceSet, toolName);
                    configureArgsFilesConfiguration(project, sourceSet, toolName);
                    configureGenFullArgsFile(project, sourceSet, toolName);
                }
            });

            configureCompileArtifact(project, mainSourceSet, toolName);
        }
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
                genArgsFile.getDestination().set(new File(project.getBuildDir(), Names.of(sourceSet.getName()).getArgsFileName(toolName)));
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

    private void configureGenFullArgsFile(Project project, SourceSet sourceSet, String toolName) {
        AbstractGenArgsFile genArgsFile = (AbstractGenArgsFile) project.getTasks()
                .getByName(sourceSet.getGenArgsFileTaskName(toolName));
        project.getTasks().register(sourceSet.getGenFullArgsFileTaskName(toolName), GenFullArgsFile.class, new Action<GenFullArgsFile>() {
            @Override
            public void execute(GenFullArgsFile genFullArgsFile) {
                genFullArgsFile.setDescription("Generates an argument file for the " + sourceSet.getName() + " source code and its dependencies.");
                genFullArgsFile.getSource().set(genArgsFile.getDestination());
                genFullArgsFile.getDestination().set(new File(project.getBuildDir(), Names.of(sourceSet.getName()).getFullArgsFileName(toolName)));
                genFullArgsFile.setArgsFiles(project.getConfigurations().getByName(sourceSet.getArgsFilesConfigurationName(toolName)));
            }
        });
    }

    private void configureCompileConfiguration(Project project) {
        Configuration compileConfiguration = project.getConfigurations().create("compile");
        compileConfiguration.setCanBeConsumed(false);
        compileConfiguration.setCanBeResolved(false);

        project.getDependencies().getAttributesSchema().attribute(TOOL_ATTRIBUTE);
    }

    private void configureArgsFilesConfiguration(Project project, SourceSet sourceSet, String toolName) {
        Configuration argsFiles = project.getConfigurations().create(sourceSet.getArgsFilesConfigurationName(toolName));
        argsFiles.extendsFrom(project.getConfigurations().getByName("compile"));
        argsFiles.setCanBeConsumed(true);
        argsFiles.setCanBeResolved(true);
        argsFiles.getAttributes().attribute(HDVLBasePlugin.TOOL_ATTRIBUTE, toolName);
    }

    private void configureCompileArtifact(Project project, SourceSet mainSourceSet, String toolName) {
        AbstractGenArgsFile genArgsFile
                = (AbstractGenArgsFile) project.getTasks().getByName(mainSourceSet.getGenArgsFileTaskName(toolName));
        Action<ConfigurablePublishArtifact> configureAction = new Action<ConfigurablePublishArtifact>() {
            @Override
            public void execute(ConfigurablePublishArtifact configurablePublishArtifact) {
                configurablePublishArtifact.builtBy(genArgsFile);
            }
        };
        project.getArtifacts().add(mainSourceSet.getArgsFilesConfigurationName(toolName), genArgsFile.getDestination(), configureAction);
    }

}
