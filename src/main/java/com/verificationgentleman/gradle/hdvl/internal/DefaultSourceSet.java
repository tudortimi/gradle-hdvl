package com.verificationgentleman.gradle.hdvl.internal;

import com.verificationgentleman.gradle.hdvl.SourceSet;
import org.gradle.api.model.ObjectFactory;

import javax.inject.Inject;

public abstract class DefaultSourceSet implements SourceSet {
    private final String name;

    @Inject
    public DefaultSourceSet(String name, ObjectFactory objectFactory) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }
}
