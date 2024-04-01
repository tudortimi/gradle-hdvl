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

import com.verificationgentleman.gradle.hdvl.*;
import com.verificationgentleman.gradle.hdvl.internal.WriteCompileSpecFile;
import com.verificationgentleman.gradle.hdvl.systemverilog.internal.DefaultSystemVerilogSourceSet;
import org.gradle.api.Action;
import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.internal.plugins.DslObject;
import org.gradle.api.tasks.bundling.Zip;

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
                    });
                    project.getTasks().getByName("hdvlSourcesArchive", task -> {
                        Zip hdvlSourcesArchive = (Zip) task;
                        hdvlSourcesArchive.from(svSourceSet.getSv());
                        hdvlSourcesArchive.into("src/main/sv");  // FIXME Assumes source in conventional location
                    });
                }
            }
        });
    }

    private void configureSources(AbstractGenArgsFile genArgsFile, SystemVerilogSourceSet svSourceSet) {
        genArgsFile.setSource(svSourceSet.getSv());
        genArgsFile.setPrivateIncludeDirs(svSourceSet.getSv().getSourceDirectories());
        genArgsFile.setExportedIncludeDirs(svSourceSet.getSvHeaders().getSourceDirectories());
    }

}
