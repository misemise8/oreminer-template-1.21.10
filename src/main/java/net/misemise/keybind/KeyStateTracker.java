package net.misemise.keybind;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.misemise.ClothConfig.ConfigScreen;
import net.misemise.OreMiner;
import net.misemise.network.NetworkHandler;

/**
 * クライアント側でキーの状態を監視してサーバーに送信
 */
public class KeyStateTracker {
    private static boolean lastKeyState = false;

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) return;

            // 一括採掘キーの状態を監視
            boolean currentKeyState = KeyBindings.isVeinMinerKeyPressed();
            if (currentKeyState != lastKeyState) {
                OreMiner.LOGGER.info("Key state changed: {} -> {}", lastKeyState, currentKeyState);
                NetworkHandler.sendKeyState(currentKeyState);
                lastKeyState = currentKeyState;
            }

            // 設定画面を開くキー
            if (KeyBindings.wasOpenConfigPressed()) {
                MinecraftClient.getInstance().setScreen(ConfigScreen.createConfigScreen(client.currentScreen));
            }
        });

        OreMiner.LOGGER.info("KeyStateTracker registered");
    }
}