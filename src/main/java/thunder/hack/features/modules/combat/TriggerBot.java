package net.fabricmc.example;

import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.fabricmc.api.ModInitializer;

import java.util.Random;

public class TriggerBotMod implements ModInitializer {
    public static final TriggerBotMod INSTANCE = new TriggerBotMod();
    private int delay = 0;
    private Random random = new Random();

    // Configuration options
    private int minDelay = 10; // in milliseconds
    private int maxDelay = 50; // in milliseconds
    private boolean smartCritEnabled = true;
    private boolean autoJumpEnabled = true;
    private boolean pauseEating = true;
    private boolean requireWeapon = true;

    @Override
    public void onInitialize() {
        // Mod initialization code (if any)
    }

    public void onPlayerUpdate() {
        if (getMc().player == null) return; // Additional null check

        // Check if player is eating and the "pauseEating" option is enabled
        if (getMc().player.isUsingItem() && pauseEating) {
            return;
        }

        // Auto jump if enabled
        if (!getMc().options.jumpKey.isPressed() &&
            getMc().player.isOnGround() && 
            autoJumpEnabled) {
            getMc().player.jump();
        }

        // Smart crit logic
        if (!performSmartCrit()) {
            // Delay handling between attacks, but delay doesn't affect smart crits
            if (delay > 0) {
                delay--;
                return;
            }
        }

        // Get the entity the player is looking at
        Entity targetEntity = getRtxTarget();

        // Check for valid weapon (sword, bow, hoe, pickaxe, or axe)
        if (requireWeapon && !isHoldingValidWeapon()) {
            return;
        }

        if (targetEntity != null && !isFriend(targetEntity.getName().getString())) {
            // Check if cooldown is greater than or equal to 95%
            if (getMc().player.getAttackCooldownProgress(0.5f) >= 0.95f) {

                // If delay is active, decrease the counter and return early
                if (delay > 0) {
                    delay--;
                    return;
                }

                // Attack the target entity
                getMc().interactionManager.attackEntity(
                    getMc().player, targetEntity);
                getMc().player.swingHand(Hand.MAIN_HAND);

                // Set random delay for next attack (between 10-50 ms)
                delay = random.nextInt(minDelay, maxDelay + 1);
            }
        }
    }

    private boolean performSmartCrit() {
        if (getMc().player == null) return false;

        // Additional logic for crit timing
        if (getMc().player.isOnGround() || inAir.getObject()) {
            if (!pauseOnKill.getObject().booleanValue() || !OneLineUtil.isInvalidPlayer()) {
                if (critTiming.getObject()) {
                    if (getMc().player != null && !getMc().player.isOnGround() && getMc().player.fallDistance > 0) {
                        return false;
                    }
                }
            }
        }

        // Check for conditions where a crit should be skipped
        boolean skipCritConditions = getMc().player.getAbilities().flying ||
                getMc().player.isFallFlying() ||
                getMc().player.hasStatusEffect(net.minecraft.entity.effect.StatusEffects.BLINDNESS) ||
                getMc().player.isHoldingOntoLadder() ||
                getMc().world.getBlockState(BlockPos.ofFloored(getMc().player.getPos())).getBlock() == net.minecraft.block.Blocks.COBWEB;

        // Cooldown and ground checks for crits
        if (getMc().player.getAttackCooldownProgress(0.5f) < 0.9f) {
            return false;
        }

        // Conditions where crit is allowed
        boolean canCrit = !skipCritConditions &&
                !getMc().player.isOnGround() &&
                getMc().player.fallDistance > 0.0f;

        return canCrit;
    }

    // Placeholder for getting the entity the player is looking at
    private Entity getRtxTarget() {
        // Implement ray tracing logic or use an existing ray tracing method
        return null;
    }

    // Placeholder for checking if the entity is a friend
    private boolean isFriend(String name) {
        // Implement friend checking logic or use your mod's friend system
        return false;
    }

    // Check if the player is holding a valid weapon
    private boolean isHoldingValidWeapon() {
        Item item = getMc().player.getMainHandStack().getItem();
        return item == Items.SWORD || item == Items.BOW || 
               item == Items.HOE || item == Items.PICKAXE || item == Items.AXE;
    }

    // Placeholder for MinecraftClient accessor
    private net.minecraft.client.MinecraftClient getMc() {
        return net.minecraft.client.MinecraftClient.getInstance();
    }
}
