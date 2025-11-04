package net.misemise;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
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
		// BEFORE: つるはしでないなら破壊をキャンセル
		PlayerBlockBreakEvents.BEFORE.register((world, player, pos, state, entity) -> {
			return OreUtils.isPickaxe(player.getMainHandStack());
		});

		// AFTER: デバッグ付き（これを1つだけ残す）
		PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, entity) -> {
			LOGGER.info("(dbg) AFTER fired for player={}, pos={}, state={}",
					player.getName().getString(), pos, state);

			if (world.isClient()) {
				LOGGER.info("(dbg) world.isClient == true -> returning");
				return;
			}
			LOGGER.info("(dbg) world.isClient == false");

			if (!(world instanceof ServerWorld serverWorld)) {
				LOGGER.info("(dbg) world is NOT ServerWorld -> returning");
				return;
			} else {
				LOGGER.info("(dbg) cast to ServerWorld ok");
			}

			if (!(player instanceof ServerPlayerEntity serverPlayer)) {
				LOGGER.info("(dbg) player is NOT ServerPlayerEntity -> returning");
				return;
			} else {
				LOGGER.info("(dbg) cast to ServerPlayerEntity ok");
			}

			ItemStack held = serverPlayer.getMainHandStack();
			boolean isPick = OreUtils.isPickaxe(held);
			LOGGER.info("(dbg) heldItem={}, isPickaxe={}", held.getItem().toString(), isPick);
			if (!isPick) {
				LOGGER.info("(dbg) not a pickaxe -> returning");
				return;
			}

			boolean isOre = OreUtils.isOre(state);
			LOGGER.info("(dbg) isOre={}", isOre);
			if (!isOre) {
				LOGGER.info("(dbg) not ore -> returning");
				return;
			}

			LOGGER.info("(dbg) calling OreBreaker.breakConnectedOres(...)");
			OreBreaker.breakConnectedOres(serverWorld, pos, serverPlayer, held);
		});
	}
}
