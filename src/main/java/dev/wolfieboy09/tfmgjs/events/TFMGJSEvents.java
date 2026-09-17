package dev.wolfieboy09.tfmgjs.events;

import dev.latvian.mods.kubejs.event.EventGroup;
import dev.latvian.mods.kubejs.event.EventHandler;
import dev.wolfieboy09.tfmgjs.registries.fuel.EngineFuelEvent;
import dev.wolfieboy09.tfmgjs.registries.fuel.FlamethrowerFuelEvent;

public interface TFMGJSEvents {
    EventGroup GROUP = EventGroup.of("TFMGEvents");

    //EventHandler CABLE_TYPES = GROUP.startup("registerCableTypes", () -> KubeCableBuilder.class);
    EventHandler ENGINE_FUEL = GROUP.server("engineFuel", () -> EngineFuelEvent.class);
    EventHandler FLAMETHROWER_FUEL = GROUP.server("flamethrowerFuel", () -> FlamethrowerFuelEvent.class);
}
