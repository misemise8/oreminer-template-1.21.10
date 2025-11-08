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
                .setTitle(Text.translatable("config.oreminer.title"))
                .setSavingRunnable(() -> {
                    Config.save();
                    // 設定が保存されたことを通知
                });

        // 一般設定カテゴリ
        ConfigCategory general = builder.getOrCreateCategory(
                Text.translatable("config.oreminer.category.general"));

        ConfigEntryBuilder entryBuilder = builder.entryBuilder();

        // 最大ブロック数
        general.addEntry(entryBuilder.startIntSlider(
                        Text.translatable("config.oreminer.maxBlocks"),
                        Config.maxBlocks,
                        1, 256)
                .setDefaultValue(64)
                .setTooltip(Text.translatable("config.oreminer.maxBlocks.tooltip"))
                .setSaveConsumer(value -> Config.maxBlocks = value)
                .build());

        // 斜め探索
        general.addEntry(entryBuilder.startBooleanToggle(
                        Text.translatable("config.oreminer.searchDiagonal"),
                        Config.searchDiagonal)
                .setDefaultValue(true)
                .setTooltip(Text.translatable("config.oreminer.searchDiagonal.tooltip"))
                .setSaveConsumer(value -> Config.searchDiagonal = value)
                .build());

        // 自動回収
        general.addEntry(entryBuilder.startBooleanToggle(
                        Text.translatable("config.oreminer.autoCollect"),
                        Config.autoCollect)
                .setDefaultValue(true)
                .setTooltip(Text.translatable("config.oreminer.autoCollect.tooltip"))
                .setSaveConsumer(value -> Config.autoCollect = value)
                .build());

        // 経験値自動回収
        general.addEntry(entryBuilder.startBooleanToggle(
                        Text.translatable("config.oreminer.autoCollectExp"),
                        Config.autoCollectExp)
                .setDefaultValue(true)
                .setTooltip(Text.translatable("config.oreminer.autoCollectExp.tooltip"))
                .setSaveConsumer(value -> Config.autoCollectExp = value)
                .build());

        // デバッグログ
        general.addEntry(entryBuilder.startBooleanToggle(
                        Text.translatable("config.oreminer.debugLog"),
                        Config.debugLog)
                .setDefaultValue(false)
                .setTooltip(Text.translatable("config.oreminer.debugLog.tooltip"))
                .setSaveConsumer(value -> Config.debugLog = value)
                .build());

        return builder.build();
    }
}