package com.verificationgentleman.gradle.hdvl.systemverilog;

import org.gradle.api.file.FileCollection;
import org.gradle.api.file.FileTree;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.tasks.*;

import javax.inject.Inject;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class GenArgsFile extends SourceTask {

    private RegularFileProperty destination;
    private FileCollection privateIncludeDirs;
    private FileCollection exportedIncludeDirs;
    private FileCollection cSourceFiles = getProject().getObjects().fileCollection();

    @Inject
    public GenArgsFile(ObjectFactory objectFactory) {
        destination = objectFactory.fileProperty();
    }

    @OutputFile
    public RegularFileProperty getDestination() {
        return destination;
    }

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
        writer.write("-makelib worklib\n");
        for (File f: getPrivateIncludeDirs())
            writer.write("  " + "-incdir " + f.getAbsolutePath() + "\n");
        for (File f: getSource())
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
            writer.write("-incdir " + f.getAbsolutePath() + "\n");
    }

}
