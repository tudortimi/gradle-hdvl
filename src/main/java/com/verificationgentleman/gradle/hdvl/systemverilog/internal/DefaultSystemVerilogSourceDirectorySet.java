/*
 * Copyright 2024 the original author or authors.
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

package com.verificationgentleman.gradle.hdvl.systemverilog.internal;

import com.verificationgentleman.gradle.hdvl.systemverilog.SystemVerilogSourceDirectorySet;
import org.gradle.api.internal.file.DefaultSourceDirectorySet;
import org.gradle.api.internal.file.FileCollectionFactory;
import org.gradle.api.internal.file.collections.DirectoryFileTreeFactory;
import org.gradle.api.internal.tasks.TaskDependencyFactory;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.tasks.util.PatternSet;
import org.gradle.internal.Factory;

import javax.inject.Inject;

public class DefaultSystemVerilogSourceDirectorySet extends DefaultSourceDirectorySet implements SystemVerilogSourceDirectorySet {
    // TODO Stop using internal class by using decorator pattern

    private String first;

    @Inject
    public DefaultSystemVerilogSourceDirectorySet(String name, String displayName, Factory<PatternSet> patternSetFactory, TaskDependencyFactory taskDependencyFactory, FileCollectionFactory fileCollectionFactory, DirectoryFileTreeFactory directoryFileTreeFactory, ObjectFactory objectFactory) {
        super(name, displayName, patternSetFactory, taskDependencyFactory, fileCollectionFactory, directoryFileTreeFactory, objectFactory);
    }

    @Override
    public String getFirst() {
        return first;
    }

    @Override
    public SystemVerilogSourceDirectorySet first(String first) {
        this.first = first;
        return this;
    }
}
