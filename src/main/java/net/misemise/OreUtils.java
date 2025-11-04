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

    int MAX_BULK = 1024;

    private static final String MOD_ID = "oreminer";

    // カスタムタグ (data/oreminer/tags/blocks/ores.json を用意する想定)
    public static final TagKey<Block> OREMINER_ORES =
            TagKey.of(RegistryKeys.BLOCK, Identifier.of(MOD_ID, "ores"));

    public static boolean isPickaxe(ItemStack stack){

        if(stack == null || stack.isEmpty()) return false;

        //タグで判定する
        try{
            if(stack.isIn(ItemTags.PICKAXES)) return true;
        }catch (Throwable ignored){
            //見送る
        }
        // フォールバック：クラス名に "pick" が含まれるかどうか
        try {
            String cls = stack.getItem().getClass().getSimpleName().toLowerCase();
            return cls.contains("pick");
        } catch (Throwable ignored) {}

        return false;
    }


    //鉱石かどうかを判定
    public static boolean isOre(BlockState blockState) {
        if(blockState == null) return false;
        try {
            return blockState.isIn(OREMINER_ORES);
        }catch(Throwable e){
            //見送る
        }
        return false;
    }
}
