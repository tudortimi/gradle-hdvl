package com.verificationgentleman.gradle.hdvl.internal;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.File;
import java.io.IOException;

public class FileDeserializer extends JsonDeserializer<File> {
    private final File rootDirectory;

    public FileDeserializer(File rootDirectory) {
        this.rootDirectory = rootDirectory;
    }

    @Override
    public File deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JacksonException {
        return new File(rootDirectory, jsonParser.getText());
    }
}
