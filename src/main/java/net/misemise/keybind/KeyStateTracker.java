package net.misemise.keybind;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.misemise.OreMiner;
import net.misemise.network.NetworkHandler;

public class KeyStateTracker {
    private static boolean lastKeyState = false;

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) return;

            boolean currentKeyState = KeyBindings.isVeinMinerKeyPressed();

            if (currentKeyState != lastKeyState) {
                OreMiner.LOGGER.info("Key state changed: {} -> {}", lastKeyState, currentKeyState);
                NetworkHandler.sendKeyState(currentKeyState);
                lastKeyState = currentKeyState;
            }
        });

        OreMiner.LOGGER.info("KeyStateTracker registered");
    }
}