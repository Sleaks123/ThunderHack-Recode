package thunder.hack.features.modules.combat;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.StatusEffects;
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
    public final Setting<Float> attackRange = new Setting<>("Range", 3.0F, 1.0F, 7.0F);
    public final Setting<BooleanSettingGroup> smartCrit = new Setting<>("SmartCrit", new BooleanSettingGroup(true));
    public final Setting<Boolean> onlySpace;
    public final Setting<Boolean> autoJump;
    public final Setting<Boolean> ignoreWalls;
    public final Setting<Boolean> pauseEating;
    public final Setting<Boolean> requireWeapon;
    
    private int delay;
    private final Random random = new Random(); // For random delay

    public TriggerBot() {
        super("TriggerBot", Category.COMBAT);
        this.onlySpace = (new Setting<>("OnlyCrit", false)).addToGroup(this.smartCrit);
        this.autoJump = (new Setting<>("AutoJump", false)).addToGroup(this.smartCrit);
        this.ignoreWalls = new Setting<>("IgnoreWalls", false);
        this.pauseEating = new Setting<>("PauseWhileEating", false);
        this.requireWeapon = new Setting<>("RequireWeapon", true);
    }

    @EventHandler
    public void onAttack(PlayerUpdateEvent e) {
        if (mc.field_1755 == null) {
            if (!mc.field_1724.method_6115() || !pauseEating.getValue()) {
                if (!mc.field_1690.field_1903.method_1434() && mc.field_1724.method_24828() && autoJump.getValue()) {
                    mc.field_1724.method_6043();
                }

                if (delay > 0) {
                    --delay;
                } else if (autoCrit()) {
                    if (!requireWeapon.getValue() || isHoldingWeapon()) {
                        class_1297 ent = Managers.PLAYER.getRtxTarget(mc.field_1724.method_36454(), mc.field_1724.method_36455(), attackRange.getValue(), ignoreWalls.getValue());
                        if (ent != null && !Managers.FRIEND.isFriend(ent.method_5477().getString())) {
                            mc.field_1761.method_2918(mc.field_1724, ent);
                            mc.field_1724.method_6104(class_1268.field_5808);

                            // Set delay for the next hit (50 to 100 ms)
                            delay = random.nextInt(1, 3); // Random delay of 1-2 ticks (50-100 ms)
                        }
                    }
                }
            }
        }
    }

    private boolean autoCrit() {
        boolean reasonForSkipCrit = !smartCrit.getValue().isEnabled()
                || mc.field_1724.method_31549().field_7479
                || mc.field_1724.method_6128()
                || ModuleManager.elytraPlus.isEnabled()
                || mc.field_1724.method_6059(class_1294.field_5919)
                || mc.field_1724.method_21754()
                || mc.field_1687.method_8320(class_2338.method_49638(mc.field_1724.method_19538())).method_26204() == class_2246.field_10343;

        if (mc.field_1724.field_6017 > 1.0F && (double)mc.field_1724.field_6017 < 1.14) {
            return false;
        } else if (ModuleManager.aura.getAttackCooldown() < (mc.field_1724.method_24828() ? 1.0F : 0.9F)) {
            return false;
        } else {
            boolean mergeWithTargetStrafe = !ModuleManager.targetStrafe.isEnabled() || !ModuleManager.targetStrafe.jump.getValue();
            boolean mergeWithSpeed = !ModuleManager.speed.isEnabled() || mc.field_1724.method_24828();
            if (!mc.field_1690.field_1903.method_1434() && mergeWithTargetStrafe && mergeWithSpeed && !onlySpace.getValue() && !autoJump.getValue()) {
                return true;
            } else if (mc.field_1724.method_5771()) {
                return true;
            } else if (!mc.field_1690.field_1903.method_1434() && ModuleManager.aura.isAboveWater()) {
                return true;
            } else if (reasonForSkipCrit) {
                return true;
            } else {
                return !mc.field_1724.method_24828() && mc.field_1724.field_6017 > 0.0F;
            }
        }
    }

    private boolean isHoldingWeapon() {
        class_1792 heldItem = mc.field_1724.method_6047().method_7909();
        return heldItem instanceof class_1829 || heldItem instanceof class_1743 || heldItem instanceof class_1810 || heldItem instanceof class_1794 || heldItem instanceof class_1835 || heldItem instanceof class_1753;
    }
}
