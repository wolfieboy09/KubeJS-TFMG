package dev.wolfieboy09.tfmgjs.registries.fuel.engine;

import dev.latvian.mods.kubejs.event.KubeEvent;
import dev.latvian.mods.kubejs.util.KubeResourceLocation;
import dev.latvian.mods.rhino.util.HideFromJS;
import dev.latvian.mods.rhino.util.ReturnsSelf;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

public class EngineFuelStartup implements KubeEvent {
    public Builder create(KubeResourceLocation id) {
        EngineFuelEvent registered = new EngineFuelEvent();
        EngineFuelManager.putEngineFuel(id.wrapped(), registered);
        return new Builder(id.wrapped(), registered);
    }

    public static class Builder {
        private final ResourceLocation id;
        private final EngineFuelEvent registered;

        private Builder(ResourceLocation id, EngineFuelEvent registered) {
            this.id = id;
            this.registered = registered;
        }

        @SafeVarargs
        @ReturnsSelf
        public final Builder accepts(Holder<Item>... items) {
            for (Holder<Item> item : items) {
                this.registered.getAcceptedItems().add(item);
            }
            return this;
        }

        @HideFromJS
        public ResourceLocation getId() {
            return this.id;
        }
    }
}