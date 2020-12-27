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
package com.verificationgentleman.gradle.hdvl;

import com.verificationgentleman.gradle.hdvl.internal.DefaultSourceSet;
import org.gradle.api.*;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.model.ObjectFactory;

import java.io.File;

public class SystemVerilogPlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        final ObjectFactory objectFactory = project.getObjects();
        NamedDomainObjectFactory<SourceSet> sourceSetFactory = newSourceSetFactory(objectFactory);
        NamedDomainObjectContainer<SourceSet> sourceSets
                = objectFactory.domainObjectContainer(SourceSet.class, sourceSetFactory);
	    project.getExtensions().add("sourceSets", sourceSets);
	    final SourceSet mainSourceSet = sourceSets.create("main");
	    configureGenArgsFile(project, mainSourceSet);
	    configureArgsFileConfiguration(project);
    }

    private NamedDomainObjectFactory<SourceSet> newSourceSetFactory(ObjectFactory objectFactory) {
        return new NamedDomainObjectFactory<SourceSet>() {
            @Override
            public SourceSet create(String name) {
                return objectFactory.newInstance(DefaultSourceSet.class, name);
            }
        };
    }

    private void configureGenArgsFile(Project project, SourceSet mainSourceSet) {
        project.getTasks().register("genArgsFile", GenArgsFile.class, new Action<GenArgsFile>() {
            @Override
            public void execute(GenArgsFile genArgsFile) {
                genArgsFile.setDescription("Generates an argument file for the main source code.");
                genArgsFile.setSource(mainSourceSet.getSv());
                genArgsFile.setDestination(new File(project.getBuildDir(), "args.f"));
            }
        });
    }

    private void configureArgsFileConfiguration(Project project) {
        Configuration argsFiles = project.getConfigurations().create("argsFiles");
        argsFiles.setCanBeConsumed(true);
        argsFiles.setCanBeResolved(false);
    }
}
