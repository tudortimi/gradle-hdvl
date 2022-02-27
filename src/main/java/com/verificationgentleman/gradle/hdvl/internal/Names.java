package com.verificationgentleman.gradle.hdvl.internal;

import org.gradle.util.GUtil;

public class Names {

    public static String getMainGenArgsFileTaskName(String toolName) {
        return "gen" + toolName + "ArgsFile";
    }

    public static String getGenFullArgsFileTaskName(String toolName) {
        return "genFull" + toolName + "ArgsFile";
    }

    public static String getFullArgsFileName(String toolName) {
        String toolNameLower = toolName.toLowerCase();
        return "full" + "_" + toolNameLower + "_" + "args.f";
    }

    public static String getArgsFilesConfigurationName(String toolName) {
        return GUtil.toLowerCamelCase(toolName + "ArgsFiles");
    }

    public static String getTestTaskName(String toolName) {
        return GUtil.toLowerCamelCase("testWith" + " " + toolName);
    }

}
