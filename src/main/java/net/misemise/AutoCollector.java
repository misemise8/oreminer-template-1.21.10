package net.misemise;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

import java.util.List;

public class AutoCollector {
    public static void collectAndGiveDrops(ServerWorld world, BlockPos pos, BlockState state, BlockEntity entity, ServerPlayerEntity player, ItemStack tool) {
        if (world == null || state == null) return;

        try {
            // これで Fortune, Silk Touch, 経験値すべて含めて dropStacks が処理してくれる
            Block.dropStacks(state, world, pos, entity, player, tool);
        } catch (Throwable t) {
            // 互換性フォールバック
            Block.dropStacks(state, world, pos);
        }

        // --- 近くに落ちたアイテムを拾う ---
        Box box = new Box(pos).expand(1.5);
        List<ItemEntity> items = world.getEntitiesByClass(ItemEntity.class, box, e -> true);

        for (ItemEntity ie : items) {
            ItemStack drop = ie.getStack();
            boolean inserted = player != null && player.getInventory().insertStack(drop);
            if (inserted) {
                ie.discard();
            }
        }
        // 経験値オーブも吸収
        List<net.minecraft.entity.ExperienceOrbEntity> orbs = world.getEntitiesByClass(
                net.minecraft.entity.ExperienceOrbEntity.class,
                new Box(pos).expand(2.0),
                e -> true
        );
        for (var orb : orbs) {
            if (player != null) {
                orb.onPlayerCollision(player); // 経験値をプレイヤーに吸収させる
            }
        }

    }
}
