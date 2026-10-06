package io.github.keisuke111.questprogresshud;

import dev.ftb.mods.ftbquests.client.FTBQuestsClient;
import dev.ftb.mods.ftbquests.quest.BaseQuestFile;
import dev.ftb.mods.ftbquests.quest.TeamData;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
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

    public QuestProgressHud(ModContainer container) {
        container.registerConfig(ModConfig.Type.CLIENT, HudConfig.SPEC);
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
        NeoForge.EVENT_BUS.addListener(this::onRenderGui);
    }

    private void onRenderGui(RenderGuiEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();

        if (minecraft.player == null || minecraft.level == null || minecraft.options.hideGui || !HudConfig.ENABLED.get()) {
            return;
        }

        updateProgress();

        GuiGraphics graphics = event.getGuiGraphics();
        int x = 0;
        int y = 0;
        int padding = 6;
        int lineHeight = minecraft.font.lineHeight;
        int headerWidth = minecraft.font.width(titleText) + minecraft.font.width(percentageText) + 12;
        int width = Math.max(128, Math.max(headerWidth, minecraft.font.width(countText)) + padding * 2);
        int contentWidth = width - padding * 2;
        int countY = y + padding + lineHeight + 4;
        int barY = countY + lineHeight + 5;
        int height = progressLoaded ? barY - y + 3 + padding : countY - y + lineHeight + padding;

        HudConfig.Anchor anchor = HudConfig.ANCHOR.get();
        HudLayout.Placement placement = HudLayout.place(
                graphics.guiWidth(), graphics.guiHeight(), width, height,
                HudConfig.SCALE.get(), anchor.right, anchor.bottom,
                HudConfig.OFFSET_X.get(), HudConfig.OFFSET_Y.get());

        graphics.pose().pushPose();
        try {
            graphics.pose().translate(placement.x(), placement.y(), 0);
            graphics.pose().scale((float) placement.scale(), (float) placement.scale(), 1);
            graphics.fill(x, y, x + width, y + height, HudLayout.backgroundColor(HudConfig.BACKGROUND_OPACITY.get()));
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
        } finally {
            graphics.pose().popPose();
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
