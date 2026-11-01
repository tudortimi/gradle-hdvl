# gradle-hdvl
Family of Gradle plugins that provide support for hardware design and verification languages


## Features

Generates simulator argument files to compile SystemVerilog and C.
These argument files can be integrated into an existing simulation setup.

Currently supports Xcelium and QuestaSim.

Also supports testing using SVUnit and generating DVT projects.


## Requirements

Gradle 8.14 or later.
Gradle 9 is supported as well.


## Notes

The DSL is not yet stable and parts will most definitely change in the future.
The examples serve as de-facto documentation.
The (work-in-progress) user guide and API reference are in `src/docs`.
Build them with `./gradlew docs`;
the result is in `build/docs/site`.

The plugin API is also not yet stable,
so it's probably not a good idea to start writing extensions to it just yet.
