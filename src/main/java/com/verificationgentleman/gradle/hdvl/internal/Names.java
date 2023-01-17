package com.verificationgentleman.gradle.hdvl.internal;

import org.gradle.util.GUtil;

public class Names {

    public static String getMainGenArgsFileTaskName(String toolName) {
        return "gen" + toolName + "ArgsFile";
    }

    public static String getGenArgsFileTaskName(String sourceSetName, String toolName) {
        return GUtil.toLowerCamelCase("gen" + " " + sourceSetName + "" + toolName + "ArgsFile");
    }

    public static String getGenFullArgsFileTaskName(String sourceSetName, String toolName) {
        if (sourceSetName.equals("main"))
            return "genFull" + toolName + "ArgsFile";
        return GUtil.toLowerCamelCase("genFull" + " " + sourceSetName + "" + toolName + "ArgsFile");
    }

    public static String getFullArgsFileName(String sourceSetName, String toolName) {
        String toolNameLower = toolName.toLowerCase();
        if (sourceSetName.equals("main"))
            return "full" + "_" + toolNameLower + "_" + "args.f";
        return "full" + "_" + sourceSetName + "_"+ toolNameLower + "_" + "args.f";
    }

    public static String getArgsFilesConfigurationName(String sourceSetName, String toolName) {
        if (sourceSetName.equals("main"))
            return GUtil.toLowerCamelCase(toolName + "ArgsFiles");
        return GUtil.toLowerCamelCase(sourceSetName + " " + toolName + "ArgsFiles");
    }

    public static String getTestTaskName(String toolName) {
        return GUtil.toLowerCamelCase("testWith" + " " + toolName);
    }

}
