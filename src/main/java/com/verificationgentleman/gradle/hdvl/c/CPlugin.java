/*
 * Copyright 2021 the original author or authors.
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

package com.verificationgentleman.gradle.hdvl.c;

import com.verificationgentleman.gradle.hdvl.SourceSet;
import com.verificationgentleman.gradle.hdvl.c.internal.DefaultCSourceSet;
import com.verificationgentleman.gradle.hdvl.systemverilog.GenArgsFile;
import com.verificationgentleman.gradle.hdvl.systemverilog.SystemVerilogPlugin;
import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.internal.plugins.DslObject;

public class CPlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        project.getPluginManager().apply(SystemVerilogPlugin.class);
        NamedDomainObjectContainer<SourceSet> sourceSets
                = (NamedDomainObjectContainer<SourceSet>) project.getExtensions().getByName("sourceSets");
	    final SourceSet mainSourceSet = sourceSets.getByName("main");

	    final DefaultCSourceSet mainCSourceSet = new DefaultCSourceSet(mainSourceSet.getName(), project.getObjects());

        // XXX WORKAROUND Not part of the public API
        new DslObject(mainSourceSet).getConvention().getPlugins().put("c", mainCSourceSet);

        configureGenArgsFile(project, mainCSourceSet);
    }

    private void configureGenArgsFile(Project project, CSourceSet mainSourceSet) {
        GenArgsFile genArgsFile = (GenArgsFile) project.getTasks().getByName("genArgsFile");
        genArgsFile.setCSource(mainSourceSet.getC());
    }
}
