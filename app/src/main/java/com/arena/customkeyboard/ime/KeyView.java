package com.arena.customkeyboard.ime;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.accessibility.AccessibilityEvent;

import com.arena.customkeyboard.model.KeySpec;
import com.arena.customkeyboard.settings.KeyboardPreferences;
import com.arena.customkeyboard.settings.ThemePalette;

public class KeyView extends View {
    public interface KeyInteractionListener {
        void onKeyPress(KeySpec key);
        boolean onKeyLongPress(KeyView view, KeySpec key);
        void onKeyGesture(KeySpec key, float dx, float dy);
    }

    private final Paint fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG);
    private final Paint borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF rect = new RectF();
    private final Rect textBounds = new Rect();
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final int touchSlop;

    private KeySpec key;
    private ThemePalette palette;
    private KeyboardPreferences prefs;
    private KeyInteractionListener listener;
    private Drawable icon;
    private boolean fingerDown;
    private boolean longHandled;
    private boolean repeated;
    private float downX;
    private float downY;
    private int radiusPx;
    private int textSizePx;
    private int normalBackground;
    private int normalText;

    private final Runnable longPressRunnable = new Runnable() {
        @Override public void run() {
            if (!fingerDown || key == null || key.repeatable) return;
            if (listener != null) {
                longHandled = listener.onKeyLongPress(KeyView.this, key);
                if (longHandled) sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_LONG_CLICKED);
            }
        }
    };

    private final Runnable repeatRunnable = new Runnable() {
        @Override public void run() {
            if (!fingerDown || key == null || !key.repeatable || listener == null) return;
            repeated = true;
            listener.onKeyPress(key);
            handler.postDelayed(this, 48);
        }
    };

    public KeyView(Context context) {
        super(context);
        touchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        setFocusable(true);
        setClickable(true);
        borderPaint.setStyle(Paint.Style.STROKE);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTypeface(Typeface.create("sans", Typeface.NORMAL));
    }

    public void bind(KeySpec key, ThemePalette palette, KeyboardPreferences prefs, KeyInteractionListener listener) {
        this.key = key;
        this.palette = palette;
        this.prefs = prefs;
        this.listener = listener;
        radiusPx = dp(prefs.getKeyRadiusDp());
        textSizePx = sp(prefs.getKeyTextSizeSp());
        boolean special = key.special || key.type == KeySpec.TYPE_ACTION;
        normalBackground = special ? palette.specialKeyBackground : palette.keyBackground;
        normalText = special ? palette.specialKeyText : palette.keyText;
        if (prefs.isHighContrastEnabled()) {
            normalText = palette.isDark() ? 0xFFFFFFFF : 0xFF000000;
        }
        icon = null;
        if (key.iconRes != 0) {
            icon = getResources().getDrawable(key.iconRes, getContext().getTheme()).mutate();
            icon.setTint(normalText);
        }
        setContentDescription(key.contentDescription);
        setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_YES);
        setMinimumHeight(dp(44));
        setElevation(dp(1));
        invalidate();
    }

    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (key == null || key.type == KeySpec.TYPE_SPACER || palette == null) return;
        int w = getWidth();
        int h = getHeight();
        rect.set(1, 1, w - 1, h - 1);
        int background = fingerDown ? palette.pressedBackground : normalBackground;
        fillPaint.setStyle(Paint.Style.FILL);
        fillPaint.setColor(background);
        canvas.drawRoundRect(rect, radiusPx, radiusPx, fillPaint);

        int borderColor = prefs != null && prefs.isHighContrastEnabled() ? normalText : palette.border;
        borderPaint.setStrokeWidth(prefs != null && prefs.isHighContrastEnabled() ? dp(1.5f) : dp(0.6f));
        borderPaint.setColor(borderColor);
        canvas.drawRoundRect(rect, radiusPx, radiusPx, borderPaint);

        if (icon != null) {
            int size = (int) (Math.min(w, h) * (key.isAction("enter") ? 0.42f : 0.46f));
            size = Math.max(dp(18), size);
            int left = (w - size) / 2;
            int top = (h - size) / 2;
            icon.setBounds(left, top, left + size, top + size);
            icon.draw(canvas);
            return;
        }

        String label = key.label == null ? "" : key.label;
        if (label.length() == 0) return;
        textPaint.setColor(normalText);
        textPaint.setTextSize(textSizePxForKey());
        textPaint.setFakeBoldText(key.special && label.length() <= 3);
        textPaint.getTextBounds(label, 0, label.length(), textBounds);
        Paint.FontMetrics fm = textPaint.getFontMetrics();
        float y = h / 2f - (fm.ascent + fm.descent) / 2f;
        canvas.drawText(label, w / 2f, y, textPaint);
    }

    private float textSizePxForKey() {
        if (key == null || prefs == null) return textSizePx;
        if (key.special) return textSizePx * prefs.getSpecialKeyScale() / 100f;
        if (key.label != null && key.label.length() > 3) return textSizePx * 0.68f;
        return textSizePx;
    }

    @Override public boolean onTouchEvent(MotionEvent event) {
        if (key == null || key.type == KeySpec.TYPE_SPACER) return false;
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                fingerDown = true;
                longHandled = false;
                repeated = false;
                downX = event.getX();
                downY = event.getY();
                handler.postDelayed(longPressRunnable, 330);
                if (key.repeatable) handler.postDelayed(repeatRunnable, 410);
                setPressedState(true);
                return true;
            case MotionEvent.ACTION_MOVE:
                return true;
            case MotionEvent.ACTION_CANCEL:
                cancelPress();
                return true;
            case MotionEvent.ACTION_UP:
                float dx = event.getX() - downX;
                float dy = event.getY() - downY;
                boolean isGesture = Math.abs(dx) > touchSlop * 2.5f || Math.abs(dy) > touchSlop * 2.5f;
                boolean shouldClick = !longHandled && !repeated;
                cancelPress();
                if (shouldClick && listener != null) {
                    if (isGesture) listener.onKeyGesture(key, dx, dy);
                    else listener.onKeyPress(key);
                }
                performClick();
                return true;
        }
        return super.onTouchEvent(event);
    }

    @Override public boolean performClick() {
        super.performClick();
        return true;
    }

    private void cancelPress() {
        fingerDown = false;
        handler.removeCallbacks(longPressRunnable);
        handler.removeCallbacks(repeatRunnable);
        setPressedState(false);
    }

    private void setPressedState(boolean pressed) {
        if (prefs != null && prefs.isAnimationsEnabled()) {
            float target = pressed ? 0.94f : 1f;
            long duration = Math.max(24, 58 * 100 / Math.max(50, prefs.getAnimationSpeedPercent()));
            animate().scaleX(target).scaleY(target).setDuration(duration).start();
        }
        invalidate();
    }

    private int dp(float value) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, getResources().getDisplayMetrics());
    }

    private int sp(float value) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, value, getResources().getDisplayMetrics());
    }
}
