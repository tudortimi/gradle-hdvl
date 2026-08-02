# Contribution Guidelines

## Code Style

Java code should adhere to the [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html).

### Copyright Headers

Every source file carries an Apache 2.0 copyright header.
When you modify a file, make sure the year range in its header covers the current year.

- Single year (e.g. `2021`) — extend to a range: `2021-<current year>`.
- Existing range (e.g. `2020-2024`) — update the end year: `2020-<current year>`.
- If the header already ends with the current year, no change is needed.

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

## Changelog policy

Use [Keep a Changelog](https://keepachangelog.com/en/1.1.0/) format in `CHANGELOG.md`.

- Keep PR links next to each user-visible change; do not list internal-only changes.
- For active pre-release trains (for example `0.3.0-beta.1`, `0.3.0-beta.2`), keep separate entries while iterating.
- When the corresponding stable version is released, fold pre-release items into the stable entry to keep the changelog lean.
