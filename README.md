# gradle-hdvl
Family of Gradle plugins that provide support for hardware design and verification languages


## Features

Generates simulator argument files to compile SystemVerilog and C.
These argument files can be integrated into an existing simulation setup.

Currently supports Xcelium and QuestaSim.

Also supports testing using SVUnit and generating DVT projects.


## Notes

The DSL is not yet stable and parts will most definitely change in the future.
The examples serve as de-facto documentation.

The plugin API is also not yet stable,
so it's probably not a good idea to start writing extensions to it just yet.
