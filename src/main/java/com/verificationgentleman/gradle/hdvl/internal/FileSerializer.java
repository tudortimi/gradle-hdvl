package com.verificationgentleman.gradle.hdvl.internal;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.File;
import java.io.IOException;

public class FileSerializer extends JsonSerializer<File> {
    private final File rootDirectory;

    public FileSerializer(File rootDirectory) {
        this.rootDirectory = rootDirectory;
    }

    @Override
    public void serialize(File file, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        String relativePathOfFile = rootDirectory.toPath().relativize(file.toPath()).toString();
        jsonGenerator.writeString(relativePathOfFile);
    }
}
