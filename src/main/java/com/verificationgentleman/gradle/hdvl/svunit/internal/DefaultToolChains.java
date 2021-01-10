package com.verificationgentleman.gradle.hdvl.svunit.internal;

import com.verificationgentleman.gradle.hdvl.svunit.RunSVUnitToolChain;
import com.verificationgentleman.gradle.hdvl.svunit.ToolChains;

public class DefaultToolChains implements ToolChains {
    private DefaultRunSVUnitToolChain runSVUnit = new DefaultRunSVUnitToolChain();

    @Override
    public RunSVUnitToolChain getRunSVUnit() {
        return runSVUnit;
    }
}
