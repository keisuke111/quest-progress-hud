package io.github.keisuke111.questprogresshud;

import dev.ftb.mods.ftbquests.client.FTBQuestsClient;
import dev.ftb.mods.ftbquests.quest.BaseQuestFile;
import dev.ftb.mods.ftbquests.quest.TeamData;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.common.NeoForge;

import java.util.concurrent.atomic.AtomicInteger;

@Mod(value = QuestProgressHud.MOD_ID, dist = Dist.CLIENT)
public final class QuestProgressHud {
    public static final String MOD_ID = "quest_progress_hud";

    private static final long UPDATE_INTERVAL_MS = 1000L;

    private long lastUpdateTime;
    private Component progressText = Component.translatable("hud.quest_progress_hud.loading");

    public QuestProgressHud() {
        NeoForge.EVENT_BUS.addListener(this::onRenderGui);
    }

    private void onRenderGui(RenderGuiEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();

        if (minecraft.player == null || minecraft.level == null || minecraft.options.hideGui) {
            return;
        }

        updateProgress();

        event.getGuiGraphics().drawString(
                minecraft.font,
                progressText,
                8,
                8,
                0xFFFFFF,
                true
        );
    }

    private void updateProgress() {
        long now = System.currentTimeMillis();
        if (now - lastUpdateTime < UPDATE_INTERVAL_MS) {
            return;
        }
        lastUpdateTime = now;

        if (!FTBQuestsClient.isClientDataLoaded()) {
            progressText = Component.translatable("hud.quest_progress_hud.loading");
            return;
        }

        BaseQuestFile questFile = FTBQuestsClient.getClientQuestFile();
        if (questFile == null) {
            progressText = Component.translatable("hud.quest_progress_hud.loading");
            return;
        }

        TeamData teamData = FTBQuestsClient.getClientPlayerData();
        AtomicInteger total = new AtomicInteger();
        AtomicInteger completed = new AtomicInteger();

        questFile.forAllQuests(quest -> {
            total.incrementAndGet();
            if (teamData.isCompleted(quest)) {
                completed.incrementAndGet();
            }
        });

        progressText = Component.translatable(
                "hud.quest_progress_hud.progress",
                completed.get(),
                total.get()
        );
    }
}
