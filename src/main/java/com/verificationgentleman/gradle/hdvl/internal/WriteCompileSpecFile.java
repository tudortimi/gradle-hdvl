package com.verificationgentleman.gradle.hdvl.internal;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.verificationgentleman.gradle.hdvl.systemverilog.FileOrder;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.FileTree;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public abstract class WriteCompileSpecFile extends DefaultTask {
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

    @Input
    @Optional
    public abstract Property<FileOrder> getSvSourceOrder();

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
        DefaultHDVLCompileSpec compileSpec = new DefaultHDVLCompileSpec(getOrderedSystemVerilogSourceFiles(),
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

    // TODO Fix duplication with `AbstractGenArgsFileTask`
    private List<File> getOrderedSystemVerilogSourceFiles() {
        if (!getSvSourceOrder().isPresent() || (getSvSourceOrder().get().getFirst() == null && getSvSourceOrder().get().getLast() == null))
            return new ArrayList<>(getSvSource().getFiles());

        FileTree firstFiles = newEmptyFileTree();
        FileTree lastFiles = newEmptyFileTree();

        String first = getSvSourceOrder().get().getFirst();
        if (first != null)
            firstFiles = getSvSource().getAsFileTree().matching(patternFilterable -> patternFilterable.include(first));

        String last = getSvSourceOrder().get().getLast();
        if (last != null)
            lastFiles = getSvSource().getAsFileTree().matching(patternFilterable -> patternFilterable.include(last));

        List<File> result = new ArrayList<>();
        result.addAll(firstFiles.getFiles());
        result.addAll(getSvSource().minus(firstFiles).minus(lastFiles).getFiles());
        result.addAll(lastFiles.getFiles());

        return result;
    }

    private FileTree newEmptyFileTree() {
        return getProject().files().getAsFileTree();
    }
}
