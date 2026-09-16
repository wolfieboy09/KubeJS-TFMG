package dev.wolfieboy09.tfmgjs.events.cable;

import com.drmangotea.tfmg.registry.TFMGItems;
import com.tterrag.registrate.util.entry.ItemEntry;
import dev.latvian.mods.kubejs.event.KubeStartupEvent;
import dev.latvian.mods.rhino.util.HideFromJS;
import dev.latvian.mods.rhino.util.ReturnsSelf;
import dev.wolfieboy09.tfmgjs.bridger.ItemEntryCreator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

import java.util.LinkedList;
import java.util.function.Consumer;

public class KubeCableBuilder implements KubeStartupEvent {
    private transient final LinkedList<Builder> BUILDERS = new LinkedList<>();

    public Builder create(ResourceLocation id) {
        Builder builder = new Builder(id);
        BUILDERS.add(builder);
        return builder;
    }

    @HideFromJS
    public LinkedList<Builder> getBuilders() {
        return this.BUILDERS;
    }

    public static class Builder {
        private final transient PropertyWrapper properties;
        private final ResourceLocation id;

        public Builder(ResourceLocation id) {
            this.id = id;
            this.properties = new PropertyWrapper();
        }

        @HideFromJS
        public PropertyWrapper getProperties() {
            return this.properties;
        }

        @HideFromJS
        public ResourceLocation getId() {
            return this.id;
        }

        @ReturnsSelf
        public Builder properties(Consumer<KubeCableBuilder.PropertyWrapper> consumer) {
            consumer.accept(this.properties);
            return this;
        }
    }

    public static class PropertyWrapper {
        private transient int color = 0xFFFFFF;
        private transient float resistivity = 0;
        private transient ItemEntry<?> spool = TFMGItems.COPPER_SPOOL;
        private transient ItemEntry<?> wire = TFMGItems.COPPER_WIRE;

        @ReturnsSelf
        public PropertyWrapper resistivity(float resistivity) {
            this.resistivity = resistivity;
            return this;
        }

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
        public int getColor() {
            return this.color;
        }

        @HideFromJS
        public float getResistivity() {
            return this.resistivity;
        }

        @HideFromJS
        public ItemEntry<?> getSpool() {
            return this.spool;
        }

        @HideFromJS
        public ItemEntry<?> getWire() {
            return this.wire;
        }
    }
}
