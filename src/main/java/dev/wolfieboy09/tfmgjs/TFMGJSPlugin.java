package dev.wolfieboy09.tfmgjs;

import com.drmangotea.tfmg.TFMG;
import com.drmangotea.tfmg.TFMGRegistries;
import com.drmangotea.tfmg.content.electricity.connection.cable_type.CableType;
import com.drmangotea.tfmg.content.machinery.vat.base.registry.operations.VatOperation;
import com.drmangotea.tfmg.content.machinery.vat.base.registry.operations.VatOperationEntry;
import com.drmangotea.tfmg.content.machinery.vat.base.registry.types.VatType;
import com.drmangotea.tfmg.content.machinery.vat.electrode_holder.electrode.Electrode;
import com.drmangotea.tfmg.content.machinery.vat.industrial_mixer.IndustrialMixerModels;
import com.drmangotea.tfmg.content.machinery.vat.industrial_mixer.mode.MixerMode;
import com.drmangotea.tfmg.registry.TFMGPartialModels;
import com.google.common.collect.Sets;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import dev.latvian.mods.kubejs.event.EventGroupRegistry;
import dev.latvian.mods.kubejs.generator.KubeDataGenerator;
import dev.latvian.mods.kubejs.plugin.ClassFilter;
import dev.latvian.mods.kubejs.plugin.KubeJSPlugin;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponentTypeRegistry;
import dev.latvian.mods.kubejs.recipe.schema.RecipeFactoryRegistry;
import dev.latvian.mods.kubejs.registry.BuilderTypeRegistry;
import dev.latvian.mods.kubejs.registry.ServerRegistryRegistry;
import dev.latvian.mods.kubejs.script.BindingRegistry;
import dev.latvian.mods.kubejs.script.ScriptType;
import dev.latvian.mods.kubejs.script.TypeWrapperRegistry;
import dev.wolfieboy09.tfmgjs.bridger.ItemEntryCreator;
import dev.wolfieboy09.tfmgjs.component.MixerModeComponent;
import dev.wolfieboy09.tfmgjs.component.VatOperationComponent;
import dev.wolfieboy09.tfmgjs.component.VatTypeComponent;
import dev.wolfieboy09.tfmgjs.content.WrappedFluid;
import dev.wolfieboy09.tfmgjs.events.TFMGJSEvents;
import dev.wolfieboy09.tfmgjs.recipes.TFMGKubeRecipe;
import dev.wolfieboy09.tfmgjs.registries.PendingEntries;
import dev.wolfieboy09.tfmgjs.registries.electrode.KubeElectrodeModeBuilder;
import dev.wolfieboy09.tfmgjs.registries.fuel.BasicFuelBuilder;
import dev.wolfieboy09.tfmgjs.registries.fuel.EngineFuelEvent;
import dev.wolfieboy09.tfmgjs.registries.fuel.FlamethrowerFuelEvent;
import dev.wolfieboy09.tfmgjs.registries.mixer.KubeMixerModeBuilder;
import dev.wolfieboy09.tfmgjs.registries.vatops.KubeVatOperationBuilder;
import dev.wolfieboy09.tfmgjs.wrappers.VatOperationSpread;
import dev.wolfieboy09.tfmgjs.wrappers.VatOperationWrapper;
import dev.wolfieboy09.tfmgjs.wrappers.VatTypeWrapper;
import dev.wolfieboy09.tfmgjs.wrappers.WrappedFluidWrapper;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Set;

@ParametersAreNonnullByDefault
public class TFMGJSPlugin implements KubeJSPlugin {
    public static final ResourceLocation PROCESSING_RECIPE_FACTORY =
            ResourceLocation.fromNamespaceAndPath(TFMGJS.MODID, "processing");

    private static final Set<ResourceLocation> pendingCylinders = Sets.newHashSet();

    public static void addPendingCylinder(ResourceLocation id) {
        pendingCylinders.add(id);
    }

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
        registry.register(VatOperationSpread.class, VatOperationWrapper::wrapVatOperationSpread);
        registry.register(VatOperationEntry.class, VatOperationWrapper::wrapVatOperationEntry);
        registry.register(VatOperation.class, VatOperationWrapper::wrapVatOperation);
        registry.register(VatType.class, VatTypeWrapper::wrapVatType);

