package com.verificationgentleman.gradle.hdvl.internal;

import org.gradle.api.Project;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.io.File;

class FileAdapter extends XmlAdapter<String, File> {
    private final Project project;

    public FileAdapter(Project project) {
        this.project = project;
    }

    @Override
    public String marshal(File file) {
        return project.relativePath(file);
    }

    @Override
    public File unmarshal(String filePath) {
        return new File(filePath);
    }
}
