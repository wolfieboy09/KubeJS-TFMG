package dev.wolfieboy09.tfmgjs;

import dev.latvian.mods.kubejs.plugin.KubeJSPlugin;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponentTypeRegistry;
import dev.latvian.mods.kubejs.recipe.schema.RecipeFactoryRegistry;
import dev.wolfieboy09.tfmgjs.component.MixerModeComponent;
import dev.wolfieboy09.tfmgjs.recipes.TFMGKubeRecipe;
import net.minecraft.resources.ResourceLocation;

public class TFMGJSPlugin implements KubeJSPlugin {
    public static final ResourceLocation PROCESSING_RECIPE_FACTORY =
            ResourceLocation.fromNamespaceAndPath(TFMGJS.MODID, "processing");

    @Override
    public void registerRecipeFactories(RecipeFactoryRegistry registry) {
        registry.register(PROCESSING_RECIPE_FACTORY, TFMGKubeRecipe.class, TFMGKubeRecipe::new);
    }

    @Override
    public void registerRecipeComponents(RecipeComponentTypeRegistry registry) {
        registry.register(MixerModeComponent.MIXER_MODE);
    }
}
