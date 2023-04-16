package com.verificationgentleman.gradle.hdvl.svunit;

import org.gradle.api.provider.ListProperty;
import org.gradle.platform.base.ToolChain;

import java.util.List;

public interface RunSVUnitToolChain extends ToolChain {
    ListProperty<String> getArgs();

    /**
     * Configures the command line args.
     *
     * @param args The arg values to append
     * @return this
     */
    RunSVUnitToolChain args(String... args);
}
