package com.verificationgentleman.gradle.hdvl.dvt;

import org.gradle.api.DefaultTask;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class DVTTask extends DefaultTask {
    private RegularFileProperty argsFile;

    public DVTTask() {
        argsFile = getProject().getObjects().fileProperty();
    }

    @InputFile
    public RegularFileProperty getArgsFile() {
        return argsFile;
    }

    @TaskAction
    public void generate() throws IOException {
        File defaultBuild = getProject().file(".dvt/default.build");
        defaultBuild.getParentFile().mkdirs();
        defaultBuild.createNewFile();

        FileWriter fw = new FileWriter(defaultBuild);
        fw.write("+dvt_init+xcelium.xrun\n");
        fw.write("-f " + argsFile.getAsFile().get().getAbsolutePath() + "\n");
        fw.close();
    }
}
