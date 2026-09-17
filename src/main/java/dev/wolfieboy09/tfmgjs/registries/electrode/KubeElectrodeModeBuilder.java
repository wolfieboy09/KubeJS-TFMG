package dev.wolfieboy09.tfmgjs.registries.electrode;

import com.drmangotea.tfmg.content.machinery.vat.base.registry.operations.VatOperationEntry;
import com.drmangotea.tfmg.content.machinery.vat.electrode_holder.electrode.Electrode;
import com.drmangotea.tfmg.registry.TFMGVatOperations;
import dev.latvian.mods.kubejs.registry.BuilderBase;
import dev.latvian.mods.rhino.util.HideFromJS;
import dev.latvian.mods.rhino.util.ReturnsSelf;
import dev.wolfieboy09.tfmgjs.registries.PendingEntries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Consumer;

@ParametersAreNonnullByDefault
public class KubeElectrodeModeBuilder extends BuilderBase<Electrode> {
    private final PropertyWrapper properties;
    private VatTickTask tickTask = (vat, level, pos, active, clientTick) -> {};

    public KubeElectrodeModeBuilder(ResourceLocation id) {
        super(id);
        this.properties = new PropertyWrapper(id);
    }

    @ReturnsSelf
    public KubeElectrodeModeBuilder accepts(Item... items) {
        for (Item item : items) {
            PendingEntries.addElectrodeItem(item.kjs$getIdLocation(), this.id);
        }
        return this;
    }

    @ReturnsSelf
    public KubeElectrodeModeBuilder onTick(VatTickTask task) {
        this.tickTask = task;
        return this;
    }

    @ReturnsSelf
    public KubeElectrodeModeBuilder properties(Consumer<PropertyWrapper> consumer) {
        consumer.accept(this.properties);
        return this;
    }

    @Override
    public KubeElectrodeExtension createObject() {
        return new KubeElectrodeExtension(this.properties.buildProperties(), this.tickTask);
    }

    public static class PropertyWrapper {
        private final ResourceLocation id;

        private int resistance = 0;
        private VatOperationEntry operation = TFMGVatOperations.NONE;

        public PropertyWrapper(ResourceLocation id) {
            this.id = id;
        }

        @ReturnsSelf
        public PropertyWrapper resistance(int resistance) {
            this.resistance = resistance;
            return this;
        }

        @ReturnsSelf
        public PropertyWrapper operation(VatOperationEntry operation) {
            this.operation = operation;
            return this;
        }

        @HideFromJS
        public Electrode.Properties buildProperties() {
            return new Electrode.Properties(this.id).operationId(this.operation).resistance(this.resistance);
        }
    }
}
