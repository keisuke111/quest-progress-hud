package io.github.keisuke111.questprogresshud;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.common.NeoForge;

@Mod(value = QuestProgressHud.MOD_ID, dist = Dist.CLIENT)
public final class QuestProgressHud {
    public static final String MOD_ID = "quest_progress_hud";

    public QuestProgressHud() {
        NeoForge.EVENT_BUS.addListener(this::onRenderGui);
    }

    private void onRenderGui(RenderGuiEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();

        if (minecraft.player == null || minecraft.level == null || minecraft.options.hideGui) {
            return;
        }

        event.getGuiGraphics().drawString(
                minecraft.font,
                Component.translatable("hud.quest_progress_hud.test"),
                8,
                8,
                0xFFFFFF,
                true
        );
    }
}
