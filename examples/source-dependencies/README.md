Before building, we need to set up Git repositories for the dependencies:

    cd git-repos
    ../../../gradlew clean generateRepos

These Git repos emulate a hosting site, like GitHub. In a real project, such a step would not be necessary.

The 'settings.gradle' files for the packages look complicated, but the only things that would be needed in a real
project are the `sourceControl { ... }` blocks. Everything else is there to get it working in the plugin's development
environment.
