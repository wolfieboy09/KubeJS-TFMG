package dev.wolfieboy09.tfmgjs.component;

import com.drmangotea.tfmg.TFMG;
import com.drmangotea.tfmg.TFMGRegistries;
import com.drmangotea.tfmg.content.machinery.vat.base.registry.operations.VatOperation;
import com.drmangotea.tfmg.registry.TFMGVatOperations;
import com.mojang.serialization.Codec;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponent;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponentType;
import dev.latvian.mods.kubejs.recipe.component.UniqueIdBuilder;
import dev.latvian.mods.kubejs.recipe.filter.RecipeMatchContext;
import dev.latvian.mods.kubejs.util.OpsContainer;
import dev.latvian.mods.rhino.type.TypeInfo;
import dev.wolfieboy09.tfmgjs.wrappers.VatOperationWrapper;
import net.minecraft.MethodsReturnNonnullByDefault;

@SuppressWarnings("unused")
@MethodsReturnNonnullByDefault
public record VatOperationComponent(RecipeComponentType<?> type, Codec<VatOperation> codec) implements RecipeComponent<VatOperation> {
    public static final RecipeComponentType<VatOperation> VAT_OPERATION = RecipeComponentType.unit(TFMG.asResource("vat_operation"), VatOperationComponent::new);

    public VatOperationComponent(RecipeComponentType<?> type) {
        this(type, TFMGRegistries.VAT_OPERATION_REGISTRY.byNameCodec());
    }

    @Override
    public boolean hasPriority(RecipeMatchContext cx, Object from) {
        return VatOperationWrapper.isVatOperationLike(from);
    }

    @Override
    public void buildUniqueId(UniqueIdBuilder builder, VatOperation value) {
        if (!value.isNone()) {
            builder.append(value.id());
        }
    }

    @Override
    public TypeInfo typeInfo() {
        return VatOperationWrapper.TYPE_INFO;
    }

    static VatOperation none() {
        return TFMGVatOperations.NONE.get();
    }

    static VatOperation decompressor() {
        return TFMGVatOperations.DECOMPRESSOR.get();
    }

    static VatOperation compressor() {
        return TFMGVatOperations.COMPRESSOR.get();
    }

    static VatOperation electrode() {
        return TFMGVatOperations.ELECTRODE.get();
    }

    static VatOperation graphiteElectrode() {
        return TFMGVatOperations.GRAPHITE_ELECTRODE.get();
    }

    static VatOperation mixing() {
        return TFMGVatOperations.MIXING.get();
    }

    static VatOperation centrifuge() {
        return TFMGVatOperations.CENTRIFUGE.get();
    }

    static VatOperation freezing() {
        return TFMGVatOperations.FREEZING.get();
    }
}
