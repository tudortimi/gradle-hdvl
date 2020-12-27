package com.verificationgentleman.gradle.hdvl;

import groovy.lang.Closure;
import org.gradle.api.Action;
import org.gradle.api.Named;
import org.gradle.api.file.SourceDirectorySet;
import org.gradle.api.model.ObjectFactory;

import javax.annotation.Nullable;
import javax.inject.Inject;

import static org.gradle.util.ConfigureUtil.configure;

public class SourceSet implements Named {
    private final String name;
    private final SourceDirectorySet sv;

    @Inject
    public SourceSet(String name, ObjectFactory objectFactory) {
        this.name = name;
        sv = objectFactory.sourceDirectorySet("sv", "SystemVerilog source");
        sv.srcDir("src/" + name + "/sv");
        sv.getFilter().include("**/*.sv");
    }

    @Override
    public String getName() {
        return name;
    }

    public SourceDirectorySet getSv() {
        return sv;
    }

    public SourceSet sv(@Nullable Closure configureClosure) {
        // XXX This is not part of the public Gradle API
        configure(configureClosure, getSv());
        return this;
    }

    public SourceSet sv(Action<? super SourceDirectorySet> configureAction) {
        configureAction.execute(getSv());
        return this;
    }
}
