package io.github.keisuke111.questprogresshud;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;

import java.nio.file.Files;
import java.nio.file.Path;

/** Uses NeoForge's actual spec and TOML persistence, without touching a game installation. */
public final class HudConfigTest {
    public static void main(String[] args) throws Exception {
        Path directory = Files.createTempDirectory("quest-hud-config-check");
        Path path = directory.resolve("client.toml");
        try {
            try (CommentedFileConfig config = CommentedFileConfig.builder(path).sync().build()) {
                HudConfig.SPEC.correct(config);
                expect(config.get("enabled").equals(true) && config.get("anchor").toString().equals("TOP_LEFT")
                        && number(config, "offsetX") == 0 && number(config, "offsetY") == 0
                        && number(config, "scale") == 1.0 && number(config, "backgroundOpacity") == 72, "Existing defaults preserved");
                config.set("enabled", false);
                config.set("anchor", "BOTTOM_RIGHT");
                config.set("offsetX", 67);
                config.set("offsetY", -12);
                config.set("scale", 1.5);
                config.set("backgroundOpacity", 43);
                config.save();
            }
            try (CommentedFileConfig config = CommentedFileConfig.builder(path).sync().build()) {
                config.load();
                expect(HudConfig.SPEC.isCorrect(config), "Saved config accepted by NeoForge");
                expect(config.get("enabled").equals(false) && config.get("anchor").toString().equals("BOTTOM_RIGHT")
                        && number(config, "offsetX") == 67 && number(config, "offsetY") == -12
                        && number(config, "scale") == 1.5 && number(config, "backgroundOpacity") == 43, "All settings survive file reload");
                config.set("anchor", "INVALID");
                config.set("offsetX", 99999);
                config.set("offsetY", -99999);
                config.set("scale", 99.0);
                config.set("backgroundOpacity", -1);
                expect(!HudConfig.SPEC.isCorrect(config), "Invalid config detected");
                HudConfig.SPEC.correct(config);
                expect(HudConfig.SPEC.isCorrect(config), "Invalid config repaired");
                expect(config.get("anchor").toString().equals("TOP_LEFT")
                        && Math.abs(number(config, "offsetX")) <= 10000 && Math.abs(number(config, "offsetY")) <= 10000
                        && number(config, "scale") >= 0.5 && number(config, "scale") <= 3.0
                        && number(config, "backgroundOpacity") >= 0, "Repaired values stay within supported ranges");
            }
            System.out.println("HUD config checks passed: defaults, all six persisted settings, reload and invalid-value correction.");
        } finally {
            Files.deleteIfExists(path);
            Files.deleteIfExists(directory);
        }
    }

    private static double number(CommentedFileConfig config, String key) { return ((Number) config.get(key)).doubleValue(); }

    private static void expect(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
