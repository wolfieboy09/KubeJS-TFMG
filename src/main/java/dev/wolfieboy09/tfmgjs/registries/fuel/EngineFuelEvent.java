package dev.wolfieboy09.tfmgjs.registries.fuel;

import dev.latvian.mods.kubejs.core.RegistryObjectKJS;
import dev.latvian.mods.kubejs.event.KubeEvent;
import dev.latvian.mods.rhino.util.HideFromJS;
import dev.latvian.mods.rhino.util.ReturnsSelf;
import dev.wolfieboy09.tfmgjs.content.WrappedFluid;
import dev.wolfieboy09.tfmgjs.registries.PendingEntries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

import java.util.LinkedList;
import java.util.List;

public class EngineFuelEvent implements KubeEvent {
    private final LinkedList<Builder> builders = new LinkedList<>();

    public Builder create(WrappedFluid fluid) {
        Builder b = new Builder(fluid);
        this.builders.add(b);
        return b;
    }

    @HideFromJS
    public LinkedList<BasicFuelBuilder.FuelEntry> construct() {
        LinkedList<BasicFuelBuilder.FuelEntry> result = new LinkedList<>();

        for (Builder b : builders) {
            BasicFuelBuilder.FuelEntry entry = b.construct();

            entry.json().addProperty("efficiency", b.efficiency);
            entry.json().addProperty("torque", b.torque);

            result.add(entry);
        }

        return result;
    }

    @HideFromJS
    public LinkedList<Builder> getBuilders() {
        return this.builders;
    }

    public static class Builder extends BasicFuelBuilder {
        private float efficiency = 1;
        private float torque = 1;
        private final LinkedList<ResourceLocation> acceptedItems = new LinkedList<>();

        @ReturnsSelf
        public Builder accepts(Item... items) {
            acceptedItems.addAll(fluid.getFluids().stream().map(RegistryObjectKJS::kjs$getIdLocation).toList());
            return this;
        }

        public Builder(WrappedFluid fluid) {
            super(fluid);
        }

        @HideFromJS
        public LinkedList<ResourceLocation> getAcceptedItems() {
            return this.acceptedItems;
        }

        @ReturnsSelf
        public Builder efficiency(float efficiency) {
            this.efficiency = efficiency;
            return this;
        }

        @ReturnsSelf
        public Builder torque(float torque) {
            this.torque = torque;
            return this;
        }

        @HideFromJS
        public float getEfficiency() {
            return this.efficiency;
        }

        @HideFromJS
        public float getTorque() {
            return this.torque;
        }
    }
}
