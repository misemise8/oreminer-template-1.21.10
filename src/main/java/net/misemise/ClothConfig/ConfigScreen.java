package net.misemise.ClothConfig;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public class ConfigScreen {
    public static Screen createConfigScreen(Screen parent) {
        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Text.translatable("config.oreminer.title"));

        ConfigCategory general = builder.getOrCreateCategory(
                Text.translatable("config.oreminer.category.general"));

        ConfigEntryBuilder entryBuilder = builder.entryBuilder();

        // 最大ブロック数
        general.addEntry(entryBuilder.startIntSlider(
                        Text.translatable("config.oreminer.maxBlocks"),
                        Config.maxBlocks,
                        1, 256)
                .setDefaultValue(64)
                .setSaveConsumer(value -> Config.maxBlocks = value)
                .build());

        // 斜め探索
        general.addEntry(entryBuilder.startBooleanToggle(
                        Text.translatable("config.oreminer.searchDiagonal"),
                        Config.searchDiagonal)
                .setDefaultValue(true)
                .setSaveConsumer(value -> Config.searchDiagonal = value)
                .build());

        // 自動回収
        general.addEntry(entryBuilder.startBooleanToggle(
                        Text.translatable("config.oreminer.autoCollect"),
                        Config.autoCollect)
                .setDefaultValue(true)
                .setSaveConsumer(value -> Config.autoCollect = value)
                .build());

        // 経験値自動回収
        general.addEntry(entryBuilder.startBooleanToggle(
                        Text.translatable("config.oreminer.autoCollectExp"),
                        Config.autoCollectExp)
                .setDefaultValue(true)
                .setSaveConsumer(value -> Config.autoCollectExp = value)
                .build());

        // デバッグログ
        general.addEntry(entryBuilder.startBooleanToggle(
                        Text.translatable("config.oreminer.debugLog"),
                        Config.debugLog)
                .setDefaultValue(false)
                .setSaveConsumer(value -> Config.debugLog = value)
                .build());

        builder.setSavingRunnable(Config::save);

        return builder.build();
    }
}