/*
 * Copyright 2021 the original author or authors.
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

package com.verificationgentleman.gradle.hdvl.c.internal;

import com.verificationgentleman.gradle.hdvl.c.CSourceSet;
import groovy.lang.Closure;
import org.gradle.api.Action;
import org.gradle.api.file.SourceDirectorySet;
import org.gradle.api.model.ObjectFactory;

import javax.annotation.Nullable;
import javax.inject.Inject;

import static org.gradle.util.ConfigureUtil.configure;

public class DefaultCSourceSet implements CSourceSet {
    private final SourceDirectorySet c;

    @Inject
    public DefaultCSourceSet(String name, ObjectFactory objectFactory) {
        c = objectFactory.sourceDirectorySet("c", "C source");
        c.srcDir("src/" + name + "/c");
        c.getFilter().include("**/*.c");
    }

    @Override
    public SourceDirectorySet getC() {
        return c;
    }

    @Override
    public DefaultCSourceSet c(@Nullable Closure configureClosure) {
        // XXX This is not part of the public Gradle API
        configure(configureClosure, getC());
        return this;
    }

    @Override
    public DefaultCSourceSet c(Action<? super SourceDirectorySet> configureAction) {
        configureAction.execute(getC());
        return this;
    }
}
