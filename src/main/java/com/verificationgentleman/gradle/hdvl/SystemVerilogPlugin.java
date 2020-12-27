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
import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.NamedDomainObjectFactory;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.model.ObjectFactory;

public class SystemVerilogPlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        final ObjectFactory objectFactory = project.getObjects();
        NamedDomainObjectFactory<SourceSet> sourceSetFactory = new NamedDomainObjectFactory<SourceSet>() {
            @Override
            public SourceSet create(String name) {
                return new DefaultSourceSet(name, objectFactory);
            }
        };
        NamedDomainObjectContainer<SourceSet> sourceSets
                = objectFactory.domainObjectContainer(SourceSet.class, sourceSetFactory);
	    project.getExtensions().add("sourceSets", sourceSets);
	    sourceSets.create("main");
    }
}
