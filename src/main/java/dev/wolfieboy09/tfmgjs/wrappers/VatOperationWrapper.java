package dev.wolfieboy09.tfmgjs.wrappers;

import com.drmangotea.tfmg.TFMGRegistries;
import com.drmangotea.tfmg.content.machinery.vat.base.registry.operations.VatOperation;
import com.drmangotea.tfmg.registry.TFMGVatOperations;
import dev.latvian.mods.kubejs.error.KubeRuntimeException;
import dev.latvian.mods.kubejs.script.SourceLine;
import dev.latvian.mods.rhino.Context;
import dev.latvian.mods.rhino.type.TypeInfo;
import dev.latvian.mods.rhino.util.HideFromJS;
import net.minecraft.resources.ResourceLocation;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public interface VatOperationWrapper {
    TypeInfo TYPE_INFO = TypeInfo.of(VatOperation.class);

    static boolean isVatOperationLike(Object from) {
        return from instanceof VatOperation;
    }

    static VatOperationSpread wrapVatOperation(Context cx, Object from) {
        return switch (from) {
            case null -> VatOperationSpread.of(TFMGVatOperations.NONE.get());
            case VatOperation id -> VatOperationSpread.of(id);
            case VatOperationSpread spread -> spread;
            case ResourceLocation id -> VatOperationSpread.of(TFMGRegistries.VAT_OPERATION_REGISTRY.get(id));
            case String id -> {
                // I'm sure there's a better way to do all this
                // But it works, so yay

                Matcher matcher = Pattern.compile("^(?:(\\d+)x\\s*)?(.+)$").matcher(id.trim());

                if (!matcher.matches()) {
                    throw new KubeRuntimeException("Unknown vat operation %s".formatted(id)).source(SourceLine.of(cx));
                }

                int amount = matcher.group(1) != null ? Integer.parseInt(matcher.group(1)) : 1;

                ResourceLocation rl = ResourceLocation.tryParse(matcher.group(2));

                if (rl == null) {
                    throw new KubeRuntimeException("Unknown vat operation %s".formatted(id)).source(SourceLine.of(cx));
                }

                VatOperation op = TFMGRegistries.VAT_OPERATION_REGISTRY.get(rl);

                if (op == null) {
                    throw new KubeRuntimeException("Unknown vat operation %s".formatted(id)).source(SourceLine.of(cx));
                }

                yield VatOperationSpread.of(op, amount);
            }

            default -> throw new KubeRuntimeException("Failed to read vat operation %s".formatted(from)).source(SourceLine.of(cx));
        };
    }
}
