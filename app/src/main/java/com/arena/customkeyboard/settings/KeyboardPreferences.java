package com.arena.customkeyboard.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;

import com.arena.customkeyboard.model.ToolbarActionRegistry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class KeyboardPreferences {
    private static final String PREFS = "custom_keyboard_preferences";

    private static final String KEY_THEME = "theme";
    private static final String KEY_LANGUAGE = "language";
    private static final String KEY_KEY_HEIGHT = "key_height";
    private static final String KEY_KEY_GAP = "key_gap";
    private static final String KEY_KEY_RADIUS = "key_radius";
    private static final String KEY_KEY_TEXT_SIZE = "key_text_size";
    private static final String KEY_SPECIAL_SCALE = "special_scale";
    private static final String KEY_ANIMATIONS = "animations";
    private static final String KEY_ANIMATION_SPEED = "animation_speed";
    private static final String KEY_SOUND = "sound";
    private static final String KEY_SOUND_VOLUME = "sound_volume";
    private static final String KEY_VIBRATION = "vibration";
    private static final String KEY_VIBRATION_STRENGTH = "vibration_strength";
    private static final String KEY_VIBRATION_DURATION = "vibration_duration";
    private static final String KEY_CLIPBOARD = "clipboard";
    private static final String KEY_SUGGESTIONS = "suggestions";
    private static final String KEY_SUGGESTION_COUNT = "suggestion_count";
    private static final String KEY_AUTOCORRECT = "autocorrect";
    private static final String KEY_LEARNING = "learning";
    private static final String KEY_TOOLBAR_ACTIONS = "toolbar_actions";
    private static final String KEY_TOOLBAR_SIZE = "toolbar_size";
    private static final String KEY_HIGH_CONTRAST = "high_contrast";
    private static final String KEY_BACKGROUND_MODE = "background_mode";
    private static final String KEY_BACKGROUND_OPACITY = "background_opacity";
    private static final String KEY_BACKGROUND_BLUR = "background_blur";
    private static final String KEY_BACKGROUND_BRIGHTNESS = "background_brightness";
    private static final String KEY_BACKGROUND_SATURATION = "background_saturation";
    private static final String KEY_CUSTOM_BG = "custom_bg";
    private static final String KEY_CUSTOM_KEY = "custom_key";
    private static final String KEY_CUSTOM_SPECIAL = "custom_special";
    private static final String KEY_CUSTOM_TEXT = "custom_text";
    private static final String KEY_CUSTOM_TOOLBAR = "custom_toolbar";
    private static final String KEY_CUSTOM_ACCENT = "custom_accent";
    private static final String KEY_PROFILE = "profile";
    private static final String KEY_LAYOUT_SCALE_SPACE = "layout_scale_space";
    private static final String KEY_GESTURE_SPACE_CURSOR = "gesture_space_cursor";
    private static final String KEY_GESTURE_BACKSPACE_WORD = "gesture_backspace_word";
    private static final String KEY_GESTURE_UPPERCASE = "gesture_uppercase";

    private final SharedPreferences prefs;

    public KeyboardPreferences(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        ensureDefaults();
    }

    private void ensureDefaults() {
        if (!prefs.contains(KEY_TOOLBAR_ACTIONS)) setToolbarActions(ToolbarActionRegistry.defaultSamsungLikeOrder());
    }

    public String getThemeId() { return prefs.getString(KEY_THEME, KeyboardTheme.SAMSUNG_LIGHT); }
    public void setThemeId(String value) { putString(KEY_THEME, value); }

    public String getLanguage() { return prefs.getString(KEY_LANGUAGE, "es"); }
    public void setLanguage(String value) { putString(KEY_LANGUAGE, value); }

    public int getKeyHeightDp() { return prefs.getInt(KEY_KEY_HEIGHT, 50); }
    public void setKeyHeightDp(int value) { putInt(KEY_KEY_HEIGHT, clamp(value, 34, 76)); }

    public int getKeyGapDp() { return prefs.getInt(KEY_KEY_GAP, 4); }
    public void setKeyGapDp(int value) { putInt(KEY_KEY_GAP, clamp(value, 0, 12)); }

    public int getKeyRadiusDp() { return prefs.getInt(KEY_KEY_RADIUS, 14); }
    public void setKeyRadiusDp(int value) { putInt(KEY_KEY_RADIUS, clamp(value, 0, 30)); }

    public int getKeyTextSizeSp() { return prefs.getInt(KEY_KEY_TEXT_SIZE, 18); }
    public void setKeyTextSizeSp(int value) { putInt(KEY_KEY_TEXT_SIZE, clamp(value, 12, 30)); }

    public int getSpecialKeyScale() { return prefs.getInt(KEY_SPECIAL_SCALE, 100); }
    public void setSpecialKeyScale(int value) { putInt(KEY_SPECIAL_SCALE, clamp(value, 70, 140)); }

    public boolean isAnimationsEnabled() { return prefs.getBoolean(KEY_ANIMATIONS, true); }
    public void setAnimationsEnabled(boolean value) { putBoolean(KEY_ANIMATIONS, value); }

    public int getAnimationSpeedPercent() { return prefs.getInt(KEY_ANIMATION_SPEED, 100); }
    public void setAnimationSpeedPercent(int value) { putInt(KEY_ANIMATION_SPEED, clamp(value, 50, 200)); }

    public boolean isSoundEnabled() { return prefs.getBoolean(KEY_SOUND, false); }
    public void setSoundEnabled(boolean value) { putBoolean(KEY_SOUND, value); }

    public int getSoundVolume() { return prefs.getInt(KEY_SOUND_VOLUME, 35); }
    public void setSoundVolume(int value) { putInt(KEY_SOUND_VOLUME, clamp(value, 0, 100)); }

    public boolean isVibrationEnabled() { return prefs.getBoolean(KEY_VIBRATION, true); }
    public void setVibrationEnabled(boolean value) { putBoolean(KEY_VIBRATION, value); }

    public int getVibrationStrength() { return prefs.getInt(KEY_VIBRATION_STRENGTH, 35); }
    public void setVibrationStrength(int value) { putInt(KEY_VIBRATION_STRENGTH, clamp(value, 0, 100)); }

    public int getVibrationDurationMs() { return prefs.getInt(KEY_VIBRATION_DURATION, 12); }
    public void setVibrationDurationMs(int value) { putInt(KEY_VIBRATION_DURATION, clamp(value, 5, 60)); }

    public boolean isClipboardEnabled() { return prefs.getBoolean(KEY_CLIPBOARD, true); }
    public void setClipboardEnabled(boolean value) { putBoolean(KEY_CLIPBOARD, value); }

    public boolean isSuggestionsEnabled() { return prefs.getBoolean(KEY_SUGGESTIONS, true); }
    public void setSuggestionsEnabled(boolean value) { putBoolean(KEY_SUGGESTIONS, value); }

    public int getSuggestionCount() { return prefs.getInt(KEY_SUGGESTION_COUNT, 3); }
    public void setSuggestionCount(int value) { putInt(KEY_SUGGESTION_COUNT, clamp(value, 0, 5)); }

    public boolean isAutocorrectEnabled() { return prefs.getBoolean(KEY_AUTOCORRECT, true); }
    public void setAutocorrectEnabled(boolean value) { putBoolean(KEY_AUTOCORRECT, value); }

    public boolean isLearningEnabled() { return prefs.getBoolean(KEY_LEARNING, false); }
    public void setLearningEnabled(boolean value) { putBoolean(KEY_LEARNING, value); }

    public int getToolbarSizeDp() { return prefs.getInt(KEY_TOOLBAR_SIZE, 42); }
    public void setToolbarSizeDp(int value) { putInt(KEY_TOOLBAR_SIZE, clamp(value, 32, 60)); }

    public boolean isHighContrastEnabled() { return prefs.getBoolean(KEY_HIGH_CONTRAST, false); }
    public void setHighContrastEnabled(boolean value) { putBoolean(KEY_HIGH_CONTRAST, value); }

    public String getBackgroundMode() { return prefs.getString(KEY_BACKGROUND_MODE, "solid"); }
    public void setBackgroundMode(String value) { putString(KEY_BACKGROUND_MODE, value); }

    public int getBackgroundOpacity() { return prefs.getInt(KEY_BACKGROUND_OPACITY, 100); }
    public void setBackgroundOpacity(int value) { putInt(KEY_BACKGROUND_OPACITY, clamp(value, 20, 100)); }

    public int getBackgroundBlur() { return prefs.getInt(KEY_BACKGROUND_BLUR, 0); }
    public void setBackgroundBlur(int value) { putInt(KEY_BACKGROUND_BLUR, clamp(value, 0, 30)); }

    public int getBackgroundBrightness() { return prefs.getInt(KEY_BACKGROUND_BRIGHTNESS, 100); }
    public void setBackgroundBrightness(int value) { putInt(KEY_BACKGROUND_BRIGHTNESS, clamp(value, 30, 160)); }

    public int getBackgroundSaturation() { return prefs.getInt(KEY_BACKGROUND_SATURATION, 100); }
    public void setBackgroundSaturation(int value) { putInt(KEY_BACKGROUND_SATURATION, clamp(value, 0, 200)); }

    public int getCustomBackgroundColor() { return prefs.getInt(KEY_CUSTOM_BG, Color.rgb(242, 243, 247)); }
    public void setCustomBackgroundColor(int value) { putInt(KEY_CUSTOM_BG, value); }

    public int getCustomKeyColor() { return prefs.getInt(KEY_CUSTOM_KEY, Color.WHITE); }
    public void setCustomKeyColor(int value) { putInt(KEY_CUSTOM_KEY, value); }

    public int getCustomSpecialKeyColor() { return prefs.getInt(KEY_CUSTOM_SPECIAL, Color.rgb(225, 227, 233)); }
    public void setCustomSpecialKeyColor(int value) { putInt(KEY_CUSTOM_SPECIAL, value); }

    public int getCustomTextColor() { return prefs.getInt(KEY_CUSTOM_TEXT, Color.rgb(25, 27, 32)); }
    public void setCustomTextColor(int value) { putInt(KEY_CUSTOM_TEXT, value); }

    public int getCustomToolbarColor() { return prefs.getInt(KEY_CUSTOM_TOOLBAR, Color.rgb(242, 243, 247)); }
    public void setCustomToolbarColor(int value) { putInt(KEY_CUSTOM_TOOLBAR, value); }

    public int getCustomAccentColor() { return prefs.getInt(KEY_CUSTOM_ACCENT, Color.rgb(76, 115, 255)); }
    public void setCustomAccentColor(int value) { putInt(KEY_CUSTOM_ACCENT, value); }

    public String getProfileName() { return prefs.getString(KEY_PROFILE, "Personal"); }
    public void setProfileName(String value) { putString(KEY_PROFILE, value); }

    public int getSpaceWidthScale() { return prefs.getInt(KEY_LAYOUT_SCALE_SPACE, 100); }
    public void setSpaceWidthScale(int value) { putInt(KEY_LAYOUT_SCALE_SPACE, clamp(value, 60, 160)); }

    public boolean isSpaceCursorGestureEnabled() { return prefs.getBoolean(KEY_GESTURE_SPACE_CURSOR, true); }
    public void setSpaceCursorGestureEnabled(boolean value) { putBoolean(KEY_GESTURE_SPACE_CURSOR, value); }

    public boolean isBackspaceWordGestureEnabled() { return prefs.getBoolean(KEY_GESTURE_BACKSPACE_WORD, true); }
    public void setBackspaceWordGestureEnabled(boolean value) { putBoolean(KEY_GESTURE_BACKSPACE_WORD, value); }

    public boolean isUppercaseGestureEnabled() { return prefs.getBoolean(KEY_GESTURE_UPPERCASE, true); }
    public void setUppercaseGestureEnabled(boolean value) { putBoolean(KEY_GESTURE_UPPERCASE, value); }

    public List<String> getToolbarActions() {
        String raw = prefs.getString(KEY_TOOLBAR_ACTIONS, join(ToolbarActionRegistry.defaultSamsungLikeOrder()));
        ArrayList<String> ids = new ArrayList<>();
        if (raw != null && raw.length() > 0) {
            String[] pieces = raw.split(",");
            for (String piece : pieces) {
                String id = piece.trim();
                if (id.length() > 0 && ToolbarActionRegistry.get(id) != null) ids.add(id);
            }
        }
        if (!isClipboardEnabled()) ids.remove(ToolbarActionRegistry.CLIPBOARD);
        return ids;
    }

    public void setToolbarActions(List<String> actions) {
        LinkedHashSet<String> unique = new LinkedHashSet<>();
        for (String id : actions) {
            if (ToolbarActionRegistry.get(id) != null) unique.add(id);
        }
        prefs.edit().putString(KEY_TOOLBAR_ACTIONS, join(new ArrayList<>(unique))).apply();
    }

    public String exportJson() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("format", "keyboardtheme");
        json.put("version", 1);
        json.put("profile", getProfileName());
        json.put("theme", getThemeId());
        json.put("language", getLanguage());
        json.put("keyHeight", getKeyHeightDp());
        json.put("keyGap", getKeyGapDp());
        json.put("keyRadius", getKeyRadiusDp());
        json.put("keyTextSize", getKeyTextSizeSp());
        json.put("specialKeyScale", getSpecialKeyScale());
        json.put("animations", isAnimationsEnabled());
        json.put("animationSpeed", getAnimationSpeedPercent());
        json.put("sound", isSoundEnabled());
        json.put("soundVolume", getSoundVolume());
        json.put("vibration", isVibrationEnabled());
        json.put("vibrationStrength", getVibrationStrength());
        json.put("vibrationDuration", getVibrationDurationMs());
        json.put("clipboard", isClipboardEnabled());
        json.put("suggestions", isSuggestionsEnabled());
        json.put("suggestionCount", getSuggestionCount());
        json.put("autocorrect", isAutocorrectEnabled());
        json.put("learning", isLearningEnabled());
        json.put("toolbarSize", getToolbarSizeDp());
        json.put("toolbarActions", new JSONArray(getToolbarActions()));
        json.put("highContrast", isHighContrastEnabled());
        json.put("backgroundMode", getBackgroundMode());
        json.put("backgroundOpacity", getBackgroundOpacity());
        json.put("backgroundBlur", getBackgroundBlur());
        json.put("backgroundBrightness", getBackgroundBrightness());
        json.put("backgroundSaturation", getBackgroundSaturation());
        JSONObject colors = new JSONObject();
        colors.put("background", colorToHex(getCustomBackgroundColor()));
        colors.put("key", colorToHex(getCustomKeyColor()));
        colors.put("special", colorToHex(getCustomSpecialKeyColor()));
        colors.put("text", colorToHex(getCustomTextColor()));
        colors.put("toolbar", colorToHex(getCustomToolbarColor()));
        colors.put("accent", colorToHex(getCustomAccentColor()));
        json.put("colors", colors);
        JSONObject gestures = new JSONObject();
        gestures.put("spaceCursor", isSpaceCursorGestureEnabled());
        gestures.put("backspaceWord", isBackspaceWordGestureEnabled());
        gestures.put("uppercaseSwipe", isUppercaseGestureEnabled());
        json.put("gestures", gestures);
        return json.toString(2);
    }

    public void importJson(String text) throws JSONException {
        JSONObject json = new JSONObject(text);
        SharedPreferences.Editor editor = prefs.edit();
        putIfPresent(editor, json, "profile", KEY_PROFILE, "Personal");
        putIfPresent(editor, json, "theme", KEY_THEME, KeyboardTheme.SAMSUNG_LIGHT);
        putIfPresent(editor, json, "language", KEY_LANGUAGE, "es");
        putIntIfPresent(editor, json, "keyHeight", KEY_KEY_HEIGHT, 34, 76);
        putIntIfPresent(editor, json, "keyGap", KEY_KEY_GAP, 0, 12);
        putIntIfPresent(editor, json, "keyRadius", KEY_KEY_RADIUS, 0, 30);
        putIntIfPresent(editor, json, "keyTextSize", KEY_KEY_TEXT_SIZE, 12, 30);
        putIntIfPresent(editor, json, "specialKeyScale", KEY_SPECIAL_SCALE, 70, 140);
        putBooleanIfPresent(editor, json, "animations", KEY_ANIMATIONS);
        putIntIfPresent(editor, json, "animationSpeed", KEY_ANIMATION_SPEED, 50, 200);
        putBooleanIfPresent(editor, json, "sound", KEY_SOUND);
        putIntIfPresent(editor, json, "soundVolume", KEY_SOUND_VOLUME, 0, 100);
        putBooleanIfPresent(editor, json, "vibration", KEY_VIBRATION);
        putIntIfPresent(editor, json, "vibrationStrength", KEY_VIBRATION_STRENGTH, 0, 100);
        putIntIfPresent(editor, json, "vibrationDuration", KEY_VIBRATION_DURATION, 5, 60);
        putBooleanIfPresent(editor, json, "clipboard", KEY_CLIPBOARD);
        putBooleanIfPresent(editor, json, "suggestions", KEY_SUGGESTIONS);
        putIntIfPresent(editor, json, "suggestionCount", KEY_SUGGESTION_COUNT, 0, 5);
        putBooleanIfPresent(editor, json, "autocorrect", KEY_AUTOCORRECT);
        putBooleanIfPresent(editor, json, "learning", KEY_LEARNING);
        putIntIfPresent(editor, json, "toolbarSize", KEY_TOOLBAR_SIZE, 32, 60);
        putBooleanIfPresent(editor, json, "highContrast", KEY_HIGH_CONTRAST);
        putIfPresent(editor, json, "backgroundMode", KEY_BACKGROUND_MODE, "solid");
        putIntIfPresent(editor, json, "backgroundOpacity", KEY_BACKGROUND_OPACITY, 20, 100);
        putIntIfPresent(editor, json, "backgroundBlur", KEY_BACKGROUND_BLUR, 0, 30);
        putIntIfPresent(editor, json, "backgroundBrightness", KEY_BACKGROUND_BRIGHTNESS, 30, 160);
        putIntIfPresent(editor, json, "backgroundSaturation", KEY_BACKGROUND_SATURATION, 0, 200);
        if (json.has("toolbarActions")) {
            JSONArray array = json.getJSONArray("toolbarActions");
            ArrayList<String> ids = new ArrayList<>();
            for (int i = 0; i < array.length(); i++) ids.add(array.getString(i));
            editor.putString(KEY_TOOLBAR_ACTIONS, join(ids));
        }
        if (json.has("colors")) {
            JSONObject colors = json.getJSONObject("colors");
            if (colors.has("background")) editor.putInt(KEY_CUSTOM_BG, parseColor(colors.getString("background"), getCustomBackgroundColor()));
            if (colors.has("key")) editor.putInt(KEY_CUSTOM_KEY, parseColor(colors.getString("key"), getCustomKeyColor()));
            if (colors.has("special")) editor.putInt(KEY_CUSTOM_SPECIAL, parseColor(colors.getString("special"), getCustomSpecialKeyColor()));
            if (colors.has("text")) editor.putInt(KEY_CUSTOM_TEXT, parseColor(colors.getString("text"), getCustomTextColor()));
            if (colors.has("toolbar")) editor.putInt(KEY_CUSTOM_TOOLBAR, parseColor(colors.getString("toolbar"), getCustomToolbarColor()));
            if (colors.has("accent")) editor.putInt(KEY_CUSTOM_ACCENT, parseColor(colors.getString("accent"), getCustomAccentColor()));
        }
        if (json.has("gestures")) {
            JSONObject gestures = json.getJSONObject("gestures");
            if (gestures.has("spaceCursor")) editor.putBoolean(KEY_GESTURE_SPACE_CURSOR, gestures.getBoolean("spaceCursor"));
            if (gestures.has("backspaceWord")) editor.putBoolean(KEY_GESTURE_BACKSPACE_WORD, gestures.getBoolean("backspaceWord"));
            if (gestures.has("uppercaseSwipe")) editor.putBoolean(KEY_GESTURE_UPPERCASE, gestures.getBoolean("uppercaseSwipe"));
        }
        editor.apply();
    }

    public static String colorToHex(int color) {
        return String.format("#%02X%02X%02X", Color.red(color), Color.green(color), Color.blue(color));
    }

    public static int parseColor(String text, int fallback) {
        try {
            String value = text.trim();
            if (!value.startsWith("#")) value = "#" + value;
            return Color.parseColor(value);
        } catch (RuntimeException ex) {
            return fallback;
        }
    }

    private static String join(List<String> ids) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < ids.size(); i++) {
            if (i > 0) builder.append(',');
            builder.append(ids.get(i));
        }
        return builder.toString();
    }

    private void putString(String key, String value) { prefs.edit().putString(key, value).apply(); }
    private void putInt(String key, int value) { prefs.edit().putInt(key, value).apply(); }
    private void putBoolean(String key, boolean value) { prefs.edit().putBoolean(key, value).apply(); }

    private static int clamp(int value, int min, int max) { return Math.max(min, Math.min(max, value)); }

    private static void putIfPresent(SharedPreferences.Editor editor, JSONObject json, String jsonKey, String prefKey, String fallback) throws JSONException {
        if (json.has(jsonKey)) editor.putString(prefKey, json.optString(jsonKey, fallback));
    }

    private static void putIntIfPresent(SharedPreferences.Editor editor, JSONObject json, String jsonKey, String prefKey, int min, int max) throws JSONException {
        if (json.has(jsonKey)) editor.putInt(prefKey, clamp(json.getInt(jsonKey), min, max));
    }

    private static void putBooleanIfPresent(SharedPreferences.Editor editor, JSONObject json, String jsonKey, String prefKey) throws JSONException {
        if (json.has(jsonKey)) editor.putBoolean(prefKey, json.getBoolean(jsonKey));
    }
}
