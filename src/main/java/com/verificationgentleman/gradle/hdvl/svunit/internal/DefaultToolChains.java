package com.verificationgentleman.gradle.hdvl.svunit.internal;

import com.verificationgentleman.gradle.hdvl.svunit.RunSVUnitToolChain;
import com.verificationgentleman.gradle.hdvl.svunit.ToolChains;
import groovy.lang.Closure;

import javax.annotation.Nullable;

import static org.gradle.util.ConfigureUtil.configure;

public class DefaultToolChains implements ToolChains {
    private DefaultRunSVUnitToolChain runSVUnit = new DefaultRunSVUnitToolChain();

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
