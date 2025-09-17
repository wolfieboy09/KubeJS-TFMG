package dev.wolfieboy09.tfmgjs.electrodes;

import com.drmangotea.tfmg.TFMG;
import com.drmangotea.tfmg.content.machinery.vat.electrode_holder.electrode.Electrode;
import com.drmangotea.tfmg.content.machinery.vat.electrode_holder.electrode.ElectrodeBuilder;
import com.drmangotea.tfmg.content.machinery.vat.electrode_holder.electrode.ElectrodeEntry;
import com.drmangotea.tfmg.registry.TFMGItems;
import com.tterrag.registrate.util.entry.ItemEntry;
import dev.latvian.mods.kubejs.item.ItemBuilder;
import dev.latvian.mods.kubejs.registry.BuilderBase;
import dev.latvian.mods.kubejs.registry.RegistryInfo;
import dev.latvian.mods.kubejs.typings.Info;
import dev.latvian.mods.kubejs.typings.Param;
import dev.wolfieboy09.tfmgjs.KubeJSRegistrate;
import dev.wolfieboy09.tfmgjs.TFMGJSRegistryInfo;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public class ElectrodeBuilderJS extends BuilderBase<Electrode> {
    private final ElectrodeEntry<Electrode> electrodeEntry = KubeJSRegistrate.REGISTRATE.electrode(this.id.getPath(), Electrode::new).register();

    private Item.Properties electrodeItemProperties = new Item.Properties();

    private final Item item = new Item(this.electrodeItemProperties);

    private final ItemEntry<Item> itemEntry = KubeJSRegistrate.REGISTRATE.item(this.id.getPath(), Item::new)
            .properties(p -> this.electrodeItemProperties).register();

    private final Electrode.Properties electrodeProperties = new Electrode.Properties(this.id)
            .operationId(this.id.toString())
            .item(this.itemEntry);


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
        this.electrodeProperties.operationId(operationId.toString()).item(this.itemEntry);
        return this;
    }

    public ElectrodeBuilderJS properties(Supplier<Item.Properties> itemProperties) {
       this.electrodeItemProperties = itemProperties.get();
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
                return item;
            }
        });
    }
}
