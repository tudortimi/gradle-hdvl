/*
 * Copyright 2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.verificationgentleman.gradle.hdvl;

import org.gradle.api.Named;
import org.gradle.api.plugins.ExtensionAware;

public interface SourceSet extends Named, ExtensionAware {
    /**
     * Returns the name of the gen xrun args file task for this source set.
     *
     * @return The task name. Never returns null.
     */
    String getGenXrunArgsFileTaskName();

    /**
     * Returns the name of the args file for this source set.
     *
     * @return The task name. Never returns null.
     */
    String getArgsFileName();

    /**
     * Returns the name of the gen qrun args file task for this source set.
     *
     * @return The task name. Never returns null.
     */
    String getGenQrunArgsFileTaskName();

    /**
     * Returns the name of the qrun args file for this source set.
     *
     * @return The task name. Never returns null.
     */
    String getQrunArgsFileName();
}
