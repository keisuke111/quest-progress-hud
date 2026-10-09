package io.github.keisuke111.questprogresshud;

import dev.ftb.mods.ftbquests.client.FTBQuestsClient;
import dev.ftb.mods.ftbquests.quest.BaseQuestFile;
import dev.ftb.mods.ftbquests.quest.Chapter;
import dev.ftb.mods.ftbquests.quest.TeamData;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.common.NeoForge;
import org.lwjgl.glfw.GLFW;

import java.util.Locale;
import java.util.UUID;

@Mod(value = QuestProgressHud.MOD_ID, dist = Dist.CLIENT)
public final class QuestProgressHud {
    public static final String MOD_ID = "quest_progress_hud";

    private static final UUID UNSYNCED_TEAM = new UUID(0L, 0L);
    private final ProgressTracker tracker = new ProgressTracker();
    private final HudToggleInput toggleInput = new HudToggleInput();
    private KeyMapping toggleKey;
    private ProgressTracker.Snapshot displayedSnapshot;
    private Component titleText = Component.translatable("hud.quest_progress_hud.title");
    private String selectedScope = "";
    private boolean chapterUnavailable;
    private Component countText = Component.translatable("hud.quest_progress_hud.loading");
    private Component percentageText = Component.empty();
    private double progressFraction;
    private boolean progressLoaded;

    public QuestProgressHud(ModContainer container, IEventBus modBus) {
        container.registerConfig(ModConfig.Type.CLIENT, HudConfig.SPEC);
        container.registerExtensionPoint(IConfigScreenFactory.class,
                (mod, parent) -> new HudSettingsScreen(mod, parent, this));
        modBus.addListener(this::registerKeys);
        NeoForge.EVENT_BUS.addListener(this::onRenderGui);
        NeoForge.EVENT_BUS.addListener(this::onClientTick);
        NeoForge.EVENT_BUS.addListener(this::onLogout);
    }

    private void registerKeys(RegisterKeyMappingsEvent event) {
        toggleKey = new KeyMapping("key.quest_progress_hud.toggle", KeyConflictContext.IN_GAME,
                InputConstants.Type.KEYSYM, InputConstants.UNKNOWN.getValue(), "key.categories.quest_progress_hud");
        event.register(toggleKey);
    }

    private void handleToggle(Minecraft minecraft) {
        if (toggleKey == null) return;
        boolean clicked = false;
        while (toggleKey.consumeClick()) clicked = true;
        boolean allowed = minecraft.player != null && minecraft.level != null
                && minecraft.getConnection() != null && minecraft.screen == null && minecraft.isWindowActive();
        if (toggleInput.update(togglePhysicallyDown(minecraft), clicked, allowed)) {
            HudConfig.ENABLED.set(!HudConfig.ENABLED.get());
            HudConfig.SPEC.save();
        }
    }

    private boolean togglePhysicallyDown(Minecraft minecraft) {
        // IN_GAME makes KeyMapping.isDown() false in menus even if the physical key is held.
        // Poll independently so closing a menu cannot turn that held input into a fresh press.
        InputConstants.Key key = toggleKey.getKey();
        if (key.getValue() < 0) return false;
        if (key.getType() == InputConstants.Type.KEYSYM) {
            return InputConstants.isKeyDown(minecraft.getWindow().getWindow(), key.getValue());
        }
        if (key.getType() == InputConstants.Type.MOUSE) {
            return GLFW.glfwGetMouseButton(minecraft.getWindow().getWindow(), key.getValue()) == GLFW.GLFW_PRESS;
        }
        return toggleKey.isDown();
    }

    private void onRenderGui(RenderGuiEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();

        if (minecraft.player == null || minecraft.level == null || minecraft.options.hideGui || !HudConfig.ENABLED.get()
                || minecraft.screen instanceof HudEditorScreen) {
            return;
        }

        GuiGraphics graphics = event.getGuiGraphics();
        HudLayout.Row row = hudRow();
        int width = row.width();
        int height = row.height();

        HudConfig.Anchor anchor = HudConfig.ANCHOR.get();
        HudLayout.Placement placement = HudLayout.place(
                graphics.guiWidth(), graphics.guiHeight(), width, height,
                HudConfig.SCALE.get(), anchor.right, anchor.bottom,
                HudConfig.OFFSET_X.get(), HudConfig.OFFSET_Y.get());

        renderHud(graphics, placement, HudConfig.BACKGROUND_OPACITY.get());
    }

    HudLayout.Row hudRow() {
        Minecraft minecraft = Minecraft.getInstance();
        return HudLayout.compactRow(minecraft.font.width(titleText), minecraft.font.width(countText),
                minecraft.font.width(percentageText), minecraft.font.lineHeight, progressLoaded);
    }

    boolean canReadQuestFile(BaseQuestFile file) {
        return tracker.acceptsFile(file);
    }

    void renderHud(GuiGraphics graphics, HudLayout.Placement placement, int opacity) {
        Minecraft minecraft = Minecraft.getInstance();
        int x = 0;
        int y = 0;
        int padding = HudLayout.PADDING;
        HudLayout.Row row = hudRow();
        int width = row.width();
        int contentWidth = width - padding * 2;
        int height = row.height();

        graphics.pose().pushPose();
        try {
            graphics.pose().translate(placement.x(), placement.y(), 0);
            graphics.pose().scale((float) placement.scale(), (float) placement.scale(), 1);
            drawPanel(graphics, x, y, width, height, opacity);
            graphics.drawString(minecraft.font, titleText, x + padding, y + padding, 0xFFFFFF, true);
            graphics.drawString(minecraft.font, countText, x + row.countX(), y + padding, 0xFFFFFF, true);

            if (progressLoaded) {
                graphics.drawString(minecraft.font, percentageText,
                        x + row.percentageX(),
                        y + padding, 0x8BE18B, true);
                int filledWidth = (int) Math.round((contentWidth - 2) * progressFraction);
                drawProgressBar(graphics, x + padding, y + row.barY(), contentWidth, filledWidth);
            }
        } finally {
            graphics.pose().popPose();
        }
    }

