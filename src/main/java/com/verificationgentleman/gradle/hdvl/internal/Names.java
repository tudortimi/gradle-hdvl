package com.verificationgentleman.gradle.hdvl.internal;

import org.gradle.util.GUtil;

public abstract class Names {

    public static Names of(String sourceSetName) {
        if (sourceSetName.equals("main"))
            return new NamesForMain();
        return new NamesForOther(sourceSetName);
    }

    public abstract String getGenArgsFileTaskName(String toolName);

    public abstract String getGenFullArgsFileTaskName(String toolName);

    public abstract String getArgsFilesConfigurationName(String toolName);

    private static class NamesForMain extends Names {
        @Override
        public String getGenArgsFileTaskName(String toolName) {
            return "gen" + toolName + "ArgsFile";
        }

        @Override
        public String getGenFullArgsFileTaskName(String toolName) {
            return "genFull" + toolName + "ArgsFile";
        }

        @Override
        public String getArgsFilesConfigurationName(String toolName) {
            return GUtil.toLowerCamelCase(toolName + "ArgsFiles");
        }
    }

    private static class NamesForOther extends Names {
        private final String sourceSetName;

        NamesForOther(String sourceSetName) {
            this.sourceSetName = sourceSetName;
        }

        @Override
        public String getGenArgsFileTaskName(String toolName) {
            return GUtil.toLowerCamelCase("gen" + " " + sourceSetName + "" + toolName + "ArgsFile");
        }

        @Override
        public String getGenFullArgsFileTaskName(String toolName) {
            return GUtil.toLowerCamelCase("genFull" + " " + sourceSetName + "" + toolName + "ArgsFile");
        }

        @Override
        public String getArgsFilesConfigurationName(String toolName) {
            return GUtil.toLowerCamelCase(sourceSetName + " " + toolName + "ArgsFiles");
        }
    }

    public static String getArgsFileName(String sourceSetName, String toolName) {
        String toolNameLower = toolName.toLowerCase();
        if (sourceSetName.equals("main"))
            return toolNameLower + "_" + "args.f";
        return sourceSetName + "_"+ toolNameLower + "_" + "args.f";
    }

    public static String getFullArgsFileName(String sourceSetName, String toolName) {
        String toolNameLower = toolName.toLowerCase();
        if (sourceSetName.equals("main"))
            return "full" + "_" + toolNameLower + "_" + "args.f";
        return "full" + "_" + sourceSetName + "_" + toolNameLower + "_" + "args.f";
    }

    public static String getTestTaskName(String toolName) {
        return GUtil.toLowerCamelCase("testWith" + " " + toolName);
    }

}
