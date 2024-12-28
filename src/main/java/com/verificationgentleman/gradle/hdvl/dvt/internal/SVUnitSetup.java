package com.verificationgentleman.gradle.hdvl.dvt.internal;

import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.OutputDirectory;

public interface SVUnitSetup {
    @InputDirectory
    @Optional
    public DirectoryProperty getTestsRoot();

    @InputFiles
    @Optional
    public ConfigurableFileCollection getSvunitRoot();

    @OutputDirectory
    @Optional
    public DirectoryProperty getWorkingDir();
}
