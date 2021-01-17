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
