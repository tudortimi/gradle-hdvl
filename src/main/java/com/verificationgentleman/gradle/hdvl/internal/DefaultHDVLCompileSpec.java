package com.verificationgentleman.gradle.hdvl.internal;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.verificationgentleman.gradle.hdvl.HDVLCompileSpec;

import java.io.File;
import java.util.*;

public class DefaultHDVLCompileSpec implements HDVLCompileSpec {
    private final File[] svSourceFiles;

    private final File[] svPrivateIncludeDirs;

    private final File[] svExportedHeaderDirs;

    @JsonProperty("cSourceFiles")
    private final File[] cSourceFiles;

    public DefaultHDVLCompileSpec(List<File> svSourceFiles, Set<File> svPrivateIncludeDirs, Set<File> svExportedHeaderDirs,
                                  Set<File> cSourceFiles) {
        this.svSourceFiles = svSourceFiles.toArray(new File[0]);
        this.svPrivateIncludeDirs = svPrivateIncludeDirs.toArray(new File[0]);
        this.svExportedHeaderDirs = svExportedHeaderDirs.toArray(new File[0]);
        this.cSourceFiles = cSourceFiles.toArray(new File[0]);
    }

    // Needed for JAXB
    @SuppressWarnings("unused")
    private DefaultHDVLCompileSpec() {
        this.svSourceFiles = new File[0];
        this.svPrivateIncludeDirs = new File[0];
        this.svExportedHeaderDirs = new File[0];
        this.cSourceFiles = new File[0];
    }

    @Override
    public List<File> getSvSourceFiles() {
        return Arrays.asList(svSourceFiles);
    }

    @Override
    public Set<File> getSvPrivateIncludeDirs() {
        return new HashSet<>(Arrays.asList(svPrivateIncludeDirs));
    }

    @Override
    public Set<File> getSvExportedHeaderDirs() {
        return new HashSet<>(Arrays.asList(svExportedHeaderDirs));
    }

    @JsonProperty("cSourceFiles")
    @Override
    public Set<File> getCSourceFiles() {
        return new HashSet<>(Arrays.asList(cSourceFiles));
    }
}
