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
        int padding = 5;
        int lineHeight = minecraft.font.lineHeight;
        int headerWidth = minecraft.font.width(titleText) + minecraft.font.width(percentageText) + 12;
        int width = Math.max(116, Math.max(headerWidth, minecraft.font.width(countText)) + padding * 2);
        int contentWidth = width - padding * 2;
        int countY = y + padding + lineHeight + 2;
        int barY = countY + lineHeight + 4;
        int height = progressLoaded ? barY - y + 5 + padding : countY - y + lineHeight + padding;

        HudConfig.Anchor anchor = HudConfig.ANCHOR.get();
        HudLayout.Placement placement = HudLayout.place(
                graphics.guiWidth(), graphics.guiHeight(), width, height,
                HudConfig.SCALE.get(), anchor.right, anchor.bottom,
                HudConfig.OFFSET_X.get(), HudConfig.OFFSET_Y.get());

        graphics.pose().pushPose();
        try {
            graphics.pose().translate(placement.x(), placement.y(), 0);
            graphics.pose().scale((float) placement.scale(), (float) placement.scale(), 1);
            drawPanel(graphics, x, y, width, height, HudConfig.BACKGROUND_OPACITY.get());
            graphics.drawString(minecraft.font, titleText, x + padding, y + padding, 0xFFFFFF, true);
            graphics.drawString(minecraft.font, countText, x + padding, countY, 0xFFFFFF, true);

            if (progressLoaded) {
                graphics.drawString(minecraft.font, percentageText,
                        x + width - padding - minecraft.font.width(percentageText),
                        y + padding, 0x8BE18B, true);
                int filledWidth = (int) Math.round((contentWidth - 2) * progressFraction);
                drawExperienceBar(graphics, x + padding, barY, contentWidth, filledWidth);
            }
        } finally {
            graphics.pose().popPose();
        }
    }

    private static void drawPanel(GuiGraphics graphics, int x, int y, int width, int height, int opacity) {
        graphics.fill(x, y, x + width, y + height, HudLayout.backgroundColor(opacity));
        // Keep the bevel inside the panel bounds and honor the background opacity setting.
        int alpha = (int) Math.round(255.0 * opacity / 100.0) << 24;
        graphics.fill(x, y, x + width - 1, y + 1, alpha | 0x53636D);
        graphics.fill(x, y + 1, x + 1, y + height - 1, alpha | 0x53636D);
        graphics.fill(x + 1, y + height - 1, x + width, y + height, alpha | 0x0B1014);
        graphics.fill(x + width - 1, y, x + width, y + height - 1, alpha | 0x0B1014);
    }

    private static void drawExperienceBar(GuiGraphics graphics, int x, int y, int width, int filledWidth) {
        int innerWidth = width - 2;
        graphics.fill(x, y, x + width, y + 5, 0xFF10170D);
        graphics.fill(x + 1, y + 1, x + width - 1, y + 2, 0xFF293820);
        graphics.fill(x + 1, y + 2, x + width - 1, y + 4, 0xFF354B29);
        if (filledWidth > 0) {
            graphics.fill(x + 1, y + 1, x + 1 + filledWidth, y + 2, 0xFFB3EC69);
            graphics.fill(x + 1, y + 2, x + 1 + filledWidth, y + 3, 0xFF80CE42);
            graphics.fill(x + 1, y + 3, x + 1 + filledWidth, y + 4, 0xFF4F9229);
        }
        // Small notches give the continuous fill an experience-gauge appearance.
        for (int segment = 1; segment < 18; segment++) {
            int notchX = x + 1 + innerWidth * segment / 18;
            graphics.fill(notchX, y + 1, notchX + 1, y + 4, 0x500B1407);
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
