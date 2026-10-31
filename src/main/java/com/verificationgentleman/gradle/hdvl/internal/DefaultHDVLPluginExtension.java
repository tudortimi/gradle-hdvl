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

package com.verificationgentleman.gradle.hdvl.internal;

import com.verificationgentleman.gradle.hdvl.HDVLPluginExtension;
import com.verificationgentleman.gradle.hdvl.SourceSet;
import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.NamedDomainObjectFactory;
import org.gradle.api.Project;
import org.gradle.api.model.ObjectFactory;

public class DefaultHDVLPluginExtension implements HDVLPluginExtension {
    private final Project project;
    private final NamedDomainObjectContainer<SourceSet> sourceSets;

    public DefaultHDVLPluginExtension(Project project) {
        this.project = project;
        NamedDomainObjectFactory<SourceSet> sourceSetFactory = newSourceSetFactory(project.getObjects());
        sourceSets = project.getObjects().domainObjectContainer(SourceSet.class, sourceSetFactory);
    }

    private NamedDomainObjectFactory<SourceSet> newSourceSetFactory(ObjectFactory objectFactory) {
        return new NamedDomainObjectFactory<SourceSet>() {
            @Override
            public SourceSet create(String name) {
                return objectFactory.newInstance(DefaultSourceSet.class, name);
            }
        };
    }

    @Override
    public NamedDomainObjectContainer<SourceSet> getSourceSets() {
        return sourceSets;
    }
}
