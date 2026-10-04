package com.arena.customkeyboard.ime;

import android.content.Context;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;

import com.arena.customkeyboard.model.KeySpec;
import com.arena.customkeyboard.model.KeyboardLayout;
import com.arena.customkeyboard.settings.KeyboardPreferences;
import com.arena.customkeyboard.settings.ThemePalette;

import java.util.List;

public class KeyboardSurface extends LinearLayout {
    public KeyboardSurface(Context context) {
        super(context);
        setOrientation(VERTICAL);
        setGravity(Gravity.CENTER);
    }

    public void render(List<List<KeySpec>> rows, ThemePalette palette, KeyboardPreferences prefs, KeyView.KeyInteractionListener listener) {
        removeAllViews();
        setBackgroundColor(palette.background);
        int gap = dp(prefs.getKeyGapDp());
        setPadding(gap, Math.max(1, gap / 2), gap, gap);
        int height = dp(prefs.getKeyHeightDp());
        for (List<KeySpec> specs : rows) {
            LinearLayout row = new LinearLayout(getContext());
            row.setOrientation(HORIZONTAL);
            row.setGravity(Gravity.CENTER);
            LayoutParams rowParams = new LayoutParams(LayoutParams.MATCH_PARENT, height);
            rowParams.setMargins(0, Math.max(0, gap / 2), 0, Math.max(0, gap / 2));
            addView(row, rowParams);
            for (KeySpec spec : specs) {
                int weight = spec.weight;
                if (spec.isAction(KeyboardLayout.ACTION_SPACE)) {
                    weight = Math.max(12, weight * prefs.getSpaceWidthScale() / 100);
                }
                if (spec.type == KeySpec.TYPE_SPACER) {
                    View spacer = new View(getContext());
                    row.addView(spacer, new LinearLayout.LayoutParams(0, LayoutParams.MATCH_PARENT, weight));
                    continue;
                }
                KeyView key = new KeyView(getContext());
                key.bind(spec, palette, prefs, listener);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, LayoutParams.MATCH_PARENT, weight);
                params.setMargins(gap / 2, 0, gap / 2, 0);
                row.addView(key, params);
            }
        }
    }

    private int dp(float value) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, getResources().getDisplayMetrics());
    }
}
