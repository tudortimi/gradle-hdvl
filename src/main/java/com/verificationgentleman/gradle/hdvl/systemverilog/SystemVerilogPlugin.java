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

import com.verificationgentleman.gradle.hdvl.GenArgsFile;
import com.verificationgentleman.gradle.hdvl.HDVLBasePlugin;
import com.verificationgentleman.gradle.hdvl.SourceSet;
import com.verificationgentleman.gradle.hdvl.systemverilog.internal.DefaultSystemVerilogSourceSet;
import org.gradle.api.Action;
import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.internal.plugins.DslObject;

public class SystemVerilogPlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        project.getPluginManager().apply(HDVLBasePlugin.class);
        NamedDomainObjectContainer<SourceSet> sourceSets
                = (NamedDomainObjectContainer<SourceSet>) project.getExtensions().getByName("sourceSets");
        sourceSets.all(new Action<SourceSet>() {
            @Override
            public void execute(SourceSet sourceSet) {
                final DefaultSystemVerilogSourceSet svSourceSet = new DefaultSystemVerilogSourceSet(
                        sourceSet.getName(), project.getObjects());

                // XXX WORKAROUND Not part of the public API
                new DslObject(sourceSet).getConvention().getPlugins().put("sv", svSourceSet);

                // TODO Need one 'genArgsFile' task per source set
                if (sourceSet.getName() == "main")
                    configureGenArgsFile(project, svSourceSet);
            }
        });
    }

    private void configureGenArgsFile(Project project, SystemVerilogSourceSet mainSourceSet) {
        GenArgsFile genArgsFile = (GenArgsFile) project.getTasks().getByName("genArgsFile");
        genArgsFile.setSource(mainSourceSet.getSv());
        genArgsFile.setPrivateIncludeDirs(mainSourceSet.getSv().getSourceDirectories());
        genArgsFile.setExportedIncludeDirs(mainSourceSet.getSvHeaders().getSourceDirectories());
    }
}
