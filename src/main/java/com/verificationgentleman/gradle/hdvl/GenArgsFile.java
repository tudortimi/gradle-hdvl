package com.verificationgentleman.gradle.hdvl;

import org.gradle.api.file.FileCollection;
import org.gradle.api.file.FileTree;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.tasks.*;

import javax.inject.Inject;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Set;

public class GenArgsFile extends SourceTask {

    private RegularFileProperty destination;
    private Set<File> privateIncludeDirs;
    private FileCollection cSourceFiles = getProject().getObjects().fileCollection();

    @Inject
    public GenArgsFile(ObjectFactory objectFactory) {
        destination = objectFactory.fileProperty();
    }

    @OutputFile
    public RegularFileProperty getDestination() {
        return destination;
    }

    @Input
    public Set<File> getPrivateIncludeDirs() {
        return privateIncludeDirs;
    }

    public void setPrivateIncludeDirs(Set<File> privateIncludeDirs) {
        this.privateIncludeDirs = privateIncludeDirs;
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
        writer.write("-makelib worklib\n");
        for (File f: getPrivateIncludeDirs())
            writer.write("  " + "-incdir " + f.getAbsolutePath() + "\n");
        for (File f: getSource())
            writer.write(f.getAbsolutePath() + "\n");
        for (File f: getCSource())
            writer.write(f.getAbsolutePath() + "\n");
        writer.write("-endlib\n");
        writer.close();
    }

}
