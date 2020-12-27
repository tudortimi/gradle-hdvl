package com.verificationgentleman.gradle.hdvl;

import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.SourceTask;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class GenArgsFile extends SourceTask {

    private File destinationDir;

    @OutputDirectory
    public File getDestinationDir() {
        return destinationDir;
    }

    public void setDestinationDir(File destinationDir) {
        this.destinationDir = destinationDir;
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
        File argsFile = new File(destinationDir, "args.f");
        FileWriter writer = new FileWriter(argsFile);
        for (File f: getSource())
            writer.write(f.getAbsolutePath() + "\n");
        writer.close();
    }

}
