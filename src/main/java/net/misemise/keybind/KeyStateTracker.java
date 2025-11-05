package net.misemise.keybind;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
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

            boolean currentKeyState = KeyBindings.isVeinMinerKeyPressed();

            // キー状態が変化した場合のみサーバーに送信
            if (currentKeyState != lastKeyState) {
                NetworkHandler.sendKeyState(currentKeyState);
                lastKeyState = currentKeyState;
            }
        });

        OreMiner.LOGGER.info("KeyStateTracker registered");
    }
}