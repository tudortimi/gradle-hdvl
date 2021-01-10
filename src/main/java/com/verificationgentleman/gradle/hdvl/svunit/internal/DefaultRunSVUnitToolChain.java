package com.verificationgentleman.gradle.hdvl.svunit.internal;

import com.verificationgentleman.gradle.hdvl.svunit.RunSVUnitToolChain;

import java.util.ArrayList;
import java.util.List;

public class DefaultRunSVUnitToolChain implements RunSVUnitToolChain {
    private List<String> args = new ArrayList<>();

    @Override
    public String getDisplayName() {
        return "runSVUnit";
    }

    @Override
    public String getName() {
        return "runSVUnit";
    }

    @Override
    public List<String> getArgs() {
        return args;
    }

    @Override
    public RunSVUnitToolChain args(String... args) {
        for (String arg : args) {
            this.args.add(arg);
        }
        return this;
    }
}
