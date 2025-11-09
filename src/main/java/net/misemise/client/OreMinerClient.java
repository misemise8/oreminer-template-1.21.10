package net.misemise.client;

import net.fabricmc.api.ClientModInitializer;
import net.misemise.OreMiner;
import net.misemise.keybind.KeyBindings;
import net.misemise.keybind.KeyStateTracker;
import net.misemise.network.NetworkHandler;

public class OreMinerClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        OreMiner.LOGGER.info("OreMinerClient initializing...");

        // クライアント側のネットワーク登録
        NetworkHandler.registerClient();

        // キーバインドを登録
        KeyBindings.register();

        // キー状態トラッカーを登録
        KeyStateTracker.register();

        // HUDを登録 ★★★ この行を追加 ★★★
        VeinMiningHud.register();
        OreMiner.LOGGER.info("VeinMiningHud registered");
    }
}