    private static void drawPanel(GuiGraphics graphics, int x, int y, int width, int height, int opacity) {
        graphics.fill(x, y, x + width, y + height, HudLayout.backgroundColor(opacity));
        // Keep the bevel inside the panel bounds and honor the background opacity setting.
        int alpha = (int) Math.round(255.0 * opacity / 100.0) << 24;
        graphics.fill(x, y, x + width - 1, y + 1, alpha | 0x3B4147);
        graphics.fill(x, y + 1, x + 1, y + height - 1, alpha | 0x3B4147);
        graphics.fill(x + 1, y + height - 1, x + width, y + height, alpha | 0x0B0E11);
        graphics.fill(x + width - 1, y, x + width, y + height - 1, alpha | 0x0B0E11);
    }

    private static void drawProgressBar(GuiGraphics graphics, int x, int y, int width, int filledWidth) {
        graphics.fill(x, y, x + width, y + HudLayout.BAR_HEIGHT, 0xFF10170D);
        graphics.fill(x + 1, y + 1, x + width - 1, y + 3, 0xFF293820);
        if (filledWidth > 0) {
            graphics.fill(x + 1, y + 1, x + 1 + filledWidth, y + 2, 0xFF8BE18B);
            graphics.fill(x + 1, y + 2, x + 1 + filledWidth, y + 3, 0xFF65B46A);
        }
    }

    private void onLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        tracker.disconnect(FTBQuestsClient.getClientQuestFile());
        showLoading();
    }

    private void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        handleToggle(minecraft);
        BaseQuestFile file = FTBQuestsClient.getClientQuestFile();
        if (minecraft.player == null || minecraft.level == null || minecraft.getConnection() == null) {
            tracker.disconnect(file);
            showLoading();
            return;
        }
        if (!HudConfig.ENABLED.get() || minecraft.options.hideGui) {
            tracker.clear();
            showLoading();
            return;
        }
        boolean fileReady = file != null && FTBQuestsClient.isClientDataLoaded();
        TeamData team = fileReady ? FTBQuestsClient.getClientPlayerData() : null;
        boolean ready = team != null && !UNSYNCED_TEAM.equals(team.getTeamId()) && canReadQuestFile(file);
        String scope = QuestScope.normalize(HudConfig.CHAPTER_ID.get());
        long chapterId = QuestScope.id(scope);
        if (!scope.equals(selectedScope)) {
            selectedScope = scope;
            tracker.clear();
            showLoading();
        }
        Chapter chapter = ready && !scope.isEmpty() ? file.getChapter(chapterId) : null;
        if (!ready) {
            titleText = Component.translatable(scope.isEmpty() ? "hud.quest_progress_hud.title" : "hud.quest_progress_hud.chapter");
        } else if (!scope.isEmpty() && chapter == null) {
            tracker.clear();
            titleText = Component.translatable("hud.quest_progress_hud.chapter");
            if (!chapterUnavailable) {
                showLoading();
                countText = Component.translatable("hud.quest_progress_hud.chapter_unavailable");
            }
            chapterUnavailable = true;
            return;
        }
        ProgressTracker.Snapshot snapshot = tracker.update(System.nanoTime(),
                minecraft.getConnection(), minecraft.level, file, team, ready, () -> {
                    if (chapter == null) {
                        titleText = Component.translatable("hud.quest_progress_hud.title");
                    } else {
                        String fullTitle = chapter.getTitle().getString();
                        String shortTitle = minecraft.font.plainSubstrByWidth(fullTitle, 110);
                        titleText = Component.literal(shortTitle.equals(fullTitle) ? shortTitle : shortTitle + "...");
                    }
                    ProgressTracker.Counter counter = new ProgressTracker.Counter();
                    // Use identical scope membership for numerator and denominator; never filter visibility or repeatability.
                    file.forAllQuests(quest -> {
                        if (QuestScope.includes(chapterId, quest.getQuestChapter().getId())) {
                            counter.accept(team.isCompleted(quest));
                        }
                    });
                    return counter.snapshot();
                });
        if (snapshot == null) {
            titleText = Component.translatable(scope.isEmpty() ? "hud.quest_progress_hud.title" : "hud.quest_progress_hud.chapter");
            showLoading();
        } else if (!snapshot.equals(displayedSnapshot)) {
            displayedSnapshot = snapshot;
            countText = Component.translatable("hud.quest_progress_hud.counts", snapshot.completed(),
                    Component.literal(Integer.toString(snapshot.total())).withStyle(ChatFormatting.GRAY));
            percentageText = Component.literal(String.format(Locale.ROOT, "%.1f%%", snapshot.fraction() * 100.0));
            progressFraction = snapshot.fraction();
            progressLoaded = true;
            chapterUnavailable = false;
        }
    }

    private void showLoading() {
        titleText = Component.translatable(selectedScope.isEmpty() ? "hud.quest_progress_hud.title" : "hud.quest_progress_hud.chapter");
        if (!progressLoaded && displayedSnapshot == null && !chapterUnavailable) return;
        chapterUnavailable = false;
        displayedSnapshot = null;
        countText = Component.translatable("hud.quest_progress_hud.loading");
        percentageText = Component.empty();
        progressFraction = 0.0;
        progressLoaded = false;
    }

}
