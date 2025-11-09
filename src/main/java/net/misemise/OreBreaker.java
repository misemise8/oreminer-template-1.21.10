package net.misemise;

import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.misemise.ClothConfig.Config;
import net.misemise.network.NetworkHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

/**
 * OreBreaker - 隣接する同じ種類の鉱石を一括破壊
 */
public class OreBreaker {
    private static final Logger LOGGER = LoggerFactory.getLogger("oreminer");

    /**
     * 指定位置から同じ種類の鉱石を探して一括破壊
     */
    public static void breakConnectedOres(ServerWorld world, BlockPos startPos,
                                          BlockState originalState, ServerPlayerEntity player,
                                          ItemStack heldItem) {
        if (Config.debugLog) {
            LOGGER.info("Starting vein mining from {}", startPos);
        }
        Set<BlockPos> visited = new HashSet<>();

        // 最初のブロックから開始
        dfs(world, startPos, originalState, player, heldItem, visited);

        int blocksCount = visited.size();
        if (Config.debugLog) {
            LOGGER.info("Vein mining complete: {} blocks broken", blocksCount);
        }

        // クライアントに破壊したブロック数を送信
        NetworkHandler.sendBlocksMinedCount(player, blocksCount);
    }

    private static void dfs(ServerWorld world, BlockPos pos, BlockState targetState,
                            ServerPlayerEntity player, ItemStack heldItem, Set<BlockPos> visited) {
        // 上限チェック
        if (visited.size() >= Config.maxBlocks) {
            return;
        }

        // 既に訪問済み
        if (visited.contains(pos)) {
            return;
        }

        BlockState currentState = world.getBlockState(pos);

        // 同じ種類の鉱石かチェック
        if (!currentState.isOf(targetState.getBlock())) {
            return;
        }

        // 訪問済みにマーク
        visited.add(pos);

        if (Config.debugLog) {
            LOGGER.debug("Breaking ore at {}", pos);
        }

        // ブロックを破壊してドロップを自動回収
        AutoCollector.breakAndCollect(world, pos, currentState, player, heldItem);

        // 隣接ブロックを再帰的に処理
        if (Config.searchDiagonal) {
            // 26方向探索（上下左右前後 + 斜め）
            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        // 中心（0,0,0）はスキップ
                        if (dx == 0 && dy == 0 && dz == 0) continue;

                        BlockPos neighbor = pos.add(dx, dy, dz);
                        dfs(world, neighbor, targetState, player, heldItem, visited);
                    }
                }
            }
        } else {
            // 6方向探索（上下左右前後のみ）
            BlockPos[] neighbors = {
                    pos.up(), pos.down(),
                    pos.north(), pos.south(),
                    pos.east(), pos.west()
            };
            for (BlockPos neighbor : neighbors) {
                dfs(world, neighbor, targetState, player, heldItem, visited);
            }
        }
    }

}