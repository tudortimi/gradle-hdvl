package com.verificationgentleman.gradle.hdvl;

import groovy.lang.Closure;
import org.gradle.api.Action;
import org.gradle.api.Named;
import org.gradle.api.file.SourceDirectorySet;

import javax.annotation.Nullable;

public interface SourceSet extends Named {

    SourceDirectorySet getSv();

    SourceSet sv(@Nullable Closure configureClosure);

    SourceSet sv(Action<? super SourceDirectorySet> configureAction);

    SourceDirectorySet getSvHeaders();

    SourceDirectorySet getC();

    SourceSet c(@Nullable Closure configureClosure);

    SourceSet c(Action<? super SourceDirectorySet> configureAction);
}
