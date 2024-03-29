# Contribution Guidelines

## Releasing

The Gradle build relies on annotated tags to compute the version.
Creating a tag on GitHub when drafting a new release does not create an annotated tag, surprisingly.
A GitHub tag does have a nice "verified" qualifier,
which can also be achieved by pushing a signed tag:

    git tag -s -a <version> -m ':bookmark: <version>'

To check that Gradle extracts the version properly do:

    ./gradlew version

Push the tag to GitHub and draft a release manually based on this tag:

    git push origin <version>

Publishing to the plugin portal still has to be done manually:

    ./gradlew publishPlugins
