package dev.wolfieboy09.tfmgjs;

import com.drmangotea.tfmg.CreateTFMG;
import dev.latvian.mods.kubejs.KubeJSPlugin;
import dev.latvian.mods.kubejs.recipe.schema.RegisterRecipeSchemasEvent;
import dev.wolfieboy09.tfmgjs.recipes.schema.*;

public class TFMGJSPlugin extends KubeJSPlugin {
    @Override
    public void registerRecipeSchemas(RegisterRecipeSchemasEvent event) {
        event.namespace(CreateTFMG.MOD_ID)
                .register("casting", CastingSchema.SCHEMA)
                .register("coking", CokingSchema.SCHEMA)
                .register("distillation", DistillationSchema.SCHEMA)
                // .register("gas_blasting", GasBlastingSchema.SCHEMA) //TODO FIX BUG
                .register("industrial_blasting", IndustrialBlastingSchema.SCHEMA)
                .register("polarizing", PolarizingSchema.SCHEMA);
    }
}
