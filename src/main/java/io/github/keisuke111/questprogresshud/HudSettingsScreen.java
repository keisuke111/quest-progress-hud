package io.github.keisuke111.questprogresshud;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;

/** Entry point keeps NeoForge's existing numeric config editor available. */
final class HudSettingsScreen extends Screen {
    private final Screen parent;
    private final ModContainer container;
    private final QuestProgressHud hud;

    HudSettingsScreen(ModContainer container, Screen parent, QuestProgressHud hud) {
        super(Component.translatable("quest_progress_hud.configuration.title"));
        this.parent = parent;
        this.container = container;
        this.hud = hud;
    }

    @Override
    protected void init() {
        int buttonWidth = Math.min(260, width - 20);
        int x = (width - buttonWidth) / 2;
        int y = Math.max(40, height / 2 - 48);
        Button editor = addRenderableWidget(Button.builder(Component.translatable("quest_progress_hud.editor.open"),
                button -> minecraft.setScreen(new HudEditorScreen(this, hud))).bounds(x, y, buttonWidth, 20).build());
        editor.active = minecraft.level != null && minecraft.player != null;
        if (!editor.active) editor.setTooltip(Tooltip.create(Component.translatable("quest_progress_hud.editor.world_required")));
        addRenderableWidget(Button.builder(Component.translatable("quest_progress_hud.editor.numeric"),
                button -> minecraft.setScreen(new ConfigurationScreen(container, this)))
                .bounds(x, y + 26, buttonWidth, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("quest_progress_hud.chapter.open"),
                button -> minecraft.setScreen(new ChapterSettingsScreen(this)))
                .bounds(x, y + 52, buttonWidth, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("gui.done"), button -> onClose())
                .bounds(x, y + 86, buttonWidth, 20).build());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        graphics.drawCenteredString(font, title, width / 2, 20, 0xFFFFFF);
    }

    @Override
    public void onClose() { minecraft.setScreen(parent); }
}
