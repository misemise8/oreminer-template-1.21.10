package net.misemise;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.misemise.ClothConfig.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * ブロックを破壊してドロップを自動回収するユーティリティクラス
 */
public class AutoCollector {
    private static final Logger LOGGER = LoggerFactory.getLogger("oreminer");

    /**
     * ブロックを破壊し、ドロップアイテムと経験値を処理
     */
    public static void breakAndCollect(ServerWorld world, BlockPos pos, BlockState state,
                                       ServerPlayerEntity player, ItemStack tool) {
        if (world == null || state == null || player == null) {
            return;
        }

        try {
            if (Config.debugLog) {
                LOGGER.info("Breaking block at {} - autoCollect={}, autoCollectExp={}",
                        pos, Config.autoCollect, Config.autoCollectExp);
            }

            // ブロックを破壊（ドロップなし）
            world.breakBlock(pos, false, player);

            // アイテムドロップの処理
            List<ItemStack> drops = Block.getDroppedStacks(state, world, pos,
                    world.getBlockEntity(pos), player, tool);

            if (Config.autoCollect) {
                // 自動回収：インベントリに直接追加
                for (ItemStack drop : drops) {
                    if (!drop.isEmpty()) {
                        boolean inserted = player.getInventory().insertStack(drop);
                        if (!inserted) {
                            Block.dropStack(world, pos, drop);
                        }
                    }
                }
                if (Config.debugLog) {
                    LOGGER.info("Auto-collected {} items", drops.size());
                }
            } else {
                // 通常ドロップ：地面に落とす
                for (ItemStack drop : drops) {
                    if (!drop.isEmpty()) {
                        Block.dropStack(world, pos, drop);
                    }
                }
                if (Config.debugLog) {
                    LOGGER.info("Dropped {} items normally", drops.size());
                }
            }

            // 経験値の処理
            if (Config.autoCollectExp) {
                // 経験値オーブを生成
                state.onStacksDropped(world, pos, tool, true);

                // 少し遅延させて経験値オーブを回収
                world.getServer().execute(() -> {
                    try {
                        Box searchBox = new Box(pos).expand(4.0);
                        List<ExperienceOrbEntity> orbs = world.getEntitiesByClass(
                                ExperienceOrbEntity.class, searchBox,
                                orb -> !orb.isRemoved() && orb.age < 10);

                        int collectedOrbs = 0;
                        for (ExperienceOrbEntity orb : orbs) {
                            // プレイヤーとオーブを衝突させて経験値を付与
                            orb.onPlayerCollision(player);
                            collectedOrbs++;
                        }

                        if (Config.debugLog && collectedOrbs > 0) {
                            LOGGER.info("Auto-collected {} experience orbs", collectedOrbs);
                        }
                    } catch (Exception e) {
                        if (Config.debugLog) {
                            LOGGER.error("Failed to collect experience", e);
                        }
                    }
                });
            } else {
                // 通常通り経験値オーブをドロップ
                state.onStacksDropped(world, pos, tool, true);
                if (Config.debugLog) {
                    LOGGER.info("Dropped experience normally");
                }
            }

        } catch (Exception e) {
            LOGGER.error("Failed to break and collect block at {}", pos, e);
        }
    }
}