/*
 * Copyright 2024-2026 the original author or authors.
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

import com.verificationgentleman.gradle.hdvl.systemverilog.FileOrder;
import com.verificationgentleman.gradle.hdvl.systemverilog.SystemVerilogSourceDirectorySet;
import groovy.lang.Closure;
import org.gradle.api.Action;
import org.gradle.api.Task;
import org.gradle.api.file.*;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Provider;
import org.gradle.api.specs.Spec;
import org.gradle.api.tasks.TaskDependency;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.util.PatternFilterable;

import javax.inject.Inject;
import java.io.File;
import java.io.Serializable;
import java.util.Iterator;
import java.util.Set;
import java.util.function.Function;

public class DefaultSystemVerilogSourceDirectorySet implements SystemVerilogSourceDirectorySet {
    private final SourceDirectorySet delegate;
    private final Order order;

    @Inject
    public DefaultSystemVerilogSourceDirectorySet(String name, String displayName, ObjectFactory objectFactory) {
        delegate = objectFactory.sourceDirectorySet(name, displayName);
        order = new Order();
    }

    @Override
    public FileOrder getOrder() {
        return order;
    }

    @Override
    public SystemVerilogSourceDirectorySet order(Action<FileOrder> configureAction) {
        configureAction.execute(getOrder());
        return this;
    }

    @Override
    public String getName() {
        return delegate.getName();
    }

    @Override
    public String getDisplayName() {
        return delegate.getDisplayName();
    }

    @Override
    public SourceDirectorySet srcDir(Object srcPath) {
        delegate.srcDir(srcPath);
        return this;
    }

    @Override
    public SourceDirectorySet srcDirs(Object... srcPaths) {
        delegate.srcDirs(srcPaths);
        return this;
    }

    @Override
    public Set<File> getSrcDirs() {
        return delegate.getSrcDirs();
    }

    @Override
    public SourceDirectorySet setSrcDirs(Iterable<?> srcPaths) {
        delegate.setSrcDirs(srcPaths);
        return this;
    }

    @Override
    public SourceDirectorySet source(SourceDirectorySet source) {
        delegate.source(source);
        return this;
    }

    @Override
    public FileCollection getSourceDirectories() {
        return delegate.getSourceDirectories();
    }

    @Override
    public Set<DirectoryTree> getSrcDirTrees() {
        return delegate.getSrcDirTrees();
    }

    @Override
    public PatternFilterable getFilter() {
        return delegate.getFilter();
    }

    @Override
    public DirectoryProperty getDestinationDirectory() {
        return delegate.getDestinationDirectory();
    }

    @Override
    public Provider<Directory> getClassesDirectory() {
        return delegate.getClassesDirectory();
    }

    @Override
    public <T extends Task> void compiledBy(TaskProvider<T> taskProvider, Function<T, DirectoryProperty> mapping) {
        delegate.compiledBy(taskProvider, mapping);
    }

    @Override
    public Set<String> getIncludes() {
        return delegate.getIncludes();
    }

    @Override
    public Set<String> getExcludes() {
        return delegate.getExcludes();
    }

    @Override
    public SourceDirectorySet setIncludes(Iterable<String> includes) {
        delegate.setIncludes(includes);
        return this;
    }

    @Override
    public SourceDirectorySet setExcludes(Iterable<String> excludes) {
        delegate.setExcludes(excludes);
        return this;
    }

    @Override
    public SourceDirectorySet include(String... includes) {
        delegate.include(includes);
        return this;
    }

    @Override
    public SourceDirectorySet include(Iterable<String> includes) {
        delegate.include(includes);
        return this;
    }

    @Override
    public SourceDirectorySet include(Spec<FileTreeElement> includeSpec) {
        delegate.include(includeSpec);
        return this;
    }

    @Override
    public SourceDirectorySet include(Closure includeSpec) {
        delegate.include(includeSpec);
        return this;
    }

    @Override
    public SourceDirectorySet exclude(String... excludes) {
        delegate.exclude(excludes);
        return this;
    }

    @Override
    public SourceDirectorySet exclude(Iterable<String> excludes) {
        delegate.exclude(excludes);
        return this;
    }

    @Override
    public SourceDirectorySet exclude(Spec<FileTreeElement> excludeSpec) {
        delegate.exclude(excludeSpec);
        return this;
    }

    @Override
    public SourceDirectorySet exclude(Closure excludeSpec) {
        delegate.exclude(excludeSpec);
        return this;
    }

    @Override
    public FileTree matching(Closure filterConfigClosure) {
        return delegate.matching(filterConfigClosure);
    }

    @Override
    public FileTree matching(Action<? super PatternFilterable> filterConfigAction) {
        return delegate.matching(filterConfigAction);
    }

    @Override
    public FileTree matching(PatternFilterable patterns) {
        return delegate.matching(patterns);
    }

    @Override
    public FileTree visit(FileVisitor visitor) {
        return delegate.visit(visitor);
    }

    @Override
    public FileTree visit(Closure visitor) {
        return delegate.visit(visitor);
    }

    @Override
    public FileTree visit(Action<? super FileVisitDetails> visitor) {
        return delegate.visit(visitor);
    }

    @Override
    public FileTree plus(FileTree fileTree) {
        return delegate.plus(fileTree);
    }

    @Override
    public FileTree getAsFileTree() {
        return delegate.getAsFileTree();
    }

    @Override
    public Set<File> getFiles() {
        return delegate.getFiles();
    }

    @Override
    public File getSingleFile() throws IllegalStateException {
        return delegate.getSingleFile();
    }

    @Override
    public boolean contains(File file) {
        return delegate.contains(file);
    }

    @Override
    public String getAsPath() {
        return delegate.getAsPath();
    }

    @Override
    public FileCollection plus(FileCollection collection) {
        return delegate.plus(collection);
    }

    @Override
    public FileCollection minus(FileCollection collection) {
        return delegate.minus(collection);
    }

    @Override
    public FileCollection filter(Closure filterClosure) {
        return delegate.filter(filterClosure);
    }

    @Override
    public FileCollection filter(Spec<? super File> filterSpec) {
        return delegate.filter(filterSpec);
    }

    @Override
    public boolean isEmpty() {
        return delegate.isEmpty();
    }

    @Override
    public Provider<Set<FileSystemLocation>> getElements() {
        return delegate.getElements();
    }

    @Override
    public void addToAntBuilder(Object builder, String nodeName, FileCollection.AntType type) {
        delegate.addToAntBuilder(builder, nodeName, type);
    }

    @Override
    public Object addToAntBuilder(Object builder, String nodeName) {
        return delegate.addToAntBuilder(builder, nodeName);
    }

    @Override
    public TaskDependency getBuildDependencies() {
        return delegate.getBuildDependencies();
    }

    @Override
    public Iterator<File> iterator() {
        return delegate.iterator();
    }

    private static class Order implements FileOrder, Serializable {
        private String first;
        private String last;

        @Override
        public String getFirst() {
            return first;
        }

        @Override
        public FileOrder first(String first) {
            this.first = first;
            return this;
        }

        @Override
        public String getLast() {
            return last;
        }

        @Override
        public FileOrder last(String last) {
            this.last = last;
            return this;
        }
    }
}
