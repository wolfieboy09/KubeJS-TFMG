package dev.wolfieboy09.tfmgjs.registries.fuel.engine;

import com.drmangotea.tfmg.registry.TFMGFluids;
import dev.latvian.mods.kubejs.error.KubeRuntimeException;
import dev.latvian.mods.kubejs.event.KubeEvent;
import dev.latvian.mods.kubejs.util.KubeResourceLocation;
import dev.latvian.mods.rhino.util.HideFromJS;
import dev.latvian.mods.rhino.util.ReturnsSelf;
import dev.wolfieboy09.tfmgjs.registries.fuel.framework.BasicFuelBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.material.Fluid;

import java.util.LinkedList;

public class EngineFuelEvent implements KubeEvent {
    private final LinkedList<Builder> builders = new LinkedList<>();
    private final LinkedList<Holder<Item>> acceptedItems = new LinkedList<>();

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
    public LinkedList<Holder<Item>> getAcceptedItems() {
        return this.acceptedItems;
    }

    @HideFromJS
    public LinkedList<BasicFuelBuilder.FuelEntry> construct() {
        LinkedList<BasicFuelBuilder.FuelEntry> result = new LinkedList<>();

        for (Builder b : builders) {
            BasicFuelBuilder.FuelEntry entry = b.construct();

            entry.json().addProperty("efficiency", b.efficiency);
            entry.json().addProperty("torque", b.torque);
            entry.json().addProperty("exhaust", BuiltInRegistries.FLUID.getKey(b.exhaust).toString());

            result.add(entry);
        }

        return result;
    }
    public static class Builder extends BasicFuelBuilder {
        private float efficiency = 1;
        private float torque = 1;
        private Fluid exhaust = TFMGFluids.CARBON_DIOXIDE.get();

        public Builder(ResourceLocation id) {
            super(id);
        }

        @HideFromJS
        public LinkedList<Holder<Item>> getAcceptedItems() {
            EngineFuelEvent registered = EngineFuelManager.getEngineFuel(getId());
            if (registered == null) {
                throw new KubeRuntimeException("Engine fuel '%s' was not created in startup scripts, no items will be registered as its cylinders".formatted(id));
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

        @ReturnsSelf
        public Builder exhaust(Fluid exhaust) {
            this.exhaust = exhaust;
            return this;
        }
    }
}