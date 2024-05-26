package com.verificationgentleman.gradle.hdvl.internal;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.*;

import java.util.Collections;

public class WriteCompileSpecFile extends DefaultTask {
    private final RegularFileProperty destination;

    private final ConfigurableFileCollection svSourceFiles;
    private final ConfigurableFileCollection svSPrivateIncludeDirs;

    public WriteCompileSpecFile() {
        destination = getProject().getObjects().fileProperty();
        svSourceFiles = getProject().getObjects().fileCollection();
        svSPrivateIncludeDirs = getProject().getObjects().fileCollection();
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
    public ConfigurableFileCollection getSvSPrivateIncludeDirs() {
        return svSPrivateIncludeDirs;
    }

    @TaskAction
    protected void generate() {
        DefaultHDVLCompileSpec compileSpec = new DefaultHDVLCompileSpec(getSvSource().getFiles(),
                svSPrivateIncludeDirs.getFiles(), Collections.emptySet());
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
