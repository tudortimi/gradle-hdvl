package com.verificationgentleman.gradle.hdvl.internal;

import com.verificationgentleman.gradle.hdvl.SourceSet;
import groovy.lang.Closure;
import org.gradle.api.Action;
import org.gradle.api.file.SourceDirectorySet;
import org.gradle.api.model.ObjectFactory;

import javax.annotation.Nullable;
import javax.inject.Inject;

import static org.gradle.util.ConfigureUtil.configure;

public abstract class DefaultSourceSet implements SourceSet {
    private final String name;
    private final SourceDirectorySet sv;
    private final SourceDirectorySet svHeaders;

    @Inject
    public DefaultSourceSet(String name, ObjectFactory objectFactory) {
        this.name = name;
        sv = objectFactory.sourceDirectorySet("sv", "SystemVerilog source");
        sv.srcDir("src/" + name + "/sv");
        sv.getFilter().include("**/*.sv");

        svHeaders = objectFactory.sourceDirectorySet("sv", "SystemVerilog exported headers");
        svHeaders.srcDir("src/" + name + "/sv_headers");
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public SourceDirectorySet getSv() {
        return sv;
    }

    @Override
    public DefaultSourceSet sv(@Nullable Closure configureClosure) {
        // XXX This is not part of the public Gradle API
        configure(configureClosure, getSv());
        return this;
    }

    @Override
    public DefaultSourceSet sv(Action<? super SourceDirectorySet> configureAction) {
        configureAction.execute(getSv());
        return this;
    }

    @Override
    public SourceDirectorySet getSvHeaders() {
        return svHeaders;
    }

    @Override
    public DefaultSourceSet svHeaders(@Nullable Closure configureClosure) {
        // XXX This is not part of the public Gradle API
        configure(configureClosure, getSvHeaders());
        return this;
    }

    @Override
    public DefaultSourceSet svHeaders(Action<? super SourceDirectorySet> configureAction) {
        configureAction.execute(getSvHeaders());
        return this;
    }
}
