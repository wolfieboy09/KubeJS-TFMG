package dev.wolfieboy09.tfmgjs.registries.mixer;

import com.drmangotea.tfmg.content.machinery.vat.base.registry.operations.VatOperationEntry;
import com.drmangotea.tfmg.content.machinery.vat.industrial_mixer.mode.MixerMode;
import dev.latvian.mods.kubejs.registry.BuilderBase;
import dev.latvian.mods.rhino.util.HideFromJS;
import dev.latvian.mods.rhino.util.RemapForJS;
import dev.latvian.mods.rhino.util.ReturnsSelf;
import dev.wolfieboy09.tfmgjs.registries.PendingEntries;
import dev.wolfieboy09.tfmgjs.wrappers.VatOperationWrapper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Consumer;

@SuppressWarnings("unused")
@ParametersAreNonnullByDefault
public class KubeMixerModeBuilder extends BuilderBase<MixerMode> {
    private final transient PropertyWrapper properties;

    public KubeMixerModeBuilder(ResourceLocation id) {
        super(id);
        this.properties = new PropertyWrapper(id);
    }

    @ReturnsSelf
    public KubeMixerModeBuilder accepts(Item... items) {
        for (Item item : items) {
            PendingEntries.addMixerMode(item.kjs$getIdLocation(), this.id);
        }
        return this;
    }

    @ReturnsSelf
    public KubeMixerModeBuilder properties(Consumer<PropertyWrapper> consumer) {
        consumer.accept(this.properties);
        return this;
    }

    @Override
    public MixerMode createObject() {
        return new MixerMode(this.properties);
    }

    public static class PropertyWrapper extends MixerMode.Properties {
        public PropertyWrapper(ResourceLocation id) {
            super(id);
        }

        @RemapForJS("operation")
        public MixerMode.Properties wrapOperation(Object operation) {
            // Need to call the Mixer mode prop to get it to return correctly on the VatOperationEntry stuff
            return operation(VatOperationWrapper.wrapVatOperationEntry(null, operation));
        }
    }
}
