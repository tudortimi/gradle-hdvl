/*
 * Copyright 2021-2026 the original author or authors.
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
import com.verificationgentleman.gradle.hdvl.svunit.ToolChains;
import org.gradle.api.Action;
import org.gradle.api.model.ObjectFactory;

import javax.inject.Inject;

public class DefaultToolChains implements ToolChains {
    private DefaultRunSVUnitToolChain runSVUnit;

    @Inject
    public DefaultToolChains(ObjectFactory objects) {
        runSVUnit = objects.newInstance(DefaultRunSVUnitToolChain.class);
    }

    @Override
    public RunSVUnitToolChain getRunSVUnit() {
        return runSVUnit;
    }

    @Override
    public ToolChains runSVUnit(Action<? super RunSVUnitToolChain> configureAction) {
        configureAction.execute(getRunSVUnit());
        return this;
    }
}
