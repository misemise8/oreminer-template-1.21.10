package net.misemise;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * ブロックを破壊してドロップを自動回収するユーティリティクラス
 */
public class AutoCollector {
    private static final Logger LOGGER = LoggerFactory.getLogger("oreminer");

    /**
     * ブロックを破壊し、ドロップアイテムと経験値を自動回収
     */
    public static void breakAndCollect(ServerWorld world, BlockPos pos, BlockState state,
                                       ServerPlayerEntity player, ItemStack tool) {
        if (world == null || state == null || player == null) {
            return;
        }

        try {
            // ブロックを破壊（ドロップなし）
            world.breakBlock(pos, false, player);

            // エンチャントを考慮してドロップを生成
            List<ItemStack> drops = Block.getDroppedStacks(state, world, pos,
                    world.getBlockEntity(pos), player, tool);

            // ドロップを直接インベントリに追加
            for (ItemStack drop : drops) {
                if (!drop.isEmpty()) {
                    boolean inserted = player.getInventory().insertStack(drop);
                    if (!inserted) {
                        // インベントリに入らない場合は地面にドロップ
                        ItemEntity itemEntity = new ItemEntity(world,
                                pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, drop);
                        world.spawnEntity(itemEntity);
                        LOGGER.debug("Inventory full, dropped item at {}", pos);
                    }
                }
            }

            // 経験値を付与（Block.dropExperienceを利用）
            try {
                // dropExperienceメソッドを使用して経験値オーブを生成
                state.onStacksDropped(world, pos, tool, true);
            } catch (Exception e) {
                LOGGER.debug("Could not drop experience: {}", e.getMessage());
            }

            // 近くに落ちている既存のアイテムと経験値オーブも回収
            collectNearbyItems(world, pos, player);

        } catch (Exception e) {
            LOGGER.error("Failed to break and collect block at {}", pos, e);
        }
    }

    /**
     * 近くに落ちているアイテムと経験値オーブを回収
     */
    private static void collectNearbyItems(ServerWorld world, BlockPos pos, ServerPlayerEntity player) {
        Box searchBox = new Box(pos).expand(2.0);

        // アイテムエンティティを回収
        List<ItemEntity> items = world.getEntitiesByClass(ItemEntity.class, searchBox, e -> true);
        for (ItemEntity itemEntity : items) {
            ItemStack stack = itemEntity.getStack();
            if (player.getInventory().insertStack(stack)) {
                itemEntity.discard();
            }
        }

        // 経験値オーブを回収
        List<ExperienceOrbEntity> orbs = world.getEntitiesByClass(
                ExperienceOrbEntity.class, searchBox, e -> true);
        for (ExperienceOrbEntity orb : orbs) {
            if (!orb.isRemoved()) {
                // ExperienceOrbEntityをプレイヤーに触れさせて経験値を付与
                orb.onPlayerCollision(player);
            }
        }
    }
}