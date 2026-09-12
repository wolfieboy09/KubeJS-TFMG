package dev.wolfieboy09.tfmgjs;

import com.drmangotea.tfmg.TFMGRegistries;
import com.drmangotea.tfmg.content.machinery.vat.base.registry.operations.VatOperation;
import com.drmangotea.tfmg.content.machinery.vat.base.registry.types.VatType;
import com.drmangotea.tfmg.content.machinery.vat.industrial_mixer.IndustrialMixerModels;
import com.drmangotea.tfmg.content.machinery.vat.industrial_mixer.mode.MixerMode;
import com.drmangotea.tfmg.registry.TFMGPartialModels;
import dev.latvian.mods.kubejs.plugin.ClassFilter;
import dev.latvian.mods.kubejs.plugin.KubeJSPlugin;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponentTypeRegistry;
import dev.latvian.mods.kubejs.recipe.schema.RecipeFactoryRegistry;
import dev.latvian.mods.kubejs.registry.BuilderTypeRegistry;
import dev.latvian.mods.kubejs.registry.ServerRegistryRegistry;
import dev.latvian.mods.kubejs.script.BindingRegistry;
import dev.latvian.mods.kubejs.script.TypeWrapperRegistry;
import dev.wolfieboy09.tfmgjs.component.MixerModeComponent;
import dev.wolfieboy09.tfmgjs.component.VatOperationComponent;
import dev.wolfieboy09.tfmgjs.component.VatTypeComponent;
import dev.wolfieboy09.tfmgjs.recipes.TFMGKubeRecipe;
import dev.wolfieboy09.tfmgjs.registries.PendingEntries;
import dev.wolfieboy09.tfmgjs.registries.mixer.KubeMixerModeBuilder;
import dev.wolfieboy09.tfmgjs.registries.vatops.KubeVatOperationBuilder;
import dev.wolfieboy09.tfmgjs.wrappers.VatOperationSpread;
import dev.wolfieboy09.tfmgjs.wrappers.VatOperationWrapper;
import dev.wolfieboy09.tfmgjs.wrappers.VatTypeWrapper;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
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
        registry.register(VatOperationSpread.class, VatOperationWrapper::wrapVatOperation);
        registry.register(VatType.class, VatTypeWrapper::wrapVatType);
    }

    @Override
    public void registerBindings(BindingRegistry bindings) {
        bindings.add("IndustrialMixerModels", IndustrialMixerModels.class);
        bindings.add("TFMGPartialModels", TFMGPartialModels.class);
    }

    @Override
    public void registerBuilderTypes(BuilderTypeRegistry registry) {
        registry.of(TFMGRegistries.VAT_OPERATION, reg -> reg.addDefault(KubeVatOperationBuilder.class, KubeVatOperationBuilder::new));
        registry.of(TFMGRegistries.MIXER_MODE, reg -> reg.addDefault(KubeMixerModeBuilder.class, KubeMixerModeBuilder::new));
    }

    @Override
    public void registerServerRegistries(ServerRegistryRegistry registry) {
        registry.register(TFMGRegistries.VAT_OPERATION, VatOperation.CODEC, VatOperation.class);
        registry.register(TFMGRegistries.MIXER_MODE, TFMGRegistries.MIXER_MODE_REGISTRY.byNameCodec(), MixerMode.class);
    }

    @Override
    public void registerClasses(ClassFilter filter) {
        filter.deny(PendingEntries.class);
    }
}
