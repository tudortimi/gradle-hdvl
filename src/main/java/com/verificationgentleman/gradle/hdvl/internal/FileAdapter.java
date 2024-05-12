package com.verificationgentleman.gradle.hdvl.internal;

import jakarta.xml.bind.annotation.adapters.XmlAdapter;
import java.io.File;

class FileAdapter extends XmlAdapter<String, File> {
    private final File rootDirectory;

    public FileAdapter(File rootDirectory) {
        this.rootDirectory = rootDirectory;
    }

    @Override
    public String marshal(File file) {
        return rootDirectory.toPath().relativize(file.toPath()).toString();
    }

    @Override
    public File unmarshal(String filePath) {
        return new File(rootDirectory, filePath);
    }
}
