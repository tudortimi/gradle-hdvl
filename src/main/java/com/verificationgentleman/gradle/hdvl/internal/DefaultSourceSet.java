package com.verificationgentleman.gradle.hdvl.internal;

import com.verificationgentleman.gradle.hdvl.SourceSet;
import org.gradle.api.model.ObjectFactory;
import org.gradle.util.GUtil;

import javax.inject.Inject;

public abstract class DefaultSourceSet implements SourceSet {
    private final String name;

    @Inject
    public DefaultSourceSet(String name, ObjectFactory objectFactory) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getGenArgsFileTaskName() {
        return name == "main"
                ? "genArgsFile"
                : GUtil.toLowerCamelCase("gen" + " " + name + "" + "ArgsFile");
    }

    @Override
    public String getArgsFileName() {
        return name == "main"
            ? "args.f"
            : name + "_" + "args.f";
    }
}
