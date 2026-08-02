# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Fixed

- **`svunit`**: Fixed argument forwarding in SVUnit toolchain execution so multiple arguments are passed correctly ([#176]).
- **`svunit`**: Fixed a crash when changing test locations by using lazy configuration ([#180]).

## [0.3.0-beta.1] - 2025-11-13

### Fixed

- **`base`**: Fixed race conditions when reconfiguring the build directory in multi-task scenarios ([#172]).

## [0.2.8] - 2025-01-04

### Added

- **`systemverilog`**: Added compile-order controls to pin specific files first/last and match file sets with Ant-style patterns ([#163], [#164], [#165]).
- **`dvt`**: Added DVT generation support for SVUnit tests and multi-project builds, including unit tests in multi-project setups ([#160], [#161], [#162]).

### Fixed

- **`systemverilog`**: Fixed published source metadata handling so configured SystemVerilog compile order is preserved for consumers ([#167]).

## [0.2.7] - 2024-06-16

### Added

- **Source dependencies / archives** (`base`, `systemverilog`, `c`): Added publishing and consuming of HDVL source archives ([#133], [#135]).
- **Source dependencies / archives** (`systemverilog`): Added SystemVerilog compile metadata publication and consumption with sources ([#136], [#139]).
- **Source dependencies / archives** (`systemverilog`, `c`): Added compile-spec JSON serialization to retain richer compilation metadata in exchanged archives ([#142], [#149]).

### Fixed

- **Source dependencies / archives** (`systemverilog`, `c`): Fixed archive handling for private/include directories, exported include directories, header-only projects, C sources, and dependency metadata completeness ([#144], [#145], [#146], [#147], [#151]).
- **`dvt`**: Streamlined DVT project generation for easier project setup ([#121]).
- **General compatibility**: Fixed plugin behavior on Gradle 7.5.1 by addressing multi-release JAR compatibility issues ([#154]).

## [0.2.6] - 2023-07-23

### Fixed

- **`svunit`**: Fixed a crash when `testCompile` depends on another source set ([#120]).

## [0.2.5] - 2023-04-16

### Changed

- **`svunit`**: Added property-based configuration for `runSVUnit` toolchain arguments ([#112]).

### Fixed

- **`svunit`**: Improved reliability of test task up-to-date checking ([#114]).
- **`svunit`**: Fixed handling of the `tests` symlink when running SVUnit ([#115]).

## [0.2.4] - 2023-02-05

### Fixed

- **`base`**: Fixed failures when depending on a project that defines multiple source sets ([#106]).

## [0.2.3] - 2023-01-25

### Fixed

- **`base`**: Ensured the Gradle base plugin is applied from the HDVL base plugin, preventing missing baseline task/configuration behavior ([#104]).

## [0.2.2] - 2023-01-19

### Added

- **`svunit`**: Added support for test utility code and additional `testCompile` dependencies beyond SVUnit itself ([#94], [#97]).

### Fixed

- **`systemverilog`**: Stopped generating empty include-directory arguments for empty private include dir lists ([#91]).

## [0.2.1] - 2022-06-29

### Fixed

- **Publishing / consumption**: Fixed plugin publishing issues to improve artifact consumption reliability ([#60]).

## [0.2.0] - 2022-03-05

### Added

- **`systemverilog`**: Added support for custom source sets ([#49]).
- **`systemverilog`**: Added Questa `qrun` args-file generation and aligned handling between `xrun` and `qrun` flows ([#51], [#52], [#54]).
- **`svunit`**: Added `qrun` support in SVUnit workflows ([#55]).
- **`c`**: Added C-source handling in `qrun` flows ([#56]).
- **`dvt`**: Introduced DVT IDE project-generation plugin ([#48]).

## [0.1.0] - 2021-01-17

### Added

- **`base`**: Initial base plugin with source-set model, project dependencies, and args-file task generation ([#6], [#8], [#9], [#12], [#15], [#43]).
- **`systemverilog`**: Initial SystemVerilog support including include-file handling, include directories, and `makelib` blocks ([#20], [#21], [#23], [#24], [#27], [#40]).
- **`c`**: Initial C source directory integration for DPI-oriented builds ([#22]).
- **`svunit`** and **`svunit-build`**: Initial SVUnit integration and build plugin split/toolchain support ([#29], [#30], [#31]).

[Unreleased]: https://github.com/tudortimi/gradle-hdvl/compare/v0.3.0-beta.1...HEAD
[0.3.0-beta.1]: https://github.com/tudortimi/gradle-hdvl/compare/v0.2.8...v0.3.0-beta.1
[0.2.8]: https://github.com/tudortimi/gradle-hdvl/compare/v0.2.7...v0.2.8
[0.2.7]: https://github.com/tudortimi/gradle-hdvl/compare/v0.2.6...v0.2.7
[0.2.6]: https://github.com/tudortimi/gradle-hdvl/compare/v0.2.5...v0.2.6
[0.2.5]: https://github.com/tudortimi/gradle-hdvl/compare/v0.2.4...v0.2.5
[0.2.4]: https://github.com/tudortimi/gradle-hdvl/compare/v0.2.3...v0.2.4
[0.2.3]: https://github.com/tudortimi/gradle-hdvl/compare/v0.2.2...v0.2.3
[0.2.2]: https://github.com/tudortimi/gradle-hdvl/compare/v0.2.1...v0.2.2
[0.2.1]: https://github.com/tudortimi/gradle-hdvl/compare/v0.2.0...v0.2.1
[0.2.0]: https://github.com/tudortimi/gradle-hdvl/compare/v0.1.0...v0.2.0
[0.1.0]: https://github.com/tudortimi/gradle-hdvl/compare/511ccf6...v0.1.0

[#180]: https://github.com/tudortimi/gradle-hdvl/pull/180
[#176]: https://github.com/tudortimi/gradle-hdvl/pull/176
[#172]: https://github.com/tudortimi/gradle-hdvl/pull/172
[#167]: https://github.com/tudortimi/gradle-hdvl/pull/167
[#165]: https://github.com/tudortimi/gradle-hdvl/pull/165
[#164]: https://github.com/tudortimi/gradle-hdvl/pull/164
[#163]: https://github.com/tudortimi/gradle-hdvl/pull/163
[#162]: https://github.com/tudortimi/gradle-hdvl/pull/162
[#161]: https://github.com/tudortimi/gradle-hdvl/pull/161
[#160]: https://github.com/tudortimi/gradle-hdvl/pull/160
[#154]: https://github.com/tudortimi/gradle-hdvl/pull/154
[#151]: https://github.com/tudortimi/gradle-hdvl/pull/151
[#149]: https://github.com/tudortimi/gradle-hdvl/pull/149
[#147]: https://github.com/tudortimi/gradle-hdvl/pull/147
[#146]: https://github.com/tudortimi/gradle-hdvl/pull/146
[#145]: https://github.com/tudortimi/gradle-hdvl/pull/145
[#144]: https://github.com/tudortimi/gradle-hdvl/pull/144
[#142]: https://github.com/tudortimi/gradle-hdvl/pull/142
[#139]: https://github.com/tudortimi/gradle-hdvl/pull/139
[#136]: https://github.com/tudortimi/gradle-hdvl/pull/136
[#135]: https://github.com/tudortimi/gradle-hdvl/pull/135
[#133]: https://github.com/tudortimi/gradle-hdvl/pull/133
[#121]: https://github.com/tudortimi/gradle-hdvl/pull/121
[#120]: https://github.com/tudortimi/gradle-hdvl/pull/120
[#115]: https://github.com/tudortimi/gradle-hdvl/pull/115
[#114]: https://github.com/tudortimi/gradle-hdvl/pull/114
[#112]: https://github.com/tudortimi/gradle-hdvl/pull/112
[#106]: https://github.com/tudortimi/gradle-hdvl/pull/106
[#104]: https://github.com/tudortimi/gradle-hdvl/pull/104
[#97]: https://github.com/tudortimi/gradle-hdvl/pull/97
[#94]: https://github.com/tudortimi/gradle-hdvl/pull/94
[#91]: https://github.com/tudortimi/gradle-hdvl/pull/91
[#60]: https://github.com/tudortimi/gradle-hdvl/pull/60
[#56]: https://github.com/tudortimi/gradle-hdvl/pull/56
[#55]: https://github.com/tudortimi/gradle-hdvl/pull/55
[#54]: https://github.com/tudortimi/gradle-hdvl/pull/54
[#52]: https://github.com/tudortimi/gradle-hdvl/pull/52
[#51]: https://github.com/tudortimi/gradle-hdvl/pull/51
[#49]: https://github.com/tudortimi/gradle-hdvl/pull/49
[#48]: https://github.com/tudortimi/gradle-hdvl/pull/48
[#43]: https://github.com/tudortimi/gradle-hdvl/pull/43
[#40]: https://github.com/tudortimi/gradle-hdvl/pull/40
[#31]: https://github.com/tudortimi/gradle-hdvl/pull/31
[#30]: https://github.com/tudortimi/gradle-hdvl/pull/30
[#29]: https://github.com/tudortimi/gradle-hdvl/pull/29
[#27]: https://github.com/tudortimi/gradle-hdvl/pull/27
[#24]: https://github.com/tudortimi/gradle-hdvl/pull/24
[#23]: https://github.com/tudortimi/gradle-hdvl/pull/23
[#22]: https://github.com/tudortimi/gradle-hdvl/pull/22
[#21]: https://github.com/tudortimi/gradle-hdvl/pull/21
[#20]: https://github.com/tudortimi/gradle-hdvl/pull/20
[#15]: https://github.com/tudortimi/gradle-hdvl/pull/15
[#12]: https://github.com/tudortimi/gradle-hdvl/pull/12
[#9]: https://github.com/tudortimi/gradle-hdvl/pull/9
[#8]: https://github.com/tudortimi/gradle-hdvl/pull/8
[#6]: https://github.com/tudortimi/gradle-hdvl/pull/6
