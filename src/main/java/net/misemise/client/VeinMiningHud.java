package net.misemise.client;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.misemise.OreMiner;

/**
 * 一括破壊した鉱石の数をHUDに表示
 */
public class VeinMiningHud {
    private static int blocksMinedCount = 0;
    private static long lastMineTime = 0;
    private static final long DISPLAY_DURATION = 3000; // 3秒間表示

    public static void register() {
        OreMiner.LOGGER.info("Registering VeinMiningHud...");

        HudRenderCallback.EVENT.register((drawContext, tickDelta) -> {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player == null) return;

            long currentTime = System.currentTimeMillis();
            if (currentTime - lastMineTime > DISPLAY_DURATION) {
                if (blocksMinedCount > 0) {
                    OreMiner.LOGGER.info("HUD display timeout - resetting count from {}", blocksMinedCount);
                    blocksMinedCount = 0;
                }
                return;
            }

            if (blocksMinedCount <= 0) return;

            TextRenderer textRenderer = client.textRenderer;

            int screenWidth = client.getWindow().getScaledWidth();
            int screenHeight = client.getWindow().getScaledHeight();

            String messageStr = blocksMinedCount + " ores mined!";
            Text message = Text.literal(messageStr);
            int textWidth = textRenderer.getWidth(messageStr);

            int x = (screenWidth - textWidth) / 2 + 40;
            int y = screenHeight / 2 + 30;

            /*
            // 背景（DrawContext の fill を使う）
            int padding = 5;
            drawContext.fill(
                    x - padding,
                    y - padding,
                    x + textWidth + padding,
                    y + textRenderer.fontHeight + padding,
                    0xAA000000 // より濃い半透明の黒
            );

             */
            // テキストを描画（DrawContext 側のメソッドを利用）
            // drawTextWithShadow(TextRenderer, Text, x, y, color)
            drawContext.drawTextWithShadow(textRenderer, message, x, y, 0xFFFFAA00);

            if (currentTime - lastMineTime < 100) {
                OreMiner.LOGGER.info("Drawing HUD: count={}, x={}, y={}", blocksMinedCount, x, y);
            }
        });

        OreMiner.LOGGER.info("VeinMiningHud registered successfully");
    }

    public static void setBlocksMinedCount(int count) {
        OreMiner.LOGGER.info("VeinMiningHud.setBlocksMinedCount called with count: {}", count);
        blocksMinedCount = count;
        lastMineTime = System.currentTimeMillis();
    }

    public static void addBlocksMined(int count) {
        blocksMinedCount += count;
        lastMineTime = System.currentTimeMillis();
    }
}
