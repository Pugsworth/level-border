package net.axay.levelborder.vanilla;

import net.axay.levelborder.common.BorderMode;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.saveddata.SavedData;

public class BorderModeSavedData extends SavedData {
    public BorderMode borderMode;

    public Vec3i center = new Vec3i(0, 0, 0);

    public BorderModeSavedData() {
        this.borderMode = BorderMode.OWN;
    }

    public BorderModeSavedData(BorderMode borderMode) {
        this.borderMode = borderMode;
    }

    public BorderModeSavedData(BorderMode borderMode, Vec3i center) {
        this.borderMode = borderMode;
        this.center = center;
    }

    public static BorderModeSavedData load(CompoundTag nbt) {
        var mode = BorderMode.valueOf(nbt.getString("levelBorderMode"));

        var centerArr = nbt.getIntArray("levelBorderCenter");
        var center = new Vec3i(centerArr[0], centerArr[1], centerArr[2]);

        return new BorderModeSavedData( mode, center );
    }

    @Override
    public CompoundTag save(CompoundTag nbt) {
        nbt.putString("levelBorderMode", borderMode.name());
        nbt.putIntArray("levelBorderCenter", new int[] { center.getX(), center.getY(), center.getZ() });

        return nbt;
    }
}
