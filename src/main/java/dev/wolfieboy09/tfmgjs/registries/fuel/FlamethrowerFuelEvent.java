package dev.wolfieboy09.tfmgjs.registries.fuel;

import dev.latvian.mods.kubejs.event.KubeEvent;
import dev.latvian.mods.rhino.util.HideFromJS;
import dev.latvian.mods.rhino.util.ReturnsSelf;
import dev.wolfieboy09.tfmgjs.content.WrappedFluid;

import java.util.LinkedList;

public class FlamethrowerFuelEvent implements KubeEvent {
    private final LinkedList<Builder> builders = new LinkedList<>();

    public Builder create(WrappedFluid fluid) {
        Builder builder = new Builder(fluid);
        this.builders.add(builder);
        return builder;
    }

    @HideFromJS
    public LinkedList<BasicFuelBuilder.FuelEntry> construct() {
        LinkedList<BasicFuelBuilder.FuelEntry> result = new LinkedList<>();

        for (Builder b : builders) {
            BasicFuelBuilder.FuelEntry entry = b.construct();
            entry.json().addProperty("amount", b.getAmount());
            entry.json().addProperty("spread", b.getSpread());
            entry.json().addProperty("color", b.getColor());

            result.add(entry);
        }

        return result;
    }

    public static class Builder extends BasicFuelBuilder {
        private float amount = 1;
        private float color = 0xFFFFFF;
        private float spread = 1;

        public Builder(WrappedFluid fluid) {
            super(fluid);
        }

        @ReturnsSelf
        public Builder amount(float amount) {
            this.amount = amount;
            return this;
        }

        @ReturnsSelf
        public Builder color(float color) {
            this.color = color;
            return this;
        }

        @ReturnsSelf
        public Builder spread(float spread) {
            this.spread = spread;
            return this;
        }

        @HideFromJS
        public float getAmount() {
            return this.amount;
        }

        @HideFromJS
        public float getColor() {
            return this.color;
        }

        @HideFromJS
        public float getSpread() {
            return this.spread;
        }
    }
}
