package net.misemise.client;

import net.fabricmc.api.ClientModInitializer;
import net.misemise.keybind.KeyBindings;
import net.misemise.keybind.KeyStateTracker;
import net.misemise.network.NetworkHandler;

public class OreMinerClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // クライアント側のネットワーク登録のみ
        NetworkHandler.registerClient();

        // キーバインドを登録
        KeyBindings.register();

        // キー状態トラッカーを登録
        KeyStateTracker.register();
    }
}
