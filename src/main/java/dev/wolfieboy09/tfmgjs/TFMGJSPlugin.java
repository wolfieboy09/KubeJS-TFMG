package dev.wolfieboy09.tfmgjs;

import com.drmangotea.tfmg.CreateTFMG;
import dev.latvian.mods.kubejs.KubeJSPlugin;
import dev.latvian.mods.kubejs.recipe.schema.RegisterRecipeSchemasEvent;
import dev.wolfieboy09.tfmgjs.recipes.schema.CastingSchema;

public class TFMGJSPlugin extends KubeJSPlugin {
    @Override
    public void registerRecipeSchemas(RegisterRecipeSchemasEvent event) {
        event.namespace(CreateTFMG.MOD_ID)
                .register("casting", CastingSchema.SCHEMA);
    }
}
