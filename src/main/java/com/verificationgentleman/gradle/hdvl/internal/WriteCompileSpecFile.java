package com.verificationgentleman.gradle.hdvl.internal;

import com.fasterxml.jackson.core.exc.StreamWriteException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.*;

import java.io.IOException;

public class WriteCompileSpecFile extends DefaultTask {
    private final RegularFileProperty destination;
    private final RegularFileProperty destinationForJson;

    private final ConfigurableFileCollection svSourceFiles;
    private final ConfigurableFileCollection svPrivateIncludeDirs;
    private final ConfigurableFileCollection svExportedHeaderDirs;
    private final ConfigurableFileCollection cSourceFiles;


    public WriteCompileSpecFile() {
        destination = getProject().getObjects().fileProperty();
        destinationForJson = getProject().getObjects().fileProperty();
        svSourceFiles = getProject().getObjects().fileCollection();
        svPrivateIncludeDirs = getProject().getObjects().fileCollection();
        svExportedHeaderDirs = getProject().getObjects().fileCollection();
        cSourceFiles = getProject().getObjects().fileCollection();
    }

    @OutputFile
    public RegularFileProperty getDestination() {
        return destination;
    }

    @OutputFile
    public RegularFileProperty getDestinationForJson() {
        return destinationForJson;
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
    protected void generate() {
        DefaultHDVLCompileSpec compileSpec = new DefaultHDVLCompileSpec(getSvSource().getFiles(),
                svPrivateIncludeDirs.getFiles(), svExportedHeaderDirs.getFiles(), cSourceFiles.getFiles());
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(DefaultHDVLCompileSpec.class);
            Marshaller marshaller = jaxbContext.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

            FileAdapter fileAdapter = new FileAdapter(getProject().getProjectDir());
            marshaller.setAdapter(fileAdapter);

            marshaller.marshal(compileSpec, destination.get().getAsFile());
        } catch (JAXBException e) {
            throw new RuntimeException(e);
        }
    }

    @TaskAction
    protected void generateJson() {
        DefaultHDVLCompileSpec compileSpec = new DefaultHDVLCompileSpec(getSvSource().getFiles(),
                svPrivateIncludeDirs.getFiles(), svExportedHeaderDirs.getFiles(), cSourceFiles.getFiles());
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(destinationForJson.get().getAsFile(), compileSpec);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
