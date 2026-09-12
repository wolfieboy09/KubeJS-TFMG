package dev.wolfieboy09.tfmgjs;

import com.drmangotea.tfmg.base.TFMGRegistrate;
import com.simibubi.create.foundation.item.ItemDescription;
import com.simibubi.create.foundation.item.KineticStats;
import com.simibubi.create.foundation.item.TooltipModifier;
import dev.latvian.mods.kubejs.KubeJS;
import net.createmod.catnip.lang.FontHelper;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

/**
 * Access to {@link TFMGRegistrate} with the KubeJS namespace
 */
public class KubeRegistrate extends TFMGRegistrate {
    protected KubeRegistrate() {
        super(KubeJS.MOD_ID);
    }

    public static KubeRegistrate create() {
        return new KubeRegistrate().setTooltipModifierFactory(item ->
                new ItemDescription.Modifier(item, FontHelper.Palette.STANDARD_CREATE)
                        .andThen(TooltipModifier.mapNull(KineticStats.create(item)))
        );
    }

    public KubeRegistrate setTooltipModifierFactory(@Nullable Function<Item, TooltipModifier> factory) {
        currentTooltipModifierFactory = factory;
        return this;
    }
}
