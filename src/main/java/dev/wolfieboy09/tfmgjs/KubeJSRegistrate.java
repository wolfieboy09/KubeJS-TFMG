package dev.wolfieboy09.tfmgjs;

import com.drmangotea.tfmg.content.electricity.connection.cable_type.CableType;
import com.drmangotea.tfmg.content.electricity.connection.cable_type.CableTypeBuilder;
import com.drmangotea.tfmg.content.machinery.vat.electrode_holder.electrode.Electrode;
import com.drmangotea.tfmg.content.machinery.vat.electrode_holder.electrode.ElectrodeBuilder;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.simibubi.create.foundation.item.ItemDescription;
import com.simibubi.create.foundation.item.KineticStats;
import com.simibubi.create.foundation.item.TooltipModifier;
import com.tterrag.registrate.util.nullness.NonNullFunction;
import dev.latvian.mods.kubejs.KubeJS;
import net.createmod.catnip.lang.FontHelper;

// Code from TFMG's Registrate, pointing to KubeJS's mod id
public class KubeJSRegistrate extends CreateRegistrate {
    public static final KubeJSRegistrate REGISTRATE = create();

    protected KubeJSRegistrate() {
        // We want to point to KubeJS's mod id for everything
        super(KubeJS.MOD_ID);
    }

    public static KubeJSRegistrate create() {
        return (KubeJSRegistrate) new KubeJSRegistrate().setTooltipModifierFactory(item ->
                new ItemDescription.Modifier(item, FontHelper.Palette.STANDARD_CREATE)
                        .andThen(TooltipModifier.mapNull(KineticStats.create(item)))
        );
    }

    public <T extends Electrode> ElectrodeBuilder<T, KubeJSRegistrate> electrode(NonNullFunction<Electrode.Properties, T> factory) {
        return electrode((KubeJSRegistrate) self(), factory);
    }

    public <T extends Electrode> ElectrodeBuilder<T, KubeJSRegistrate> electrode(String name, NonNullFunction<Electrode.Properties, T> factory) {
        return electrode((KubeJSRegistrate) self(), name, factory);
    }

    public <T extends Electrode, P> ElectrodeBuilder<T, P> electrode(P parent, NonNullFunction<Electrode.Properties, T> factory) {
        return electrode(parent, currentName(), factory);
    }

    public <T extends Electrode, P> ElectrodeBuilder<T, P> electrode(P parent, String name, NonNullFunction<Electrode.Properties, T> factory) {
        return entry(name, callback -> ElectrodeBuilder.create(this, parent, name, callback, factory));
    }

    public <T extends CableType> CableTypeBuilder<T, KubeJSRegistrate> cableType(NonNullFunction<CableType.Properties, T> factory) {
        return cableType((KubeJSRegistrate) self(), factory);
    }

    public <T extends CableType> CableTypeBuilder<T, KubeJSRegistrate> cableType(String name, NonNullFunction<CableType.Properties, T> factory) {
        return cableType((KubeJSRegistrate) self(), name, factory);
    }

    public <T extends CableType, P> CableTypeBuilder<T, P> cableType(P parent, NonNullFunction<CableType.Properties, T> factory) {
        return cableType(parent, currentName(), factory);
    }

    public <T extends CableType, P> CableTypeBuilder<T, P> cableType(P parent, String name, NonNullFunction<CableType.Properties, T> factory) {
        return entry(name, callback -> CableTypeBuilder.create(this, parent, name, callback, factory));
    }
}
