package com.verificationgentleman.gradle.hdvl.svunit.internal;

import com.verificationgentleman.gradle.hdvl.svunit.RunSVUnitToolChain;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ListProperty;

import javax.inject.Inject;

public class DefaultRunSVUnitToolChain implements RunSVUnitToolChain {
    private ListProperty<String> args;

    @Inject
    public DefaultRunSVUnitToolChain(ObjectFactory objects) {
        args = objects.listProperty(String.class);
    }

    @Override
    public String getDisplayName() {
        return "runSVUnit";
    }

    @Override
    public String getName() {
        return "runSVUnit";
    }

    @Override
    public ListProperty<String> getArgs() {
        return args;
    }

    @Override
    public RunSVUnitToolChain args(Object... args) {
        for (Object arg : args) {
            if (arg == null) {
                throw new IllegalArgumentException("null argument is not allowed");
            }
            this.args.add(arg.toString());
        }
        return this;
    }
}
