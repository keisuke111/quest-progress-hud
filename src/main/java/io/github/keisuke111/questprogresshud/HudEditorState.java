package io.github.keisuke111.questprogresshud;

/** An isolated edit transaction. Nothing here writes the live or saved config. */
final class HudEditorState {
    record Settings(boolean right, boolean bottom, int offsetX, int offsetY, double scale, int opacity) {}
    private final Settings original;
    private Settings draft;
    private boolean dragging;
    private double grabX;
    private double grabY;

    HudEditorState(Settings original) {
        this.original = original;
        this.draft = original;
    }

    Settings draft() { return draft; }
    Settings original() { return original; }
    boolean dragging() { return dragging; }

    HudLayout.Placement placement(int width, int height, HudLayout.Row row) {
        return HudLayout.place(width, height, row.width(), row.height(), draft.scale(),
                draft.right(), draft.bottom(), draft.offsetX(), draft.offsetY());
    }

    boolean beginDrag(double mouseX, double mouseY, int width, int height, HudLayout.Row row) {
        HudLayout.Placement p = placement(width, height, row);
        if (mouseX < p.x() || mouseY < p.y() || mouseX >= p.x() + row.width() * p.scale()
                || mouseY >= p.y() + row.height() * p.scale()) return false;
        grabX = mouseX - p.x();
        grabY = mouseY - p.y();
        dragging = true;
        return true;
    }

    void dragTo(double mouseX, double mouseY, int width, int height, HudLayout.Row row) {
        if (!dragging) return;
        HudLayout.Placement p = placement(width, height, row);
        double panelWidth = row.width() * p.scale();
        double panelHeight = row.height() * p.scale();
        double x = Math.max(0, Math.min(width - panelWidth, mouseX - grabX));
        double y = Math.max(0, Math.min(height - panelHeight, mouseY - grabY));
        int offsetX = offset(draft.right() ? width - panelWidth - 8 - x : x - 8);
        int offsetY = offset(draft.bottom() ? height - panelHeight - 8 - y : y - 8);
        draft = new Settings(draft.right(), draft.bottom(), offsetX, offsetY, draft.scale(), draft.opacity());
    }

    void endDrag() { dragging = false; }

    void setScale(double scale) {
        if (!Double.isFinite(scale)) return;
        draft = new Settings(draft.right(), draft.bottom(), draft.offsetX(), draft.offsetY(),
                Math.max(0.5, Math.min(3.0, Math.round(scale * 20) / 20.0)), draft.opacity());
    }

    void setOpacity(int opacity) {
        draft = new Settings(draft.right(), draft.bottom(), draft.offsetX(), draft.offsetY(), draft.scale(),
                Math.max(0, Math.min(100, opacity)));
    }

    void reset() {
        endDrag();
        draft = new Settings(false, false, 0, 0, 1.0, 72);
    }

    private static int offset(double value) { return (int) Math.max(-10000, Math.min(10000, Math.round(value))); }
}
