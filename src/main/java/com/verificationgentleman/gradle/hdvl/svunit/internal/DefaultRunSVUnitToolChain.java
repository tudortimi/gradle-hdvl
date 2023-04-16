package com.verificationgentleman.gradle.hdvl.svunit.internal;

import com.verificationgentleman.gradle.hdvl.svunit.RunSVUnitToolChain;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ListProperty;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

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
    public RunSVUnitToolChain args(String... args) {
        for (String arg : args) {
            this.args.add(arg);
        }
        return this;
    }
}
