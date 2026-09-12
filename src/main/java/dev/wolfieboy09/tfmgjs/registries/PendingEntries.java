package dev.wolfieboy09.tfmgjs.registries;

import com.drmangotea.tfmg.TFMGRegistries;
import com.drmangotea.tfmg.content.machinery.vat.industrial_mixer.mode.MixerMode;
import com.drmangotea.tfmg.registry.TFMGDataComponents;
import dev.latvian.mods.kubejs.script.ConsoleJS;
import dev.latvian.mods.rhino.util.HideFromJS;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.event.ModifyDefaultComponentsEvent;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class PendingEntries {
    private static final Map<ResourceLocation, ResourceLocation> MIXER_MODE = new HashMap<>();

    public static void addMixerMode(ResourceLocation itemId, ResourceLocation modeId) {
        ResourceLocation previous = MIXER_MODE.put(itemId, modeId);
        if (previous != null && !previous.equals(modeId)) {
            ConsoleJS.STARTUP.warn("Item %s already had mixer mode %s, overwriting with %s".formatted(itemId, previous, modeId));
        }
    }

    public static void onModifyDefaultComponents(ModifyDefaultComponentsEvent event) {
        MIXER_MODE.forEach((itemId, modeId) -> {
            Optional<Item> item = BuiltInRegistries.ITEM.getOptional(itemId);
            if (item.isEmpty()) {
                ConsoleJS.STARTUP.error("Could not resolve item %s for mixer mode %s".formatted(itemId, modeId));
                return;
            }

            Holder<MixerMode> holder = resolveMixerMode(modeId);
            if (holder == null) {
                ConsoleJS.STARTUP.error("Could not resolve Mixer Mode %s for item %s".formatted(modeId, itemId));
                return;
            }

            event.modify(item.get(), builder ->
                    builder.set(TFMGDataComponents.MIXER_MODE, new MixerMode.Stored(holder))
            );
        });
    }

    @Nullable
    private static Holder<MixerMode> resolveMixerMode(ResourceLocation id) {
        return TFMGRegistries.MIXER_MODE_REGISTRY
                .getHolder(ResourceKey.create(TFMGRegistries.MIXER_MODE, id))
                .orElse(null);
    }
}