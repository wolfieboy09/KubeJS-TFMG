package dev.wolfieboy09.tfmgjs.registries.fuel;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import dev.latvian.mods.kubejs.core.RegistryObjectKJS;
import dev.latvian.mods.kubejs.error.KubeRuntimeException;
import dev.latvian.mods.rhino.util.HideFromJS;
import dev.latvian.mods.rhino.util.ReturnsSelf;
import dev.wolfieboy09.tfmgjs.content.WrappedFluid;
import dev.wolfieboy09.tfmgjs.registries.PendingEntries;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.material.Fluid;

import java.util.List;

public class BasicFuelBuilder {
    private final WrappedFluid fluid;
    private float speed = 1;

    public BasicFuelBuilder(WrappedFluid fluid) {
        this.fluid = fluid;
    }

    @HideFromJS
    public FuelEntry construct() {
        if (this.fluid.isEmpty()) {
            throw new KubeRuntimeException("Fluid can not be empty");
        }

        JsonObject json = new JsonObject();
        json.add("fluids", serializeFluid());
        json.addProperty("speed", this.speed);

        return new FuelEntry(getFileId(), json);
    }

    private JsonElement serializeFluid() {
        if (this.fluid.isTag()) {
            return new JsonPrimitive("#" + this.fluid.getTag().location());
        }

        List<Fluid> fluids = this.fluid.getFluids();
        if (fluids.size() == 1) {
            return new JsonPrimitive(BuiltInRegistries.FLUID.getKey(fluids.getFirst()).toString());
        }

        JsonArray arr = new JsonArray();
        for (Fluid f : fluids) {
            arr.add(BuiltInRegistries.FLUID.getKey(f).toString());
        }
        return arr;
    }

    @HideFromJS
    public ResourceLocation getFileId() {
        if (this.fluid.isTag()) {
            return this.fluid.getTag().location();
        }

        return BuiltInRegistries.FLUID.getKey(this.fluid.getFluids().getFirst());
    }

    @ReturnsSelf
    public BasicFuelBuilder speed(float speed) {
        this.speed = speed;
        return this;
    }

    @ReturnsSelf
    public BasicFuelBuilder accepts(Item... items) {
        for (Item item : items) {
            List<ResourceLocation> locationList = this.fluid.getFluids().stream().map(RegistryObjectKJS::kjs$getIdLocation).toList();
            PendingEntries.addCylinderItem(item.kjs$getIdLocation(), locationList);
        }

        return this;
    }

    @HideFromJS
    public WrappedFluid getFluid() {
        return this.fluid;
    }

    @HideFromJS
    public float getSpeed() {
        return this.speed;
    }

    public record FuelEntry(ResourceLocation id, JsonObject json) {}
}