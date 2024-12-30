package com.verificationgentleman.gradle.hdvl;

import java.io.File;
import java.util.List;
import java.util.Set;

public interface HDVLCompileSpec {
    List<File> getSvSourceFiles();
    Set<File> getSvPrivateIncludeDirs();
    Set<File> getSvExportedHeaderDirs();
    Set<File> getCSourceFiles();
}
