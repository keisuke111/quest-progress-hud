package io.github.keisuke111.questprogresshud;

import dev.ftb.mods.ftbquests.client.FTBQuestsClient;
import dev.ftb.mods.ftbquests.quest.BaseQuestFile;
import dev.ftb.mods.ftbquests.quest.TeamData;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.common.NeoForge;

import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

@Mod(value = QuestProgressHud.MOD_ID, dist = Dist.CLIENT)
public final class QuestProgressHud {
    public static final String MOD_ID = "quest_progress_hud";

    private static final long UPDATE_INTERVAL_MS = 1000L;

    private long lastUpdateTime;
    private final Component titleText = Component.translatable("hud.quest_progress_hud.title");
    private Component countText = Component.translatable("hud.quest_progress_hud.loading");
    private Component percentageText = Component.empty();
    private double progressFraction;
    private boolean progressLoaded;

    public QuestProgressHud() {
        NeoForge.EVENT_BUS.addListener(this::onRenderGui);
    }

    private void onRenderGui(RenderGuiEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();

        if (minecraft.player == null || minecraft.level == null || minecraft.options.hideGui) {
            return;
        }

        updateProgress();

        GuiGraphics graphics = event.getGuiGraphics();
        int x = 8;
        int y = 8;
        int padding = 6;
        int lineHeight = minecraft.font.lineHeight;
        int headerWidth = minecraft.font.width(titleText) + minecraft.font.width(percentageText) + 12;
        int width = Math.max(128, Math.max(headerWidth, minecraft.font.width(countText)) + padding * 2);
        int contentWidth = width - padding * 2;
        int countY = y + padding + lineHeight + 4;
        int barY = countY + lineHeight + 5;
        int height = progressLoaded ? barY - y + 3 + padding : countY - y + lineHeight + padding;

        graphics.fill(x, y, x + width, y + height, 0xB814171A);
        graphics.drawString(minecraft.font, titleText, x + padding, y + padding, 0xFFFFFF, true);
        graphics.drawString(minecraft.font, countText, x + padding, countY, 0xFFFFFF, true);

        if (progressLoaded) {
            graphics.drawString(minecraft.font, percentageText,
                    x + width - padding - minecraft.font.width(percentageText),
                    y + padding, 0x8BE18B, true);
            graphics.fill(x + padding, barY, x + width - padding, barY + 3, 0xFF48504B);
            int filledWidth = (int) Math.round(contentWidth * progressFraction);
            if (filledWidth > 0) {
                graphics.fill(x + padding, barY, x + padding + filledWidth, barY + 3, 0xFF8BE18B);
            }
        }
    }

    private void showLoading() {
        countText = Component.translatable("hud.quest_progress_hud.loading");
        percentageText = Component.empty();
        progressFraction = 0.0;
        progressLoaded = false;
    }

    private void updateProgress() {
        long now = System.currentTimeMillis();
        if (now - lastUpdateTime < UPDATE_INTERVAL_MS) {
            return;
        }
        lastUpdateTime = now;

        if (!FTBQuestsClient.isClientDataLoaded()) {
            showLoading();
            return;
        }

        BaseQuestFile questFile = FTBQuestsClient.getClientQuestFile();
        if (questFile == null) {
            showLoading();
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

        int completedCount = completed.get();
        int totalCount = total.get();
        double percentage = totalCount == 0 ? 0.0 : 100.0 * completedCount / totalCount;

        countText = Component.translatable(
                "hud.quest_progress_hud.counts",
                completedCount,
                Component.literal(Integer.toString(totalCount)).withStyle(ChatFormatting.GRAY)
        );
        percentageText = Component.literal(String.format(Locale.ROOT, "%.1f%%", percentage));
        progressFraction = Math.max(0.0, Math.min(1.0, percentage / 100.0));
        progressLoaded = true;
    }
}
