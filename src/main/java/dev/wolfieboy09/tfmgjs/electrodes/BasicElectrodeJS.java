package dev.wolfieboy09.tfmgjs.electrodes;

import com.drmangotea.tfmg.content.machinery.vat.electrode_holder.electrode.Electrode;
import dev.latvian.mods.kubejs.item.ItemBuilder;
import dev.latvian.mods.kubejs.registry.BuilderBase;
import dev.latvian.mods.kubejs.registry.RegistryInfo;
import dev.wolfieboy09.tfmgjs.TFMGJSRegistryInfo;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Experimental
public class BasicElectrodeJS extends BuilderBase<Electrode> {
    public BasicElectrodeJS(ResourceLocation i) {
        super(i);
    }

    @Override
    public RegistryInfo<Electrode> getRegistryType() {
        return TFMGJSRegistryInfo.ELECTRODE;
    }

    @Override
    public Electrode createObject() {
        return new Electrode(new Electrode.Properties(this.id));
    }

    @Override
    public void createAdditionalObjects() {
        RegistryInfo.ITEM.addBuilder(new ItemBuilder(this.id) {
            @Override
            public Item createObject() {
                return new Item(new Item.Properties().stacksTo(1));
            }
        });
    }
}
