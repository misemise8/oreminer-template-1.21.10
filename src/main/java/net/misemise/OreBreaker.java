package net.misemise;

import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

/**
 * OreBreaker - 隣接する同じ種類の鉱石を一括破壊
 */
public class OreBreaker {
    private static final Logger LOGGER = LoggerFactory.getLogger("oreminer");
    private static final int MAX_BLOCKS = 64;

    /**
     * 最初に壊したブロックの位置から、隣接する同じ鉱石を探して破壊
     * @param world サーバーワールド
     * @param startPos 最初に壊したブロックの位置（既に空気ブロックになっている）
     * @param originalState 最初に壊したブロックの状態
     * @param player プレイヤー
     * @param heldItem 手に持っているツール
     */
    public static void breakConnectedOres(ServerWorld world, BlockPos startPos,
                                          BlockState originalState, ServerPlayerEntity player,
                                          ItemStack heldItem) {
        LOGGER.info("Starting vein mining from {}", startPos);
        Set<BlockPos> visited = new HashSet<>();

        // 最初の位置は既に壊れているので、隣接ブロックから開始
        BlockPos[] neighbors = {
                startPos.up(), startPos.down(),
                startPos.north(), startPos.south(),
                startPos.east(), startPos.west()
        };

        for (BlockPos neighbor : neighbors) {
            dfs(world, neighbor, originalState, player, heldItem, visited);
        }

        LOGGER.info("Vein mining complete: {} blocks broken", visited.size());
    }

    private static void dfs(ServerWorld world, BlockPos pos, BlockState targetState,
                            ServerPlayerEntity player, ItemStack heldItem, Set<BlockPos> visited) {
        // 上限チェック
        if (visited.size() >= MAX_BLOCKS) {
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

        LOGGER.debug("Breaking ore at {}", pos);

        // ブロックを破壊してドロップを自動回収
        AutoCollector.breakAndCollect(world, pos, currentState, player, heldItem);

        // 隣接ブロックを再帰的に処理
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
