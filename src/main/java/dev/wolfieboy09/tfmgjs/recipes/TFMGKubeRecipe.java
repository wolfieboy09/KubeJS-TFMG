package dev.wolfieboy09.tfmgjs.recipes;

import com.drmangotea.tfmg.TFMG;
import com.mojang.datafixers.util.Either;
import com.simibubi.create.content.processing.recipe.HeatCondition;
import com.simibubi.create.content.processing.recipe.ProcessingOutput;
import dev.latvian.mods.kubejs.error.KubeRuntimeException;
import dev.latvian.mods.kubejs.recipe.KubeRecipe;
import dev.latvian.mods.kubejs.recipe.RecipeKey;
import dev.latvian.mods.kubejs.recipe.component.RecipeValidationContext;
import dev.latvian.mods.kubejs.util.TickDuration;
import dev.latvian.mods.rhino.Context;
import dev.latvian.mods.rhino.util.HideFromJS;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Shared recipe object used by the data-driven TFMG schemas.
 *
 * <p>Create stores item and fluid values in the same JSON arrays. KubeJS Create
 * represents every entry as an {@link Either}. The exact side is schema-specific,
 * so validation classifies the wrapped value by its actual item/fluid type. Keeping
 * the limits here lets all eight schemas reject recipes before TFMG machines index
 * a missing input or output.</p>
 */
public final class TFMGKubeRecipe extends KubeRecipe {
    private static final Pattern VERSION_PARTS = Pattern.compile("(\\d+)\\.(\\d+)\\.(\\d+)");
    private static final List<String> ALL_VAT_TYPES = List.of(
            "tfmg:steel_vat",
            "tfmg:cast_iron_vat",
            "tfmg:firebrick_lined_vat"
    );

    private static final Map<String, Shape> SHAPES = Map.of(
            "casting", new Shape(0, 0, 1, 1, 1, 1, 0, 0, false),
            "coking", new Shape(1, 1, 0, 0, 1, 1, 2, 2, false),
            "distillation", new Shape(0, 0, 1, 1, 0, 0, 1, 6, false),
            "industrial_blasting", new Shape(1, 2, 0, 0, 0, 0, 2, 3, false),
            "polarizing", new Shape(1, 1, 0, 0, 1, 1, 0, 0, false),
            "winding", new Shape(2, 2, 0, 0, 1, 1, 0, 0, false),
            "hot_blast", new Shape(0, 0, 2, 2, 0, 0, 2, 2, false),
            "vat_machine_recipe", new Shape(0, 4, 0, 4, 0, 4, 0, 4, true)
    );

    @Override
    @HideFromJS
    public void validate(RecipeValidationContext context) {
        validateShape();
        validateSpecialValue("processing_time", get("processing_time"));
        validateSpecialValue("heat_requirement", get("heat_requirement"));
        validateNumericFields();

        // Schema-generated functions and existing JSON can bypass Java helper methods.
        validatePressureAvailability();
    }

    @Override
    @HideFromJS
    public <T> TFMGKubeRecipe setValue(RecipeKey<T> key, T value) {
        validateSpecialValue(key.name, value);

        if ("pressure".equals(key.name) && !supportsPressure()) {
            throw pressureVersionError();
        }

        var previous = getValue(key);
        super.setValue(key, value);

        if ("ingredients".equals(key.name) || "results".equals(key.name)) {
            try {
                validateShapeIfReady();
            } catch (RuntimeException exception) {
                super.setValue(key, previous);
                throw exception;
            }
        }

        return this;
    }

    @Override
    @HideFromJS
    public TFMGKubeRecipe set(Context context, String key, Object value) {
        validateSpecialValue(key, value);

        if ("pressure".equals(key) && !supportsPressure()) {
            throw pressureVersionError();
        }

        var validatesShape = "ingredients".equals(key) || "results".equals(key);
        var previous = validatesShape ? get(key) : null;

        super.set(context, key, value);

        try {
            if (validatesShape) {
                validateShapeIfReady();
            }
        } catch (RuntimeException exception) {
            if (validatesShape) {
                setSchemaValue(key, previous);
            }
            throw exception;
        }

        return this;
    }

    @SuppressWarnings("unused")
    public TFMGKubeRecipe processingTime(int ticks) {
        if (ticks <= 0) {
            throw recipeError("TFMG processing time must be greater than zero");
        }

        return setSchemaValue("processing_time", TickDuration.of(ticks));
    }

