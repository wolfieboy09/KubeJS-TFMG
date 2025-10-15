package dev.wolfieboy09.tfmgjs.electrodes;

import com.drmangotea.tfmg.content.machinery.vat.electrode_holder.electrode.Electrode;
import com.tterrag.registrate.util.entry.ItemEntry;
import dev.latvian.mods.kubejs.registry.BuilderBase;
import dev.latvian.mods.kubejs.typings.Info;
import dev.latvian.mods.kubejs.typings.Param;
import dev.latvian.mods.rhino.util.HideFromJS;
import dev.wolfieboy09.tfmgjs.KubeJSRegistrate;
import dev.wolfieboy09.tfmgjs.TFMGJSRegistryInfo;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class ElectrodeBuilderJS extends BuilderBase<Electrode> {
    private Consumer<Item.Properties> electrodeItemProperties = (p) -> new Item.Properties();
    private Electrode.Properties electrodeProperties;
    private boolean built = false;

    public ElectrodeBuilderJS(ResourceLocation id) {
        super(id);
    }

    @Info(value = "Set's the electrode's resistance", params = {
            @Param("The electrode's resistance")
    })
    public ElectrodeBuilderJS resistance(int resistance) {
        ensureNotBuilt();
        getElectrodeProperties().resistance(resistance);
        return this;
    }

    @Info(value = "Set's the operation ID for the chemical vat machines. Defaults to the namespace given", params = {
            @Param("The new operation id")
    })
    public ElectrodeBuilderJS operationId(@NotNull ResourceLocation operationId) {
        ensureNotBuilt();
        getElectrodeProperties().operationId(operationId.toString());
        return this;
    }

    public ElectrodeBuilderJS properties(Consumer<Item.Properties> itemProperties) {
        ensureNotBuilt();
        this.electrodeItemProperties = itemProperties;
        return this;
    }

//    @Override
//    public RegistryInfo<Electrode> getRegistryType() {
//        return TFMGJSRegistryInfo.ELECTRODE;
//    }

    @Override
    public Electrode createObject() {
        if (!built) {
            buildEntries();
        }
        return new Electrode(electrodeProperties);
    }

//    @Override
//    public void createAdditionalObjects() {
//        if (!built) {
//            buildEntries();
//        }
//    }

    @HideFromJS
    private void buildEntries() {
        if (built) return;
        //Supplier<Item.Properties> sup = () -> this.electrodeItemProperties.accept(new Item.Properties());
        // Create item entry with proper properties
        ItemEntry<Item> itemEntry = KubeJSRegistrate.REGISTRATE
                .item(this.id.getPath(), Item::new)
                //.properties(p -> sup.get())
                .register();

        // Initialize electrode properties with the item entry
        this.electrodeProperties = new Electrode.Properties(this.id)
                .operationId(this.id.toString())
                .item(itemEntry);

        this.built = true;
    }

    @HideFromJS
    private Electrode.Properties getElectrodeProperties() {
        if (electrodeProperties == null) {
            electrodeProperties = new Electrode.Properties(this.id)
                    .operationId(this.id.toString());
        }
        return electrodeProperties;
    }

    @HideFromJS
    private void ensureNotBuilt() {
        if (built) {
            throw new IllegalStateException("Cannot modify electrode builder after it has been built");
        }
    }
}