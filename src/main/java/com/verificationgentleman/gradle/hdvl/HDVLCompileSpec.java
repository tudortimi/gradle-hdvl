package com.verificationgentleman.gradle.hdvl;

import java.io.File;
import java.util.Set;

public interface HDVLCompileSpec {
    Set<File> getSvSourceFiles();
    Set<File> getSvPrivateIncludeDirs();
}
