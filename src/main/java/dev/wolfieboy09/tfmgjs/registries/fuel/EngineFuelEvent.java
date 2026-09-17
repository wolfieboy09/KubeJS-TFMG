package dev.wolfieboy09.tfmgjs.registries.fuel;

import dev.latvian.mods.kubejs.event.KubeEvent;
import dev.latvian.mods.rhino.util.HideFromJS;
import dev.latvian.mods.rhino.util.ReturnsSelf;
import dev.wolfieboy09.tfmgjs.content.WrappedFluid;

import java.util.LinkedList;

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

    public static class Builder extends BasicFuelBuilder {
        private float efficiency = 1;
        private float torque = 1;

        public Builder(WrappedFluid fluid) {
            super(fluid);
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
