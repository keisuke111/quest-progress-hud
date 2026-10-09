package io.github.keisuke111.questprogresshud;

import dev.ftb.mods.ftbquests.client.FTBQuestsClient;
import dev.ftb.mods.ftbquests.quest.BaseQuestFile;
import dev.ftb.mods.ftbquests.quest.TeamData;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** A paged title picker. Draft selection never writes the config before Save. */
final class ChapterSettingsScreen extends Screen {
    private record Choice(String id, String title, String group) {}
    private static final UUID UNSYNCED_TEAM = new UUID(0, 0);
    private final Screen parent;
    private String selected = QuestScope.normalize(HudConfig.CHAPTER_ID.get());
    private List<Choice> choices = List.of();
    private boolean ready;
    private int page;
    private int rows;
    private int ticks;

    ChapterSettingsScreen(Screen parent) {
        super(Component.translatable("quest_progress_hud.chapter.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        refreshChoices();
        rebuild();
    }

    private boolean refreshChoices() {
        BaseQuestFile file = FTBQuestsClient.getClientQuestFile();
        TeamData team = file != null && FTBQuestsClient.isClientDataLoaded() ? FTBQuestsClient.getClientPlayerData() : null;
        boolean synced = minecraft.player != null && minecraft.level != null && minecraft.getConnection() != null
                && team != null && !UNSYNCED_TEAM.equals(team.getTeamId());
        List<Choice> updated = new ArrayList<>();
        if (synced) {
            file.forAllChapters(chapter -> updated.add(new Choice(chapter.getCodeString(), chapter.getTitle().getString(),
                    chapter.getGroup().getTitle().getString())));
        }
        boolean changed = synced != ready || !choices.equals(updated);
        ready = synced;
        choices = List.copyOf(updated);
        return changed;
    }

    @Override
    public void tick() {
        // Refresh the catalog at most once per second, including loss/return of sync.
        if (++ticks >= 20) {
            ticks = 0;
            if (refreshChoices()) rebuild();
        }
    }

    private void rebuild() {
        clearWidgets();
        int buttonWidth = Math.max(100, Math.min(320, width - 20));
        int x = (width - buttonWidth) / 2;
        rows = Math.max(1, Math.min(8, (height - 142) / 24));
        int pages = Math.max(1, (choices.size() + rows - 1) / rows);
        page = Math.max(0, Math.min(page, pages - 1));
        addRenderableWidget(Button.builder(label("", Component.translatable("quest_progress_hud.chapter.all").getString()),
                b -> select("")).bounds(x, 52, buttonWidth, 20).build());
        for (int row = 0; row < rows && page * rows + row < choices.size(); row++) {
            Choice choice = choices.get(page * rows + row);
            Button button = addRenderableWidget(Button.builder(label(choice.id(), choice.title()), b -> select(choice.id()))
                    .bounds(x, 78 + row * 24, buttonWidth, 20).build());
            // Group and ID disambiguate duplicate or truncated titles without changing the saved identity.
            button.setTooltip(Tooltip.create(Component.literal(choice.title() + "\n" + choice.group() + "\n" + choice.id())));
        }
        int navigationY = height - 58;
        Button previous = addRenderableWidget(Button.builder(Component.literal("<"), b -> { page--; rebuild(); })
                .bounds(x, navigationY, 35, 20).build());
        previous.active = page > 0;
        Button next = addRenderableWidget(Button.builder(Component.literal(">"), b -> { page++; rebuild(); })
                .bounds(x + buttonWidth - 35, navigationY, 35, 20).build());
        next.active = page + 1 < pages;
        addRenderableWidget(Button.builder(Component.translatable("gui.cancel"), b -> onClose())
                .bounds(x, height - 30, buttonWidth / 2 - 3, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("quest_progress_hud.editor.save"), b -> {
            HudConfig.CHAPTER_ID.set(selected);
            HudConfig.SPEC.save();
            onClose();
        }).bounds(x + buttonWidth / 2 + 3, height - 30, buttonWidth / 2 - 3, 20).build());
    }

    private Component label(String id, String text) {
        int maxWidth = Math.max(70, Math.min(320, width - 20) - 30);
        String shortText = font.plainSubstrByWidth(text, maxWidth);
        return Component.literal((id.equals(selected) ? "> " : "") + shortText + (shortText.equals(text) ? "" : "..."));
    }

    private void select(String id) {
        selected = id;
        rebuild();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        graphics.drawCenteredString(font, title, width / 2, 12, 0xFFFFFF);
        String selectedTitle = choices.stream().filter(c -> c.id().equals(selected)).map(Choice::title).findFirst()
                .orElse(Component.translatable(selected.isEmpty() ? "quest_progress_hud.chapter.all"
                        : ready ? "hud.quest_progress_hud.chapter_unavailable" : "hud.quest_progress_hud.loading").getString());
        String status = Component.translatable("quest_progress_hud.chapter.selected", selectedTitle).getString();
        graphics.drawCenteredString(font, font.plainSubstrByWidth(status, Math.max(80, width - 30)), width / 2, 32, 0xFFFFFF);
        int pages = Math.max(1, (choices.size() + rows - 1) / rows);
        graphics.drawCenteredString(font, Component.translatable(ready ? "quest_progress_hud.chapter.page"
                : "hud.quest_progress_hud.loading", page + 1, pages), width / 2, height - 51, 0xAAAAAA);
    }

    @Override
    public void onClose() { minecraft.setScreen(parent); }
}
