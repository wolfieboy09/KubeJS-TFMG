package dev.wolfieboy09.tfmgjs.wrappers;

import com.drmangotea.tfmg.TFMGRegistries;
import com.drmangotea.tfmg.content.machinery.vat.base.registry.types.VatType;
import dev.latvian.mods.kubejs.error.KubeRuntimeException;
import dev.latvian.mods.kubejs.script.SourceLine;
import dev.latvian.mods.rhino.Context;
import dev.latvian.mods.rhino.type.TypeInfo;
import net.minecraft.resources.ResourceLocation;

public interface VatTypeWrapper {
    TypeInfo TYPE_INFO = TypeInfo.of(VatType.class);

    static boolean isVatTypeLike(Object from) {
        return from instanceof VatType;
    }

    static VatType wrapVatType(Context cx, Object from) {
        return switch (from) {
            case VatType id -> id;
            case ResourceLocation id -> TFMGRegistries.VAT_TYPE_REGISTRY.get(id);
            case String id -> {
                ResourceLocation rl = ResourceLocation.tryParse(id);
                if (rl == null) {
                    throw new KubeRuntimeException("Invalid vat type id '%s'".formatted(id)).source(SourceLine.of(cx));
                }
                VatType type = TFMGRegistries.VAT_TYPE_REGISTRY.get(rl);
                if (type == null) {
                    throw new KubeRuntimeException("Unknown vat type '%s'".formatted(id)).source(SourceLine.of(cx));
                }
                yield type;
            }
            case null, default -> throw new KubeRuntimeException("Failed to read vat type %s".formatted(from)).source(SourceLine.of(cx));
        };
    }
}
