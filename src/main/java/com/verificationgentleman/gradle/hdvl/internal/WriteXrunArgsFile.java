package com.verificationgentleman.gradle.hdvl.internal;

import com.verificationgentleman.gradle.hdvl.HDVLCompileSpec;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import org.gradle.api.artifacts.transform.InputArtifact;
import org.gradle.api.artifacts.transform.TransformAction;
import org.gradle.api.artifacts.transform.TransformOutputs;
import org.gradle.api.artifacts.transform.TransformParameters;
import org.gradle.api.file.FileSystemLocation;
import org.gradle.api.provider.Provider;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public abstract class WriteXrunArgsFile implements TransformAction<TransformParameters.None> {
    @InputArtifact
    public abstract Provider<FileSystemLocation> getInputArtifact();

    @Override
    public void transform(TransformOutputs outputs) {
        File input = getInputArtifact().get().getAsFile();
        File xrunArgsFile = outputs.file(input.getName() + ".xrun_args.f");
        DefaultHDVLCompileSpec compileSpec = getCompileSpec(input);
        writeXrunArgsFile(xrunArgsFile, compileSpec);
    }

    private static DefaultHDVLCompileSpec getCompileSpec(File input) {
        File compileSpec = new File(input, ".gradle-hdvl/compile-spec.xml");
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(DefaultHDVLCompileSpec.class);
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();

            FileAdapter fileAdapter = new FileAdapter(input);
            unmarshaller.setAdapter(fileAdapter);

            DefaultHDVLCompileSpec result = (DefaultHDVLCompileSpec) unmarshaller.unmarshal(compileSpec);
            for (File svSourceFile : result.getSvSourceFiles()) {
                assert svSourceFile.isAbsolute() : "not absolute: " + svSourceFile;
                assert svSourceFile.exists() : "doesn't exist: " + svSourceFile;
            }

            return result;
        } catch (JAXBException e) {
            throw new RuntimeException(e);
        }
    }

    private static void writeXrunArgsFile(File xrunArgsFile, HDVLCompileSpec compileSpec) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(xrunArgsFile, true))) {
            writer.write("-makelib worklib\n");
            for (File svSourceFile : compileSpec.getSvSourceFiles())
                writer.write("  " + svSourceFile + "\n");
            writer.write("-endlib\n");
        }
        catch (IOException ex) {
            ex.printStackTrace();  // TODO Implement better exception handling
        }
    }
}
