package dev.wolfieboy09.tfmgjs;

import com.drmangotea.tfmg.registry.TFMGRecipeTypes;
import com.simibubi.create.content.processing.recipe.StandardProcessingRecipe;
import dev.latvian.mods.kubejs.plugin.KubeJSPlugin;
import dev.latvian.mods.kubejs.recipe.schema.RecipeSchema;
import dev.latvian.mods.kubejs.recipe.schema.RecipeSchemaRegistry;
import dev.wolfieboy09.tfmgjs.recipes.schemas.TFMGRecipeSchema;
import dev.wolfieboy09.tfmgjs.recipes.schemas.casting.CastingSchema;
import dev.wolfieboy09.tfmgjs.recipes.schemas.vat.VatRecipeSchema;

import java.util.HashMap;
import java.util.Map;

public class TFMGJSPlugin implements KubeJSPlugin {
    private static final Map<TFMGRecipeTypes, RecipeSchema> recipeSchemas = new HashMap<>();

    static {
        recipeSchemas.put(TFMGRecipeTypes.CASTING, CastingSchema.SCHEMA);
        recipeSchemas.put(TFMGRecipeTypes.COKING, TFMGRecipeSchema.COKING);
        recipeSchemas.put(TFMGRecipeTypes.DISTILLATION, TFMGRecipeSchema.DISTILLATION);
        recipeSchemas.put(TFMGRecipeTypes.INDUSTRIAL_BLASTING, TFMGRecipeSchema.INDUSTRIAL_BLASTING);
        recipeSchemas.put(TFMGRecipeTypes.POLARIZING, TFMGRecipeSchema.POLARIZING);
        recipeSchemas.put(TFMGRecipeTypes.WINDING, TFMGRecipeSchema.WINDING);
        recipeSchemas.put(TFMGRecipeTypes.HOT_BLAST, TFMGRecipeSchema.HOT_BLASTING);
        recipeSchemas.put(TFMGRecipeTypes.VAT_MACHINE_RECIPE, VatRecipeSchema.VAT);
    }

    @Override
    public void registerRecipeSchemas(RecipeSchemaRegistry registry) {
        for (TFMGRecipeTypes entry : TFMGRecipeTypes.values()) {
            if (entry.getSerializer() instanceof StandardProcessingRecipe<?>) {
                RecipeSchema schema = recipeSchemas.getOrDefault(entry, TFMGRecipeSchema.PROCESSING_DEFAULT);
                registry.register(entry.id, schema);
            }
        }
    }


//    @Override
//    public void init() {
//        TFMGJSRegistryInfo.ELECTRODE.addType("electrode", ElectrodeBuilderJS.class, ElectrodeBuilderJS::new);
//        TFMGJSRegistryInfo.CABLE_TYPE.addType("cable_type", CableSpoolBuilderJS.class, CableSpoolBuilderJS::new);
//    }
}
