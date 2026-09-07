package dev.wolfieboy09.tfmgjs;

import com.drmangotea.tfmg.content.machinery.vat.base.registry.operations.VatOperation;
import com.drmangotea.tfmg.content.machinery.vat.base.registry.types.VatType;
import dev.latvian.mods.kubejs.plugin.KubeJSPlugin;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponentTypeRegistry;
import dev.latvian.mods.kubejs.recipe.schema.RecipeFactoryRegistry;
import dev.latvian.mods.kubejs.script.TypeWrapperRegistry;
import dev.wolfieboy09.tfmgjs.component.MixerModeComponent;
import dev.wolfieboy09.tfmgjs.component.VatOperationComponent;
import dev.wolfieboy09.tfmgjs.component.VatTypeComponent;
import dev.wolfieboy09.tfmgjs.recipes.TFMGKubeRecipe;
import dev.wolfieboy09.tfmgjs.wrappers.VatOperationWrapper;
import dev.wolfieboy09.tfmgjs.wrappers.VatTypeWrapper;
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
        registry.register(VatOperationComponent.VAT_OPERATION);
        registry.register(VatTypeComponent.VAT_TYPE);
    }

    @Override
    public void registerTypeWrappers(TypeWrapperRegistry registry) {
        registry.register(VatOperation.class, VatOperationWrapper::wrapVatOperation);
        registry.register(VatType.class, VatTypeWrapper::wrapVatType);
    }
}
