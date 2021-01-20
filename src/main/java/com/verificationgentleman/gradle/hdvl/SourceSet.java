package com.verificationgentleman.gradle.hdvl;

import org.gradle.api.Named;
import org.gradle.api.plugins.ExtensionAware;

public interface SourceSet extends Named, ExtensionAware {
    /**
     * Returns the name of the gen args file task for this source set.
     *
     * @return The task name. Never returns null.
     */
    String getGenArgsFileTaskName();

    /**
     * Returns the name of the args file for this source set.
     *
     * @return The task name. Never returns null.
     */
    String getArgsFileName();
}
