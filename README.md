# KubeJS TFMG

[![CurseForge Downloads](https://img.shields.io/curseforge/dt/1151941)](https://www.curseforge.com/minecraft/mc-mods/kubejs-tfmg)
[![Modrinth Downloads](https://img.shields.io/modrinth/dt/HXlL9LeK)](https://modrinth.com/mod/kubejs-tfmg)

KubeJS integration for [Create: The Factory Must Grow](https://github.com/DrMango14/Create-The_Factory_Must_Grow). The `1.21.1` line targets Minecraft 1.21.1 on NeoForge and provides schemas for Casting, Coking, Distillation, Industrial Blasting, Polarizing, Winding, Hot Blast, and VAT Machine recipes.

The Minecraft 1.20.1 release remains on `main`. Do not combine the two source lines into one mod installation.

## Requirements

- Java 21 and NeoForge 21.1.233
- KubeJS 2101.7.2
- KubeJS Create 2101.3.1
- Create 6
- Create: The Factory Must Grow

The default dependency profile uses TFMG 1.2.0 with Create 6.0.8. The `current` build profile uses TFMG 1.2.2 with Create 6.0.10 and includes a fixture for the version-gated VAT pressure API. Use the complete profile instead of mixing its individual library versions.

## Script examples

Copy the positive examples from [`tests/fixtures/kubejs`](tests/fixtures/kubejs/README.md) into a dedicated KubeJS instance. The same directory contains disabled negative cases used to verify early validation errors.

If you are moving an existing modpack, read [the 1.20.1 → 1.21.1 migration guide](MIGRATION_1.20.1_TO_1.21.1.md). The important intentional changes are a single Casting output, removal of Polarizing's obsolete energy field, and version-gated VAT pressure.

## Building

Windows:

```powershell
.\gradlew.bat build -PtfmgProfile=stable
.\gradlew.bat build -PtfmgProfile=current
```

Linux/macOS:

```sh
./gradlew build -PtfmgProfile=stable
./gradlew build -PtfmgProfile=current
```

`stable` is the default when `tfmgProfile` is omitted. The CI workflow is configured to compile both profiles against KubeJS builds 368 and 370.

A clean local build and dedicated-server `/reload` on NeoForge 21.1.233 passed for all four supported combinations:

| Profile | KubeJS | Runtime result |
|---|---|---|
| `stable` (TFMG 1.2.0 / Create 6.0.8) | build 368 | 8 positive recipes and all 11 negative fixtures passed |
| `current` (TFMG 1.2.2 / Create 6.0.10) | build 368 | 9 positive recipes, including VAT pressure, passed |
| `stable` | build 370 | 8 positive recipes passed |
| `current` | build 370 | 9 positive recipes passed; all 8 schemas exported successfully |

The packaged metadata was also inspected after the final source corrections. Client startup, JEI visibility, and actual execution in TFMG machines must be repeated interactively on NeoForge 21.1.233 before publishing a release.

## Project documentation

- [Migration from Minecraft 1.20.1](MIGRATION_1.20.1_TO_1.21.1.md)
- [Runtime fixture procedure](tests/fixtures/kubejs/README.md)

Please report addon problems in this repository rather than the TFMG support channels.

## License

KubeJS TFMG is available under the [MIT License](LICENSE).
