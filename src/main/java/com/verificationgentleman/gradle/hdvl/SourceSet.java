package com.verificationgentleman.gradle.hdvl;

import groovy.lang.Closure;
import org.gradle.api.Action;
import org.gradle.api.Named;
import org.gradle.api.file.SourceDirectorySet;
import org.gradle.api.plugins.ExtensionAware;

import javax.annotation.Nullable;

public interface SourceSet extends Named, ExtensionAware {

    SourceDirectorySet getSv();

    SourceSet sv(@Nullable Closure configureClosure);

    SourceSet sv(Action<? super SourceDirectorySet> configureAction);

    SourceDirectorySet getSvHeaders();

    SourceSet svHeaders(@Nullable Closure configureClosure);

    SourceSet svHeaders(Action<? super SourceDirectorySet> configureAction);

}
