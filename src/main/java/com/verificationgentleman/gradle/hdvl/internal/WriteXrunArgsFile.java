/*
 * Copyright 2024 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.verificationgentleman.gradle.hdvl.internal;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.verificationgentleman.gradle.hdvl.HDVLCompileSpec;
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
        File compileSpec = new File(input, ".gradle-hdvl/compile-spec.json");

        ObjectMapper objectMapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addDeserializer(File.class, new FileDeserializer(input));
        objectMapper.registerModule(module);

        try {
            DefaultHDVLCompileSpec result = objectMapper.readValue(compileSpec, DefaultHDVLCompileSpec.class);
            for (File svSourceFile : result.getSvSourceFiles()) {
                assert svSourceFile.isAbsolute() : "not absolute: " + svSourceFile;
                assert svSourceFile.exists() : "doesn't exist: " + svSourceFile;
            }
            for (File svPrivateIncludeDir : result.getSvPrivateIncludeDirs()) {
                assert svPrivateIncludeDir.isAbsolute() : "not absolute: " + svPrivateIncludeDir;
                assert svPrivateIncludeDir.exists() : "doesn't exist: " + svPrivateIncludeDir;
            }
            for (File svExportedHeaderDir : result.getSvExportedHeaderDirs()) {
                assert svExportedHeaderDir.isAbsolute() : "not absolute: " + svExportedHeaderDir;
                assert svExportedHeaderDir.exists() : "doesn't exist: " + svExportedHeaderDir;
            }
            for (File cSourceFile : result.getCSourceFiles()) {
                assert cSourceFile.isAbsolute() : "not absolute: " + cSourceFile;
                assert cSourceFile.exists() : "doesn't exist: " + cSourceFile;
            }

            return result;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void writeXrunArgsFile(File xrunArgsFile, HDVLCompileSpec compileSpec) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(xrunArgsFile, true))) {
            for (File svExportedHeaderDir: compileSpec.getSvExportedHeaderDirs())
                writer.write("-incdir " + svExportedHeaderDir + "\n");
            writer.write("-makelib worklib\n");
            for (File svPrivateIncludeDir: compileSpec.getSvPrivateIncludeDirs())
                writer.write("  -incdir " + svPrivateIncludeDir + "\n");
            for (File svSourceFile : compileSpec.getSvSourceFiles())
                writer.write("  " + svSourceFile + "\n");
            for (File cSourceFile : compileSpec.getCSourceFiles())
                writer.write("  " + cSourceFile + "\n");
            writer.write("-endlib\n");
        }
        catch (IOException ex) {
            ex.printStackTrace();  // TODO Implement better exception handling
        }
    }
}
