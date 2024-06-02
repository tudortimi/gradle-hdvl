package com.verificationgentleman.gradle.hdvl.internal;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.verificationgentleman.gradle.hdvl.HDVLCompileSpec;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@XmlRootElement(name="compileSpec")
public class DefaultHDVLCompileSpec implements HDVLCompileSpec {
    @XmlElementWrapper
    @XmlElement(name="svSourceFile")
    @XmlJavaTypeAdapter(value=FileAdapter.class)
    private final File[] svSourceFiles;

    @XmlElementWrapper
    @XmlElement(name="svPrivateIncludeDir")
    @XmlJavaTypeAdapter(value=FileAdapter.class)
    private final File[] svPrivateIncludeDirs;

    @XmlElementWrapper
    @XmlElement(name="svExportedHeaderDir")
    @XmlJavaTypeAdapter(value=FileAdapter.class)
    private final File[] svExportedHeaderDirs;

    @XmlElementWrapper
    @XmlElement(name="cSourceFile")
    @XmlJavaTypeAdapter(value=FileAdapter.class)
    @JsonProperty("cSourceFiles")
    private final File[] cSourceFiles;

    public DefaultHDVLCompileSpec(Set<File> svSourceFiles, Set<File> svPrivateIncludeDirs, Set<File> svExportedHeaderDirs,
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
    public Set<File> getSvSourceFiles() {
        return new HashSet<>(Arrays.asList(svSourceFiles));
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
