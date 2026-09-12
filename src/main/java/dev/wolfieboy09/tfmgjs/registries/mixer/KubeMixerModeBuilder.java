package dev.wolfieboy09.tfmgjs.registries.mixer;

import com.drmangotea.tfmg.content.machinery.vat.base.registry.operations.VatOperation;
import com.drmangotea.tfmg.content.machinery.vat.industrial_mixer.mode.MixerMode;
import dev.latvian.mods.kubejs.registry.BuilderBase;
import dev.latvian.mods.rhino.util.ReturnsSelf;
import dev.wolfieboy09.tfmgjs.TFMGJS;
import dev.wolfieboy09.tfmgjs.registries.PendingEntries;
import net.minecraft.resources.ResourceLocation;

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

    public KubeMixerModeBuilder accepts(ResourceLocation... items) {
        for (ResourceLocation itemId : items) {
            PendingEntries.addMixerMode(itemId, this.id);
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

        // This is just waiting to explode at some point
        public MixerMode.Properties operation(String name) {
            return operation(TFMGJS.REGISTRATE.vatOperation(name, VatOperation::new).register());
        }
    }
}
