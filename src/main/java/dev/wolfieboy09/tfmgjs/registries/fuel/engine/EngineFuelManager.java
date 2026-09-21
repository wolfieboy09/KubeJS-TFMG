package dev.wolfieboy09.tfmgjs.registries.fuel.engine;

import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

public final class EngineFuelManager {
    private EngineFuelManager() {}

    private static final Map<ResourceLocation, EngineFuelEvent> ENGINE_FUEL_MAP = new HashMap<>();

    public static Map<ResourceLocation, EngineFuelEvent> getEngineFuelMap() {
        return ENGINE_FUEL_MAP;
    }

    public static EngineFuelEvent getEngineFuel(ResourceLocation id) {
        return ENGINE_FUEL_MAP.get(id);
    }

    public static void putEngineFuel(ResourceLocation id, EngineFuelEvent fuel) {
        ENGINE_FUEL_MAP.put(id, fuel);
    }

    public static void clear() {
        ENGINE_FUEL_MAP.clear();
    }
}