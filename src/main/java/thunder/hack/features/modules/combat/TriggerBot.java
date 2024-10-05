package thunder.hack.features.modules.combat;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import thunder.hack.core.Managers;
import thunder.hack.core.manager.client.ModuleManager;
import thunder.hack.events.impl.PlayerUpdateEvent;
import thunder.hack.features.modules.Module;

import java.util.Random;

public final class TriggerBot extends Module {
    private final TriggerBotSettings settings = new TriggerBotSettings();  // Initialize settings class

    private int delay;
    private int hitDelayTicks;  // Counter for the hit delay
    private final Random random = new Random(); // For random delay

    public TriggerBot() {
        super("TriggerBot", Category.COMBAT);
    }

    @EventHandler
    public void onAttack(PlayerUpdateEvent e) {
        if (mc.player.isUsingItem() && settings.pauseEating.getValue()) {
            return;
        }

        // Check if requireWeapon is enabled and if the player has a weapon in hand
        if (settings.requireWeapon.getValue() && !isHoldingWeapon()) {
            return; // Exit if no weapon is found in hand
        }
        // Implement the hit delay (1 tick delay after the cooldown is ready)
        if (settings.hitDelayEnabled.getValue()) {
            if (hitDelayTicks > 0) {
                hitDelayTicks--;  // Wait for the hit delay to finish
                return;
            } else {
                hitDelayTicks = 1;  // Set delay to 1 tick for the next swing
            }
        }

        // If delay between hits is enabled, apply 1-2 ticks delay before hitting again
        if (settings.enableDelay.getValue()) {
            if (delay > 0) {
                delay--;
                return; // Wait for the delay to finish
            }
        }

        if (!mc.options.jumpKey.isPressed() && mc.player.isOnGround() && settings.autoJump.getValue()) {
            mc.player.jump();
        }

        // Smart crits should not be delayed
        if (!autoCrit()) {
            if (delay > 0) {
                delay--;
                return;
            }
        }

        Entity ent = Managers.PLAYER.getRtxTarget(mc.player.getYaw(), mc.player.getPitch(), settings.attackRange.getValue(), settings.ignoreWalls.getValue());
        if (ent != null && !Managers.FRIEND.isFriend(ent.getName().getString())) {
            mc.interactionManager.attackEntity(mc.player, ent);
            mc.player.swingHand(Hand.MAIN_HAND);

            // Set delay for the next hit (only if delay between hits is enabled)
            if (settings.enableDelay.getValue()) {
                delay = random.nextInt(2) + 1;  // Random delay between 1 and 2 ticks (50-100ms)
            }
        }
    }

    private boolean autoCrit() {
        boolean reasonForSkipCrit =
                !settings.smartCrit.getValue().isEnabled()
                        || mc.player.getAbilities().flying
                        || (mc.player.isFallFlying() || ModuleManager.elytraPlus.isEnabled())
                        || mc.player.hasStatusEffect(StatusEffects.BLINDNESS)
                        || mc.player.isHoldingOntoLadder()
                        || mc.world.getBlockState(BlockPos.ofFloored(mc.player.getPos())).getBlock() == Blocks.COBWEB;

        if (mc.player.fallDistance > 1 && mc.player.fallDistance < 1.14)
            return false;

        if (ModuleManager.aura.getAttackCooldown() < (mc.player.isOnGround() ? 1f : 0.9f))
            return false;

        boolean mergeWithTargetStrafe = !ModuleManager.targetStrafe.isEnabled() || !ModuleManager.targetStrafe.jump.getValue();
        boolean mergeWithSpeed = !ModuleManager.speed.isEnabled() || mc.player.isOnGround();

        if (!mc.options.jumpKey.isPressed() && mergeWithTargetStrafe && mergeWithSpeed && !settings.onlySpace.getValue() && !settings.autoJump.getValue()) {
            return true;
        }

        if (mc.player.isInLava()) {
            return true;
        }

        if (!mc.options.jumpKey.isPressed() && ModuleManager.aura.isAboveWater()) {
            return true;
        }

        if (!reasonForSkipCrit) {
            return !mc.player.isOnGround() && mc.player.fallDistance > 0.0f;
        }
        return true;
    }

    // Helper function to check if the player is holding a weapon
    private boolean isHoldingWeapon() {
        ItemStack itemInHand = mc.player.getMainHandStack();
        return itemInHand.getItem() == Items.DIAMOND_SWORD || itemInHand.getItem() == Items.NETHERITE_SWORD ||
               itemInHand.getItem() == Items.IRON_SWORD || itemInHand.getItem() == Items.GOLDEN_SWORD ||
               itemInHand.getItem() == Items.STONE_SWORD || itemInHand.getItem() == Items.WOODEN_SWORD;
    }
}
