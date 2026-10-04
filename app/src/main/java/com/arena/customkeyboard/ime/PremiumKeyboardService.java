package com.arena.customkeyboard.ime;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.GradientDrawable;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.provider.Settings;
import android.text.InputType;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodManager;
import android.inputmethodservice.InputMethodService;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.arena.customkeyboard.MainActivity;
import com.arena.customkeyboard.R;
import com.arena.customkeyboard.clipboard.ClipboardRepository;
import com.arena.customkeyboard.emoji.EmojiRepository;
import com.arena.customkeyboard.emoji.SymbolRepository;
import com.arena.customkeyboard.model.KeySpec;
import com.arena.customkeyboard.model.KeyboardLayout;
import com.arena.customkeyboard.model.KeyboardMode;
import com.arena.customkeyboard.model.ToolbarActionRegistry;
import com.arena.customkeyboard.settings.KeyboardPreferences;
import com.arena.customkeyboard.settings.KeyboardTheme;
import com.arena.customkeyboard.settings.ThemePalette;
import com.arena.customkeyboard.suggestions.SuggestionEngine;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PremiumKeyboardService extends InputMethodService implements
        KeyView.KeyInteractionListener,
        ToolbarView.ToolbarListener,
        SuggestionStripView.SuggestionListener {

    private KeyboardPreferences prefs;
    private SuggestionEngine suggestionEngine;
    private ThemePalette palette;
    private LinearLayout root;
    private ToolbarView toolbarView;
    private SuggestionStripView suggestionStripView;
    private LinearLayout panelContainer;
    private KeyboardSurface keyboardSurface;
    private final StringBuilder composing = new StringBuilder();
    private KeyboardMode mode = KeyboardMode.LETTERS;
    private boolean shifted;
    private boolean capsLocked;
    private long lastShiftTap;
    private boolean passwordField;
    private boolean noSuggestionsField;
    private EditorInfo currentEditorInfo;
    private String emojiCategory = "people";
    private String symbolCategory = "math";

    @Override public void onCreate() {
        super.onCreate();
        prefs = new KeyboardPreferences(this);
        suggestionEngine = new SuggestionEngine(this);
        palette = KeyboardTheme.resolve(prefs);
    }

    @Override public View onCreateInputView() {
        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER);
        rebuildAllViews();
        return root;
    }

    @Override public void onStartInput(EditorInfo attribute, boolean restarting) {
        super.onStartInput(attribute, restarting);
        currentEditorInfo = attribute;
        prefs = new KeyboardPreferences(this);
        palette = KeyboardTheme.resolve(prefs);
        composing.setLength(0);
        passwordField = isPasswordInput(attribute == null ? 0 : attribute.inputType);
        noSuggestionsField = passwordField || isNoSuggestionsField(attribute == null ? 0 : attribute.inputType);
        shifted = shouldStartShifted(attribute);
        capsLocked = false;
        mode = detectInitialMode(attribute);
        if (root != null) rebuildAllViews();
    }

    @Override public void onFinishInput() {
        super.onFinishInput();
        composing.setLength(0);
        hidePanel();
    }

    @Override public void onUpdateSelection(int oldSelStart, int oldSelEnd, int newSelStart, int newSelEnd,
                                            int candidatesStart, int candidatesEnd) {
        super.onUpdateSelection(oldSelStart, oldSelEnd, newSelStart, newSelEnd, candidatesStart, candidatesEnd);
        if (composing.length() > 0 && (newSelStart < candidatesStart || newSelStart > candidatesEnd)) {
            composing.setLength(0);
            updateSuggestions();
        }
    }

    private void rebuildAllViews() {
        if (root == null) return;
        prefs = new KeyboardPreferences(this);
        palette = KeyboardTheme.resolve(prefs);
        root.removeAllViews();
        root.setBackgroundColor(applyBackgroundControls(palette.background));
        if (!prefs.getToolbarActions().isEmpty()) {
            toolbarView = new ToolbarView(this);
            toolbarView.render(prefs, palette, this);
            root.addView(toolbarView, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(prefs.getToolbarSizeDp() + 8)));
        }
        if (prefs.isSuggestionsEnabled() && !noSuggestionsField) {
            suggestionStripView = new SuggestionStripView(this);
            root.addView(suggestionStripView, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(36)));
        } else {
            suggestionStripView = null;
        }
        panelContainer = new LinearLayout(this);
        panelContainer.setOrientation(LinearLayout.VERTICAL);
        panelContainer.setVisibility(View.GONE);
        panelContainer.setBackgroundColor(palette.backgroundSecondary);
        root.addView(panelContainer, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(220)));
        keyboardSurface = new KeyboardSurface(this);
        rebuildKeyboardOnly();
        root.addView(keyboardSurface, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        updateSuggestions();
    }

    private void rebuildKeyboardOnly() {
        if (keyboardSurface == null) return;
        keyboardSurface.render(KeyboardLayout.forMode(mode, prefs.getLanguage(), shifted || capsLocked, capsLocked), palette, prefs, this);
    }

    @Override public void onKeyPress(KeySpec key) {
        if (key == null) return;
        feedback();
        if (key.type == KeySpec.TYPE_TEXT) {
            hidePanel();
            handleText(key.output);
            return;
        }
        if (key.type != KeySpec.TYPE_ACTION) return;
        String action = key.action;
        if (KeyboardLayout.ACTION_SHIFT.equals(action)) handleShift();
        else if (KeyboardLayout.ACTION_BACKSPACE.equals(action)) handleBackspace();
        else if (KeyboardLayout.ACTION_ENTER.equals(action)) handleEnter();
        else if (KeyboardLayout.ACTION_SPACE.equals(action)) handleSpace();
        else if (KeyboardLayout.ACTION_MODE_NUMBERS.equals(action)) setMode(KeyboardMode.NUMBERS);
        else if (KeyboardLayout.ACTION_MODE_LETTERS.equals(action)) setMode(KeyboardMode.LETTERS);
        else if (KeyboardLayout.ACTION_MODE_SYMBOLS.equals(action)) setMode(KeyboardMode.SYMBOLS);
        else if (KeyboardLayout.ACTION_MODE_PROGRAMMER.equals(action)) setMode(KeyboardMode.PROGRAMMER);
        else if (KeyboardLayout.ACTION_EMOJI.equals(action)) showEmojiPanel();
        else if (KeyboardLayout.ACTION_TAB.equals(action)) commitRawText("\t");
        else if (KeyboardLayout.ACTION_ESC.equals(action)) sendDownUpKeyEvents(KeyEvent.KEYCODE_ESCAPE);
        else if (KeyboardLayout.ACTION_CTRL.equals(action)) sendDownUpKeyEvents(KeyEvent.KEYCODE_CTRL_LEFT);
        else if (KeyboardLayout.ACTION_ALT.equals(action)) sendDownUpKeyEvents(KeyEvent.KEYCODE_ALT_LEFT);
    }

    @Override public boolean onKeyLongPress(KeyView view, KeySpec key) {
        if (key == null) return false;
        if (KeyboardLayout.ACTION_SPACE.equals(key.action)) {
            showSelectionPanel();
            return true;
        }
        if (key.alternatives == null || key.alternatives.isEmpty()) return false;
        showAlternatives(view, key);
        return true;
    }

    @Override public void onKeyGesture(KeySpec key, float dx, float dy) {
        if (key == null) return;
        float adx = Math.abs(dx);
        float ady = Math.abs(dy);
        if (KeyboardLayout.ACTION_SPACE.equals(key.action) && prefs.isSpaceCursorGestureEnabled() && adx >= ady) {
            moveCursor(dx > 0 ? 1 : -1, Math.max(1, Math.round(adx / Math.max(1, dp(18)))));
            return;
        }
        if (KeyboardLayout.ACTION_BACKSPACE.equals(key.action) && prefs.isBackspaceWordGestureEnabled() && dx < -dp(18)) {
            deleteWordBeforeCursor();
            return;
        }
        if (prefs.isUppercaseGestureEnabled() && ady > adx) {
            shifted = dy < 0;
            if (dy > 0) capsLocked = false;
            rebuildKeyboardOnly();
            return;
        }
        onKeyPress(key);
    }

    @Override public void onSuggestion(String suggestion) {
        if (TextUtils.isEmpty(suggestion)) return;
        feedback();
        InputConnection ic = getCurrentInputConnection();
        if (ic == null) return;
        if (composing.length() > 0) {
            ic.commitText(suggestion, 1);
            if (prefs.isLearningEnabled()) suggestionEngine.learnWord(suggestion);
            composing.setLength(0);
        } else {
            ic.commitText(suggestion, 1);
        }
        updateSuggestions();
    }

    @Override public void onToolbarAction(String actionId) {
        feedback();
        if (ToolbarActionRegistry.EMOJI.equals(actionId)) showEmojiPanel();
        else if (ToolbarActionRegistry.CLIPBOARD.equals(actionId)) showClipboardPanel();
        else if (ToolbarActionRegistry.SETTINGS.equals(actionId)) openSettings();
        else if (ToolbarActionRegistry.SEARCH.equals(actionId)) showSearchPanel();
        else if (ToolbarActionRegistry.TRANSLATE.equals(actionId)) showTranslatePanel();
        else if (ToolbarActionRegistry.SELECTION.equals(actionId)) showSelectionPanel();
        else if (ToolbarActionRegistry.SYMBOLS.equals(actionId)) showSymbolPanel();
        else if (ToolbarActionRegistry.LANGUAGE.equals(actionId)) toggleLanguage();
        else if (ToolbarActionRegistry.CLOSE.equals(actionId)) requestHideSelf(0);
        else if (ToolbarActionRegistry.KEYBOARD.equals(actionId)) hidePanel();
        else if (ToolbarActionRegistry.GIF.equals(actionId)) showGifStickerPanel();
        else if (ToolbarActionRegistry.MICROPHONE.equals(actionId)) showInfoPanel("Micrófono", "El dictado por voz requiere un proveedor del sistema. No se graba audio desde este teclado.", null);
    }

    private void handleText(String text) {
        if (TextUtils.isEmpty(text)) return;
        if (shouldCompose(text)) {
            InputConnection ic = getCurrentInputConnection();
            if (ic == null) return;
            composing.append(text);
            ic.setComposingText(composing, 1);
            updateSuggestions();
            if (shifted && !capsLocked) {
                shifted = false;
                rebuildKeyboardOnly();
            }
        } else {
            commitComposing(true, "");
            commitRawText(text);
        }
    }

    private boolean shouldCompose(String text) {
        if (TextUtils.isEmpty(text) || text.codePointCount(0, text.length()) != 1) return false;
        if (!prefs.isSuggestionsEnabled() || noSuggestionsField) return false;
        if (currentEditorInfo == null) return false;
        int inputType = currentEditorInfo.inputType;
        int cls = inputType & InputType.TYPE_MASK_CLASS;
        if (cls != InputType.TYPE_CLASS_TEXT) return false;
        int cp = text.codePointAt(0);
        return Character.isLetter(cp);
    }

    private void handleSpace() {
        hidePanel();
        if (composing.length() > 0) commitComposing(true, " ");
        else commitRawText(" ");
    }

    private void handleEnter() {
        hidePanel();
        commitComposing(true, "");
        InputConnection ic = getCurrentInputConnection();
        if (ic == null) return;
        int action = currentEditorInfo == null ? EditorInfo.IME_ACTION_NONE : currentEditorInfo.imeOptions & EditorInfo.IME_MASK_ACTION;
        boolean multiLine = currentEditorInfo != null && (currentEditorInfo.inputType & InputType.TYPE_TEXT_FLAG_MULTI_LINE) != 0;
        if (multiLine && (action == EditorInfo.IME_ACTION_NONE || action == EditorInfo.IME_ACTION_UNSPECIFIED)) {
            ic.commitText("\n", 1);
        } else if (action != EditorInfo.IME_ACTION_NONE && action != EditorInfo.IME_ACTION_UNSPECIFIED) {
            ic.performEditorAction(action);
        } else {
            sendDownUpKeyEvents(KeyEvent.KEYCODE_ENTER);
        }
    }

    private void handleBackspace() {
        hidePanel();
        InputConnection ic = getCurrentInputConnection();
        if (ic == null) return;
        CharSequence selected = ic.getSelectedText(0);
        if (!TextUtils.isEmpty(selected)) {
            ic.commitText("", 1);
            return;
        }
        if (composing.length() > 0) {
            composing.deleteCharAt(composing.length() - 1);
            if (composing.length() > 0) ic.setComposingText(composing, 1);
            else ic.finishComposingText();
            updateSuggestions();
            return;
        }
        if (Build.VERSION.SDK_INT >= 24) ic.deleteSurroundingTextInCodePoints(1, 0);
        else ic.deleteSurroundingText(1, 0);
        updateSuggestions();
    }

    private void handleShift() {
        long now = System.currentTimeMillis();
        if (now - lastShiftTap < 480) {
            capsLocked = !capsLocked;
            shifted = capsLocked;
        } else {
            shifted = !shifted;
            if (!shifted) capsLocked = false;
        }
        lastShiftTap = now;
        rebuildKeyboardOnly();
    }

    private void setMode(KeyboardMode newMode) {
        hidePanel();
        mode = newMode;
        rebuildKeyboardOnly();
    }

    private void commitComposing(boolean autocorrect, String suffix) {
        InputConnection ic = getCurrentInputConnection();
        if (ic == null || composing.length() == 0) return;
        String word = composing.toString();
        String finalWord = autocorrect && prefs.isAutocorrectEnabled() ? suggestionEngine.autoCorrect(word, prefs.getLanguage()) : word;
        ic.commitText(finalWord + suffix, 1);
        if (prefs.isLearningEnabled() && !passwordField) suggestionEngine.learnWord(finalWord);
        composing.setLength(0);
        updateSuggestions();
    }

    private void commitRawText(String text) {
        InputConnection ic = getCurrentInputConnection();
        if (ic == null) return;
        ic.commitText(text, 1);
        updateSuggestions();
    }

    private void updateSuggestions() {
        if (suggestionStripView == null) return;
        List<String> suggestions = suggestionEngine.suggest(composing.toString(), prefs.getLanguage(), prefs.getSuggestionCount());
        suggestionStripView.render(suggestions, prefs, palette, this);
    }

    private void feedback() {
        if (prefs.isSoundEnabled()) {
            AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            if (audioManager != null) audioManager.playSoundEffect(AudioManager.FX_KEY_CLICK, prefs.getSoundVolume() / 100f);
        }
        if (prefs.isVibrationEnabled() && prefs.getVibrationStrength() > 0) {
            Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            if (vibrator != null && vibrator.hasVibrator()) {
                int duration = prefs.getVibrationDurationMs();
                if (Build.VERSION.SDK_INT >= 26) {
                    int amplitude = Math.max(1, Math.min(255, prefs.getVibrationStrength() * 255 / 100));
                    vibrator.vibrate(VibrationEffect.createOneShot(duration, amplitude));
                } else {
                    vibrator.vibrate(duration);
                }
            }
        }
    }

    private void showAlternatives(KeyView anchor, KeySpec key) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER);
        row.setPadding(dp(6), dp(6), dp(6), dp(6));
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(palette.toolbarBackground);
        bg.setCornerRadius(dp(18));
        bg.setStroke(dp(1), palette.border);
        row.setBackground(bg);
        final PopupWindow popup = new PopupWindow(row, ViewGroup.LayoutParams.WRAP_CONTENT, dp(56), false);
        popup.setOutsideTouchable(true);
        for (String alternative : key.alternatives) {
            TextView item = new TextView(this);
            String value = (shifted || capsLocked) && alternative.length() == 1 ? alternative.toUpperCase(Locale.getDefault()) : alternative;
            item.setText(value);
            item.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
            item.setTextColor(palette.keyText);
            item.setGravity(Gravity.CENTER);
            item.setContentDescription("Carácter alternativo " + value);
            GradientDrawable itemBg = new GradientDrawable();
            itemBg.setColor(palette.keyBackground);
            itemBg.setCornerRadius(dp(14));
            item.setBackground(itemBg);
            item.setOnClickListener(v -> {
                popup.dismiss();
                feedback();
                handleText(value);
            });
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dp(42), dp(44));
            params.setMargins(dp(3), 0, dp(3), 0);
            row.addView(item, params);
        }
        popup.showAsDropDown(anchor, -Math.max(0, row.getChildCount() - 1) * dp(20), -anchor.getHeight() - dp(68));
    }

    private void showEmojiPanel() {
        commitComposing(false, "");
        if (!preparePanel()) return;
        renderEmojiPanel();
    }

    private void renderEmojiPanel() {
        panelContainer.removeAllViews();
        panelContainer.addView(categoryBarForEmoji(), new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(44)));
        EmojiRepository.Category selected = null;
        for (EmojiRepository.Category category : EmojiRepository.categories(this)) {
            if (category.id.equals(emojiCategory)) selected = category;
        }
        if (selected == null || selected.items.isEmpty()) {
            selected = findFirstNonEmptyEmojiCategory();
            if (selected != null) emojiCategory = selected.id;
        }
        if (selected == null || selected.items.isEmpty()) {
            panelContainer.addView(messageView("Sin emojis recientes. Mantén pulsado un emoji para marcarlo como favorito."), matchWrap());
            return;
        }
        panelContainer.addView(gridForItems(selected.items, true), new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1));
    }

    private EmojiRepository.Category findFirstNonEmptyEmojiCategory() {
        for (EmojiRepository.Category category : EmojiRepository.categories(this)) if (!category.items.isEmpty()) return category;
        return null;
    }

    private HorizontalScrollView categoryBarForEmoji() {
        HorizontalScrollView scroll = baseHorizontalBar();
        LinearLayout row = (LinearLayout) scroll.getChildAt(0);
        for (EmojiRepository.Category category : EmojiRepository.categories(this)) {
            ImageButton b = iconButton(category.iconRes, category.title);
            b.setSelected(category.id.equals(emojiCategory));
            b.setOnClickListener(v -> { emojiCategory = category.id; renderEmojiPanel(); });
            row.addView(b, iconButtonParams());
        }
        return scroll;
    }

    private void showSymbolPanel() {
        commitComposing(false, "");
        if (!preparePanel()) return;
        renderSymbolPanel();
    }

    private void renderSymbolPanel() {
        panelContainer.removeAllViews();
        panelContainer.addView(categoryBarForSymbols(), new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(44)));
        SymbolRepository.Category selected = null;
        for (SymbolRepository.Category category : SymbolRepository.categories(this)) if (category.id.equals(symbolCategory)) selected = category;
        if (selected == null || selected.items.isEmpty()) {
            selected = findFirstNonEmptySymbolCategory();
            if (selected != null) symbolCategory = selected.id;
        }
        if (selected == null || selected.items.isEmpty()) {
            panelContainer.addView(messageView("Agrega símbolos personalizados desde Configuración o usa otras categorías."), matchWrap());
            return;
        }
        panelContainer.addView(gridForItems(selected.items, false), new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1));
    }

    private SymbolRepository.Category findFirstNonEmptySymbolCategory() {
        for (SymbolRepository.Category category : SymbolRepository.categories(this)) if (!category.items.isEmpty()) return category;
        return null;
    }

    private HorizontalScrollView categoryBarForSymbols() {
        HorizontalScrollView scroll = baseHorizontalBar();
        LinearLayout row = (LinearLayout) scroll.getChildAt(0);
        for (SymbolRepository.Category category : SymbolRepository.categories(this)) {
            ImageButton b = iconButton(category.iconRes, category.title);
            b.setSelected(category.id.equals(symbolCategory));
            b.setOnClickListener(v -> { symbolCategory = category.id; renderSymbolPanel(); });
            row.addView(b, iconButtonParams());
        }
        return scroll;
    }

    private ScrollView gridForItems(List<String> items, boolean emoji) {
        ScrollView scrollView = new ScrollView(this);
        scrollView.setFillViewport(false);
        GridLayout grid = new GridLayout(this);
        grid.setColumnCount(8);
        grid.setPadding(dp(6), dp(4), dp(6), dp(8));
        scrollView.addView(grid, new ScrollView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        for (String item : items) {
            TextView cell = new TextView(this);
            cell.setText(item);
            cell.setTextSize(TypedValue.COMPLEX_UNIT_SP, emoji ? 27 : 22);
            cell.setTextColor(palette.keyText);
            cell.setGravity(Gravity.CENTER);
            cell.setContentDescription((emoji ? "Emoji " : "Símbolo ") + item);
            GradientDrawable bg = new GradientDrawable();
            bg.setColor(palette.keyBackground);
            bg.setCornerRadius(dp(14));
            cell.setBackground(bg);
            cell.setOnClickListener(v -> {
                feedback();
                commitRawText(item);
                if (emoji) EmojiRepository.addRecent(this, item);
            });
            cell.setOnLongClickListener(v -> {
                feedback();
                if (emoji) {
                    EmojiRepository.toggleFavorite(this, item);
                    Toast.makeText(this, "Favorito actualizado", Toast.LENGTH_SHORT).show();
                } else {
                    SymbolRepository.addCustom(this, item);
                    Toast.makeText(this, "Símbolo agregado a personalizados", Toast.LENGTH_SHORT).show();
                }
                return true;
            });
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = dp(42);
            params.height = dp(42);
            params.setMargins(dp(3), dp(3), dp(3), dp(3));
            grid.addView(cell, params);
        }
        return scrollView;
    }

    private void showClipboardPanel() {
        commitComposing(false, "");
        if (!prefs.isClipboardEnabled() || passwordField) {
            showInfoPanel("Portapapeles desactivado", "Actívalo en Privacidad/Portapapeles. En campos de contraseña no se muestra por seguridad.", null);
            return;
        }
        ClipboardRepository.captureSystemClipboard(this);
        if (!preparePanel()) return;
        panelContainer.removeAllViews();
        LinearLayout header = new LinearLayout(this);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setPadding(dp(8), dp(3), dp(8), dp(3));
        TextView title = label("Portapapeles local", 15, true);
        header.addView(title, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        Button clear = smallButton("Limpiar");
        clear.setOnClickListener(v -> { ClipboardRepository.clear(this); showClipboardPanel(); });
        header.addView(clear);
        panelContainer.addView(header, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(42)));

        ScrollView scroll = new ScrollView(this);
        LinearLayout list = new LinearLayout(this);
        list.setOrientation(LinearLayout.VERTICAL);
        list.setPadding(dp(6), dp(2), dp(6), dp(8));
        scroll.addView(list);
        ArrayList<String> all = new ArrayList<>();
        all.addAll(ClipboardRepository.pinned(this));
        for (String item : ClipboardRepository.history(this)) if (!all.contains(item)) all.add(item);
        if (all.isEmpty()) {
            list.addView(messageView("Copia texto para verlo aquí. Todo se almacena solo en este dispositivo."), matchWrap());
        } else {
            for (String item : all) list.addView(clipboardRow(item));
        }
        panelContainer.addView(scroll, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1));
    }

    private View clipboardRow(String text) {
        LinearLayout row = new LinearLayout(this);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(0, dp(3), 0, dp(3));
        TextView content = label(singleLine(text), 14, false);
        content.setGravity(Gravity.CENTER_VERTICAL);
        content.setPadding(dp(10), 0, dp(10), 0);
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(palette.keyBackground);
        bg.setCornerRadius(dp(14));
        content.setBackground(bg);
        content.setOnClickListener(v -> { feedback(); commitRawText(text); ClipboardRepository.addHistory(this, text); });
        row.addView(content, new LinearLayout.LayoutParams(0, dp(42), 1));
        ImageButton pin = iconButton(R.drawable.ic_add, "Fijar");
        pin.setOnClickListener(v -> { ClipboardRepository.pin(this, text); showClipboardPanel(); });
        row.addView(pin, iconButtonParams());
        ImageButton del = iconButton(R.drawable.ic_delete, "Eliminar");
        del.setOnClickListener(v -> { ClipboardRepository.remove(this, text); showClipboardPanel(); });
        row.addView(del, iconButtonParams());
        return row;
    }

    private void showSelectionPanel() {
        commitComposing(false, "");
        if (!preparePanel()) return;
        panelContainer.removeAllViews();
        LinearLayout top = new LinearLayout(this);
        top.setGravity(Gravity.CENTER);
        top.setPadding(dp(6), dp(8), dp(6), dp(4));
        addSelectionButton(top, "Todo", R.drawable.ic_cursor, android.R.id.selectAll);
        addSelectionButton(top, "Copiar", R.drawable.ic_copy, android.R.id.copy);
        addSelectionButton(top, "Cortar", R.drawable.ic_cut, android.R.id.cut);
        addSelectionButton(top, "Pegar", R.drawable.ic_paste, android.R.id.paste);
        panelContainer.addView(top, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(64)));
        LinearLayout cursor = new LinearLayout(this);
        cursor.setGravity(Gravity.CENTER);
        Button left = smallButton("← Cursor");
        left.setOnClickListener(v -> moveCursor(-1, 1));
        Button right = smallButton("Cursor →");
        right.setOnClickListener(v -> moveCursor(1, 1));
        Button wordDel = smallButton("Borrar palabra");
        wordDel.setOnClickListener(v -> deleteWordBeforeCursor());
        cursor.addView(left, new LinearLayout.LayoutParams(0, dp(44), 1));
        cursor.addView(right, new LinearLayout.LayoutParams(0, dp(44), 1));
        cursor.addView(wordDel, new LinearLayout.LayoutParams(0, dp(44), 1));
        panelContainer.addView(cursor, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(56)));
        panelContainer.addView(messageView("Desliza en ESPACIO para mover el cursor. Las acciones usan InputConnection/menú contextual del editor activo."), matchWrap());
    }

    private void addSelectionButton(LinearLayout parent, String label, int iconRes, int menuId) {
        ImageButton b = iconButton(iconRes, label);
        b.setOnClickListener(v -> {
            InputConnection ic = getCurrentInputConnection();
            if (ic != null) {
                ic.performContextMenuAction(menuId);
                if (menuId == android.R.id.copy || menuId == android.R.id.cut || menuId == android.R.id.paste) {
                    panelContainer.postDelayed(() -> ClipboardRepository.captureSystemClipboard(this), 250);
                }
            }
        });
        parent.addView(b, new LinearLayout.LayoutParams(dp(54), dp(54)));
    }

    private void showSearchPanel() {
        commitComposing(false, "");
        showInfoPanel("Búsqueda", "Selecciona texto o sitúa el cursor tras una palabra y pulsa Abrir búsqueda. Solo se enviará al navegador si confirmas tocando el botón.", v -> {
            String q = selectedOrPreviousText();
            if (q.length() == 0) q = "";
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/search?q=" + URLEncoder.encode(q, "UTF-8")));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            } catch (Exception ignored) {}
        });
    }

    private void showTranslatePanel() {
        commitComposing(false, "");
        showInfoPanel("Traducir", "Por privacidad no se envía texto automáticamente a servicios externos. Puedes abrir traducción web manualmente; cualquier envío requiere tu acción explícita.", v -> {
            String q = selectedOrPreviousText();
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://translate.google.com/?sl=auto&tl=es&text=" + URLEncoder.encode(q, "UTF-8") + "&op=translate"));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            } catch (Exception ignored) {}
        });
    }

    private void showGifStickerPanel() {
        showInfoPanel("GIFs y stickers", "Panel preparado para paquetes locales. No se descargan recursos ni se contactan servidores sin consentimiento. Usa Configuración para añadir acciones personalizadas.", null);
    }

    private void showInfoPanel(String title, String message, View.OnClickListener positive) {
        if (!preparePanel()) return;
        panelContainer.removeAllViews();
        panelContainer.setPadding(dp(12), dp(10), dp(12), dp(10));
        panelContainer.addView(label(title, 17, true), matchWrap());
        panelContainer.addView(messageView(message), matchWrap());
        if (positive != null) {
            Button button = smallButton("Abrir con consentimiento");
            button.setOnClickListener(positive);
            panelContainer.addView(button, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(46)));
        }
    }

    private boolean preparePanel() {
        if (panelContainer == null) return false;
        panelContainer.setPadding(0, 0, 0, 0);
        panelContainer.setVisibility(View.VISIBLE);
        return true;
    }

    private void hidePanel() {
        if (panelContainer != null) panelContainer.setVisibility(View.GONE);
    }

    private HorizontalScrollView baseHorizontalBar() {
        HorizontalScrollView scroll = new HorizontalScrollView(this);
        scroll.setHorizontalScrollBarEnabled(false);
        LinearLayout row = new LinearLayout(this);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(6), dp(4), dp(6), dp(4));
        scroll.addView(row, new HorizontalScrollView.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT));
        return scroll;
    }

    private ImageButton iconButton(int iconRes, String description) {
        ImageButton b = new ImageButton(this);
        b.setImageResource(iconRes);
        b.setColorFilter(palette.keyText, PorterDuff.Mode.SRC_IN);
        b.setPadding(dp(10), dp(10), dp(10), dp(10));
        b.setContentDescription(description);
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(palette.keyBackground);
        bg.setCornerRadius(dp(15));
        b.setBackground(bg);
        return b;
    }

    private LinearLayout.LayoutParams iconButtonParams() {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dp(42), dp(42));
        params.setMargins(dp(3), 0, dp(3), 0);
        return params;
    }

    private TextView label(String text, int sp, boolean bold) {
        TextView view = new TextView(this);
        view.setText(text);
        view.setTextSize(TypedValue.COMPLEX_UNIT_SP, sp);
        view.setTextColor(palette.keyText);
        view.setGravity(Gravity.CENTER_VERTICAL);
        if (bold) view.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        return view;
    }

    private TextView messageView(String text) {
        TextView view = label(text, 14, false);
        view.setPadding(dp(10), dp(8), dp(10), dp(8));
        view.setLineSpacing(0, 1.08f);
        return view;
    }

    private Button smallButton(String text) {
        Button b = new Button(this);
        b.setAllCaps(false);
        b.setText(text);
        b.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        b.setTextColor(palette.specialKeyText);
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(palette.specialKeyBackground);
        bg.setCornerRadius(dp(14));
        b.setBackground(bg);
        return b;
    }

    private LinearLayout.LayoutParams matchWrap() {
        return new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    private String singleLine(String text) {
        String s = text.replace('\n', ' ').replace('\r', ' ');
        return s.length() > 120 ? s.substring(0, 120) + "…" : s;
    }

    private void moveCursor(int direction, int count) {
        int keyCode = direction < 0 ? KeyEvent.KEYCODE_DPAD_LEFT : KeyEvent.KEYCODE_DPAD_RIGHT;
        for (int i = 0; i < count; i++) sendDownUpKeyEvents(keyCode);
    }

    private void deleteWordBeforeCursor() {
        InputConnection ic = getCurrentInputConnection();
        if (ic == null) return;
        CharSequence before = ic.getTextBeforeCursor(80, 0);
        if (TextUtils.isEmpty(before)) {
            handleBackspace();
            return;
        }
        int end = before.length();
        int index = end;
        while (index > 0 && Character.isWhitespace(before.charAt(index - 1))) index--;
        while (index > 0 && !Character.isWhitespace(before.charAt(index - 1))) index--;
        int delete = end - index;
        if (delete <= 0) delete = 1;
        ic.deleteSurroundingText(delete, 0);
        composing.setLength(0);
        updateSuggestions();
    }

    private String selectedOrPreviousText() {
        InputConnection ic = getCurrentInputConnection();
        if (ic == null) return "";
        CharSequence selected = ic.getSelectedText(0);
        if (!TextUtils.isEmpty(selected)) return selected.toString();
        CharSequence before = ic.getTextBeforeCursor(60, 0);
        if (TextUtils.isEmpty(before)) return "";
        String s = before.toString().trim();
        int last = Math.max(s.lastIndexOf(' '), Math.max(s.lastIndexOf('\n'), s.lastIndexOf('\t')));
        return last >= 0 ? s.substring(last + 1) : s;
    }

    private void toggleLanguage() {
        String lang = prefs.getLanguage().toLowerCase(Locale.US).startsWith("es") ? "en" : "es";
        prefs.setLanguage(lang);
        Toast.makeText(this, lang.equals("es") ? "Español" : "English", Toast.LENGTH_SHORT).show();
        rebuildAllViews();
    }

    private void openSettings() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    @SuppressWarnings("unused")
    private void openSystemImePicker() {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (imm != null) imm.showInputMethodPicker();
    }

    private KeyboardMode detectInitialMode(EditorInfo info) {
        int inputType = info == null ? 0 : info.inputType;
        int cls = inputType & InputType.TYPE_MASK_CLASS;
        if (cls == InputType.TYPE_CLASS_NUMBER || cls == InputType.TYPE_CLASS_PHONE || cls == InputType.TYPE_CLASS_DATETIME) {
            return KeyboardMode.NUMBERS;
        }
        return KeyboardMode.LETTERS;
    }

    private boolean shouldStartShifted(EditorInfo info) {
        InputConnection ic = getCurrentInputConnection();
        if (ic == null || info == null) return false;
        int caps = ic.getCursorCapsMode(info.inputType);
        return caps != 0;
    }

    private boolean isPasswordInput(int inputType) {
        int cls = inputType & InputType.TYPE_MASK_CLASS;
        int variation = inputType & InputType.TYPE_MASK_VARIATION;
        if (cls == InputType.TYPE_CLASS_TEXT) {
            return variation == InputType.TYPE_TEXT_VARIATION_PASSWORD
                    || variation == InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                    || variation == InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD;
        }
        if (cls == InputType.TYPE_CLASS_NUMBER) {
            return variation == InputType.TYPE_NUMBER_VARIATION_PASSWORD;
        }
        return false;
    }

    private boolean isNoSuggestionsField(int inputType) {
        int variation = inputType & InputType.TYPE_MASK_VARIATION;
        return (inputType & InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS) != 0
                || variation == InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
                || variation == InputType.TYPE_TEXT_VARIATION_URI
                || variation == InputType.TYPE_TEXT_VARIATION_WEB_EMAIL_ADDRESS;
    }

    private int applyBackgroundControls(int color) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[1] = clampFloat(hsv[1] * prefs.getBackgroundSaturation() / 100f, 0f, 1f);
        hsv[2] = clampFloat(hsv[2] * prefs.getBackgroundBrightness() / 100f, 0f, 1f);
        int alpha = Math.max(20, Math.min(100, prefs.getBackgroundOpacity())) * 255 / 100;
        return Color.HSVToColor(alpha, hsv);
    }

    private float clampFloat(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    private int dp(float value) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, getResources().getDisplayMetrics());
    }
}
