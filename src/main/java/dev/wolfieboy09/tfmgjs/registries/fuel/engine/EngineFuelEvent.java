package dev.wolfieboy09.tfmgjs.registries.fuel.engine;

import dev.latvian.mods.kubejs.event.KubeEvent;
import dev.latvian.mods.kubejs.script.ConsoleJS;
import dev.latvian.mods.kubejs.util.KubeResourceLocation;
import dev.latvian.mods.rhino.util.HideFromJS;
import dev.latvian.mods.rhino.util.ReturnsSelf;
import dev.wolfieboy09.tfmgjs.content.WrappedFluid;
import dev.wolfieboy09.tfmgjs.registries.fuel.framework.BasicFuelBuilder;
import net.minecraft.resources.ResourceLocation;

import java.util.LinkedList;

public class EngineFuelEvent implements KubeEvent {
    private final LinkedList<Builder> builders = new LinkedList<>();
    private final LinkedList<ResourceLocation> acceptedItems = new LinkedList<>();

    public Builder fuel(KubeResourceLocation fuelId) {
        Builder b = new Builder(fuelId.wrapped());
        this.builders.add(b);
        return b;
    }

    @HideFromJS
    public LinkedList<Builder> getBuilders() {
        return this.builders;
    }

    @HideFromJS
    public LinkedList<ResourceLocation> getAcceptedItems() {
        return this.acceptedItems;
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
    public static class Builder extends BasicFuelBuilder {
        private float efficiency = 1;
        private float torque = 1;

        public Builder(ResourceLocation id) {
            super(id);
        }

        @HideFromJS
        public LinkedList<ResourceLocation> getAcceptedItems() {
            EngineFuelEvent registered = EngineFuelManager.getEngineFuel(getId());
            if (registered == null) {
                ConsoleJS.SERVER.warn("Engine fuel '%s' was not created in startup scripts, no items will be registered as its cylinders".formatted(id));
                return new LinkedList<>();
            }
            return registered.getAcceptedItems();
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