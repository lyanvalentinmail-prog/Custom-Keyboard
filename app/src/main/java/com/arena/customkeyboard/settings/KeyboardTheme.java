package com.arena.customkeyboard.settings;

import android.graphics.Color;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class KeyboardTheme {
    public static final String SAMSUNG_LIGHT = "samsung_light";
    public static final String SAMSUNG_DARK = "samsung_dark";
    public static final String AMOLED = "amoled";
    public static final String MINIMAL = "minimal";
    public static final String GLASS = "glass";
    public static final String MIDNIGHT = "midnight";
    public static final String CUSTOM = "custom";

    private static final LinkedHashMap<String, ThemePalette> THEMES = new LinkedHashMap<>();

    static {
        register(new ThemePalette(
                SAMSUNG_LIGHT, "Samsung-like Light",
                Color.rgb(242, 243, 247), Color.rgb(232, 234, 240),
                Color.rgb(242, 243, 247), Color.rgb(242, 243, 247),
                Color.rgb(255, 255, 255), Color.rgb(26, 28, 33),
                Color.rgb(222, 225, 232), Color.rgb(26, 28, 33),
                Color.rgb(218, 225, 241), Color.argb(48, 120, 125, 135),
                Color.rgb(74, 114, 255), Color.argb(44, 0, 0, 0), false));
        register(new ThemePalette(
                SAMSUNG_DARK, "Samsung-like Dark",
                Color.rgb(33, 35, 42), Color.rgb(25, 27, 33),
                Color.rgb(33, 35, 42), Color.rgb(33, 35, 42),
                Color.rgb(53, 56, 66), Color.rgb(242, 244, 248),
                Color.rgb(68, 72, 84), Color.rgb(242, 244, 248),
                Color.rgb(78, 86, 108), Color.argb(55, 255, 255, 255),
                Color.rgb(123, 160, 255), Color.argb(88, 0, 0, 0), false));
        register(new ThemePalette(
                AMOLED, "AMOLED",
                Color.BLACK, Color.rgb(5, 5, 5),
                Color.BLACK, Color.BLACK,
                Color.rgb(18, 18, 18), Color.WHITE,
                Color.rgb(32, 32, 32), Color.WHITE,
                Color.rgb(45, 45, 45), Color.rgb(33, 33, 33),
                Color.rgb(0, 200, 255), Color.argb(96, 0, 0, 0), false));
        register(new ThemePalette(
                MINIMAL, "Minimal",
                Color.rgb(250, 250, 250), Color.rgb(245, 245, 245),
                Color.rgb(250, 250, 250), Color.rgb(250, 250, 250),
                Color.rgb(248, 248, 248), Color.rgb(32, 32, 32),
                Color.rgb(238, 238, 238), Color.rgb(32, 32, 32),
                Color.rgb(230, 230, 230), Color.rgb(220, 220, 220),
                Color.rgb(80, 80, 80), Color.argb(20, 0, 0, 0), false));
        register(new ThemePalette(
                GLASS, "Glass",
                Color.argb(178, 230, 238, 248), Color.argb(160, 210, 225, 245),
                Color.argb(145, 255, 255, 255), Color.argb(145, 255, 255, 255),
                Color.argb(135, 255, 255, 255), Color.rgb(23, 31, 46),
                Color.argb(155, 226, 234, 248), Color.rgb(23, 31, 46),
                Color.argb(180, 210, 225, 255), Color.argb(65, 255, 255, 255),
                Color.rgb(72, 120, 255), Color.argb(70, 90, 100, 140), true));
        register(new ThemePalette(
                MIDNIGHT, "Midnight",
                Color.rgb(12, 18, 34), Color.rgb(19, 28, 52),
                Color.rgb(12, 18, 34), Color.rgb(12, 18, 34),
                Color.rgb(29, 43, 76), Color.rgb(235, 241, 255),
                Color.rgb(42, 61, 100), Color.WHITE,
                Color.rgb(58, 82, 130), Color.argb(55, 120, 160, 255),
                Color.rgb(83, 140, 255), Color.argb(90, 0, 0, 0), false));
    }

    private KeyboardTheme() {}

    private static void register(ThemePalette palette) {
        THEMES.put(palette.id, palette);
    }

    public static ThemePalette byId(String id) {
        ThemePalette palette = THEMES.get(id);
        return palette == null ? THEMES.get(SAMSUNG_LIGHT) : palette;
    }

    public static List<ThemePalette> presets() {
        return new ArrayList<>(THEMES.values());
    }

    public static ThemePalette customFrom(KeyboardPreferences prefs) {
        return new ThemePalette(CUSTOM, "Custom",
                prefs.getCustomBackgroundColor(), darken(prefs.getCustomBackgroundColor()),
                prefs.getCustomToolbarColor(), prefs.getCustomToolbarColor(),
                prefs.getCustomKeyColor(), prefs.getCustomTextColor(),
                prefs.getCustomSpecialKeyColor(), prefs.getCustomTextColor(),
                blend(prefs.getCustomAccentColor(), prefs.getCustomKeyColor(), 0.30f),
                withAlpha(prefs.getCustomTextColor(), 42), prefs.getCustomAccentColor(),
                Color.argb(65, 0, 0, 0), false);
    }

    public static ThemePalette resolve(KeyboardPreferences prefs) {
        if (CUSTOM.equals(prefs.getThemeId())) return customFrom(prefs);
        return byId(prefs.getThemeId());
    }

    public static Map<String, ThemePalette> themeMap() {
        return THEMES;
    }

    public static int withAlpha(int color, int alpha) {
        return Color.argb(alpha, Color.red(color), Color.green(color), Color.blue(color));
    }

    public static int blend(int a, int b, float ratio) {
        float inv = 1f - ratio;
        return Color.rgb(
                Math.round(Color.red(a) * ratio + Color.red(b) * inv),
                Math.round(Color.green(a) * ratio + Color.green(b) * inv),
                Math.round(Color.blue(a) * ratio + Color.blue(b) * inv));
    }

    private static int darken(int color) {
        return Color.rgb(Math.max(0, (int)(Color.red(color) * 0.85f)),
                Math.max(0, (int)(Color.green(color) * 0.85f)),
                Math.max(0, (int)(Color.blue(color) * 0.85f)));
    }
}
