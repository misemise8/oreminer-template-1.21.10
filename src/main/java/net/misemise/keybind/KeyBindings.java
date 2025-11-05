package net.misemise.keybind;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.misemise.OreMiner;
import org.lwjgl.glfw.GLFW;

/**
 * キーバインド管理クラス
 */
public class KeyBindings {
    public static KeyBinding veinMinerKey;

    public static void register() {
        // CATEGORY_GAMEPLAY を使用する
        veinMinerKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.oreminer.veinminer",  // 表示されるキー名
                InputUtil.Type.KEYSYM,     // キータイプ（KEYSYMは通常のキー）
                GLFW.GLFW_KEY_V,          // 使用するキー（Vキー）
                KeyBinding.Category.GAMEPLAY // CATEGORY_GAMEPLAY を使用
        ));

        OreMiner.LOGGER.info("KeyBindings registered");
    }

    /**
     * 一括破壊キーが押されているかチェック
     */
    public static boolean isVeinMinerKeyPressed() {
        return veinMinerKey != null && veinMinerKey.isPressed();
    }
}
