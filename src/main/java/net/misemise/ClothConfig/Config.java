package net.misemise.ClothConfig;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.misemise.OreMiner;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * OreMiner設定クラス
 */
public class Config {
    // 一括破壊の最大ブロック数
    public static int maxBlocks = 64;

    // 斜め方向も探索するか（26方向 vs 6方向）
    public static boolean searchDiagonal = true;

    // 自動回収を有効にするか
    public static boolean autoCollect = true;

    // 経験値も自動回収するか
    public static boolean autoCollectExp = true;

    // デバッグログを出力するか
    public static boolean debugLog = false;

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File CONFIG_FILE = new File(
            FabricLoader.getInstance().getConfigDir().toFile(),
            "oreminer.json"
    );

    /**
     * 設定を読み込む
     */
    public static void load() {
        if (CONFIG_FILE.exists()) {
            try (FileReader reader = new FileReader(CONFIG_FILE)) {
                ConfigData data = GSON.fromJson(reader, ConfigData.class);
                if (data != null) {
                    maxBlocks = data.maxBlocks;
                    searchDiagonal = data.searchDiagonal;
                    autoCollect = data.autoCollect;
                    autoCollectExp = data.autoCollectExp;
                    debugLog = data.debugLog;
                }
                OreMiner.LOGGER.info("Config loaded from file");
            } catch (IOException e) {
                OreMiner.LOGGER.error("Failed to load config", e);
            }
        } else {
            save(); // デフォルト設定で保存
        }

        OreMiner.LOGGER.info("Config: maxBlocks={}, searchDiagonal={}, autoCollect={}, autoCollectExp={}, debugLog={}",
                maxBlocks, searchDiagonal, autoCollect, autoCollectExp, debugLog);
    }

    /**
     * 設定を保存する
     */
    public static void save() {
        try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
            ConfigData data = new ConfigData();
            data.maxBlocks = maxBlocks;
            data.searchDiagonal = searchDiagonal;
            data.autoCollect = autoCollect;
            data.autoCollectExp = autoCollectExp;
            data.debugLog = debugLog;

            GSON.toJson(data, writer);
            OreMiner.LOGGER.info("Config saved to file");
        } catch (IOException e) {
            OreMiner.LOGGER.error("Failed to save config", e);
        }
    }

    private static class ConfigData {
        int maxBlocks = 64;
        boolean searchDiagonal = true;
        boolean autoCollect = true;
        boolean autoCollectExp = true;
        boolean debugLog = false;
    }
}