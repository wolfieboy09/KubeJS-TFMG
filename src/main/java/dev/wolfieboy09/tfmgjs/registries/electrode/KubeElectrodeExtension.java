package dev.wolfieboy09.tfmgjs.registries.electrode;

import com.drmangotea.tfmg.content.machinery.vat.base.VatBlockEntity;
import com.drmangotea.tfmg.content.machinery.vat.electrode_holder.electrode.Electrode;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public class KubeElectrodeExtension extends Electrode {
    private final VatTickTask task;

    public KubeElectrodeExtension(Properties properties, VatTickTask task) {
        super(properties);
        this.task = task;
    }

    @Override
    public void tick(VatBlockEntity controllerVat, Level level, BlockPos pos, boolean active, boolean clientTick) {
        if (task != null) {
            task.tick(controllerVat, level, pos, active, clientTick);
        }
    }
}
