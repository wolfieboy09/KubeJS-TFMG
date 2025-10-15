package dev.wolfieboy09.tfmgjs.cables;

import com.drmangotea.tfmg.content.electricity.connection.cable_type.CableType;
import com.drmangotea.tfmg.content.machinery.misc.winding_machine.SpoolItem;
import com.drmangotea.tfmg.registry.TFMGItems;
import com.tterrag.registrate.util.entry.ItemEntry;
import dev.latvian.mods.kubejs.registry.AdditionalObjectRegistry;
import dev.latvian.mods.kubejs.registry.BuilderBase;
import dev.latvian.mods.kubejs.typings.Info;
import dev.latvian.mods.rhino.util.HideFromJS;
import dev.wolfieboy09.tfmgjs.TFMGJSRegistryInfo;
import net.minecraft.resources.ResourceLocation;

public class CableSpoolBuilderJS extends BuilderBase<CableType> {
    private int color = 0xFFFFFF;
    private ItemEntry<SpoolItem> cableEntry;

    public CableSpoolBuilderJS(ResourceLocation id) {
        super(id);
    }

//    @Override
//    @HideFromJS
//    public RegistryInfo<?> getRegistryType() {
//        return TFMGJSRegistryInfo.CABLE_TYPE;
//    }

    @Override
    @HideFromJS
    public CableType createObject() {
        return new CableType(new CableType.Properties(this.id)
                .spool(this.cableEntry)
                .color(this.color));
    }

    @Info("Set's the cable's color")
    public CableSpoolBuilderJS color(int color) {
         this.color = color;
        return this;
    }

//    @Override
//    @HideFromJS
//    public void createAdditionalObjects() {
//        buildEntries();
//    }


//    @Override
//    public void createAdditionalObjects(AdditionalObjectRegistry registry) {
//        String spoolName = this.id.getPath() + "_spool";
//        this.cableEntry = TFMGItems.spoolItem(spoolName, this.color, this.id).register();
//        registry.add(this.cableEntry);
//    }
}