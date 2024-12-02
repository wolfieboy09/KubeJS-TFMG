package dev.wolfieboy09.tfmgjs;

import com.drmangotea.createindustry.CreateTFMG;
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
                .register("industrial_blasting", IndustrialBlastingSchema.SCHEMA)
                .register("polarizing", PolarizingSchema.SCHEMA);
                // .register("gas_blasting", GasBlastingSchema.SCHEMA)
                // .register("welding", WeldingSchema.SCHEMA);
    }
}
