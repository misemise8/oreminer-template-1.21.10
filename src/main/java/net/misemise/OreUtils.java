package net.misemise;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OreUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger("oreminer");
    private static final String MOD_ID = "oreminer";

    // カスタムタグ (data/oreminer/tags/blocks/ores.json)
    public static final TagKey<Block> OREMINER_ORES =
            TagKey.of(RegistryKeys.BLOCK, Identifier.of(MOD_ID, "ores"));

    /**
     * アイテムがつるはしかどうかを判定
     */
    public static boolean isPickaxe(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }

        // タグで判定
        try {
            if (stack.isIn(ItemTags.PICKAXES)) {
                return true;
            }
        } catch (Throwable e) {
            LOGGER.warn("Failed to check pickaxe tag", e);
        }

        // フォールバック：クラス名チェック
        try {
            String itemName = stack.getItem().toString().toLowerCase();
            String className = stack.getItem().getClass().getSimpleName().toLowerCase();
            return itemName.contains("pickaxe") || className.contains("pick");
        } catch (Throwable e) {
            LOGGER.warn("Failed to check pickaxe by name", e);
        }

        return false;
    }

    /**
     * ブロックが鉱石かどうかを判定
     */
    public static boolean isOre(BlockState state) {
        if (state == null || state.isAir()) {
            return false;
        }

        try {
            // カスタムタグで判定
            if (state.isIn(OREMINER_ORES)) {
                return true;
            }
        } catch (Throwable e) {
            LOGGER.warn("Failed to check ore tag for {}", state.getBlock(), e);
        }

        // フォールバック：ブロック名に"ore"が含まれるか
        try {
            String blockName = state.getBlock().toString().toLowerCase();
            return blockName.contains("ore") || blockName.contains("debris");
        } catch (Throwable e) {
            LOGGER.warn("Failed to check ore by name", e);
        }

        return false;
    }
}