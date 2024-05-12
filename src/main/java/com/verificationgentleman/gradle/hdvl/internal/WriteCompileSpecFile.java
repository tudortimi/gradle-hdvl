package com.verificationgentleman.gradle.hdvl.internal;

import org.gradle.api.DefaultTask;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.*;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;

public class WriteCompileSpecFile extends DefaultTask {
    private final RegularFileProperty destination;

    private ConfigurableFileCollection svSourceFiles;

    public WriteCompileSpecFile() {
        destination = getProject().getObjects().fileProperty();
        svSourceFiles = getProject().getObjects().fileCollection();
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

    @TaskAction
    protected void generate() {
        DefaultHDVLCompileSpec compileSpec = new DefaultHDVLCompileSpec(getSvSource().getFiles());
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
}
