package com.verificationgentleman.gradle.hdvl;

import org.gradle.api.tasks.SourceTask;
import org.gradle.api.tasks.TaskAction;

public class GenArgsFile extends SourceTask {

    @TaskAction
    protected void generate() {
        System.out.println("Generating");
    }

}
