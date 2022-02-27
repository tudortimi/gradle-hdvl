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

package com.verificationgentleman.gradle.hdvl.c;

import com.verificationgentleman.gradle.hdvl.*;
import com.verificationgentleman.gradle.hdvl.c.internal.DefaultCSourceSet;
import org.gradle.api.Action;
import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.internal.plugins.DslObject;

public class CPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        project.getPluginManager().apply(HDVLBasePlugin.class);
        NamedDomainObjectContainer<SourceSet> sourceSets
                = project.getExtensions().getByType(HDVLPluginExtension.class).getSourceSets();
	    sourceSets.all(new Action<SourceSet>() {
            @Override
            public void execute(SourceSet sourceSet) {
                final DefaultCSourceSet cSourceSet = new DefaultCSourceSet(sourceSet.getName(), project.getObjects());

                // XXX WORKAROUND Not part of the public API
                new DslObject(sourceSet).getConvention().getPlugins().put("c", cSourceSet);

                String[] toolNames = {"Xrun", "Qrun"};
                for (String toolName: toolNames) {
                    AbstractGenArgsFile genArgsFile
                            = (AbstractGenArgsFile) project.getTasks().getByName(sourceSet.getGenArgsFileTaskName(toolName));
                    genArgsFile.setCSource(cSourceSet.getC());
                }
            }
        });
    }

}
