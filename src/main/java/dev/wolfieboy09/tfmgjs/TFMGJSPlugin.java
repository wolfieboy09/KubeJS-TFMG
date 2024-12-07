package dev.wolfieboy09.tfmgjs;

import com.drmangotea.tfmg.registry.TFMGRecipeTypes;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeSerializer;
import dev.latvian.mods.kubejs.KubeJSPlugin;
import dev.latvian.mods.kubejs.recipe.schema.RecipeSchema;
import dev.latvian.mods.kubejs.recipe.schema.RegisterRecipeSchemasEvent;
import dev.wolfieboy09.tfmgjs.recipes.*;

import java.util.Map;

public class TFMGJSPlugin extends KubeJSPlugin {
    private static final Map<TFMGRecipeTypes, RecipeSchema> schemas = Map.of(
            TFMGRecipeTypes.CASTING, CastingSchema.SCHEMA,
            TFMGRecipeTypes.COKING, CokingSchema.SCHEMA,
            TFMGRecipeTypes.DISTILLATION, DistillationSchema.SCHEMA,
            TFMGRecipeTypes.INDUSTRIAL_BLASTING, IndustrialBlastingSchema.SCHEMA,
            TFMGRecipeTypes.POLARIZING, PolarizingSchema.SCHEMA
    );

    @Override
    public void registerRecipeSchemas(RegisterRecipeSchemasEvent event) {
        for (TFMGRecipeTypes entry : TFMGRecipeTypes.values()) {
            if (entry.getSerializer() instanceof ProcessingRecipeSerializer<?>) {
                RecipeSchema schema = schemas.get(entry);
                event.register(entry.getId(), schema);
            }
        }
    }
}