        registry.register(WrappedFluid.class, WrappedFluidWrapper::wrapFluid);
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
        registry.of(TFMGRegistries.ELECTRODE, reg -> reg.addDefault(KubeElectrodeModeBuilder.class, KubeElectrodeModeBuilder::new));
    }

    @Override
    public void registerServerRegistries(ServerRegistryRegistry registry) {
        registry.register(TFMGRegistries.VAT_OPERATION, VatOperation.CODEC, VatOperation.class);
        registry.register(TFMGRegistries.MIXER_MODE, TFMGRegistries.MIXER_MODE_REGISTRY.byNameCodec(), MixerMode.class);
        registry.register(TFMGRegistries.ELECTRODE, TFMGRegistries.ELECTRODE_REGISTRY.byNameCodec(), Electrode.class);
        registry.register(TFMGRegistries.CABLE_TYPE, TFMGRegistries.CABLE_TYPE_REGISTRY.byNameCodec(), CableType.class);
    }

    @Override
    public void generateData(KubeDataGenerator generator) {
        if (TFMGJSEvents.ENGINE_FUEL.hasListeners()) {
            EngineFuelEvent engineFuel = new EngineFuelEvent();
            TFMGJSEvents.ENGINE_FUEL.post(ScriptType.SERVER, engineFuel);

            for (BasicFuelBuilder.FuelEntry entry : engineFuel.construct()) {
                generator.json(
                        ResourceLocation.fromNamespaceAndPath(entry.id().getNamespace(), "tfmg/fuel_type/engine/" + entry.id().getPath()),
                        entry.json()
                );
            }

            if (!pendingCylinders.isEmpty()) {
                JsonArray values = new JsonArray();
                pendingCylinders.forEach(id -> values.add(id.toString()));

                JsonObject tagJson = new JsonObject();
                tagJson.addProperty("replace", false);
                tagJson.add("values", values);

                generator.json(
                        TFMG.asResource("tags/item/engine/cylinder"),
                        tagJson
                );

                pendingCylinders.clear();
            }
        }

        if (TFMGJSEvents.FLAMETHROWER_FUEL.hasListeners()) {
            FlamethrowerFuelEvent flamethrowerFuel = new FlamethrowerFuelEvent();
            TFMGJSEvents.FLAMETHROWER_FUEL.post(ScriptType.SERVER, flamethrowerFuel);

            for (BasicFuelBuilder.FuelEntry entry : flamethrowerFuel.construct()) {
                generator.json(
                        ResourceLocation.fromNamespaceAndPath(entry.id().getNamespace(), "tfmg/fuel_type/flamethrower/" + entry.id().getPath()),
                        entry.json()
                );
            }
        }
    }

    @Override
    public void registerEvents(EventGroupRegistry registry) {
        registry.register(TFMGJSEvents.GROUP);
    }

    @Override
    public void registerClasses(ClassFilter filter) {
        filter.deny(PendingEntries.class);
        filter.deny(ItemEntryCreator.class);
    }

    //TODO: WHY REGISTRATE, WHYYYYY
    // Get this to register correctly, and get SpoolItem correctly
    // That's a later problem really
//    @Override
//    public void initStartup() {
//        if (TFMGJSEvents.CABLE_TYPES.hasListeners()) {
//            KubeCableBuilder cableBuilder = new KubeCableBuilder();
//            TFMGJSEvents.CABLE_TYPES.post(ScriptType.STARTUP, cableBuilder);
//            for (KubeCableBuilder.Builder builder : cableBuilder.getBuilders()) {
//                KubeCableBuilder.PropertyWrapper properties = builder.getProperties();
//                TFMGJS.REGISTRATE.cableType(builder.getId().getPath(), CableType::new)
//                        .properties(p -> p
//                                .spool(properties.getSpool())
//                                .wire(properties.getWire())
//                                .color(properties.getColor()))
//                        .transform(TFMGResistivity.setResistivity(properties.getResistivity()))
//                        .register();
//            }
//        }
//    }
}