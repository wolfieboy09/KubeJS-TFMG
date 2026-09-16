package dev.wolfieboy09.tfmgjs.events;

import dev.latvian.mods.kubejs.event.EventGroup;
import dev.latvian.mods.kubejs.event.EventHandler;
import dev.wolfieboy09.tfmgjs.events.cable.KubeCableBuilder;

public interface TFMGJSEvents {
    EventGroup GROUP = EventGroup.of("TFMGEvents");

    //EventHandler CABLE_TYPES = GROUP.startup("registerCableTypes", () -> KubeCableBuilder.class);
}
