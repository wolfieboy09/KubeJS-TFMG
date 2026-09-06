package dev.wolfieboy09.tfmgjs.wrappers;

import com.drmangotea.tfmg.content.machinery.vat.industrial_mixer.mode.MixerMode;
import dev.latvian.mods.rhino.type.TypeInfo;

public interface MixerModeWrapper {
    TypeInfo TYPE_INFO = TypeInfo.of(MixerMode.class);

    static boolean isMixerModeLike(Object from) {
        return from instanceof MixerMode;
    }
}
