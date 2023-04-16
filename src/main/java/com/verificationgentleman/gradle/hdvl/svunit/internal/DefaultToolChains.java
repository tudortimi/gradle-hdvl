package com.verificationgentleman.gradle.hdvl.svunit.internal;

import com.verificationgentleman.gradle.hdvl.svunit.RunSVUnitToolChain;
import com.verificationgentleman.gradle.hdvl.svunit.ToolChains;
import groovy.lang.Closure;
import org.gradle.api.model.ObjectFactory;

import javax.annotation.Nullable;
import javax.inject.Inject;

import static org.gradle.util.ConfigureUtil.configure;

public class DefaultToolChains implements ToolChains {
    private DefaultRunSVUnitToolChain runSVUnit;

    @Inject
    public DefaultToolChains(ObjectFactory objects) {
        runSVUnit = objects.newInstance(DefaultRunSVUnitToolChain.class);
    }

    @Override
    public RunSVUnitToolChain getRunSVUnit() {
        return runSVUnit;
    }

    @Override
    public ToolChains runSVUnit(@Nullable Closure configureClosure) {
        // XXX This is not part of the public Gradle API
        configure(configureClosure, getRunSVUnit());
        return this;
    }
}
