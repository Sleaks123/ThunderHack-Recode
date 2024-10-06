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
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.BooleanSettingGroup;

import java.util.Random;

public final class TriggerBot extends Module {
    // Settings directly in TriggerBot class
    public final Setting<Float> attackRange = new Setting<>("Range", 3f, 1f, 7.0f);
    public final Setting<BooleanSettingGroup> smartCrit = new Setting<>("SmartCrit", new BooleanSettingGroup(true));
    public final Setting<Boolean> onlySpace = new Setting<>("OnlyCrit", false).addToGroup(smartCrit);
    public final Setting<Boolean> autoJump = new Setting<>("AutoJump", false).addToGroup(smartCrit);
    public final Setting<Boolean> ignoreWalls = new Setting<>("IgnoreWalls", false);
    public final Setting<Boolean> pauseEating = new Setting<>("PauseWhileEating", false);
    public final Setting<Boolean> requireWeapon = new Setting<>("RequireWeapon", false);
    
    // New setting for random delay (reaction)
    public final Setting<Float> reaction = new Setting<>("Reaction", 10.0f, v -> v >= 0.0f && v <= 200.0f).description("Delay between looking at the entity and attacking");

    private long lastAttackTime = System.currentTimeMillis(); // Timer for managing delay
    private final Random random = new Random(); // For random delay

    public TriggerBot() {
        super("TriggerBot", Category.COMBAT);
    }

    @EventHandler
    public void onAttack(PlayerUpdateEvent e) {
        if (mc.player.isUsingItem() && pauseEating.getValue()) {
            return;
        }

        // Check if requireWeapon is enabled and if the player has a weapon in hand
        if (requireWeapon.getValue() && !isHoldingWeapon()) {
            return; // Exit if no weapon is found in hand
        }

        // Implement random delay (reaction delay between looking at entity and attacking)
        if (System.currentTimeMillis() - lastAttackTime < reaction.getValue()) {
            return; // Wait until the delay is finished before attacking
        }

        if (!mc.options.jumpKey.isPressed() && mc.player.isOnGround() && autoJump.getValue()) {
            mc.player.jump();
        }

        // Smart crits should not be delayed
        if (!autoCrit()) {
            return;
        }

        Entity ent = Managers.PLAYER.getRtxTarget(mc.player.getYaw(), mc.player.getPitch(), attackRange.getValue(), ignoreWalls.getValue());
        if (ent != null && !Managers.FRIEND.isFriend(ent.getName().getString())) {
            mc.interactionManager.attackEntity(mc.player, ent);
            mc.player.swingHand(Hand.MAIN_HAND);

            // Reset the timer after attacking
            lastAttackTime = System.currentTimeMillis();
        }
    }

    private boolean autoCrit() {
        boolean reasonForSkipCrit =
                !smartCrit.getValue().isEnabled()
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

        if (!mc.options.jumpKey.isPressed() && mergeWithTargetStrafe && mergeWithSpeed && !onlySpace.getValue() && !autoJump.getValue()) {
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
