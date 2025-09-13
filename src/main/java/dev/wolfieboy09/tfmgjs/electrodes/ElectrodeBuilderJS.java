package dev.wolfieboy09.tfmgjs.electrodes;

import com.drmangotea.tfmg.TFMG;
import com.drmangotea.tfmg.content.machinery.vat.electrode_holder.electrode.Electrode;
import com.drmangotea.tfmg.content.machinery.vat.electrode_holder.electrode.ElectrodeBuilder;
import com.tterrag.registrate.util.entry.ItemEntry;
import dev.latvian.mods.kubejs.item.ItemBuilder;
import dev.latvian.mods.kubejs.registry.BuilderBase;
import dev.latvian.mods.kubejs.registry.RegistryInfo;
import dev.latvian.mods.kubejs.typings.Info;
import dev.latvian.mods.kubejs.typings.Param;
import dev.wolfieboy09.tfmgjs.TFMGJSRegistryInfo;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

@ApiStatus.Experimental
public class ElectrodeBuilderJS extends BuilderBase<Electrode> {
    private final Electrode.Properties electrodeProperties = new Electrode.Properties(this.id)
            .operationId(this.id.toString()); // Default to the electrode's namespace
            //TODO .item() requires an ItemEntry<?> from registrate
    private final Item.Properties electrodeItemProperties = new Item.Properties();

    public ElectrodeBuilderJS(ResourceLocation i) {
        super(i);
    }

    @Info(value = "Set's the electrode's resistance", params = {
            @Param("The electrode's resistance")
    })
    public ElectrodeBuilderJS resistance(int resistance) {
        this.electrodeProperties.resistance(resistance);
        return this;
    }

    @Info(value = "Set's the operation ID for the chemical vat machines. Defaults to the namespace given", params = {
            @Param("The new operation id")
    })
    public ElectrodeBuilderJS operationId(@NotNull ResourceLocation operationId) {
        this.electrodeProperties.operationId(operationId.toString());
        return this;
    }

    @Override
    public RegistryInfo<Electrode> getRegistryType() {
        return TFMGJSRegistryInfo.ELECTRODE;
    }

    @Override
    public Electrode createObject() {
        return new Electrode(this.electrodeProperties);
    }

    @Override
    public void createAdditionalObjects() {
        RegistryInfo.ITEM.addBuilder(new ItemBuilder(this.id) {
            @Override
            public Item createObject() {
                return new Item(electrodeItemProperties);
            }
        });
    }
}
