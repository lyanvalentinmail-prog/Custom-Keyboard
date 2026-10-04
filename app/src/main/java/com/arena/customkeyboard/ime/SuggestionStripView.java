package com.arena.customkeyboard.ime;

import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.arena.customkeyboard.settings.KeyboardPreferences;
import com.arena.customkeyboard.settings.ThemePalette;

import java.util.List;

public class SuggestionStripView extends LinearLayout {
    public interface SuggestionListener {
        void onSuggestion(String suggestion);
    }

    public SuggestionStripView(Context context) {
        super(context);
        setOrientation(HORIZONTAL);
        setGravity(Gravity.CENTER);
    }

    public void render(List<String> suggestions, KeyboardPreferences prefs, ThemePalette palette, SuggestionListener listener) {
        removeAllViews();
        setBackgroundColor(palette.suggestionBackground);
        int hPad = dp(6);
        setPadding(hPad, dp(3), hPad, dp(3));
        if (suggestions == null || suggestions.isEmpty() || prefs.getSuggestionCount() == 0) {
            TextView hint = chip("Arena Keyboard", palette, false);
            hint.setAlpha(0.55f);
            addView(hint, new LayoutParams(0, LayoutParams.MATCH_PARENT, 1));
            return;
        }
        int count = Math.min(suggestions.size(), prefs.getSuggestionCount());
        for (int i = 0; i < count; i++) {
            final String suggestion = suggestions.get(i);
            TextView chip = chip(suggestion, palette, i == 0);
            chip.setOnClickListener(v -> listener.onSuggestion(suggestion));
            LayoutParams params = new LayoutParams(0, LayoutParams.MATCH_PARENT, 1);
            params.setMargins(dp(3), 0, dp(3), 0);
            addView(chip, params);
        }
    }

    private TextView chip(String text, ThemePalette palette, boolean primary) {
        TextView view = new TextView(getContext());
        view.setText(text);
        view.setSingleLine(true);
        view.setGravity(Gravity.CENTER);
        view.setTextSize(TypedValue.COMPLEX_UNIT_SP, primary ? 15 : 14);
        view.setTypeface(Typeface.create("sans", primary ? Typeface.BOLD : Typeface.NORMAL));
        view.setTextColor(primary ? palette.accent : palette.keyText);
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(primary ? adjustAlpha(palette.pressedBackground, 110) : 0x00000000);
        bg.setCornerRadius(dp(16));
        view.setBackground(bg);
        view.setContentDescription("Sugerencia " + text);
        return view;
    }

    private int adjustAlpha(int color, int alpha) {
        return (color & 0x00FFFFFF) | (alpha << 24);
    }

    private int dp(float value) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, getResources().getDisplayMetrics());
    }
}
