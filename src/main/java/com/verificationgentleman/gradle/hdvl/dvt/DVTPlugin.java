/*
 * Copyright 2021-2026 the original author or authors.
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
import com.verificationgentleman.gradle.hdvl.SourceSet;
import com.verificationgentleman.gradle.hdvl.dvt.internal.SVUnitSetup;
import com.verificationgentleman.gradle.hdvl.systemverilog.SystemVerilogSourceDirectorySet;
import org.gradle.api.*;
import org.gradle.api.reflect.TypeOf;

import java.io.File;

public class DVTPlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        if (project != project.getRootProject()) {
            throw new InvalidUserDataException("Can only be applied to the root project. Tried to apply to " + project);
        }

        project.getTasks().register("dvt", DVTTask.class, new Action<DVTTask>() {
            @Override
            public void execute(DVTTask dvt) {
                dvt.setDescription("Generates a DVT project.");

                project.allprojects(project -> {
                    project.getPluginManager().withPlugin("com.verificationgentleman.gradle.hdvl.base", appliedPlugin -> {
                        addArgsFile(dvt, project);
                    });
                    project.getPluginManager().withPlugin("com.verificationgentleman.gradle.hdvl.svunit", appliedPlugin -> {
                        maybeConfigureTests(dvt, project);
                    });
                });
            }

            private void addArgsFile(DVTTask dvt, Project sourceProject) {
                GenFullArgsFile genFullArgsFile
                        = sourceProject.getTasks().withType(GenFullArgsFile.class).getByName("genFullXrunArgsFile");
                dvt.getArgsFiles().from(genFullArgsFile.getDestination());
            }

            private void maybeConfigureTests(DVTTask dvt, Project sourceProject) {
                SVUnitSetup svUnitSetup = dvt.getProject().getObjects().newInstance(SVUnitSetup.class);
                SystemVerilogSourceDirectorySet testSvSources = getTestSources(sourceProject);
                svUnitSetup.getTestsRoot().set(
                        sourceProject.getLayout().dir(
                                sourceProject.provider(() -> testSvSources.getSourceDirectories().getSingleFile())
                        )
                );
                svUnitSetup.getSvunitRoot().setFrom(sourceProject.getConfigurations().getByName("svUnitRoot"));
                svUnitSetup.getWorkingDir().set(sourceProject.getLayout().getBuildDirectory().dir("dvt/svunit"));
                dvt.getSvUnitSetups().add(svUnitSetup);
            }

            private SystemVerilogSourceDirectorySet getTestSources(Project sourceProject) {
                NamedDomainObjectContainer<SourceSet> sourceSets = sourceProject.getExtensions()
                    .getByType(new TypeOf<NamedDomainObjectContainer<SourceSet>>() {});
                return sourceSets.getByName("test").getExtensions().getByType(SystemVerilogSourceDirectorySet.class);
            }
        });
    }
}
