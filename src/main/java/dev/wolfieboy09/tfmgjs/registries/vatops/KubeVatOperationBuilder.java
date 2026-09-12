package dev.wolfieboy09.tfmgjs.registries.vatops;

import com.drmangotea.tfmg.content.machinery.vat.base.registry.operations.VatOperation;
import dev.latvian.mods.kubejs.KubeJS;
import dev.latvian.mods.kubejs.client.LangKubeEvent;
import dev.latvian.mods.kubejs.plugin.builtin.wrapper.StringUtilsWrapper;
import dev.latvian.mods.kubejs.registry.BuilderBase;
import net.minecraft.resources.ResourceLocation;

public class KubeVatOperationBuilder extends BuilderBase<VatOperation> {
    public KubeVatOperationBuilder(ResourceLocation id) {
        super(id);
    }

    @Override
    public VatOperation createObject() {
        return new VatOperation(this.id);
    }

    @Override
    public void generateLang(LangKubeEvent lang) {
        super.generateLang(lang);
        String name = this.displayName != null
                ? this.displayName.getString()
                : StringUtilsWrapper.snakeCaseToTitleCase(this.id.getPath());
        lang.add(KubeJS.MOD_ID, "tfmg.goggles.vat." + this.id.getNamespace() + "." + this.id.getPath(), name);
    }
}
