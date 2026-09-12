package dev.wolfieboy09.tfmgjs;

import com.mojang.logging.LogUtils;
import dev.wolfieboy09.tfmgjs.registries.PendingEntries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;

@Mod(TFMGJS.MODID)
public class TFMGJS {
    public static final String MODID = "tfmgjs";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static final KubeRegistrate REGISTRATE = KubeRegistrate.create();

    public TFMGJS(IEventBus bus, ModContainer container) {
        bus.addListener(PendingEntries::onModifyDefaultComponents);
    }
}
