package net.misemise.keybind;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.misemise.OreMiner;
import org.lwjgl.glfw.GLFW;

public class KeyBindings {
    public static KeyBinding veinMinerKey;

    public static void register() {
        veinMinerKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.oreminer.veinminer",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_V,
                KeyBinding.Category.GAMEPLAY
        ));

        OreMiner.LOGGER.info("KeyBindings registered");
    }

    public static boolean isVeinMinerKeyPressed() {
        return veinMinerKey != null && veinMinerKey.isPressed();
    }
}