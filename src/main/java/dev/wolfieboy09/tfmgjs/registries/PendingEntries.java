package dev.wolfieboy09.tfmgjs.registries;

import com.drmangotea.tfmg.TFMGRegistries;
import com.drmangotea.tfmg.content.machinery.vat.electrode_holder.electrode.Electrode;
import com.drmangotea.tfmg.content.machinery.vat.industrial_mixer.mode.MixerMode;
import com.drmangotea.tfmg.registry.TFMGDataComponents;
import dev.latvian.mods.kubejs.error.KubeRuntimeException;
import dev.latvian.mods.kubejs.script.ConsoleJS;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.event.ModifyDefaultComponentsEvent;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public class PendingEntries {
    private static final Map<ResourceLocation, ResourceLocation> MIXER_MODE = new LinkedHashMap<>();
    private static final Map<ResourceLocation, ResourceLocation> ELECTRODE_ITEM = new LinkedHashMap<>();

    public static void addMixerMode(ResourceLocation itemId, ResourceLocation modeId) {
        ResourceLocation previous = MIXER_MODE.put(itemId, modeId);
        if (previous != null && !previous.equals(modeId)) {
            ConsoleJS.STARTUP.warn("Item %s already had mixer mode %s, overwriting with %s".formatted(itemId, previous, modeId));
        }
    }

    public static void addElectrodeItem(ResourceLocation itemId, ResourceLocation electrodeId) {
        ResourceLocation previous = ELECTRODE_ITEM.put(itemId, electrodeId);
        if (previous != null && !previous.equals(electrodeId)) {
            ConsoleJS.STARTUP.warn("Item %s already had electrode %s, overwriting with %s".formatted(itemId, previous, electrodeId));
        }
    }

    public static void onModifyDefaultComponents(ModifyDefaultComponentsEvent event) {
        MIXER_MODE.forEach((itemId, modeId) -> {
            Optional<Item> item = BuiltInRegistries.ITEM.getOptional(itemId);
            if (item.isEmpty()) {
                throw new KubeRuntimeException("Could not resolve item %s for mixer mode %s".formatted(itemId, modeId));
            }

            Holder<MixerMode> holder = resolveMixerMode(modeId);
            if (holder == null) {
                throw new KubeRuntimeException("Could not resolve Mixer Mode %s for item %s".formatted(modeId, itemId));
            }

            event.modify(item.get(), builder -> builder.set(TFMGDataComponents.MIXER_MODE, new MixerMode.Stored(holder))
            );
        });

        ELECTRODE_ITEM.forEach((itemId, electrodeId) -> {
            Optional<Item> item = BuiltInRegistries.ITEM.getOptional(itemId);
            if (item.isEmpty()) {
                throw new KubeRuntimeException("Could not resolve item %s for electrode %s".formatted(itemId, electrodeId));
            }

            Holder<Electrode> holder = resolveElectrode(electrodeId);
            if (holder == null) {
                throw new KubeRuntimeException("Could not resolve Electrode %s for item %s".formatted(electrodeId, itemId));
            }

            event.modify(item.get(), builder -> builder.set(TFMGDataComponents.ELECTRODE, new Electrode.Stored(holder)));
        });

        clear();
    }

    private static Holder<MixerMode> resolveMixerMode(ResourceLocation id) {
        return TFMGRegistries.MIXER_MODE_REGISTRY
                .getHolder(ResourceKey.create(TFMGRegistries.MIXER_MODE, id))
                .orElse(null);
    }

    private static Holder<Electrode> resolveElectrode(ResourceLocation id) {
        return TFMGRegistries.ELECTRODE_REGISTRY
                .getHolder(ResourceKey.create(TFMGRegistries.ELECTRODE, id))
                .orElse(null);
    }

    private static void clear() {
        MIXER_MODE.clear();
        ELECTRODE_ITEM.clear();
    }
}