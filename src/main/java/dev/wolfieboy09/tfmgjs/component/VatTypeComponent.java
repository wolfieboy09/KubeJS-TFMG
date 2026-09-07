package dev.wolfieboy09.tfmgjs.component;

import com.drmangotea.tfmg.TFMG;
import com.drmangotea.tfmg.TFMGRegistries;
import com.drmangotea.tfmg.content.machinery.vat.base.registry.types.VatType;
import com.mojang.serialization.Codec;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponent;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponentType;
import dev.latvian.mods.kubejs.recipe.component.UniqueIdBuilder;
import dev.latvian.mods.kubejs.recipe.filter.RecipeMatchContext;
import dev.latvian.mods.rhino.type.TypeInfo;
import dev.wolfieboy09.tfmgjs.wrappers.VatTypeWrapper;

public record VatTypeComponent(RecipeComponentType<?> type, Codec<VatType> codec) implements RecipeComponent<VatType> {
    public static final RecipeComponentType<VatType> VAT_TYPE = RecipeComponentType.unit(TFMG.asResource("vat_type"), VatTypeComponent::new);

    public VatTypeComponent(RecipeComponentType<?> type) {
        this(type, TFMGRegistries.VAT_TYPE_REGISTRY.byNameCodec());
    }

    @Override
    public boolean hasPriority(RecipeMatchContext cx, Object from) {
        return VatTypeWrapper.isVatTypeLike(from);
    }

    @Override
    public void buildUniqueId(UniqueIdBuilder builder, VatType value) {
        // It should never be null, but just in case
        if (value != null) {
            builder.append(value.type());
        }
    }

    @Override
    public TypeInfo typeInfo() {
        return VatTypeWrapper.TYPE_INFO;
    }
}
