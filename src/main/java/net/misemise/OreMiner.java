package net.misemise;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.misemise.ClothConfig.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OreMiner implements ModInitializer {
	public static final String MOD_ID = "oreminer";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("OreMiner initialized!");

		// 設定を読み込む
		Config.load();

		// BEFOREイベント：鉱石をつるはしで壊す場合、標準処理をキャンセルしてMod側で処理
		PlayerBlockBreakEvents.BEFORE.register((world, player, pos, state, entity) -> {
			// クライアント側では何もしない
			if (world.isClient()) return true;

			// サーバー側のチェック
			if (!(world instanceof ServerWorld serverWorld)) return true;
			if (!(player instanceof ServerPlayerEntity serverPlayer)) return true;

			ItemStack held = serverPlayer.getMainHandStack();

			// つるはしで鉱石を壊す場合のみ特別処理
			if (OreUtils.isPickaxe(held) && OreUtils.isOre(state)) {
				LOGGER.info("Vein mining triggered at {} by player {}",
						pos, serverPlayer.getName().getString());

				// Mod側で一括破壊を実行
				OreBreaker.breakConnectedOres(serverWorld, pos, state, serverPlayer, held);

				// 標準のブロック破壊処理をキャンセル
				return false;
			}

			// 通常通り処理
			return true;
		});
	}
}