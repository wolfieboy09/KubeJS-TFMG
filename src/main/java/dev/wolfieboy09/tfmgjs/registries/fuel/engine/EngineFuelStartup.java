package dev.wolfieboy09.tfmgjs.registries.fuel.engine;

import dev.latvian.mods.kubejs.event.KubeEvent;
import dev.latvian.mods.kubejs.util.KubeResourceLocation;
import dev.latvian.mods.rhino.util.HideFromJS;
import dev.latvian.mods.rhino.util.ReturnsSelf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

import java.util.LinkedList;

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

        @ReturnsSelf
        public Builder accepts(Item... items) {
            for (Item item : items) {
                this.registered.getAcceptedItems().add(item.kjs$getIdLocation());
            }
            return this;
        }

        @HideFromJS
        public ResourceLocation getId() {
            return this.id;
        }

        @HideFromJS
        public LinkedList<ResourceLocation> getAcceptedItems() {
            return this.registered.getAcceptedItems();
        }
    }
}