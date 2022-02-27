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

    @Override
    public String getGenQrunArgsFileTaskName() {
        return name == "main"
            ? "genQrunArgsFile"
            : GUtil.toLowerCamelCase("gen" + " " + name + "" + "QrunArgsFile");
    }

    @Override
    public String getQrunArgsFileName() {
        return name == "main"
            ? "qrun_args.f"
            : name + "_" + "qrun_args.f";
    }
}
