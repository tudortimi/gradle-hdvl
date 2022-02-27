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

package com.verificationgentleman.gradle.hdvl.internal;

import com.verificationgentleman.gradle.hdvl.SourceSet;
import org.gradle.api.model.ObjectFactory;
import org.gradle.util.GUtil;

import javax.inject.Inject;

public abstract class DefaultSourceSet implements SourceSet {
    private final String name;

    @Inject
    public DefaultSourceSet(String name, ObjectFactory objectFactory) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getGenXrunArgsFileTaskName() {
        return name == "main"
                ? "genXrunArgsFile"
                : GUtil.toLowerCamelCase("gen" + " " + name + "" + "XrunArgsFile");
    }

    @Override
    public String getXrunArgsFileName() {
        return name == "main"
            ? "xrun_args.f"
            : name + "_" + "xrun_args.f";
    }

    @Override
    public String getGenQrunArgsFileTaskName() {
        return name == "main"
            ? "genQrunArgsFile"
            : GUtil.toLowerCamelCase("gen" + " " + name + "" + "QrunArgsFile");
    }

    @Override
    public String getQrunArgsFileName() {
        return name == "main"
            ? "qrun_args.f"
            : name + "_" + "qrun_args.f";
    }
}
