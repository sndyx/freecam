package com.acapitos.freecam;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.MouseHelper;
import net.minecraft.util.MovementInput;
import net.minecraft.util.MovementInputFromOptions;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;


@Mod(modid = "freecam", version = "1.0")
public class FreeCamMod {

    @Mod.Instance(value = "freecam")
    public static FreeCamMod INSTANCE;

    public KeyBinding keyBindToggle = new KeyBinding("key.freecam.toggle", Keyboard.KEY_NONE, "category.freecam");
    public KeyBinding keyBindTeleport = new KeyBinding("key.freecam.teleport", Keyboard.KEY_NONE, "category.freecam");

    public EntityFreeCam entity;
    public MouseHelper mouseHelper;
    public boolean enabled = false;

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(this);
        ClientRegistry.registerKeyBinding(keyBindToggle);
        ClientRegistry.registerKeyBinding(keyBindTeleport);
        mouseHelper = new MouseHelper();
    }

    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        if (keyBindToggle.isPressed()) {
            toggle();
        } else if (enabled && keyBindTeleport.isPressed()) {
            Minecraft.getMinecraft().thePlayer.sendChatMessage(
                    String.format("/tp %.2f %.2f %.2f", entity.posX, entity.posY, entity.posZ)
            );
            teleportDestination = entity.getPositionVector();
        }
    }

    @SubscribeEvent
    public void onMouseInput(MouseEvent event) {
        if (enabled) {
            EntityFreeCam.speed += Integer.signum(event.dwheel) * 0.25;
            if (EntityFreeCam.speed < 0.25) EntityFreeCam.speed = 0.25;
            if (EntityFreeCam.speed > 5.0) EntityFreeCam.speed = 5.0;

            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onRenderPre(TickEvent.RenderTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;
        if (!enabled) return;

        Minecraft mc = Minecraft.getMinecraft();
        if (Display.isActive() && mc.inGameHasFocus) {
            mouseHelper.mouseXYChange();

            float multi = mc.gameSettings.mouseSensitivity * 0.6F + 0.2F;
            multi = multi * multi * multi * 8.0F;

            float x = mouseHelper.deltaX * multi;
            float y = mouseHelper.deltaY * multi;

            if (mc.gameSettings.invertMouse) {
                y *= -1;
            }

            entity.setAngles(x, y);
        }

        mc.setRenderViewEntity(entity);
    }

    @SubscribeEvent
    public void onRenderPost(TickEvent.RenderTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!enabled) return;

        Minecraft mc = Minecraft.getMinecraft();
        mc.setRenderViewEntity(mc.thePlayer);
    }

    public void toggle() {
        Minecraft mc = Minecraft.getMinecraft();
        EntityPlayerSP player = mc.thePlayer;

        enabled = !enabled;

        if (enabled) { // enable
            entity = new EntityFreeCam();
            mc.theWorld.spawnEntityInWorld(entity);
            mc.setRenderViewEntity(entity);

            player.movementInput = new MovementInput(); // stop all movements
            mc.mouseHelper = new StaticMouseHelper();
        } else { // disable
            player.movementInput = new MovementInputFromOptions(mc.gameSettings);

            mc.setRenderViewEntity(mc.thePlayer);
            mc.theWorld.removeEntity(entity);
            mc.mouseHelper = new MouseHelper();
            entity = null;
        }
    }

    // toggle when world changes

    @SubscribeEvent
    public void onEntityJoinWorld(EntityJoinWorldEvent event) {
        if (enabled && event.entity instanceof EntityPlayerSP) toggle();
    }

    // for teleport

    Vec3 teleportDestination;

    @SubscribeEvent
    public void onTick(TickEvent.PlayerTickEvent event) {
        if (enabled && teleportDestination != null) {
            Minecraft mc = Minecraft.getMinecraft();
            if (mc.thePlayer.getPositionVector().distanceTo(teleportDestination) < 1.0) {
                teleportDestination = null;

                mc.thePlayer.rotationPitch = entity.rotationPitch;
                mc.thePlayer.rotationYaw = entity.rotationYaw;

                if (mc.thePlayer.capabilities.allowFlying) {
                    mc.thePlayer.capabilities.isFlying = true;
                    mc.thePlayer.sendPlayerAbilities();
                }

                toggle();
            }
        }
    }

    // change some rendering

    @SubscribeEvent
    public void onRenderPlayerPre(RenderPlayerEvent.Pre event) {
        if (!enabled) return;
        if (!(event.entity instanceof EntityPlayerSP)) return;
        if (teleportDestination != null) event.setCanceled(true);

        // forces player to render
        Minecraft.getMinecraft().getRenderManager().livingPlayer = Minecraft.getMinecraft().thePlayer;
    }

    @SubscribeEvent
    public void onRenderHand(RenderHandEvent event) {
        if (enabled) event.setCanceled(true);
    }

    @SubscribeEvent
    public void onRenderOverlay(RenderGameOverlayEvent event) {
        if (enabled) {
            if (event.type == RenderGameOverlayEvent.ElementType.HOTBAR
                    || event.type == RenderGameOverlayEvent.ElementType.EXPERIENCE
                    || event.type == RenderGameOverlayEvent.ElementType.HEALTH
                    || event.type == RenderGameOverlayEvent.ElementType.FOOD
                    || event.type == RenderGameOverlayEvent.ElementType.ARMOR
                    || event.type == RenderGameOverlayEvent.ElementType.AIR
                    || event.type == RenderGameOverlayEvent.ElementType.CROSSHAIRS
            ) {
                event.setCanceled(true);
            }
        }
    }

}
