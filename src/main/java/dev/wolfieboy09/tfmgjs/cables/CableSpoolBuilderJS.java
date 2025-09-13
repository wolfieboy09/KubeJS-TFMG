package dev.wolfieboy09.tfmgjs.cables;

import com.drmangotea.tfmg.content.electricity.connection.cable_type.CableType;
import com.drmangotea.tfmg.content.machinery.misc.winding_machine.SpoolItem;
import com.drmangotea.tfmg.registry.TFMGItems;
import com.tterrag.registrate.util.entry.ItemEntry;
import dev.latvian.mods.kubejs.item.ItemBuilder;
import dev.latvian.mods.kubejs.registry.BuilderBase;
import dev.latvian.mods.kubejs.registry.RegistryInfo;
import dev.latvian.mods.kubejs.typings.Info;
import dev.latvian.mods.rhino.util.HideFromJS;
import dev.wolfieboy09.tfmgjs.TFMGJSRegistryInfo;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.ApiStatus;

@SuppressWarnings("unused")
@ApiStatus.Experimental
public class CableSpoolBuilderJS extends BuilderBase<CableType> {
    private final Item spoolItem = new Item(new Item.Properties());
    // Soooo a bad idea....
    private final ItemEntry<SpoolItem> cableEntry = TFMGItems.spoolItem(this.spoolItem.getDescriptionId().split(":", 1)[1], this.color, this.id).register();

    private int color = 0xFFFFFF;

    public CableSpoolBuilderJS(ResourceLocation i) {
        super(i);
    }

    @Override
    @HideFromJS
    public RegistryInfo<?> getRegistryType() {
        return TFMGJSRegistryInfo.CABLE_TYPE;
    }

    @Override
    @HideFromJS
    public CableType createObject() {
        return new CableType(new CableType.Properties(this.id)
                .spool(this.cableEntry)
                .color(this.color));
    }


    @Info("Set's the bar's color")
    public CableSpoolBuilderJS color(int color) {
        this.color = color;
        return this;
    }

    @Override
    @HideFromJS
    public void createAdditionalObjects() {
        RegistryInfo.ITEM.addBuilder(new ItemBuilder(this.id) {
            @Override
            public Item createObject() {
                return spoolItem;
            }
        });
    }
}
