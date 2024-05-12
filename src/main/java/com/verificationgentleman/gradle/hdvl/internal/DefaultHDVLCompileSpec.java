package com.verificationgentleman.gradle.hdvl.internal;

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

    public DefaultHDVLCompileSpec(Set<File> svSourceFiles) {
        this.svSourceFiles = svSourceFiles.toArray(new File[0]);
    }

    // Needed for JAXB
    @SuppressWarnings("unused")
    private DefaultHDVLCompileSpec() {
        this.svSourceFiles = new File[0];
    }

    @Override
    public Set<File> getSvSourceFiles() {
        return new HashSet<>(Arrays.asList(svSourceFiles));
    }
}
