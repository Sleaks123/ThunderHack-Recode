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
import thunder.hack.*;
import java.util.Random;

public final class JumpReset extends Module {
package thunder.hack.features.modules.combat;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerEntity;
import thunder.hack.core.managers.Managers;
import thunder.hack.events.impl.TickEvent;
import thunder.hack.features.modules.Module;
import thunder.hack.setting.Setting;
import thunder.hack.util.RandomUtil;

public class AutoJumpReset extends Module {
    public final Setting<Float> jumpChance = new Setting<>("Chance", 50f, 0f, 100f);

    public AutoJumpReset() {
        super("AutoJumpReset", Category.COMBAT);
    }

  public void onEnable() {
        if (!mc.player.isOnGround()) {
            return;
        }

        // Check if player is attacking or being attacked by another player and is on the ground
        if (mc.player.getAttacker() instanceof PlayerEntity attacker && attacker.isOnGround()) {
            // Check if the attacker has hit the player and meets certain conditions
            if (attacker.hurtTime > 0 && attacker.hurtTime < 10 && attacker.hurtTime == attacker.maxHurtTime - 1
                    && !attacker.isOnFire()) {

                // Random chance to jump based on setting
                if (RandomUtil.INSTANCE.getRandom().nextFloat() <= jumpChance.getValue() / 100) {
                    mc.player.jump();  // Perform the jump to reset knockback
                }
}

}
  }
}

