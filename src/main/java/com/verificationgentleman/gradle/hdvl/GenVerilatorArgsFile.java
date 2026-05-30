package com.verificationgentleman.gradle.hdvl;

import org.gradle.api.model.ObjectFactory;

import javax.inject.Inject;

public abstract class GenVerilatorArgsFile extends AbstractGenArgsFile {

    @Inject
    public GenVerilatorArgsFile(ObjectFactory objectFactory) {
        super(objectFactory);
    }

    @Override
    protected String getLibName() {
        return "work";
    }

    @Override
    protected String getIncdirOpt(String incdirPath) {
        return "+incdir+" + incdirPath;
    }

    @Override
    protected boolean isMakelibBlockEnabled() {
        return false;
    }
}
