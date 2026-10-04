package com.arena.customkeyboard.ime;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.GradientDrawable;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.arena.customkeyboard.model.ToolbarAction;
import com.arena.customkeyboard.model.ToolbarActionRegistry;
import com.arena.customkeyboard.settings.KeyboardPreferences;
import com.arena.customkeyboard.settings.ThemePalette;

public class ToolbarView extends HorizontalScrollView {
    public interface ToolbarListener {
        void onToolbarAction(String actionId);
    }

    private final LinearLayout container;

    public ToolbarView(Context context) {
        super(context);
        setHorizontalScrollBarEnabled(false);
        setOverScrollMode(OVER_SCROLL_NEVER);
        container = new LinearLayout(context);
        container.setOrientation(LinearLayout.HORIZONTAL);
        container.setGravity(Gravity.CENTER_VERTICAL);
        addView(container, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT));
    }

    public void render(KeyboardPreferences prefs, ThemePalette palette, ToolbarListener listener) {
        container.removeAllViews();
        setBackgroundColor(palette.toolbarBackground);
        int size = dp(prefs.getToolbarSizeDp());
        int gap = dp(4);
        container.setPadding(gap, dp(2), gap, dp(2));
        for (ToolbarAction action : ToolbarActionRegistry.fromIds(prefs.getToolbarActions())) {
            ImageButton button = new ImageButton(getContext());
            button.setImageResource(action.iconRes);
            button.setColorFilter(palette.keyText, PorterDuff.Mode.SRC_IN);
            button.setScaleType(ImageButton.ScaleType.CENTER);
            button.setPadding(dp(9), dp(9), dp(9), dp(9));
            button.setContentDescription(action.title);
            GradientDrawable bg = new GradientDrawable();
            bg.setColor(Color.TRANSPARENT);
            bg.setCornerRadius(dp(14));
            button.setBackground(bg);
            button.setOnClickListener(v -> listener.onToolbarAction(action.id));
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(size, size);
            params.setMargins(gap / 2, 0, gap / 2, 0);
            container.addView(button, params);
        }
    }

    private int dp(float value) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, getResources().getDisplayMetrics());
    }
}
