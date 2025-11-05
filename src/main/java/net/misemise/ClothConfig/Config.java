package net.misemise.ClothConfig;

import net.misemise.OreMiner;

/**
 * OreMiner設定クラス
 * 将来的にCloth Configで設定画面を追加予定
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

    /**
     * 設定を読み込む（将来的にファイルから読み込み予定）
     */
    public static void load() {
        // TODO: Cloth Configで設定ファイルから読み込み
        OreMiner.LOGGER.info("Config loaded: maxBlocks={}, searchDiagonal={}, autoCollect={}",
                maxBlocks, searchDiagonal, autoCollect);
    }

    /**
     * 設定を保存する（将来的にファイルに保存予定）
     */
    public static void save() {
        // TODO: Cloth Configで設定ファイルに保存
    }
}