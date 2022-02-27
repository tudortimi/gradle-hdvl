package com.verificationgentleman.gradle.hdvl.internal;

import org.gradle.util.GUtil;

public class Names {
    public static String getArgsFilesConfigurationName(String toolName) {
        return GUtil.toLowerCamelCase(toolName + "ArgsFiles");
    }
}
