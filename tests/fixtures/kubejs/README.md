# KubeJS runtime fixtures

These files exercise the public KubeJS TFMG 1.21.1 API against a real NeoForge instance. They are runtime fixtures, not Java unit tests: a successful Gradle build cannot prove that Create/TFMG codecs accept the emitted JSON or that a machine consumes the recipe.

## Layout

```text
server_scripts/
├── positive/          # eight recipes verified on both profiles
├── positive-current/  # TFMG 1.2.2+ additions; load only with the current profile
└── negative/          # deliberately invalid; every file ends in .disabled
```

The fixtures use the input-first constructor order retained from 1.20.1:

```js
event.recipes.tfmg.recipe_name(ingredients, results, optionalProcessingTime)
```

Polarizing deliberately has no third positional argument. Use `.processingTime(ticks)`.

## Positive run

1. Build and launch the desired dependency profile.
2. Copy all `.js` files from `server_scripts/positive` to the test instance's `kubejs/server_scripts` directory.
3. For `-PtfmgProfile=current`, also copy `server_scripts/positive-current/09_vat_pressure.js`. Never copy it to a TFMG 1.2.0 instance.
4. Start a dedicated server and run `/reload` once it is ready.
5. Confirm that all IDs below load without schema/codec errors and appear in JEI.
6. Execute each recipe in its matching TFMG machine. Output items and fluids are deliberately artificial so they are easy to distinguish from upstream recipes.

| File | Recipe ID | Extra coverage |
|---|---|---|
| `01_casting.js` | `kubejs:tfmgjs_fixture/casting` | item data components and chance |
| `02_coking.js` | `kubejs:tfmgjs_fixture/coking` | item tag and mixed results |
| `03_distillation.js` | `kubejs:tfmgjs_fixture/distillation` | fluid tag and `heated()` |
| `04_industrial_blasting.js` | `kubejs:tfmgjs_fixture/industrial_blasting` | sized item ingredient, maximum counts, and `hotAirUsage()` |
| `05_polarizing.js` | `kubejs:tfmgjs_fixture/polarizing` | time through a chain method only |
| `06_winding.js` | `kubejs:tfmgjs_fixture/winding` | exact two-item input |
| `07_hot_blast.js` | `kubejs:tfmgjs_fixture/hot_blast` | exact two-fluid input/output |
| `08_vat_machine_recipe.js` | `kubejs:tfmgjs_fixture/vat_machine_recipe` | mixed I/O, heat, repeated machines, `allowAllVatTypes`, `minSize`, and `heatLevel` |
| `09_vat_pressure.js` | `kubejs:tfmgjs_fixture/vat_pressure` | `pressure()` on TFMG 1.2.2+ only |

The item-size example uses `Ingredient.of('minecraft:raw_iron').withCount(2)`; KubeJS Create spreads it into the two item entries accepted by Industrial Blasting. The fluid-tag example uses `Fluid.ingredientOf('#c:water').withAmount(1000)`, which produces a sized fluid ingredient rather than a fluid output stack.

### Automated server run

On Windows, the checked-in harness prepares the scripts, starts the dedicated server, performs an explicit reload, checks the log, and stops the server:

```powershell
powershell.exe -NoProfile -ExecutionPolicy Bypass -File .\tests\runtime\Invoke-TFMGRuntimeTests.ps1 -Profile stable -KubeJSVersion 2101.7.2-build.368 -IncludeNegative
powershell.exe -NoProfile -ExecutionPolicy Bypass -File .\tests\runtime\Invoke-TFMGRuntimeTests.ps1 -Profile current -KubeJSVersion 2101.7.2-build.370 -ExportSchemas
```

Verified server matrix:

