package com.verificationgentleman.gradle.hdvl.svunit;

import com.verificationgentleman.gradle.hdvl.SystemVerilogPlugin;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class SvunitPlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        project.getPluginManager().apply(SystemVerilogPlugin.class);
    }
}
