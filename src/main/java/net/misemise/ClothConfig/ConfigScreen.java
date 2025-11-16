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

        // アウトラインの色
        general.addEntry(entryBuilder.startSelector(
                        Text.translatable("config.oreminer.outlineColor"),
                        new String[]{"Cyan", "Red", "Yellow", "Green", "Purple", "White"},
                        getColorName(Config.outlineColor))
                .setDefaultValue("Cyan")
                .setTooltip(Text.translatable("config.oreminer.outlineColor.tooltip"))
                .setSaveConsumer(value -> {
                    Config.outlineColor = getColorIndex(value);
                })
                .build());

        // 破壊後のブロック数表示
        general.addEntry(entryBuilder.startBooleanToggle(
                        Text.translatable("config.oreminer.showBlocksMinedCount"),
                        Config.showBlocksMinedCount)
                .setDefaultValue(true)
                .setTooltip(Text.translatable("config.oreminer.showBlocksMinedCount.tooltip"))
                .setSaveConsumer(value -> Config.showBlocksMinedCount = value)
                .build());

        // 破壊前のブロック数プレビュー
        general.addEntry(entryBuilder.startBooleanToggle(
                        Text.translatable("config.oreminer.showBlocksPreview"),
                        Config.showBlocksPreview)
                .setDefaultValue(true)
                .setTooltip(Text.translatable("config.oreminer.showBlocksPreview.tooltip"))
                .setSaveConsumer(value -> Config.showBlocksPreview = value)
                .build());

        // アウトラインの太さ
        general.addEntry(entryBuilder.startFloatField(
                        Text.translatable("config.oreminer.outlineThickness"),
                        Config.outlineThickness)
                .setDefaultValue(2.0f)
                .setMin(1.0f)
                .setMax(5.0f)
                .setTooltip(Text.translatable("config.oreminer.outlineThickness.tooltip"))
                .setSaveConsumer(value -> Config.outlineThickness = value)
                .build());

        return builder.build();
    }

    private static String getColorName(int index) {
        String[] colors = {"Cyan", "Red", "Yellow", "Green", "Purple", "White"};
        if (index >= 0 && index < colors.length) {
            return colors[index];
        }
        return "Cyan";
    }

    private static int getColorIndex(String name) {
        switch (name) {
            case "Red": return 1;
            case "Yellow": return 2;
            case "Green": return 3;
            case "Purple": return 4;
            case "White": return 5;
            default: return 0; // Cyan
        }
    }
}