package com.verificationgentleman.gradle.hdvl;

import org.gradle.api.Named;

public class SourceSet implements Named {
    private final String name;

    public SourceSet(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }
}
