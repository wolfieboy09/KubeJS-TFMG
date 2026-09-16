package dev.wolfieboy09.tfmgjs.bridger;

import com.tterrag.registrate.util.entry.ItemEntry;
import dev.latvian.mods.rhino.util.HideFromJS;
import dev.wolfieboy09.tfmgjs.TFMGJS;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;

@HideFromJS
public class ItemEntryCreator {
    public static ItemEntry<?> fromItem(Item item) {
        //This is such a bad idea
        return new ItemEntry<>(TFMGJS.REGISTRATE, DeferredItem.createItem(item.kjs$getKey()));
    }
}
