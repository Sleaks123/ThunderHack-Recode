package net.fabricmc.example;

import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.fabricmc.api.ModInitializer;

import java.util.Random;

public class TriggerBot implements Module {
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
        if (net.minecraft.client.MinecraftClient.getInstance().player == null) return; // Null check for player

        // Check if player is eating and the "pauseEating" option is enabled
        if (net.minecraft.client.MinecraftClient.getInstance().player.isUsingItem() && pauseEating) {
            return;
        }

        // Auto jump if enabled
        if (!net.minecraft.client.MinecraftClient.getInstance().options.jumpKey.isPressed() &&
            net.minecraft.client.MinecraftClient.getInstance().player.isOnGround() && 
            autoJumpEnabled) {
            net.minecraft.client.MinecraftClient.getInstance().player.jump();
        }

        // Smart crit logic (delay does not affect smart crits)
        if (!performSmartCrit()) {
            // Delay handling between attacks
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
            if (net.minecraft.client.MinecraftClient.getInstance().player.getAttackCooldownProgress(0.5f) >= 0.95f) {

                // If delay is active, decrease the counter and return early
                if (delay > 0) {
                    delay--;
                    return;
                }

                // Attack the target entity
                net.minecraft.client.MinecraftClient.getInstance().interactionManager.attackEntity(
                    net.minecraft.client.MinecraftClient.getInstance().player, targetEntity);
                net.minecraft.client.MinecraftClient.getInstance().player.swingHand(Hand.MAIN_HAND);

                // Set random delay for next attack (between 10-50 ms)
                delay = random.nextInt(minDelay, maxDelay + 1);
            }
        }
    }

    private boolean performSmartCrit() {
        if (net.minecraft.client.MinecraftClient.getInstance().player == null) return false;

        // Check for conditions where a crit should be skipped
        boolean skipCritConditions = net.minecraft.client.MinecraftClient.getInstance().player.getAbilities().flying ||
                net.minecraft.client.MinecraftClient.getInstance().player.isFallFlying() ||
                net.minecraft.client.MinecraftClient.getInstance().player.hasStatusEffect(net.minecraft.entity.effect.StatusEffects.BLINDNESS) ||
                net.minecraft.client.MinecraftClient.getInstance().player.isHoldingOntoLadder() ||
                net.minecraft.client.MinecraftClient.getInstance().world.getBlockState(BlockPos.ofFloored(net.minecraft.client.MinecraftClient.getInstance().player.getPos())).getBlock() == net.minecraft.block.Blocks.COBWEB;

        // Cooldown and ground checks for crits
        if (net.minecraft.client.MinecraftClient.getInstance().player.getAttackCooldownProgress(0.5f) < 0.9f) {
            return false;
        }

        // Conditions where crit is allowed
        boolean canCrit = !skipCritConditions &&
                !net.minecraft.client.MinecraftClient.getInstance().player.isOnGround() &&
                net.minecraft.client.MinecraftClient.getInstance().player.fallDistance > 0.0f;

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
    Item item = net.minecraft.client.MinecraftClient.getInstance().player.getMainHandStack().getItem();
    return item == Items.WOODEN_SWORD || item == Items.STONE_SWORD || item == Items.IRON_SWORD || 
           item == Items.GOLDEN_SWORD || item == Items.DIAMOND_SWORD || item == Items.NETHERITE_SWORD ||
           item == Items.BOW ||
           item == Items.WOODEN_HOE || item == Items.STONE_HOE || item == Items.IRON_HOE || 
           item == Items.GOLDEN_HOE || item == Items.DIAMOND_HOE || item == Items.NETHERITE_HOE ||
           item == Items.WOODEN_PICKAXE || item == Items.STONE_PICKAXE || item == Items.IRON_PICKAXE || 
           item == Items.GOLDEN_PICKAXE || item == Items.DIAMOND_PICKAXE || item == Items.NETHERITE_PICKAXE ||
           item == Items.WOODEN_AXE || item == Items.STONE_AXE || item == Items.IRON_AXE || 
           item == Items.GOLDEN_AXE || item == Items.DIAMOND_AXE || item == Items.NETHERITE_AXE;
}

}
