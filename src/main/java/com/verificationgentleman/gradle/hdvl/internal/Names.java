package com.verificationgentleman.gradle.hdvl.internal;

import org.gradle.util.GUtil;

public abstract class Names {

    public static Names of(String sourceSetName) {
        return new DefaultNames(sourceSetName);
    }

    public abstract String getGenArgsFileTaskName(String toolName);

    public abstract String getGenFullArgsFileTaskName(String toolName);

    public abstract String getArgsFilesConfigurationName(String toolName);

    private static class DefaultNames extends Names {
        private final String sourceSetName;

        DefaultNames(String sourceSetName) {
            this.sourceSetName = sourceSetName;
        }

        @Override
        public String getGenArgsFileTaskName(String toolName) {
            if (sourceSetName.equals("main"))
                return "gen" + toolName + "ArgsFile";
            return GUtil.toLowerCamelCase("gen" + " " + sourceSetName + "" + toolName + "ArgsFile");
        }

        @Override
        public String getGenFullArgsFileTaskName(String toolName) {
            if (sourceSetName.equals("main"))
                return "genFull" + toolName + "ArgsFile";
            return GUtil.toLowerCamelCase("genFull" + " " + sourceSetName + "" + toolName + "ArgsFile");
        }

        @Override
        public String getArgsFilesConfigurationName(String toolName) {
            if (sourceSetName.equals("main"))
                return GUtil.toLowerCamelCase(toolName + "ArgsFiles");
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
