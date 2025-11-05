package net.misemise;

import net.fabricmc.api.ClientModInitializer;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.misemise.ClothConfig.Config; // あなたの設定クラスの場所に合わせて

public class OreMinerClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        AutoConfig.register(Config.class, GsonConfigSerializer::new);
    }
}
