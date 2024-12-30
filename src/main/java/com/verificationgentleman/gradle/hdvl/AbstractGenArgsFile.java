/*
 * Copyright 2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.verificationgentleman.gradle.hdvl;

import org.gradle.api.file.FileCollection;
import org.gradle.api.file.FileTree;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.*;

import javax.inject.Inject;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

public abstract class AbstractGenArgsFile extends SourceTask {

    private RegularFileProperty destination;
    private FileCollection privateIncludeDirs;
    private FileCollection exportedIncludeDirs;
    private FileCollection cSourceFiles = getProject().getObjects().fileCollection();

    @Inject
    public AbstractGenArgsFile(ObjectFactory objectFactory) {
        destination = objectFactory.fileProperty();
    }

    @OutputFile
    public RegularFileProperty getDestination() {
        return destination;
    }

    @Input
    @Optional
    public abstract Property<String> getFirst();

    @InputFiles
    @SkipWhenEmpty
    @PathSensitive(PathSensitivity.ABSOLUTE)
    public FileCollection getPrivateIncludeDirs() {
        return privateIncludeDirs;
    }

    public void setPrivateIncludeDirs(FileCollection privateIncludeDirs) {
        this.privateIncludeDirs = privateIncludeDirs;
    }

    @InputFiles
    @SkipWhenEmpty
    @PathSensitive(PathSensitivity.ABSOLUTE)
    public FileCollection getExportedIncludeDirs() {
        return exportedIncludeDirs;
    }

    public void setExportedIncludeDirs(FileCollection exportedIncludeDirs) {
        this.exportedIncludeDirs = exportedIncludeDirs;
    }

    @InputFiles
    @SkipWhenEmpty
    @PathSensitive(PathSensitivity.ABSOLUTE)
    public FileTree getCSource() {
        return cSourceFiles.getAsFileTree();
    }

    public void setCSource(FileTree source) {
        setCSource((Object) source);
    }

    public void setCSource(Object source) {
        cSourceFiles = getProject().getObjects().fileCollection().from(source);
    }

    @TaskAction
    protected void generate() {
        try {
            writeArgsFile();
        } catch (IOException e) {
            System.out.println("Could not create args file");
        }
    }

    private void writeArgsFile() throws IOException {
        FileWriter writer = new FileWriter(destination.get().getAsFile());
        writeExportedHeaders(writer);
        writer.write("-makelib " + getLibName() + "\n");
        for (File f: getPrivateIncludeDirs().filter((File::exists)))
            writer.write("  " + getIncdirOpt(f.getAbsolutePath()) + "\n");
        for (File f: getOrderedSystemVerilogSourceFiles())
            writer.write("  " + f.getAbsolutePath() + "\n");
        for (File f: getCSource())
            writer.write("  " + f.getAbsolutePath() + "\n");
        writer.write("-endlib\n");
        writer.close();
    }

    // TODO Implement correct exported dir handling
    // Adding an '-incdir' outside of '-makelib' for each exported header dir will make it visible to other '-makelibs'
    // that declare a dependency on this project. It will also make it visible to other projects that haven't declared
    // a dependency on this project. If such projects include headers from this project, there will be no compile error.
    // This isn't consistent with what would happen in a multi-step compilation flow, where an error would be issued.
    private void writeExportedHeaders(FileWriter writer) throws IOException {
        for (File f: getExportedIncludeDirs().filter(File::exists))
            writer.write(getIncdirOpt(f.getAbsolutePath()) + "\n");
    }

    @Internal
    protected abstract String getLibName();

    protected abstract String getIncdirOpt(String incdirPath);

    private Iterable<File> getOrderedSystemVerilogSourceFiles() {
        List<File> result = new ArrayList<>(getSource().getFiles());

        if (!getFirst().isPresent())
            return result;

        String first = getFirst().get();

        int indexOfFirstFile = IntStream.range(0, result.size())
            .filter(i -> result.get(i).getName().equals(first))
            .findFirst()
            .orElse(-1);

        if (indexOfFirstFile > 0) {
            File firstFile = result.get(indexOfFirstFile);
            result.remove(indexOfFirstFile);
            result.add(0, firstFile);
        }

        return result;
    }
}
