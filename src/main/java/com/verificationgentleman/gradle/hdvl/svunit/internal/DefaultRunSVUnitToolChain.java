package com.verificationgentleman.gradle.hdvl.svunit.internal;

import com.verificationgentleman.gradle.hdvl.svunit.RunSVUnitToolChain;

public class DefaultRunSVUnitToolChain implements RunSVUnitToolChain {
    @Override
    public String getDisplayName() {
        return "runSVUnit";
    }

    @Override
    public String getName() {
        return "runSVUnit";
    }
}
