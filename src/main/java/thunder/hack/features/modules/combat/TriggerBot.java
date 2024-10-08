package thunder.hack.features.modules.combat;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.Item;
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
    public final Setting<Float> attackRange = new Setting<>("Range", 3f, 1f, 7.0f);
    public final Setting<BooleanSettingGroup> smartCrit = new Setting<>("SmartCrit", new BooleanSettingGroup(true));
    public final Setting<Boolean> onlySpace = new Setting<>("OnlyCrit", false).addToGroup(smartCrit);
    public final Setting<Boolean> autoJump = new Setting<>("AutoJump", false).addToGroup(smartCrit);
    public final Setting<Boolean> ignoreWalls = new Setting<>("IgnoreWalls", false);
    public final Setting<Boolean> pauseEating = new Setting<>("PauseWhileEating", false);
    public final Setting<Integer> minDelay = new Setting<>("RandomDelayMin", 10, 0, 50);  // Changed to 10ms
    public final Setting<Integer> maxDelay = new Setting<>("RandomDelayMax", 50, 0, 50);  // Changed to 50ms
    public final Setting<Boolean> requireWeapons = new Setting<>("RequireWeapons", true);  // Added requireWeapons option

    private int delay;
    private final Random random = new Random(); // For random delay
    private boolean wasAiming = false;  // Track previous aiming state

    public TriggerBot() {
        super("TriggerBot", Category.COMBAT);
    }

    @EventHandler
    public void onAttack(PlayerUpdateEvent e) {
        if (mc.player.isUsingItem() && pauseEating.getValue()) {
            return;
        }
        if (!mc.options.jumpKey.isPressed() && mc.player.isOnGround() && autoJump.getValue()) {
            mc.player.jump();
        }

        // Check if holding required weapon
        if (requireWeapons.getValue() && !isHoldingRequiredWeapon()) {
            return;
        }

        Entity ent = Managers.PLAYER.getRtxTarget(mc.player.getYaw(), mc.player.getPitch(), attackRange.getValue(), ignoreWalls.getValue());
        boolean isAiming = (ent != null && !Managers.FRIEND.isFriend(ent.getName().getString()));

        // Trigger delay only when going from not aiming to aiming
        if (isAiming && !wasAiming) {
            delay = random.nextInt(minDelay.getValue(), maxDelay.getValue() + 1);  // Random delay between 10 and 50 ms
        }

        // Wait for delay before attacking
        if (delay > 0) {
            delay--;
            wasAiming = isAiming;
            return;
        }

        // Wait until sword is fully charged
        if (ModuleManager.aura.getAttackCooldown() < 1.0f) {
            wasAiming = isAiming;
            return;
        }

        // Attack logic
        if (isAiming) {
            mc.interactionManager.attackEntity(mc.player, ent);
            mc.player.swingHand(Hand.MAIN_HAND);

            // Reset delay after attack
            delay = random.nextInt(minDelay.getValue(), maxDelay.getValue() + 1);
        }

        // Update the aiming state
        wasAiming = isAiming;
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

        if (!mc.options.jumpKey.isPressed() && mergeWithTargetStrafe && mergeWithSpeed && !onlySpace.getValue() && !autoJump.getValue())
            return true;

        if (mc.player.isInLava())
            return true;

        if (!mc.options.jumpKey.isPressed() && ModuleManager.aura.isAboveWater())
            return true;

        if (!reasonForSkipCrit)
            return !mc.player.isOnGround() && mc.player.fallDistance > 0.0f;
        return true;
    }

    private boolean isHoldingRequiredWeapon() {
        Item heldItem = mc.player.getMainHandStack().getItem();
        return heldItem == Items.SWORD || heldItem == Items.BOW || heldItem == Items.AXE ||
                heldItem == Items.PICKAXE || heldItem == Items.TRIDENT;
    }
}
