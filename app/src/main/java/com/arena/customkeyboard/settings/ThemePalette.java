package com.arena.customkeyboard.settings;

import android.graphics.Color;

public final class ThemePalette {
    public final String id;
    public final String name;
    public final int background;
    public final int backgroundSecondary;
    public final int toolbarBackground;
    public final int suggestionBackground;
    public final int keyBackground;
    public final int keyText;
    public final int specialKeyBackground;
    public final int specialKeyText;
    public final int pressedBackground;
    public final int border;
    public final int accent;
    public final int shadow;
    public final boolean glass;

    public ThemePalette(String id, String name, int background, int backgroundSecondary,
                        int toolbarBackground, int suggestionBackground,
                        int keyBackground, int keyText, int specialKeyBackground,
                        int specialKeyText, int pressedBackground, int border,
                        int accent, int shadow, boolean glass) {
        this.id = id;
        this.name = name;
        this.background = background;
        this.backgroundSecondary = backgroundSecondary;
        this.toolbarBackground = toolbarBackground;
        this.suggestionBackground = suggestionBackground;
        this.keyBackground = keyBackground;
        this.keyText = keyText;
        this.specialKeyBackground = specialKeyBackground;
        this.specialKeyText = specialKeyText;
        this.pressedBackground = pressedBackground;
        this.border = border;
        this.accent = accent;
        this.shadow = shadow;
        this.glass = glass;
    }

    public boolean isDark() {
        double luminance = (0.299 * Color.red(background) + 0.587 * Color.green(background) + 0.114 * Color.blue(background));
        return luminance < 120;
    }
}
