package net.misemise.client;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

/**
 * 一括破壊した鉱石の数をHUDに表示
 */
public class VeinMiningHud {
    private static int blocksMinedCount = 0;
    private static long lastMineTime = 0;
    private static final long DISPLAY_DURATION = 3000; // 3秒間表示

    public static void register() {
        HudRenderCallback.EVENT.register((drawContext, tickDelta) -> {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player == null) return;

            // 表示時間が経過したら非表示
            if (System.currentTimeMillis() - lastMineTime > DISPLAY_DURATION) {
                blocksMinedCount = 0;
                return;
            }

            // ブロック数が0なら表示しない
            if (blocksMinedCount <= 0) return;

            // 画面サイズを取得
            int screenWidth = client.getWindow().getScaledWidth();
            int screenHeight = client.getWindow().getScaledHeight();

            // クロスヘアの右下に表示（中心からオフセット）
            int x = (screenWidth / 2) + 20;  // 右に20ピクセル
            int y = (screenHeight / 2) + 10; // 下に10ピクセル

            // テキストを作成
            Text text = Text.literal(blocksMinedCount + " ores mined")
                    .formatted(Formatting.GOLD, Formatting.BOLD);

            // 影付きでテキストを描画
            drawContext.drawText(
                    client.textRenderer,
                    text,
                    x,
                    y,
                    0xFFFFFF, // 白色
                    true // 影を有効
            );

            // フェードアウト効果（残り1秒でフェード開始）
            long timeLeft = DISPLAY_DURATION - (System.currentTimeMillis() - lastMineTime);
            if (timeLeft < 1000) {
                // 透明度を徐々に下げる
                float alpha = timeLeft / 1000.0f;
                // 次回の描画で透明度が反映される
            }
        });
    }

    /**
     * 破壊したブロック数を設定
     */
    public static void setBlocksMinedCount(int count) {
        blocksMinedCount = count;
        lastMineTime = System.currentTimeMillis();
    }

    /**
     * 破壊したブロック数を追加
     */
    public static void addBlocksMined(int count) {
        blocksMinedCount += count;
        lastMineTime = System.currentTimeMillis();
    }
}