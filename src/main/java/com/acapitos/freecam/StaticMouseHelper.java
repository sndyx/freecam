package com.acapitos.freecam;

import net.minecraft.util.MouseHelper;

public class StaticMouseHelper extends MouseHelper {

    @Override
    public void mouseXYChange() {
        deltaX = 0;
        deltaY = 0;
    }

}
