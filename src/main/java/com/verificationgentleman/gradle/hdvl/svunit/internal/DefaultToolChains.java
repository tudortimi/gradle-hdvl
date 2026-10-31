package com.verificationgentleman.gradle.hdvl.svunit.internal;

import com.verificationgentleman.gradle.hdvl.svunit.RunSVUnitToolChain;
import com.verificationgentleman.gradle.hdvl.svunit.ToolChains;
import groovy.lang.Closure;
import org.gradle.api.model.ObjectFactory;

import javax.annotation.Nullable;
import javax.inject.Inject;

import static com.verificationgentleman.gradle.hdvl.internal.Closures.configure;

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
        configure(configureClosure, getRunSVUnit());
        return this;
    }
}
