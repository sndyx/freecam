package com.acapitos.freecam;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.util.MovementInput;
import net.minecraft.util.MovementInputFromOptions;
import net.minecraft.util.Vec3;

public class EntityFreeCam extends EntityOtherPlayerMP {

    MovementInput movementInput;

    public static double speed = 1.0;

    public EntityFreeCam() {
        // Minecraft.getMinecraft()... pain peko
        super(
                Minecraft.getMinecraft().theWorld,
                Minecraft.getMinecraft().thePlayer.getGameProfile()
        );

        Minecraft mc = Minecraft.getMinecraft();

        dimension = mc.thePlayer.dimension;
        movementInput = new MovementInputFromOptions(mc.gameSettings);

        preparePlayerToSpawn();

        capabilities.allowFlying = true;
        capabilities.isFlying = true;
        noClip = true;

        setInvisible(true);

        EntityPlayerSP player = mc.thePlayer;
        setPositionAndRotation(player.posX, player.posY + 2, player.posZ, player.rotationYaw, player.rotationPitch);
    }

    @Override
    public boolean isSpectator() {
        return true;
    }

    @Override
    public void onLivingUpdate() {
        prevRotationYaw = rotationYaw;
        prevRotationPitch = rotationPitch;
        prevRotationYawHead = rotationYawHead;

        movementInput.updatePlayerMoveState();

        Vec3 left = getMovementVector(1.0, 0.0);
        Vec3 forward = getMovementVector(0.0, 1.0);

        double forwardVelocity = movementInput.moveForward;
        double leftVelocity = movementInput.moveStrafe;
        double upVelocity = 0;

        if (movementInput.jump) {
            upVelocity += 1;
        }
        if (movementInput.sneak) {
            forwardVelocity *= 3.333F;
            leftVelocity *= 3.333F;
            upVelocity -= 1;
        }

        motionX = (forward.xCoord * forwardVelocity + left.xCoord * leftVelocity) * speed;
        motionY = (forward.yCoord * forwardVelocity + left.yCoord * leftVelocity + upVelocity) * speed;
        motionZ = (forward.zCoord * forwardVelocity + left.zCoord * leftVelocity) * speed;

        moveEntity(motionX, motionY, motionZ);

        prevCameraYaw = cameraYaw;
        prevCameraPitch = cameraPitch;
    }

    private Vec3 getMovementVector(double x, double z) {
        return new Vec3(x, 0.0, z)
                .rotatePitch(-rotationPitch * 0.017453292F)
                .rotateYaw(-rotationYaw * 0.017453292F);
    }

}
