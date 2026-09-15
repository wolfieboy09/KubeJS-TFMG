package dev.wolfieboy09.tfmgjs.registries.electrode;

import com.drmangotea.tfmg.content.machinery.vat.base.VatBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

@FunctionalInterface
public interface VatTickTask {
    void tick(VatBlockEntity controllerVat, Level level, BlockPos pos, boolean active, boolean clientTick);
}