    @SuppressWarnings("unused")
    public TFMGKubeRecipe hotAirUsage(int amount) {
        requireType("industrial_blasting", "hotAirUsage");
        if (amount < 0) {
            throw recipeError("TFMG hot air usage cannot be negative");
        }

        return setSchemaValue("hot_air_usage", amount);
    }

    @SuppressWarnings("unused")
    public TFMGKubeRecipe machines(String... machines) {
        requireVatMethod("machines");
        return setSchemaValue("machines", List.of(machines));
    }

    @SuppressWarnings("unused")
    public TFMGKubeRecipe allowedVatTypes(String... types) {
        requireVatMethod("allowedVatTypes");
        return setSchemaValue("allowed_vat_types", List.of(types));
    }

    @SuppressWarnings("unused")
    public TFMGKubeRecipe allowAllVatTypes() {
        requireVatMethod("allowAllVatTypes");
        return setSchemaValue("allowed_vat_types", ALL_VAT_TYPES);
    }

    @SuppressWarnings("unused")
    public TFMGKubeRecipe minSize(int size) {
        requireVatMethod("minSize");
        if (size <= 0) {
            throw recipeError("TFMG vat minimum size must be greater than zero");
        }

        return setSchemaValue("min_size", size);
    }

    @SuppressWarnings("unused")
    public TFMGKubeRecipe heatLevel(int level) {
        requireVatMethod("heatLevel");
        if (level < 0) {
            throw recipeError("TFMG vat heat level cannot be negative");
        }

        return setSchemaValue("heat_level", level);
    }

    @SuppressWarnings("unused")
    public TFMGKubeRecipe pressure(int pressure) {
        requireVatMethod("pressure");
        if (pressure < 0) {
            throw recipeError("TFMG vat pressure cannot be negative");
        }
        if (!supportsPressure()) {
            throw pressureVersionError();
        }

        return setSchemaValue("pressure", pressure);
    }

    @SuppressWarnings("unused")
    private void validateShapeIfReady() {
        if (get("ingredients") != null && get("results") != null) {
            validateShape();
        }
    }

    private void validateShape() {
        var shape = SHAPES.get(recipePath());
        if (shape == null) {
            return;
        }

        var inputs = countEitherList("ingredients");
        var outputs = countEitherList("results");
        try {
            shape.validate(recipePath(), inputs, outputs);
        } catch (KubeRuntimeException exception) {
            markInvalid();
            throw exception;
        }
    }

