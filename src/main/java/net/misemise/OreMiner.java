package net.misemise;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OreMiner implements ModInitializer {
	public static final String MOD_ID = "oreminer";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("OreMiner initialized!");

		// AFTERイベント：鉱石の一括破壊
		PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, entity) -> {
			// クライアント側では何もしない
			if (world.isClient()) return;

			// サーバー側のチェック
			if (!(world instanceof ServerWorld serverWorld)) return;
			if (!(player instanceof ServerPlayerEntity serverPlayer)) return;

			ItemStack held = serverPlayer.getMainHandStack();

			// つるはしでない場合はスキップ
			if (!OreUtils.isPickaxe(held)) {
				return;
			}

			// 鉱石でない場合はスキップ
			if (!OreUtils.isOre(state)) {
				return;
			}

			LOGGER.info("Starting vein mining at {} for player {}",
					pos, serverPlayer.getName().getString());

			// 隣接する鉱石を一括破壊（最初のブロックは既に壊れている）
			OreBreaker.breakConnectedOres(serverWorld, pos, state, serverPlayer, held);
		});
	}
}