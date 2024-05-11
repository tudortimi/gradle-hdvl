package com.verificationgentleman.gradle.hdvl.internal;

import com.verificationgentleman.gradle.hdvl.HDVLCompileSpec;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class DefaultHDVLCompileSpec implements HDVLCompileSpec {
    private final File[] svSourceFiles;

    public DefaultHDVLCompileSpec(File[] svSourceFiles) {
        this.svSourceFiles = svSourceFiles;
    }

    public DefaultHDVLCompileSpec(Set<File> svSourceFiles) {
        this.svSourceFiles = svSourceFiles.toArray(new File[0]);
    }

    @Override
    public Set<File> getSvSourceFiles() {
        return new HashSet<>(Arrays.asList(svSourceFiles));
    }
}
