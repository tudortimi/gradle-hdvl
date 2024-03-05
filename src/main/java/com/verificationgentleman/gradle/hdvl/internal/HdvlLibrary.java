package com.verificationgentleman.gradle.hdvl.internal;

import org.gradle.api.artifacts.PublishArtifact;
import org.gradle.api.attributes.AttributeContainer;
import org.gradle.api.internal.component.DefaultSoftwareComponentVariant;
import org.gradle.api.internal.component.SoftwareComponentInternal;
import org.gradle.api.internal.component.UsageContext;

import javax.inject.Inject;
import java.util.Collections;
import java.util.Set;

public class HdvlLibrary implements SoftwareComponentInternal {
    private final UsageContext variant;

    @Inject
    public HdvlLibrary(String variantName, AttributeContainer attributes, PublishArtifact artifact) {
        this.variant = new DefaultSoftwareComponentVariant(variantName, attributes, Collections.singleton(artifact));
    }

    @Override
    public String getName() {
        return "hdvlLibrary";
    }

    @Override
    public Set<UsageContext> getUsages() {
        return Collections.singleton(variant);
    }
}
