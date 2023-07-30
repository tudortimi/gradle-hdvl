package com.verificationgentleman.gradle.hdvl.dvt;

import org.gradle.api.DefaultTask;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class DVTTask extends DefaultTask {
    private RegularFileProperty argsFile;
    private RegularFileProperty defaultBuild;

    public DVTTask() {
        argsFile = getProject().getObjects().fileProperty();
        defaultBuild = getProject().getObjects().fileProperty().convention(
                getProject().getLayout().getProjectDirectory().dir(".dvt").file("default.build"));
    }

    @InputFile
    public RegularFileProperty getArgsFile() {
        return argsFile;
    }

    @OutputFile
    public RegularFileProperty getDefaultBuild() {
        return defaultBuild;
    }

    @TaskAction
    public void generate() throws IOException {
        FileWriter fw = new FileWriter(defaultBuild.get().getAsFile());
        fw.write("+dvt_init+xcelium.xrun\n");
        fw.write("-f " + argsFile.getAsFile().get().getAbsolutePath() + "\n");
        fw.close();
    }
}
