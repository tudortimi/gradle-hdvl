/*
 * Copyright 2021-2023 the original author or authors.
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

package com.verificationgentleman.gradle.hdvl.svunit.internal;

import com.verificationgentleman.gradle.hdvl.svunit.RunSVUnitToolChain;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ListProperty;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

public class DefaultRunSVUnitToolChain implements RunSVUnitToolChain {
    private ListProperty<String> args;

    @Inject
    public DefaultRunSVUnitToolChain(ObjectFactory objects) {
        args = objects.listProperty(String.class);
    }

    @Override
    public String getDisplayName() {
        return "runSVUnit";
    }

    @Override
    public String getName() {
        return "runSVUnit";
    }

    @Override
    public ListProperty<String> getArgs() {
        return args;
    }

    @Override
    public RunSVUnitToolChain args(String... args) {
        for (String arg : args) {
            this.args.add(arg);
        }
        return this;
    }
}
