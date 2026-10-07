package io.github.keisuke111.questprogresshud;

/** Screen-space placement, independent of Minecraft so all corner and scale combinations can be tested. */
final class HudLayout {
    record Placement(double x, double y, double scale) {}
    record Row(int width, int height, int countX, int percentageX, int barY) {}
    static final int PADDING = 5;
    static final int GAP = 8;
    static final int BAR_HEIGHT = 4;

    static Row compactRow(int titleWidth, int countWidth, int percentageWidth, int lineHeight, boolean loaded) {
        int countX = PADDING + titleWidth + GAP;
        int width = Math.max(116, countX + countWidth + PADDING + (loaded ? GAP + percentageWidth : 0));
        int barY = PADDING + lineHeight + 3;
        int height = loaded ? barY + BAR_HEIGHT + PADDING : PADDING * 2 + lineHeight;
        return new Row(width, height, countX, width - PADDING - percentageWidth, barY);
    }

    static Placement place(int screenWidth, int screenHeight, int panelWidth, int panelHeight,
                           double requestedScale, boolean right, boolean bottom, int offsetX, int offsetY) {
        double scale = Math.min(requestedScale,
                Math.min((double) screenWidth / panelWidth, (double) screenHeight / panelHeight));
        double width = panelWidth * scale;
        double height = panelHeight * scale;
        double x = right ? screenWidth - width - 8 - offsetX : 8 + offsetX;
        double y = bottom ? screenHeight - height - 8 - offsetY : 8 + offsetY;
        return new Placement(
                Math.max(0, Math.min(screenWidth - width, x)),
                Math.max(0, Math.min(screenHeight - height, y)),
                scale);
    }

    static int backgroundColor(int opacity) {
        int alpha = (int) Math.round(255.0 * opacity / 100.0);
        return (alpha << 24) | 0x14171A;
    }

    private HudLayout() {}
}
