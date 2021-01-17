package com.verificationgentleman.gradle.hdvl.dvt;

import org.gradle.api.Action;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;
import org.gradle.process.ExecSpec;

public class DVTTask extends DefaultTask {
    @TaskAction
    public void generate() {
        getProject().exec(new Action<ExecSpec>() {
            @Override
            public void execute(ExecSpec execSpec) {
                execSpec.executable("dvt_cli.sh");
                execSpec.args("createProject");
            }
        });
    }
}
