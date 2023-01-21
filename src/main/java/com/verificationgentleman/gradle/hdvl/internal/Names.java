package com.verificationgentleman.gradle.hdvl.internal;

import org.gradle.util.GUtil;

import java.util.ArrayList;
import java.util.List;

public abstract class Names {

    public static Names of(String sourceSetName) {
        if (sourceSetName.equals("main"))
            return new NamesForMain();
        return new NamesForOther(sourceSetName);
    }

    public abstract String getGenArgsFileTaskName(String toolName);

    public abstract String getGenFullArgsFileTaskName(String toolName);

    public abstract String getArgsFilesConfigurationName(String toolName);

    public abstract String getArgsFileName(String toolName);

    public abstract String getFullArgsFileName(String toolName);

    private static class NamesForMain extends Names {
        @Override
        public String getGenArgsFileTaskName(String toolName) {
            return toLowerCamelCase("gen", toolName, "argsFile");
        }

        @Override
        public String getGenFullArgsFileTaskName(String toolName) {
            return toLowerCamelCase("genFull", toolName, "ArgsFile");
        }

        @Override
        public String getArgsFilesConfigurationName(String toolName) {
            return toLowerCamelCase(toolName, "ArgsFiles");
        }

        @Override
        public String getArgsFileName(String toolName) {
            return toLowerSnakeCase(toolName, "args.f");
        }

        @Override
        public String getFullArgsFileName(String toolName) {
            return toLowerSnakeCase("full", toolName, "args.f");
        }
    }

    private static class NamesForOther extends Names {
        private final String sourceSetName;

        NamesForOther(String sourceSetName) {
            this.sourceSetName = sourceSetName;
        }

        @Override
        public String getGenArgsFileTaskName(String toolName) {
            return toLowerCamelCase("gen", sourceSetName, toolName, "ArgsFile");
        }

        @Override
        public String getGenFullArgsFileTaskName(String toolName) {
            return toLowerCamelCase("genFull", sourceSetName, toolName, "ArgsFile");
        }

        @Override
        public String getArgsFilesConfigurationName(String toolName) {
            return toLowerCamelCase(sourceSetName, toolName, "ArgsFiles");
        }

        @Override
        public String getArgsFileName(String toolName) {
            return toLowerSnakeCase(sourceSetName, toolName, "args.f");
        }

        @Override
        public String getFullArgsFileName(String toolName) {
            return toLowerSnakeCase("full", sourceSetName, toolName, "args.f");
        }
    }

    public static String getTestTaskName(String toolName) {
        return toLowerCamelCase("testWith", toolName);
    }

    private static String toLowerCamelCase(String ... words) {
        StringBuilder stringBuilder = new StringBuilder();
        for (String word: words)
            stringBuilder.append(word).append(" ");
        return GUtil.toLowerCamelCase(stringBuilder.toString());
    }

    private static String toLowerSnakeCase(String ... words) {
        List<String> lowerCaseWords = new ArrayList<>();
        for (String word: words)
            lowerCaseWords.add(word.toLowerCase());
        return String.join("_", lowerCaseWords);
    }

}
