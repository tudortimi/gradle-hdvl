/*
 * Copyright 2020-2022 the original author or authors.
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

import com.verificationgentleman.gradle.hdvl.AbstractGenArgsFile;
import com.verificationgentleman.gradle.hdvl.HDVLBasePlugin;
import com.verificationgentleman.gradle.hdvl.HDVLPluginExtension;
import com.verificationgentleman.gradle.hdvl.SourceSet;
import com.verificationgentleman.gradle.hdvl.internal.WriteCompileSpecFile;
import com.verificationgentleman.gradle.hdvl.systemverilog.internal.DefaultSystemVerilogSourceSet;
import org.gradle.api.Action;
import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.file.DuplicatesStrategy;
import org.gradle.api.internal.plugins.DslObject;
import org.gradle.api.tasks.bundling.Zip;

import java.io.File;

public class SystemVerilogPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        project.getPluginManager().apply(HDVLBasePlugin.class);
        NamedDomainObjectContainer<SourceSet> sourceSets
                = project.getExtensions().getByType(HDVLPluginExtension.class).getSourceSets();
        sourceSets.all(new Action<SourceSet>() {
            @Override
            public void execute(SourceSet sourceSet) {
                final DefaultSystemVerilogSourceSet svSourceSet = new DefaultSystemVerilogSourceSet(
                        sourceSet.getName(), project.getObjects());

                // XXX WORKAROUND Not part of the public API
                new DslObject(sourceSet).getConvention().getPlugins().put("sv", svSourceSet);

                String[] toolNames = {"Xrun", "Qrun"};
                for (String toolName: toolNames) {
                    AbstractGenArgsFile genArgsFile
                        = (AbstractGenArgsFile) project.getTasks().getByName(sourceSet.getGenArgsFileTaskName(toolName));
                    configureSources(genArgsFile, svSourceSet);
                }

                if (sourceSet.getName() == "main") {
                    project.getTasks().withType(WriteCompileSpecFile.class, task -> {
                        task.getSvSource().from(svSourceSet.getSv());
                        task.getSvPrivateIncludeDirs().from(svSourceSet.getSv().getSourceDirectories().filter(File::exists));
                        task.getSvExportedHeaderDirs().from(svSourceSet.getSvHeaders().getSourceDirectories().filter(File::exists));
                    });
                    project.getTasks().getByName("hdvlSourcesArchive", task -> {
                        Zip hdvlSourcesArchive = (Zip) task;
                        hdvlSourcesArchive.from(svSourceSet.getSv(), it -> {
                            it.eachFile(file -> {
                                file.setPath(project.relativePath(file.getFile()));
                            });
                        });

                        hdvlSourcesArchive.setDuplicatesStrategy(DuplicatesStrategy.EXCLUDE);

                        // FIXME Implement proper handling of SV private headers
                        hdvlSourcesArchive.from(svSourceSet.getSv().getSourceDirectories().getElements().map(project::files), it -> {
                            it.eachFile(file -> {
                                file.setPath(project.relativePath(file.getFile()));
                            });
                        });

                        // FIXME Implement proper handling of SV exported headers
                        hdvlSourcesArchive.from(svSourceSet.getSvHeaders().getSourceDirectories().getElements().map(project::files), it -> {
                            it.eachFile(file -> {
                                file.setPath(project.relativePath(file.getFile()));
                            });
                        });
                    });
                }
            }
        });
    }

    private void configureSources(AbstractGenArgsFile genArgsFile, SystemVerilogSourceSet svSourceSet) {
        genArgsFile.setSource(svSourceSet.getSv());
        genArgsFile.getFirst().set(svSourceSet.getSv().getFirst());
        genArgsFile.setPrivateIncludeDirs(svSourceSet.getSv().getSourceDirectories());
        genArgsFile.setExportedIncludeDirs(svSourceSet.getSvHeaders().getSourceDirectories());
    }

}
