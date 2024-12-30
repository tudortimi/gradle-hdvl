package com.verificationgentleman.gradle.hdvl.internal;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class WriteCompileSpecFile extends DefaultTask {
    private final RegularFileProperty destination;

    private final ConfigurableFileCollection svSourceFiles;
    private final ConfigurableFileCollection svPrivateIncludeDirs;
    private final ConfigurableFileCollection svExportedHeaderDirs;
    private final ConfigurableFileCollection cSourceFiles;


    public WriteCompileSpecFile() {
        destination = getProject().getObjects().fileProperty();
        svSourceFiles = getProject().getObjects().fileCollection();
        svPrivateIncludeDirs = getProject().getObjects().fileCollection();
        svExportedHeaderDirs = getProject().getObjects().fileCollection();
        cSourceFiles = getProject().getObjects().fileCollection();
    }

    @OutputFile
    public RegularFileProperty getDestination() {
        return destination;
    }

    @InputFiles
    @SkipWhenEmpty
    @PathSensitive(PathSensitivity.ABSOLUTE)
    public ConfigurableFileCollection getSvSource() {
        return svSourceFiles;
    }

    @InputFiles
    @SkipWhenEmpty
    @PathSensitive(PathSensitivity.ABSOLUTE)
    public ConfigurableFileCollection getSvPrivateIncludeDirs() {
        return svPrivateIncludeDirs;
    }

    @InputFiles
    @SkipWhenEmpty
    @PathSensitive(PathSensitivity.ABSOLUTE)
    public ConfigurableFileCollection getSvExportedHeaderDirs() {
        return svExportedHeaderDirs;
    }

    @InputFiles
    @SkipWhenEmpty
    @PathSensitive(PathSensitivity.ABSOLUTE)
    public ConfigurableFileCollection getCSource() {
        return cSourceFiles;
    }

    @TaskAction
    protected void generateJson() {
        DefaultHDVLCompileSpec compileSpec = new DefaultHDVLCompileSpec(new ArrayList<>(getSvSource().getFiles()),
                svPrivateIncludeDirs.getFiles(), svExportedHeaderDirs.getFiles(), cSourceFiles.getFiles());
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            SimpleModule module = new SimpleModule();
            module.addSerializer(File.class, new FileSerializer(getProject().getProjectDir()));
            objectMapper.registerModule(module);

            objectMapper.writerWithDefaultPrettyPrinter().writeValue(destination.get().getAsFile(), compileSpec);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
