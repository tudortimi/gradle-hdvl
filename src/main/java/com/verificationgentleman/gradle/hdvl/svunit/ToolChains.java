package com.verificationgentleman.gradle.hdvl.svunit;

import groovy.lang.Closure;

import javax.annotation.Nullable;

public interface ToolChains {
    RunSVUnitToolChain getRunSVUnit();

    /**
     * Configures the 'runSVUnit' tool chain.
     *
     * @param configureClosure The closure to use to configure the tool chain
     * @return this
     */
    ToolChains runSVUnit(@Nullable Closure configureClosure);
}
