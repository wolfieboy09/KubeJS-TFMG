package dev.wolfieboy09.tfmgjs.events;

import dev.latvian.mods.kubejs.event.EventGroup;

public interface TFMGJSEvents {
    EventGroup GROUP = EventGroup.of("TFMGEvents");

    //EventHandler CABLE_TYPES = GROUP.startup("registerCableTypes", () -> KubeCableBuilder.class);
}
