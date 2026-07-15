# Migrating KubeJS TFMG scripts from 1.20.1 to 1.21.1

This guide covers the intentional script-facing differences in the NeoForge 1.21.1 port. It does not describe a single multi-version installation: keep 1.20.1 and 1.21.1 in separate Minecraft instances and Git branches.

## Required mods

The 1.21.1 build requires Java 21, NeoForge, KubeJS, KubeJS Create, Create, and Create: The Factory Must Grow. KubeJS Create is a new required dependency because it supplies Create's item/fluid recipe components and processing result support.

Both supported profiles have passed dedicated-server startup and explicit recipe reloads on NeoForge 21.1.233 with KubeJS builds 368 and 370. TFMG 1.2.2 with Create 6.0.10 is the `current` profile and adds VAT pressure. Client, JEI, and actual machine execution still require an interactive release check on NeoForge 21.1.233. Do not mix versions across profiles without testing the complete set.

## What stays familiar

- Recipe IDs remain in the `tfmg` namespace.
- Recipes are still added inside `ServerEvents.recipes(event => { ... })`.
- `heated()`, `superheated()`, `processingTime(int)`, `machines(...)`, `allowedVatTypes(...)`, `allowAllVatTypes()`, and `minSize(int)` remain camelCase script methods where applicable.
- Item and fluid tags, sized ingredients, item data components, and chance outputs are handled by KubeJS Create.

KubeJS Create represents a sized item ingredient by repeating its underlying ingredient in the serialized list. The repeated entries count toward TFMG's item-input limit. For example, `Ingredient.of('minecraft:raw_iron').withCount(2)` is valid for Industrial Blasting's two-item limit, but a count of two is not valid for Coking's one-item limit.

The retained constructors are input-first:

| Recipe | Constructor |
|---|---|
| Casting | `casting(ingredients, results[, processingTime])` |
| Coking | `coking(ingredients, results[, processingTime])` |
| Distillation | `distillation(ingredients, results[, processingTime])` |
| Industrial Blasting | `industrial_blasting(ingredients, results[, processingTime])` |
| Polarizing | `polarizing(ingredients, results)` |
| Winding | `winding(ingredients, results[, processingTime])` |
| Hot Blast | `hot_blast(ingredients, results[, processingTime])` |
| VAT Machine | `vat_machine_recipe(ingredients, results)` |

`ingredients` and `results` are arrays even where TFMG requires exactly one value. The optional positional time exists for the six constructors shown above (all except Polarizing and VAT); `.processingTime(ticks)` is available on every type and is the only supported way to set Polarizing time.

## Intentional breaking changes

### Casting has one item output

The old KubeJS TFMG documentation advertised up to three item outputs. TFMG 1.21.1's `CastingRecipe` accepts one. Split a multi-output script into multiple recipes or choose one output; the port will reject more than one instead of producing a recipe that TFMG cannot execute safely.

### Polarizing no longer has energy

The old third positional argument was documented as FE. TFMG 1.21.1 has no recipe energy field. It is not silently reinterpreted as processing time. Remove the old energy value and set time explicitly with `processingTime(int)`.

```js
// 1.20.1: the third argument was energy
event.recipes.tfmg.polarizing(['minecraft:iron_ingot'], ['minecraft:compass'], 400)

// 1.21.1: time is explicit and energy is gone
event.recipes.tfmg.polarizing(['minecraft:iron_ingot'], ['minecraft:compass'])
  .processingTime(60)
```

### JSON field names use snake_case

Create 6 and TFMG codecs use keys such as `processing_time`, `heat_requirement`, and `hot_air_usage`. KubeJS chain methods remain camelCase. Scripts should use methods rather than manually assigning raw codec fields unless a fixture specifically tests raw JSON.

### Processing inputs and outputs share lists

Internally, Create 6 stores mixed item/fluid data in `ingredients` and `results`. This is why old 1.20.1 Java wrappers are not part of the port. The constructor and chain methods still expose typed KubeJS values and validate the actual TFMG limits.

## Recipe limit changes

| Recipe | Valid 1.21.1 shape |
|---|---|
| `casting` | exactly 1 fluid input and 1 item output |
| `coking` | exactly 1 item input, 1 item output, and 2 fluid outputs |
| `distillation` | exactly 1 fluid input and 1–6 fluid outputs |
| `industrial_blasting` | 1–2 item inputs and 2–3 fluid outputs |
| `polarizing` | exactly 1 item input and 1 item output |
| `winding` | exactly 2 item inputs and 1 item output |
| `hot_blast` | exactly 2 fluid inputs and 2 fluid outputs |
| `vat_machine_recipe` | up to four of each item/fluid input/output; the combined input and output sides must both be non-empty |

Runnable examples for every type are stored in [`tests/fixtures/kubejs`](tests/fixtures/kubejs/README.md). Copy the positive scripts to a dedicated KubeJS test instance rather than treating them as a datapack.

## Industrial blasting

Set Create processing time with `processingTime(int)`. Use `hotAirUsage(int)` for TFMG's additional `hot_air_usage` codec field. It is independent from processing time.

## VAT recipes

The port preserves these convenience methods:

- `machines(...)` keeps order and duplicates; repeated electrodes may be meaningful;
- `allowedVatTypes(...)` restricts the accepted VAT material/type;
- `allowAllVatTypes()` resets that restriction to all supported types;
- `minSize(int)` writes `min_size`;
- `heatLevel(int)` writes `heat_level`;
- `processingTime(int)` writes `processing_time`.

`pressure(int)` is available only with TFMG 1.2.2 or newer. On the stable TFMG 1.2.0 profile, using it raises a clear recipe-load error. Do not use it in a pack that must run on both profiles.

## Migration checklist

1. Install KubeJS Create alongside the other required mods.
2. Copy scripts, then remove the old Polarizing energy argument.
3. Replace multi-output Casting recipes.
4. Check Industrial Blasting input/output counts and move hot-air consumption to `hotAirUsage(int)`.
5. Check every VAT recipe for an explicit non-empty input and output, and gate `pressure(int)` to TFMG 1.2.2+ packs.
6. Start a dedicated server and run `/reload`; a successful Gradle build alone cannot validate runtime recipe codecs.
7. Inspect the recipes in JEI and execute each one in its TFMG machine before migrating a production world.

## Removed experimental API

The cable and electrode registration builders are not included. Their registration calls were already disabled in the released 1.20.1 source, and TFMG 1.21.1 changed the related Registrate lifecycle. Reintroducing them would be a new feature, not a compatibility fix.
