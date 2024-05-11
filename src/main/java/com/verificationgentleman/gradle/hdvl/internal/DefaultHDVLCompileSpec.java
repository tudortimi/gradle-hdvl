package com.verificationgentleman.gradle.hdvl.internal;

import com.verificationgentleman.gradle.hdvl.HDVLCompileSpec;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
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

    public DefaultHDVLCompileSpec(File[] svSourceFiles) {
        this.svSourceFiles = svSourceFiles;
    }

    public DefaultHDVLCompileSpec(Set<File> svSourceFiles) {
        this.svSourceFiles = svSourceFiles.toArray(new File[0]);
    }

    // Needed for JAXB
    @SuppressWarnings("unused")
    private DefaultHDVLCompileSpec() {
        this(new File[0]);
    }

    @Override
    public Set<File> getSvSourceFiles() {
        return new HashSet<>(Arrays.asList(svSourceFiles));
    }
}
