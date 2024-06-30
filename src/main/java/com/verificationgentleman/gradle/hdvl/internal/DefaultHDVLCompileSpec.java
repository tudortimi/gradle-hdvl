/*
 * Copyright 2024 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.verificationgentleman.gradle.hdvl.internal;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.verificationgentleman.gradle.hdvl.HDVLCompileSpec;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class DefaultHDVLCompileSpec implements HDVLCompileSpec {
    private final File[] svSourceFiles;

    private final File[] svPrivateIncludeDirs;

    private final File[] svExportedHeaderDirs;

    @JsonProperty("cSourceFiles")
    private final File[] cSourceFiles;

    public DefaultHDVLCompileSpec(Set<File> svSourceFiles, Set<File> svPrivateIncludeDirs, Set<File> svExportedHeaderDirs,
                Set<File> cSourceFiles) {
        this.svSourceFiles = svSourceFiles.toArray(new File[0]);
        this.svPrivateIncludeDirs = svPrivateIncludeDirs.toArray(new File[0]);
        this.svExportedHeaderDirs = svExportedHeaderDirs.toArray(new File[0]);
        this.cSourceFiles = cSourceFiles.toArray(new File[0]);
    }

    // Needed for JAXB
    @SuppressWarnings("unused")
    private DefaultHDVLCompileSpec() {
        this.svSourceFiles = new File[0];
        this.svPrivateIncludeDirs = new File[0];
        this.svExportedHeaderDirs = new File[0];
        this.cSourceFiles = new File[0];
    }

    @Override
    public Set<File> getSvSourceFiles() {
        return new HashSet<>(Arrays.asList(svSourceFiles));
    }

    @Override
    public Set<File> getSvPrivateIncludeDirs() {
        return new HashSet<>(Arrays.asList(svPrivateIncludeDirs));
    }

    @Override
    public Set<File> getSvExportedHeaderDirs() {
        return new HashSet<>(Arrays.asList(svExportedHeaderDirs));
    }

    @JsonProperty("cSourceFiles")
    @Override
    public Set<File> getCSourceFiles() {
        return new HashSet<>(Arrays.asList(cSourceFiles));
    }
}
