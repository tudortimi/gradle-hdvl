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
import com.verificationgentleman.gradle.hdvl.internal.UnzipAndWriteXrunArgsFile;
import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.ConfigurablePublishArtifact;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.transform.TransformParameters;
import org.gradle.api.artifacts.transform.TransformSpec;
import org.gradle.api.artifacts.type.ArtifactTypeDefinition;
import org.gradle.api.attributes.Attribute;
import org.gradle.api.component.AdhocComponentWithVariants;
import org.gradle.api.component.SoftwareComponentFactory;
import org.gradle.api.publish.PublishingExtension;
import org.gradle.api.publish.maven.MavenPublication;
import org.gradle.api.tasks.bundling.Zip;

import javax.inject.Inject;
import java.io.File;

public class HDVLBasePlugin implements Plugin<Project> {

    public static final Attribute<String> TOOL_ATTRIBUTE
            = Attribute.of("com.verificationgentlenan.gradle.hdvl.tool", String.class);
    public static final Attribute<String> HDVL_USAGE_ATTRIBUTE
            = Attribute.of("com.verificationgentlenan.gradle.hdvl.usage", String.class);

    private final SoftwareComponentFactory softwareComponentFactory;

    @Inject
    HDVLBasePlugin(SoftwareComponentFactory softwareComponentFactory) {
        this.softwareComponentFactory = softwareComponentFactory;
    }

    @Override
    public void apply(Project project) {
        project.getPluginManager().apply("base");

        final DefaultHDVLPluginExtension extension = new DefaultHDVLPluginExtension(project);
        project.getExtensions().add("hdvl", extension);
        project.getExtensions().add("sourceSets", extension.getSourceSets());
        SourceSet mainSourceSet = extension.getSourceSets().create("main");

        extension.getSourceSets().all(sourceSet -> configureCompileConfiguration(project, sourceSet));

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

        configureHdvlSourceArchiveTask(project);
        configureHdvlSourcesArchiveArtifact(project, mainSourceSet);
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

    private void configureCompileConfiguration(Project project, SourceSet sourceSet) {
        Configuration compileConfiguration = project.getConfigurations().create(sourceSet.getCompileConfigurationName());
        compileConfiguration.setCanBeConsumed(false);
        compileConfiguration.setCanBeResolved(false);

        project.getDependencies().getAttributesSchema().attribute(TOOL_ATTRIBUTE);
        project.getDependencies().getArtifactTypes().register("zip").configure(new Action<ArtifactTypeDefinition>() {
            @Override
            public void execute(ArtifactTypeDefinition artifactTypeDefinition) {
                artifactTypeDefinition.getAttributes().attribute(TOOL_ATTRIBUTE, "None");
            }
        });
        project.getDependencies().registerTransform(UnzipAndWriteXrunArgsFile.class, new Action<TransformSpec<UnzipAndWriteXrunArgsFile.Parameters>>() {
            @Override
            public void execute(TransformSpec<UnzipAndWriteXrunArgsFile.Parameters> transformSpec) {
                transformSpec.getFrom().attribute(TOOL_ATTRIBUTE, "None");
                transformSpec.getTo().attribute(TOOL_ATTRIBUTE, "Xrun");
                transformSpec.getParameters().getUnzipRootDirectory().set(project.getLayout().getBuildDirectory().dir("unzipped-hdvl-source-archives"));
            }
        });
    }

    private void configureArgsFilesConfiguration(Project project, SourceSet sourceSet, String toolName) {
        Configuration argsFiles = project.getConfigurations().create(sourceSet.getArgsFilesConfigurationName(toolName));
        argsFiles.extendsFrom(project.getConfigurations().getByName(sourceSet.getCompileConfigurationName()));
        argsFiles.setCanBeConsumed(sourceSet.getName().equals("main"));
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

    private void configureHdvlSourceArchiveTask(Project project) {
        project.getTasks().register("hdvlSourcesArchive", Zip.class, zip -> {
            zip.getDestinationDirectory().convention(project.getLayout().getBuildDirectory());
            zip.getArchiveFileName().convention("hdvl-sources.zip");
        });
    }

    private void configureHdvlSourcesArchiveArtifact(Project project, SourceSet mainSourceSet) {
        Configuration hdvlSourcesArchiveElements = project.getConfigurations().create("hdvlSourcesArchiveElements");
        hdvlSourcesArchiveElements.setCanBeConsumed(true);
        hdvlSourcesArchiveElements.setCanBeResolved(false);
        hdvlSourcesArchiveElements.getAttributes().attribute(HDVLBasePlugin.HDVL_USAGE_ATTRIBUTE, "HdvlSourcesArchive");

        project.getTasks().named("hdvlSourcesArchive").configure(task -> {
            Zip hdvlSourcesArchive = (Zip) task;
            project.getArtifacts().add(hdvlSourcesArchiveElements.getName(), hdvlSourcesArchive.getArchiveFile(), artifact -> {
                artifact.builtBy(hdvlSourcesArchive);
                maybeConfigureHdvlSourcesArchiveArtifactPublishing(project, hdvlSourcesArchiveElements);
            });
        });
    }

    private void maybeConfigureHdvlSourcesArchiveArtifactPublishing(Project project, Configuration hdvlSourcesArchiveElements) {
        project.getPluginManager().withPlugin("maven-publish", appliedPlugin -> {
            PublishingExtension publishing = (PublishingExtension) project.getExtensions().getByName("publishing");
            publishing.getPublications().create("hdvlLibrary", MavenPublication.class, mavenPublication -> {
                mavenPublication.setArtifactId(project.getName());
                project.afterEvaluate(p -> {
                    mavenPublication.setGroupId(project.getGroup().toString());
                    mavenPublication.setVersion(project.getVersion().toString());
                });

                AdhocComponentWithVariants hdvlLibrary =  softwareComponentFactory.adhoc("hdvlLibrary");
                hdvlLibrary.addVariantsFromConfiguration(hdvlSourcesArchiveElements, configurationVariantDetails -> {});
                mavenPublication.from(hdvlLibrary);
            });
        });
    }

}