| NeoForge | Profile | KubeJS | Positive recipes | Negative fixtures | Explicit reload |
|---|---|---|---:|---:|---|
| 21.1.233 | `stable` | build 368 | 8 | 11/11 | passed |
| 21.1.233 | `current` | build 368 | 9 | not applicable | passed |
| 21.1.233 | `stable` | build 370 | 8 | not repeated | passed |
| 21.1.233 | `current` | build 370 | 9 | not applicable | passed |

All eight schema exports succeeded on NeoForge 21.1.233 with `current` and KubeJS build 370. Client, JEI, and in-machine execution are separate interactive checks and must be repeated on NeoForge 21.1.233.

### Prepare an interactive client

The same harness can prepare the correct scripts without starting a server. Run one profile at a time:

```powershell
powershell.exe -NoProfile -ExecutionPolicy Bypass -File .\tests\runtime\Invoke-TFMGRuntimeTests.ps1 -Profile stable -PrepareOnly
.\gradlew.bat runClient "-PtfmgProfile=stable" "-Pkubejs_version=2101.7.2-build.368"

powershell.exe -NoProfile -ExecutionPolicy Bypass -File .\tests\runtime\Invoke-TFMGRuntimeTests.ps1 -Profile current -PrepareOnly
.\gradlew.bat runClient "-PtfmgProfile=current" "-Pkubejs_version=2101.7.2-build.370"
```

Create a temporary Creative world with cheats, run `/reload`, and inspect each recipe in JEI. Then execute every recipe in the machine/category shown by JEI. Do not reuse a world after switching profiles; the `current` fixture includes VAT pressure data that stable TFMG cannot load.

## Negative run

Do not rename or copy the whole negative directory. Select exactly one file, copy it to `kubejs/server_scripts`, and change only its final extension from `.disabled` to `.js`. Remove it before running another case.

Each case must emit the expected loading error and must not remain in KubeJS's final recipe map. A crash inside a TFMG machine, silent truncation, or a usable invalid recipe is a failed test.

| File | Expected rejection |
|---|---|
| `01_casting_too_many_outputs.js.disabled` | Casting has more than one item output |
| `02_coking_missing_fluid_output.js.disabled` | Coking does not have its required two fluid outputs |
| `03_distillation_too_many_outputs.js.disabled` | Distillation has seven fluid outputs |
| `04_industrial_blasting_too_many_inputs.js.disabled` | Industrial Blasting has three item inputs |
| `05_polarizing_legacy_energy.js.disabled` | obsolete third positional energy argument |
| `06_winding_missing_input.js.disabled` | Winding has only one item input |
| `07_hot_blast_wrong_input_type.js.disabled` | Hot Blast receives an item where a fluid is required |
| `08_vat_empty.js.disabled` | VAT has empty combined input and output sides |
| `09_vat_pressure_on_stable.js.disabled` | `pressure()` is used with TFMG 1.2.0; run this case only on `stable` |
| `10_non_positive_time.js.disabled` | positional processing time is zero |
| `11_unsupported_heat.js.disabled` | `heated()` is used on Casting, which does not support heat |

Nine constructor/shape cases increment KubeJS's `failed recipes` counter. The two chain-method cases, `09_vat_pressure_on_stable` and `11_unsupported_heat`, throw after KubeJS has constructed the recipe object, so KubeJS's raw status line may still say `Added 9 recipes ... with 0 failed recipes`. The addon's validator marks that object as removed and creation-failed; KubeJS excludes it from the final recipe map. The harness verifies the exact error and then verifies that a clean reload returns to the normal `8 recipes ... with 0 failed recipes` state.

## Acceptance record

Record the exact Minecraft, NeoForge, KubeJS, KubeJS Create, Create, and TFMG versions for every run. For each profile capture:

- Gradle build result;
- generated `neoforge.mods.toml` inspection;
- dedicated-server startup and `/reload` result;
- client startup and JEI visibility;
- actual machine execution;
- the expected error for each negative fixture.

Update `PROJECT_CONTEXT.md` with the results before widening dependency ranges or publishing a JAR.
