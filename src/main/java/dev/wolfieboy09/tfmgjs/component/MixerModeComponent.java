package dev.wolfieboy09.tfmgjs.component;

import com.drmangotea.tfmg.TFMG;
import com.drmangotea.tfmg.TFMGRegistries;
import com.drmangotea.tfmg.content.machinery.vat.industrial_mixer.mode.MixerMode;
import com.drmangotea.tfmg.registry.TFMGMixerModes;
import com.mojang.serialization.Codec;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponent;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponentType;
import dev.latvian.mods.kubejs.recipe.component.UniqueIdBuilder;
import dev.latvian.mods.kubejs.recipe.filter.RecipeMatchContext;
import dev.latvian.mods.kubejs.util.OpsContainer;
import dev.latvian.mods.rhino.type.TypeInfo;
import dev.wolfieboy09.tfmgjs.wrappers.MixerModeWrapper;
import net.minecraft.MethodsReturnNonnullByDefault;

@SuppressWarnings("unused")
@MethodsReturnNonnullByDefault
public record MixerModeComponent(RecipeComponentType<?> type, Codec<MixerMode> codec) implements RecipeComponent<MixerMode> {
    public static final RecipeComponentType<MixerMode> MIXER_MODE = RecipeComponentType.unit(TFMG.asResource("mixer_mode"), MixerModeComponent::new);

    public MixerModeComponent(RecipeComponentType<?> type) {
        this(type, TFMGRegistries.MIXER_MODE_REGISTRY.byNameCodec());
    }

    @Override
    public boolean hasPriority(RecipeMatchContext cx, Object from) {
        return MixerModeWrapper.isMixerModeLike(from);
    }

    @Override
    public void buildUniqueId(UniqueIdBuilder builder, MixerMode value) {
        if (value.isValid()) {
            builder.append(value.getKey());
        }
    }

    @Override
    public String toString() {
        return type.toString();
    }

    @Override
    public String toString(OpsContainer ops, MixerMode value) {
        return value.getKey().getPath();
    }

    @Override
    public TypeInfo typeInfo() {
        return MixerModeWrapper.TYPE_INFO;
    }

    static MixerMode none() {
        return TFMGMixerModes.none.get();
    }

    static MixerMode mixing() {
        return TFMGMixerModes.mixing.get();
    }

    static MixerMode centrifuge() {
        return TFMGMixerModes.centrifuge.get();
    }
}