    private Counts countEitherList(String key) {
        var value = get(key);
        if (!(value instanceof List<?> entries)) {
            throw recipeError("TFMG recipe '" + recipePath() + "' requires '" + key + "' to be a list");
        }

        var fluids = 0;
        var items = 0;

        for (var entry : entries) {
            if (!(entry instanceof Either<?, ?> either)) {
                throw recipeError("TFMG recipe '" + recipePath() + "' has an invalid entry in '" + key + "'");
            }

            var payload = either.map(left -> left, right -> right);
            if (payload instanceof SizedFluidIngredient || payload instanceof FluidStack) {
                fluids++;
            } else if (payload instanceof Ingredient || payload instanceof ProcessingOutput) {
                items++;
            } else {
                throw recipeError("TFMG recipe '" + recipePath() + "' has an empty entry in '" + key + "'");
            }
        }

        return new Counts(items, fluids);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private TFMGKubeRecipe setSchemaValue(String keyName, Object value) {
        var key = (RecipeKey) type.schemaType.schema.getKey(keyName);
        setValue(key, value);
        return this;
    }

    private void requireVatMethod(String method) {
        requireType("vat_machine_recipe", method);
    }

    private void validateSpecialValue(String key, Object value) {
        if ("processing_time".equals(key)) {
            var ticks = value instanceof TickDuration(long ticks1)
                    ? ticks1
                    : value instanceof Number number ? number.longValue() : 1L;
            if (ticks <= 0L) {
                throw recipeError("TFMG processing time must be greater than zero");
            }
        }

        if ("heat_requirement".equals(key) && hasHeatRequirement(value) && !supportsHeat()) {
            throw recipeError(
                    "TFMG recipe '" + recipePath() + "' does not support .heated() or .superheated()"
            );
        }
    }

    private boolean supportsHeat() {
        var path = recipePath();
        return "distillation".equals(path) || "vat_machine_recipe".equals(path);
    }

    private void validateNumericFields() {
        if ("industrial_blasting".equals(recipePath())) {
            requireNonNegative("hot_air_usage", "TFMG hot air usage cannot be negative");
        }

        if (isVatRecipe()) {
            requireNonNegative("min_size", "TFMG vat minimum size cannot be negative");
            requireNonNegative("heat_level", "TFMG vat heat level cannot be negative");
            requireNonNegative("pressure", "TFMG vat pressure cannot be negative");
        }
    }

    private void requireNonNegative(String key, String message) {
        if (get(key) instanceof Number number && number.longValue() < 0L) {
            throw recipeError(message);
        }
    }

    private void validatePressureAvailability() {
        if (!isVatRecipe() || supportsPressure()) {
            return;
        }

        var pressure = get("pressure");
        var typedPressureWasSet = pressure instanceof Number number && number.longValue() != 0L;
        var jsonPressureWasSet = json != null && json.has("pressure");
        if (typedPressureWasSet || jsonPressureWasSet) {
            throw pressureVersionError();
        }
    }

    private static boolean hasHeatRequirement(Object value) {
        if (value instanceof HeatCondition condition) {
            return condition != HeatCondition.NONE;
        }

        return value != null && !"none".equalsIgnoreCase(value.toString());
    }

    private void requireType(String expected, String method) {
        if (!expected.equals(recipePath())) {
            throw recipeError("." + method + "() is only available for tfmg:" + expected + " recipes");
        }
    }

    private void markInvalid() {
        removed = true;
        creationError = true;
    }

    private KubeRuntimeException recipeError(String message) {
        markInvalid();
        return new KubeRuntimeException(message);
    }

    private boolean isVatRecipe() {
        return "vat_machine_recipe".equals(recipePath());
    }

    private String recipePath() {
        return type == null ? "unknown" : type.id.getPath();
    }

    private static boolean supportsPressure() {
        var modList = ModList.get();
        if (modList == null) {
            return false;
        }

        var version = modList.getModContainerById(TFMG.MOD_ID)
                .map(container -> container.getModInfo().getVersion().toString())
                .orElse("");
        var matcher = VERSION_PARTS.matcher(version);
        if (!matcher.find()) {
            return false;
        }

        var major = Integer.parseInt(matcher.group(1));
        var minor = Integer.parseInt(matcher.group(2));
        var patch = Integer.parseInt(matcher.group(3));
        return major > 1 || major == 1 && (minor > 2 || minor == 2 && patch >= 2);
    }

    private KubeRuntimeException pressureVersionError() {
        return recipeError("TFMG vat pressure requires TFMG 1.2.2 or newer");
    }

    private record Counts(int items, int fluids) {
    }

    private record Shape(
            int minItemInputs,
            int maxItemInputs,
            int minFluidInputs,
            int maxFluidInputs,
            int minItemOutputs,
            int maxItemOutputs,
            int minFluidOutputs,
            int maxFluidOutputs,
            boolean requireAnyInputAndOutput
    ) {
        private void validate(String type, Counts inputs, Counts outputs) {
            check(type, "item inputs", inputs.items, minItemInputs, maxItemInputs);
            check(type, "fluid inputs", inputs.fluids, minFluidInputs, maxFluidInputs);
            check(type, "item outputs", outputs.items, minItemOutputs, maxItemOutputs);
            check(type, "fluid outputs", outputs.fluids, minFluidOutputs, maxFluidOutputs);

            if (requireAnyInputAndOutput && inputs.items + inputs.fluids == 0) {
                throw new KubeRuntimeException("TFMG recipe '" + type + "' requires at least one input");
            }
            if (requireAnyInputAndOutput && outputs.items + outputs.fluids == 0) {
                throw new KubeRuntimeException("TFMG recipe '" + type + "' requires at least one output");
            }
        }

        private static void check(String type, String label, int actual, int minimum, int maximum) {
            if (actual < minimum || actual > maximum) {
                var expected = minimum == maximum ? Integer.toString(minimum) : minimum + ".." + maximum;
                throw new KubeRuntimeException(
                        "TFMG recipe '" + type + "' requires " + expected + " " + label + ", got " + actual
                );
            }
        }
    }
}
