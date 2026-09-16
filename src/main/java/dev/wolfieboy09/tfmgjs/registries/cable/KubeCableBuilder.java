package dev.wolfieboy09.tfmgjs.registries.cable;

import com.drmangotea.tfmg.content.electricity.connection.cable_type.CableType;
import com.drmangotea.tfmg.registry.TFMGItems;
import com.tterrag.registrate.util.entry.ItemEntry;
import dev.latvian.mods.kubejs.registry.BuilderBase;
import dev.latvian.mods.rhino.util.HideFromJS;
import dev.latvian.mods.rhino.util.ReturnsSelf;
import dev.wolfieboy09.tfmgjs.bridger.ItemEntryCreator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

import java.util.function.Consumer;

public class KubeCableBuilder extends BuilderBase<CableType> {
    private final transient PropertyWrapper properties;

    public KubeCableBuilder(ResourceLocation id) {
        super(id);
        this.properties = new PropertyWrapper(id);
    }

    @ReturnsSelf
    public KubeCableBuilder properties(Consumer<KubeCableBuilder.PropertyWrapper> consumer) {
        consumer.accept(this.properties);
        return this;
    }

    @Override
    public CableType createObject() {
        return new CableType(this.properties.build());
    }

    public static class PropertyWrapper {
        private final ResourceLocation id;
        private int color = 0xFFFFFF;
        // private float resistivity = 0;
        private ItemEntry<?> spool = TFMGItems.COPPER_SPOOL;
        private ItemEntry<?> wire = TFMGItems.COPPER_WIRE;

        public PropertyWrapper(ResourceLocation id) {
            this.id = id;
        }

//        @ReturnsSelf
//        public PropertyWrapper resistivity(float resistivity) {
//            this.resistivity = resistivity;
//            return this;
//        }

        @ReturnsSelf
        public PropertyWrapper color(int color) {
            this.color = color;
            return this;
        }

        @ReturnsSelf
        public PropertyWrapper spool(Item spool) {
            this.spool = ItemEntryCreator.fromItem(spool);
            return this;
        }

        @ReturnsSelf
        public PropertyWrapper wire(Item wire) {
            this.wire = ItemEntryCreator.fromItem(wire);
            return this;
        }

        @HideFromJS
        public CableType.Properties build() {
            return new CableType.Properties(this.id).spool(this.spool).wire(this.wire).color(this.color);
        }
    }
}
