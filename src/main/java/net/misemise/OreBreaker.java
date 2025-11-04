package net.misemise;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

/**
 * OreBreaker - 一括破壊の再帰処理（ログ付きでデバッグしやすくしてある）
 */
public class OreBreaker {
    private static final Logger LOGGER = LoggerFactory.getLogger("oreminer");
    private static final int MAX_BLOCKS = 64; // 無限ループ防止（必要なら増やす）

    public static void breakConnectedOres(ServerWorld world, BlockPos startPos, ServerPlayerEntity player, ItemStack heldItem) {
        LOGGER.info("OreBreaker: start at {} by player {}", startPos, player.getName().getString());
        Set<BlockPos> visited = new HashSet<>();
        dfs(world, startPos, player, heldItem, visited);
        LOGGER.info("OreBreaker: finished, visited {} blocks", visited.size());
    }

    private static void dfs(ServerWorld world, BlockPos pos, ServerPlayerEntity player, ItemStack heldItem, Set<BlockPos> visited) {
        if (visited.size() >= MAX_BLOCKS) {
            LOGGER.info("OreBreaker: reached MAX_BLOCKS ({}) - stop expanding", MAX_BLOCKS);
            return;
        }
        if (visited.contains(pos)) {
            // 既に処理済み
            return;
        }

        BlockState state = world.getBlockState(pos);
        if (!OreUtils.isOre(state)) {
            LOGGER.debug("OreBreaker: skipping {}, not ore (state={})", pos, state);
            return;
        }

        visited.add(pos);

        // 破壊前ログ
        LOGGER.info("OreBreaker: breaking block at {}, block={}", pos, state.getBlock().toString());

        // ブロックを破壊（false = ドロップは自前で処理）
        boolean broken = world.breakBlock(pos, false, player);
        LOGGER.info("OreBreaker: breakBlock returned {} at {}", broken, pos);

        // ブロックエンティティを取得して AutoCollector を呼ぶ（ドロップ自動回収）
        BlockEntity entity = world.getBlockEntity(pos);
        try {
            AutoCollector.collectAndGiveDrops(world, pos, state, entity, player, heldItem);
            LOGGER.info("OreBreaker: collected drops at {}", pos);
        } catch (Throwable t) {
            LOGGER.error("OreBreaker: AutoCollector failed at " + pos, t);
        }

        // 隣接6方向を再帰
        BlockPos[] neighbors = new BlockPos[] {
                pos.up(), pos.down(), pos.north(), pos.south(), pos.east(), pos.west()
        };
        for (BlockPos nb : neighbors) {
            dfs(world, nb, player, heldItem, visited);
        }
    }
}
