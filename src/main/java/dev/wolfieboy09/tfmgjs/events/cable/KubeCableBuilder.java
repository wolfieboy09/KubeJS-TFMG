package dev.wolfieboy09.tfmgjs.events.cable;

import com.drmangotea.tfmg.content.machinery.misc.winding_machine.SpoolItem;
import com.drmangotea.tfmg.registry.TFMGItems;
import dev.latvian.mods.kubejs.event.KubeStartupEvent;
import dev.latvian.mods.rhino.util.HideFromJS;
import dev.latvian.mods.rhino.util.ReturnsSelf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

import java.util.LinkedList;
import java.util.function.Consumer;
import java.util.function.Supplier;

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
        private transient Supplier<SpoolItem> spool = TFMGItems.COPPER_SPOOL;
        private transient Supplier<Item> wire = TFMGItems.COPPER_WIRE;

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
        public PropertyWrapper spool(SpoolItem spool) {
            this.spool = () -> spool;
            return this;
        }

        @ReturnsSelf
        public PropertyWrapper wire(Item wire) {
            this.wire = () -> wire;
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
        public Supplier<SpoolItem> getSpool() {
            return this.spool;
        }

        @HideFromJS
        public Supplier<Item> getWire() {
            return this.wire;
        }
    }
}
