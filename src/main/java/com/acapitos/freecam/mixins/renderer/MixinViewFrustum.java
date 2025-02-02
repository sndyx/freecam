package com.acapitos.freecam.mixins.renderer;

import com.acapitos.freecam.FreeCamMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ViewFrustum;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ViewFrustum.class)
public abstract class MixinViewFrustum {

    @ModifyVariable(
            method = "updateChunkPositions",
            at = @At("HEAD"),
            ordinal = 0
    )
    private double updateChunkPositionsViewEntityX(double viewEntityX) {
        if (FreeCamMod.INSTANCE.enabled) {
            return Minecraft.getMinecraft().thePlayer.posX;
        }
        return viewEntityX;
    }

    @ModifyVariable(
            method = "updateChunkPositions",
            at = @At("HEAD"),
            ordinal = 1
    )
    private double updateChunkPositionsViewEntityZ(double viewEntityZ) {
        if (FreeCamMod.INSTANCE.enabled) {
            return Minecraft.getMinecraft().thePlayer.posZ;
        }
        return viewEntityZ;
    }

}
