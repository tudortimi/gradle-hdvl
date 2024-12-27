/*
 * Copyright 2021-2024 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.verificationgentleman.gradle.hdvl.dvt;

import com.verificationgentleman.gradle.hdvl.GenFullArgsFile;
import com.verificationgentleman.gradle.hdvl.HDVLBasePlugin;
import com.verificationgentleman.gradle.hdvl.SourceSet;
import com.verificationgentleman.gradle.hdvl.systemverilog.SystemVerilogSourceSet;
import org.gradle.api.Action;
import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.internal.HasConvention;
import org.gradle.api.plugins.AppliedPlugin;
import org.gradle.api.reflect.TypeOf;

import java.io.File;

public class DVTPlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        project.getPluginManager().apply(HDVLBasePlugin.class);

        project.getTasks().register("dvt", DVTTask.class, new Action<DVTTask>() {
            @Override
            public void execute(DVTTask dvt) {
                dvt.setDescription("Generates a DVT project.");
                setArgsFile(dvt);
                maybeConfigureTests(dvt);
            }

            private void setArgsFile(DVTTask dvt) {
                GenFullArgsFile genFullArgsFile
                        = dvt.getProject().getTasks().withType(GenFullArgsFile.class).getByName("genFullXrunArgsFile");
                dvt.getArgsFile().set(genFullArgsFile.getDestination());
            }

            private void maybeConfigureTests(DVTTask dvt) {
                project.getPluginManager().withPlugin("com.verificationgentleman.gradle.hdvl.svunit", new Action<AppliedPlugin>() {
                    @Override
                    public void execute(AppliedPlugin appliedPlugin) {
                        dvt.setTestsRoot(getTestSourceSet().getSv().getSourceDirectories().getSingleFile());
                        dvt.getWorkingDir().set(new File(project.getBuildDir(), "dvt/svunit"));
                    }

                    private SystemVerilogSourceSet getTestSourceSet() {
                        NamedDomainObjectContainer<SourceSet> sourceSets = project.getExtensions()
                            .getByType(new TypeOf<NamedDomainObjectContainer<SourceSet>>() {});
                        HasConvention sourceSetWithConvention = (HasConvention) sourceSets.getByName("test");
                        return (SystemVerilogSourceSet) sourceSetWithConvention.getConvention().getPlugins().get("sv");
                    }
                });
            }
        });
    }
}
