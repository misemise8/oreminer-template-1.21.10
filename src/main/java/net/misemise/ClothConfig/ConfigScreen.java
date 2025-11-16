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
                });

        ConfigEntryBuilder entryBuilder = builder.entryBuilder();

        // ==================== 採掘設定カテゴリ ====================
        ConfigCategory mining = builder.getOrCreateCategory(
                Text.translatable("config.oreminer.category.mining"));

        // 最大ブロック数
        mining.addEntry(entryBuilder.startIntSlider(
                        Text.translatable("config.oreminer.maxBlocks"),
                        Config.maxBlocks,
                        1, 256)
                .setDefaultValue(64)
                .setTooltip(Text.translatable("config.oreminer.maxBlocks.tooltip"))
                .setSaveConsumer(value -> Config.maxBlocks = value)
                .build());

        // 斜め探索
        mining.addEntry(entryBuilder.startBooleanToggle(
                        Text.translatable("config.oreminer.searchDiagonal"),
                        Config.searchDiagonal)
                .setDefaultValue(true)
                .setTooltip(Text.translatable("config.oreminer.searchDiagonal.tooltip"))
                .setSaveConsumer(value -> Config.searchDiagonal = value)
                .build());

        // 自動回収
        mining.addEntry(entryBuilder.startBooleanToggle(
                        Text.translatable("config.oreminer.autoCollect"),
                        Config.autoCollect)
                .setDefaultValue(true)
                .setTooltip(Text.translatable("config.oreminer.autoCollect.tooltip"))
                .setSaveConsumer(value -> Config.autoCollect = value)
                .build());

        // 経験値自動回収
        mining.addEntry(entryBuilder.startBooleanToggle(
                        Text.translatable("config.oreminer.autoCollectExp"),
                        Config.autoCollectExp)
                .setDefaultValue(true)
                .setTooltip(Text.translatable("config.oreminer.autoCollectExp.tooltip"))
                .setSaveConsumer(value -> Config.autoCollectExp = value)
                .build());

        // ==================== アウトライン設定カテゴリ ====================
        ConfigCategory outline = builder.getOrCreateCategory(
                Text.translatable("config.oreminer.category.outline"));

        // アウトラインの色
        outline.addEntry(entryBuilder.startSelector(
                        Text.translatable("config.oreminer.outlineColor"),
                        new String[]{"Cyan", "Red", "Yellow", "Green", "Purple", "White"},
                        getColorName(Config.outlineColor))
                .setDefaultValue("Cyan")
                .setTooltip(Text.translatable("config.oreminer.outlineColor.tooltip"))
                .setSaveConsumer(value -> {
                    Config.outlineColor = getColorIndex(value);
                })
                .build());

        // アウトラインの太さ（スライダーに変更）
        outline.addEntry(entryBuilder.startIntSlider(
                        Text.translatable("config.oreminer.outlineThickness"),
                        (int)(Config.outlineThickness * 10), // 内部的に10倍して整数化
                        10, 50) // 1.0 ~ 5.0 を 10 ~ 50 として扱う
                .setDefaultValue(20) // デフォルト 2.0
                .setTooltip(Text.translatable("config.oreminer.outlineThickness.tooltip"))
                .setSaveConsumer(value -> Config.outlineThickness = value / 10.0f)
                .setTextGetter(value -> Text.literal(String.format("%.1f", value / 10.0f)))
                .build());

        // ==================== その他設定カテゴリ ====================
        ConfigCategory other = builder.getOrCreateCategory(
                Text.translatable("config.oreminer.category.other"));

        // 破壊後のブロック数表示
        other.addEntry(entryBuilder.startBooleanToggle(
                        Text.translatable("config.oreminer.showBlocksMinedCount"),
                        Config.showBlocksMinedCount)
                .setDefaultValue(true)
                .setTooltip(Text.translatable("config.oreminer.showBlocksMinedCount.tooltip"))
                .setSaveConsumer(value -> Config.showBlocksMinedCount = value)
                .build());

        // 破壊前のブロック数プレビュー
        other.addEntry(entryBuilder.startBooleanToggle(
                        Text.translatable("config.oreminer.showBlocksPreview"),
                        Config.showBlocksPreview)
                .setDefaultValue(true)
                .setTooltip(Text.translatable("config.oreminer.showBlocksPreview.tooltip"))
                .setSaveConsumer(value -> Config.showBlocksPreview = value)
                .build());

        // デバッグログ
        other.addEntry(entryBuilder.startBooleanToggle(
                        Text.translatable("config.oreminer.debugLog"),
                        Config.debugLog)
                .setDefaultValue(false)
                .setTooltip(Text.translatable("config.oreminer.debugLog.tooltip"))
                .setSaveConsumer(value -> Config.debugLog = value)
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