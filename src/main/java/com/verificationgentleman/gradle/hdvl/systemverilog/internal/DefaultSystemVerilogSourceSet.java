/*
 * Copyright 2021-2024 the original author or authors.
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
import com.verificationgentleman.gradle.hdvl.systemverilog.SystemVerilogSourceSet;
import groovy.lang.Closure;
import org.gradle.api.Action;
import org.gradle.api.file.SourceDirectorySet;
import org.gradle.api.model.ObjectFactory;

import javax.annotation.Nullable;
import javax.inject.Inject;

import static org.gradle.util.ConfigureUtil.configure;

public class DefaultSystemVerilogSourceSet implements SystemVerilogSourceSet {
    private final DefaultSystemVerilogSourceDirectorySet sv;
    private final SourceDirectorySet svHeaders;

    @Inject
    public DefaultSystemVerilogSourceSet(String name, ObjectFactory objectFactory) {
        sv = objectFactory.newInstance(DefaultSystemVerilogSourceDirectorySet.class, "sv", "SystemVerilog source");
        sv.srcDir("src/" + name + "/sv");
        sv.getFilter().include("**/*.sv");

        svHeaders = objectFactory.sourceDirectorySet("sv", "SystemVerilog exported headers");
        svHeaders.srcDir("src/" + name + "/sv_headers");
    }

    @Override
    public SystemVerilogSourceDirectorySet getSv() {
        return sv;
    }

    @Override
    public DefaultSystemVerilogSourceSet sv(@Nullable Closure configureClosure) {
        // XXX This is not part of the public Gradle API
        configure(configureClosure, getSv());
        return this;
    }

    @Override
    public DefaultSystemVerilogSourceSet sv(Action<? super SystemVerilogSourceDirectorySet> configureAction) {
        configureAction.execute(getSv());
        return this;
    }

    @Override
    public SourceDirectorySet getSvHeaders() {
        return svHeaders;
    }

    @Override
    public DefaultSystemVerilogSourceSet svHeaders(@Nullable Closure configureClosure) {
        // XXX This is not part of the public Gradle API
        configure(configureClosure, getSvHeaders());
        return this;
    }

    @Override
    public DefaultSystemVerilogSourceSet svHeaders(Action<? super SourceDirectorySet> configureAction) {
        configureAction.execute(getSvHeaders());
        return this;
    }
}
