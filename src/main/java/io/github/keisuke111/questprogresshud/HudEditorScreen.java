package io.github.keisuke111.questprogresshud;

import java.util.Locale;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** Preview the real renderer over the world; only Save touches the config. */
final class HudEditorScreen extends Screen {
    private final Screen parent;
    private final QuestProgressHud hud;
    private final HudEditorState state;
    private Button scaleDown;
    private Button scaleUp;
    private Button opacityDown;
    private Button opacityUp;
    private Button reset;
    private Button cancel;
    private Button save;
    private int controlsY;

    HudEditorScreen(Screen parent, QuestProgressHud hud) {
        super(Component.translatable("quest_progress_hud.editor.title"));
        this.parent = parent;
        this.hud = hud;
        HudConfig.Anchor anchor = HudConfig.ANCHOR.get();
        state = new HudEditorState(new HudEditorState.Settings(anchor.right, anchor.bottom,
                HudConfig.OFFSET_X.get(), HudConfig.OFFSET_Y.get(), HudConfig.SCALE.get(), HudConfig.BACKGROUND_OPACITY.get()));
    }

    @Override
    protected void init() {
        state.endDrag();
        scaleDown = addRenderableWidget(Button.builder(Component.literal("-"), b -> state.setScale(state.draft().scale() - 0.05)).bounds(0, 0, 20, 20).build());
        scaleUp = addRenderableWidget(Button.builder(Component.literal("+"), b -> state.setScale(state.draft().scale() + 0.05)).bounds(0, 0, 20, 20).build());
        opacityDown = addRenderableWidget(Button.builder(Component.literal("-"), b -> state.setOpacity(state.draft().opacity() - 5)).bounds(0, 0, 20, 20).build());
        opacityUp = addRenderableWidget(Button.builder(Component.literal("+"), b -> state.setOpacity(state.draft().opacity() + 5)).bounds(0, 0, 20, 20).build());
        reset = addRenderableWidget(Button.builder(Component.translatable("quest_progress_hud.editor.reset"), b -> state.reset()).bounds(0, 0, 80, 20).build());
        cancel = addRenderableWidget(Button.builder(Component.translatable("gui.cancel"), b -> onClose()).bounds(0, 0, 80, 20).build());
        save = addRenderableWidget(Button.builder(Component.translatable("quest_progress_hud.editor.save"), b -> save()).bounds(0, 0, 80, 20).build());
        positionControls();
    }

    // Dock opposite the HUD so bottom-corner layouts can still be grabbed.
    private void positionControls() {
        HudLayout.Placement p = state.placement(width, height, hud.hudRow());
        controlsY = p.y() + hud.hudRow().height() * p.scale() / 2 > height / 2.0 ? 8 : height - 106;
        controlsY = Math.max(0, controlsY);
        int x = Math.max(0, (width - 260) / 2);
        scaleDown.setPosition(x + 2, controlsY + 30);
        scaleUp.setPosition(x + 110, controlsY + 30);
        opacityDown.setPosition(x + 136, controlsY + 30);
        opacityUp.setPosition(x + 240, controlsY + 30);
        reset.setPosition(x + 2, controlsY + 80);
        cancel.setPosition(x + 90, controlsY + 80);
        save.setPosition(x + 178, controlsY + 80);
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // Screen.render calls this automatically. Keep the game view visible without blur or menu texture.
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        positionControls();
        HudLayout.Row row = hud.hudRow();
        HudLayout.Placement p = state.placement(width, height, row);
        hud.renderHud(graphics, p, state.draft().opacity());
        int left = (int) Math.floor(p.x());
        int top = (int) Math.floor(p.y());
        int right = (int) Math.ceil(p.x() + row.width() * p.scale());
        int bottom = (int) Math.ceil(p.y() + row.height() * p.scale());
        graphics.renderOutline(left, top, right - left, bottom - top, state.dragging() ? 0xFF8BE18B : 0xFFFFFFFF);
        int x = Math.max(0, (width - 260) / 2);
        graphics.fill(x, controlsY, Math.min(width, x + 262), Math.min(height, controlsY + 104), 0xD014171A);
        graphics.drawCenteredString(font, title, x + 131, controlsY + 5, 0xFFFFFF);
        graphics.drawCenteredString(font, Component.translatable("quest_progress_hud.editor.scale", String.format(Locale.ROOT, "%.2f", state.draft().scale())), x + 66, controlsY + 18, 0xFFFFFF);
        graphics.drawCenteredString(font, Component.translatable("quest_progress_hud.editor.opacity", state.draft().opacity()), x + 198, controlsY + 18, 0xFFFFFF);
        graphics.drawCenteredString(font, Component.translatable("quest_progress_hud.editor.drag"), x + 131, controlsY + 58, 0xDDDDDD);
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) return true;
        if (button == 0 && state.beginDrag(mouseX, mouseY, width, height, hud.hudRow())) {
            setFocused(null);
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (button == 0 && state.dragging()) {
            state.dragTo(mouseX, mouseY, width, height, hud.hudRow());
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0 && state.dragging()) {
            state.endDrag();
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    private void save() {
        HudEditorState.Settings draft = state.draft();
        for (HudConfig.Anchor anchor : HudConfig.Anchor.values()) {
            if (anchor.right == draft.right() && anchor.bottom == draft.bottom()) HudConfig.ANCHOR.set(anchor);
        }
        HudConfig.OFFSET_X.set(draft.offsetX());
        HudConfig.OFFSET_Y.set(draft.offsetY());
        HudConfig.SCALE.set(draft.scale());
        HudConfig.BACKGROUND_OPACITY.set(draft.opacity());
        HudConfig.SPEC.save();
        onClose();
    }

    @Override
    public void onClose() { state.endDrag(); minecraft.setScreen(parent); }
}
