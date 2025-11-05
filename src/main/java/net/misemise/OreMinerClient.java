package net.misemise;

import net.fabricmc.api.ClientModInitializer;
import net.misemise.keybind.KeyBindings;
import net.misemise.keybind.KeyStateTracker;
import net.misemise.network.NetworkHandler;

public class OreMinerClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // キーバインドを登録
        KeyBindings.register();

        // クライアント側のネットワークハンドラを登録
        NetworkHandler.registerClient();

        // キー状態トラッカーを登録
        KeyStateTracker.register();
    }
}