package com.arena.customkeyboard;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.provider.Settings;
import android.text.InputType;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.arena.customkeyboard.emoji.SymbolRepository;
import com.arena.customkeyboard.ime.KeyView;
import com.arena.customkeyboard.ime.KeyboardSurface;
import com.arena.customkeyboard.model.KeySpec;
import com.arena.customkeyboard.model.KeyboardLayout;
import com.arena.customkeyboard.model.KeyboardMode;
import com.arena.customkeyboard.model.ToolbarAction;
import com.arena.customkeyboard.model.ToolbarActionRegistry;
import com.arena.customkeyboard.settings.KeyboardPreferences;
import com.arena.customkeyboard.settings.KeyboardTheme;
import com.arena.customkeyboard.settings.ThemePalette;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends Activity implements KeyView.KeyInteractionListener {
    private KeyboardPreferences prefs;
    private ThemePalette palette;
    private LinearLayout content;
    private KeyboardSurface previewKeyboard;
    private TextView previewEditor;
    private EditText exportBox;
    private final StringBuilder previewText = new StringBuilder("hola");
    private KeyboardMode previewMode = KeyboardMode.LETTERS;
    private boolean previewShifted;
    private boolean previewCaps;

    private interface BoolSetter { void set(boolean value); }
    private interface IntSetter { void set(int value); }
    private interface TextSetter { void set(String value); }

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = new KeyboardPreferences(this);
        palette = KeyboardTheme.resolve(prefs);
        buildScreen();
    }

    @Override protected void onResume() {
        super.onResume();
        prefs = new KeyboardPreferences(this);
        palette = KeyboardTheme.resolve(prefs);
        renderPreview();
    }

    private void buildScreen() {
        palette = KeyboardTheme.resolve(prefs);
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(false);
        scroll.setBackgroundColor(palette.background);
        content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(16), dp(18), dp(16), dp(28));
        scroll.addView(content, new ScrollView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        setContentView(scroll);

        addHeader();
        addPreview();
        addAppearanceSection();
        addKeysSection();
        addColorsSection();
        addTypographySection();
        addAnimationSection();
        addSoundVibrationSection();
        addToolbarSection();
        addLayoutSection();
        addGestureSection();
        addLanguageSection();
        addSuggestionSection();
        addClipboardSection();
        addEmojiSymbolSection();
        addProfilesSection();
        addPrivacySection();
        addAdvancedSection();
        addExportImportSection();
    }

    private void addHeader() {
        TextView title = text("TECLADO", 30, true);
        title.setLetterSpacing(0.06f);
        content.addView(title, matchWrap());
        TextView subtitle = text("IME real, local y personalizable inspirado en una experiencia Samsung moderna con recursos propios.", 14, false);
        subtitle.setAlpha(0.76f);
        subtitle.setPadding(0, dp(4), 0, dp(12));
        content.addView(subtitle, matchWrap());
        LinearLayout actions = new LinearLayout(this);
        actions.setOrientation(LinearLayout.HORIZONTAL);
        actions.setGravity(Gravity.CENTER);
        Button enable = pillButton("Activar teclado");
        enable.setOnClickListener(v -> startActivity(new Intent(Settings.ACTION_INPUT_METHOD_SETTINGS)));
        Button choose = pillButton("Seleccionar teclado");
        choose.setOnClickListener(v -> {
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            if (imm != null) imm.showInputMethodPicker();
        });
        actions.addView(enable, new LinearLayout.LayoutParams(0, dp(48), 1));
        actions.addView(choose, new LinearLayout.LayoutParams(0, dp(48), 1));
        content.addView(actions, matchWrapWithBottom(14));
    }

    private void addPreview() {
        addSectionTitle("Vista previa en tiempo real", "Prueba teclas, temas, tamaños, radios, idioma y distribución sin salir de la app.");
        LinearLayout card = card();
        previewEditor = text("", 18, false);
        previewEditor.setMinHeight(dp(46));
        previewEditor.setGravity(Gravity.CENTER_VERTICAL);
        previewEditor.setPadding(dp(12), 0, dp(12), 0);
        GradientDrawable editorBg = rounded(palette.keyBackground, dp(16));
        editorBg.setStroke(dp(1), palette.border);
        previewEditor.setBackground(editorBg);
        card.addView(previewEditor, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(52)));
        previewKeyboard = new KeyboardSurface(this);
        card.addView(previewKeyboard, matchWrap());
        content.addView(card, matchWrapWithBottom(18));
        renderPreview();
    }

    private void addAppearanceSection() {
        addSectionTitle("Apariencia", "Temas predeterminados, fondos sólidos/degradados y aspecto general.");
        LinearLayout card = card();
        for (ThemePalette theme : KeyboardTheme.presets()) {
            Button b = pillButton(theme.name);
            b.setOnClickListener(v -> { prefs.setThemeId(theme.id); palette = KeyboardTheme.resolve(prefs); buildScreen(); });
            card.addView(b, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(44)));
        }
        Button custom = pillButton("Custom / Crear tema");
        custom.setOnClickListener(v -> { prefs.setThemeId(KeyboardTheme.CUSTOM); palette = KeyboardTheme.resolve(prefs); buildScreen(); });
        card.addView(custom, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(44)));
        addSlider(card, "Opacidad de fondo", prefs.getBackgroundOpacity(), 20, 100, value -> { prefs.setBackgroundOpacity(value); renderPreview(); });
        addSlider(card, "Brillo", prefs.getBackgroundBrightness(), 30, 160, value -> { prefs.setBackgroundBrightness(value); renderPreview(); });
        addSlider(card, "Saturación", prefs.getBackgroundSaturation(), 0, 200, value -> { prefs.setBackgroundSaturation(value); renderPreview(); });
        addSlider(card, "Blur preparado", prefs.getBackgroundBlur(), 0, 30, value -> prefs.setBackgroundBlur(value));
        content.addView(card, matchWrapWithBottom(18));
    }

    private void addKeysSection() {
        addSectionTitle("Teclas", "Altura, separación, radio, texto, transparencia, sombra suave y escalas por tecla especial.");
        LinearLayout card = card();
        addSlider(card, "Altura", prefs.getKeyHeightDp(), 34, 76, value -> { prefs.setKeyHeightDp(value); renderPreview(); });
        addSlider(card, "Separación", prefs.getKeyGapDp(), 0, 12, value -> { prefs.setKeyGapDp(value); renderPreview(); });
        addSlider(card, "Radio de esquinas", prefs.getKeyRadiusDp(), 0, 30, value -> { prefs.setKeyRadiusDp(value); renderPreview(); });
        addSlider(card, "Tamaño de texto", prefs.getKeyTextSizeSp(), 12, 30, value -> { prefs.setKeyTextSizeSp(value); renderPreview(); });
        addSlider(card, "Escala de teclas especiales", prefs.getSpecialKeyScale(), 70, 140, value -> { prefs.setSpecialKeyScale(value); renderPreview(); });
        addSwitch(card, "Alto contraste", prefs.isHighContrastEnabled(), value -> { prefs.setHighContrastEnabled(value); renderPreview(); });
        content.addView(card, matchWrapWithBottom(18));
    }

    private void addColorsSection() {
        addSectionTitle("Colores", "Crea tu tema por HEX, RGB/HSL simplificado y paletas rápidas. Se exporta como JSON .keyboardtheme.");
        LinearLayout card = card();
        addHexRow(card, "Fondo", prefs.getCustomBackgroundColor(), value -> { prefs.setThemeId(KeyboardTheme.CUSTOM); prefs.setCustomBackgroundColor(value); renderPreview(); });
        addHexRow(card, "Teclas", prefs.getCustomKeyColor(), value -> { prefs.setThemeId(KeyboardTheme.CUSTOM); prefs.setCustomKeyColor(value); renderPreview(); });
        addHexRow(card, "Especiales", prefs.getCustomSpecialKeyColor(), value -> { prefs.setThemeId(KeyboardTheme.CUSTOM); prefs.setCustomSpecialKeyColor(value); renderPreview(); });
        addHexRow(card, "Texto", prefs.getCustomTextColor(), value -> { prefs.setThemeId(KeyboardTheme.CUSTOM); prefs.setCustomTextColor(value); renderPreview(); });
        addHexRow(card, "Barra superior", prefs.getCustomToolbarColor(), value -> { prefs.setThemeId(KeyboardTheme.CUSTOM); prefs.setCustomToolbarColor(value); renderPreview(); });
        addHexRow(card, "Acento", prefs.getCustomAccentColor(), value -> { prefs.setThemeId(KeyboardTheme.CUSTOM); prefs.setCustomAccentColor(value); renderPreview(); });
        addHslPicker(card);
        addSwatches(card);
        content.addView(card, matchWrapWithBottom(18));
    }

    private void addTypographySection() {
        addSectionTitle("Tipografía", "La UI usa sans nativa para rendimiento; el tamaño se ajusta en tiempo real.");
        LinearLayout card = card();
        addSlider(card, "Tamaño de letras", prefs.getKeyTextSizeSp(), 12, 30, value -> { prefs.setKeyTextSizeSp(value); renderPreview(); });
        TextView info = text("Posición centrada, contraste automático y contenido accesible para TalkBack.", 14, false);
        card.addView(info, matchWrap());
        content.addView(card, matchWrapWithBottom(18));
    }

    private void addAnimationSection() {
        addSectionTitle("Animaciones", "Scale, highlight y feedback ultrarrápido para no perder FPS.");
        LinearLayout card = card();
        addSwitch(card, "Animaciones ON/OFF", prefs.isAnimationsEnabled(), value -> { prefs.setAnimationsEnabled(value); renderPreview(); });
        addSlider(card, "Velocidad 0.5x–2x", prefs.getAnimationSpeedPercent(), 50, 200, prefs::setAnimationSpeedPercent);
        content.addView(card, matchWrapWithBottom(18));
    }

    private void addSoundVibrationSection() {
        addSectionTitle("Sonido y vibración", "Perfiles de feedback táctil y acústico con volumen/intensidad.");
        LinearLayout card = card();
        addSwitch(card, "Sonido al pulsar", prefs.isSoundEnabled(), prefs::setSoundEnabled);
        addSlider(card, "Volumen", prefs.getSoundVolume(), 0, 100, prefs::setSoundVolume);
        addSwitch(card, "Vibración", prefs.isVibrationEnabled(), prefs::setVibrationEnabled);
        addSlider(card, "Intensidad", prefs.getVibrationStrength(), 0, 100, prefs::setVibrationStrength);
        addSlider(card, "Duración ms", prefs.getVibrationDurationMs(), 5, 60, prefs::setVibrationDurationMs);
        content.addView(card, matchWrapWithBottom(18));
    }

    private void addToolbarSection() {
        addSectionTitle("Barra superior", "Activa/desactiva botones y cambia el orden con perfiles de ejemplo. Sin emojis como iconos de interfaz.");
        LinearLayout card = card();
        Button samsung = pillButton("Orden Samsung-like: GIF · Emoji · Portapapeles · Ajustes");
        samsung.setOnClickListener(v -> { prefs.setToolbarActions(ToolbarActionRegistry.defaultSamsungLikeOrder()); Toast.makeText(this, "Orden aplicado", Toast.LENGTH_SHORT).show(); });
        card.addView(samsung, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(44)));
        Button ai = pillButton("Orden IA/Traducir/GIF/Emoji");
        ai.setOnClickListener(v -> { prefs.setToolbarActions(ToolbarActionRegistry.aiOrder()); Toast.makeText(this, "Orden aplicado", Toast.LENGTH_SHORT).show(); });
        card.addView(ai, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(44)));
        addSlider(card, "Tamaño de toolbar", prefs.getToolbarSizeDp(), 32, 60, prefs::setToolbarSizeDp);
        for (ToolbarAction action : ToolbarActionRegistry.all()) {
            CheckBox check = new CheckBox(this);
            check.setText(action.title);
            check.setTextColor(palette.keyText);
            check.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
            check.setButtonTintList(android.content.res.ColorStateList.valueOf(palette.accent));
            check.setChecked(prefs.getToolbarActions().contains(action.id));
            check.setOnCheckedChangeListener((buttonView, isChecked) -> updateToolbarAction(action.id, isChecked));
            card.addView(check, matchWrap());
        }
        content.addView(card, matchWrapWithBottom(18));
    }

    private void addLayoutSection() {
        addSectionTitle("Distribución / Editor visual", "QWERTY español con Ñ automática, inglés sin Ñ, números, símbolos y modo programador.");
        LinearLayout card = card();
        addSlider(card, "Ancho de espacio", prefs.getSpaceWidthScale(), 60, 160, value -> { prefs.setSpaceWidthScale(value); renderPreview(); });
        LinearLayout modes = new LinearLayout(this);
        modes.setOrientation(LinearLayout.HORIZONTAL);
        Button abc = pillButton("ABC");
        abc.setOnClickListener(v -> { previewMode = KeyboardMode.LETTERS; renderPreview(); });
        Button num = pillButton("123");
        num.setOnClickListener(v -> { previewMode = KeyboardMode.NUMBERS; renderPreview(); });
        Button sym = pillButton("Símbolos");
        sym.setOnClickListener(v -> { previewMode = KeyboardMode.SYMBOLS; renderPreview(); });
        Button dev = pillButton("Programador");
        dev.setOnClickListener(v -> { previewMode = KeyboardMode.PROGRAMMER; renderPreview(); });
        modes.addView(abc, new LinearLayout.LayoutParams(0, dp(42), 1));
        modes.addView(num, new LinearLayout.LayoutParams(0, dp(42), 1));
        modes.addView(sym, new LinearLayout.LayoutParams(0, dp(42), 1));
        modes.addView(dev, new LinearLayout.LayoutParams(0, dp(42), 1));
        card.addView(modes, matchWrap());
        Button save = pillButton("GUARDAR DISEÑO");
        save.setOnClickListener(v -> Toast.makeText(this, "Diseño guardado en preferencias", Toast.LENGTH_SHORT).show());
        Button reset = pillButton("RESTABLECER DISEÑO");
        reset.setOnClickListener(v -> { prefs.setSpaceWidthScale(100); prefs.setKeyHeightDp(50); prefs.setKeyGapDp(4); prefs.setKeyRadiusDp(14); buildScreen(); });
        card.addView(save, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(44)));
        card.addView(reset, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(44)));
        content.addView(card, matchWrapWithBottom(18));
    }

    private void addGestureSection() {
        addSectionTitle("Gestos", "Reasigna interacciones: espacio cursor, borrar palabras, swipe mayúsculas/minúsculas y long press.");
        LinearLayout card = card();
        addSwitch(card, "Deslizar sobre espacio → mover cursor", prefs.isSpaceCursorGestureEnabled(), prefs::setSpaceCursorGestureEnabled);
        addSwitch(card, "Deslizar sobre borrar → borrar palabras", prefs.isBackspaceWordGestureEnabled(), prefs::setBackspaceWordGestureEnabled);
        addSwitch(card, "Deslizar arriba/abajo → mayúsculas/minúsculas", prefs.isUppercaseGestureEnabled(), prefs::setUppercaseGestureEnabled);
        TextView info = text("Long press: A/E/I/O/U/N muestran acentos y caracteres alternativos con popup propio.", 14, false);
        card.addView(info, matchWrap());
        content.addView(card, matchWrapWithBottom(18));
    }

    private void addLanguageSection() {
        addSectionTitle("Idiomas", "El layout se adapta a español, inglés y deja preparada la ampliación a otros idiomas.");
        LinearLayout card = card();
        RadioGroup group = new RadioGroup(this);
        group.setOrientation(RadioGroup.HORIZONTAL);
        Button es = pillButton("Español Ñ");
        es.setOnClickListener(v -> { prefs.setLanguage("es"); renderPreview(); });
        Button en = pillButton("English");
        en.setOnClickListener(v -> { prefs.setLanguage("en"); renderPreview(); });
        card.addView(es, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(44)));
        card.addView(en, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(44)));
        content.addView(card, matchWrapWithBottom(18));
    }

    private void addSuggestionSection() {
        addSectionTitle("Sugerencias", "Motor local sin red: predicción básica, autocorrección y aprendizaje personal opcional.");
        LinearLayout card = card();
        addSwitch(card, "Sugerencias", prefs.isSuggestionsEnabled(), prefs::setSuggestionsEnabled);
        addSlider(card, "Cantidad de sugerencias", prefs.getSuggestionCount(), 0, 5, prefs::setSuggestionCount);
        addSwitch(card, "Autocorrección", prefs.isAutocorrectEnabled(), prefs::setAutocorrectEnabled);
        addSwitch(card, "Aprendizaje personal local", prefs.isLearningEnabled(), prefs::setLearningEnabled);
        content.addView(card, matchWrapWithBottom(18));
    }

    private void addClipboardSection() {
        addSectionTitle("Portapapeles", "Historial, favoritos, fijar, copiar, eliminar y opción crítica para desactivarlo.");
        LinearLayout card = card();
        addSwitch(card, "DESACTIVAR/ACTIVAR PORTAPAPELES", prefs.isClipboardEnabled(), prefs::setClipboardEnabled);
        TextView info = text("Si está desactivado, el botón desaparece de la interfaz del teclado. En contraseñas no se muestra.", 14, false);
        card.addView(info, matchWrap());
        content.addView(card, matchWrapWithBottom(18));
    }

    private void addEmojiSymbolSection() {
        addSectionTitle("Emojis y símbolos", "Emojis por categorías con navegación vectorial y sección de símbolos independiente.");
        LinearLayout card = card();
        EditText symbol = input("Agregar símbolo personalizado", "⌘");
        Button add = pillButton("Agregar símbolo");
        add.setOnClickListener(v -> { SymbolRepository.addCustom(this, symbol.getText().toString()); Toast.makeText(this, "Símbolo agregado", Toast.LENGTH_SHORT).show(); });
        card.addView(symbol, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(52)));
        card.addView(add, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(44)));
        TextView info = text("Categorías: personas, animales, comida, actividades, viajes, objetos, símbolos y banderas. Símbolos: matemáticos, flechas, geométricos, monedas, técnicos, decorativos, tipográficos y letras especiales.", 14, false);
        card.addView(info, matchWrap());
        content.addView(card, matchWrapWithBottom(18));
    }

    private void addProfilesSection() {
        addSectionTitle("Perfiles", "Trabajo, Gaming, Minimal, Programación, Emoji y Personal aplican tema/tamaño/distribución.");
        LinearLayout card = card();
        addProfileButton(card, "Trabajo", KeyboardTheme.SAMSUNG_LIGHT, "es", 48, KeyboardMode.LETTERS);
        addProfileButton(card, "Gaming", KeyboardTheme.MIDNIGHT, "en", 52, KeyboardMode.NUMBERS);
        addProfileButton(card, "Minimal", KeyboardTheme.MINIMAL, "es", 46, KeyboardMode.LETTERS);
        addProfileButton(card, "Programación", KeyboardTheme.AMOLED, "en", 44, KeyboardMode.PROGRAMMER);
        addProfileButton(card, "Emoji", KeyboardTheme.GLASS, "es", 50, KeyboardMode.SYMBOLS);
        content.addView(card, matchWrapWithBottom(18));
    }

    private void addPrivacySection() {
        addSectionTitle("Privacidad", "Crítica para un teclado: no hay INTERNET permission ni envío de pulsaciones.");
        LinearLayout card = card();
        TextView privacy = text("Datos locales: preferencias, temas, símbolos personalizados, emojis recientes/favoritos, portapapeles si está activo y diccionario personal si lo activas. No se recopilan conversaciones, contraseñas ni pulsaciones en servidores. Funciones de búsqueda/traducción solo abren el navegador tras toque explícito.", 14, false);
        privacy.setLineSpacing(0, 1.12f);
        card.addView(privacy, matchWrap());
        content.addView(card, matchWrapWithBottom(18));
    }

    private void addAdvancedSection() {
        addSectionTitle("Avanzado", "Preparado para Android moderno, rendimiento y accesibilidad.");
        LinearLayout card = card();
        TextView info = text("Arquitectura separada: IME, motor, temas, settings, emojis, portapapeles, sugerencias, gestos y almacenamiento. Teclas con áreas táctiles mínimas, contentDescription y alto contraste.", 14, false);
        info.setLineSpacing(0, 1.12f);
        card.addView(info, matchWrap());
        content.addView(card, matchWrapWithBottom(18));
    }

    private void addExportImportSection() {
        addSectionTitle("Exportar / Importar", "Comparte configuración como JSON compatible con extensión .keyboardtheme.");
        LinearLayout card = card();
        exportBox = input("JSON .keyboardtheme", "");
        exportBox.setMinLines(5);
        exportBox.setGravity(Gravity.TOP | Gravity.START);
        card.addView(exportBox, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(150)));
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        Button export = pillButton("EXPORTAR");
        export.setOnClickListener(v -> exportConfig());
        Button importBtn = pillButton("IMPORTAR");
        importBtn.setOnClickListener(v -> importConfig());
        Button share = pillButton("COMPARTIR");
        share.setOnClickListener(v -> shareConfig());
        row.addView(export, new LinearLayout.LayoutParams(0, dp(44), 1));
        row.addView(importBtn, new LinearLayout.LayoutParams(0, dp(44), 1));
        row.addView(share, new LinearLayout.LayoutParams(0, dp(44), 1));
        card.addView(row, matchWrap());
        content.addView(card, matchWrapWithBottom(18));
    }

    private void renderPreview() {
        if (previewKeyboard == null || previewEditor == null) return;
        palette = KeyboardTheme.resolve(prefs);
        previewEditor.setText(previewText.toString());
        previewEditor.setTextColor(palette.keyText);
        previewKeyboard.render(KeyboardLayout.forMode(previewMode, prefs.getLanguage(), previewShifted || previewCaps, previewCaps), palette, prefs, this);
    }

    @Override public void onKeyPress(KeySpec key) {
        if (key == null) return;
        if (key.type == KeySpec.TYPE_TEXT) {
            previewText.append(key.output);
            if (previewShifted && !previewCaps) previewShifted = false;
        } else if (KeyboardLayout.ACTION_BACKSPACE.equals(key.action)) {
            if (previewText.length() > 0) previewText.deleteCharAt(previewText.length() - 1);
        } else if (KeyboardLayout.ACTION_SPACE.equals(key.action)) previewText.append(' ');
        else if (KeyboardLayout.ACTION_ENTER.equals(key.action)) previewText.append('\n');
        else if (KeyboardLayout.ACTION_SHIFT.equals(key.action)) { previewShifted = !previewShifted; previewCaps = false; }
        else if (KeyboardLayout.ACTION_MODE_NUMBERS.equals(key.action)) previewMode = KeyboardMode.NUMBERS;
        else if (KeyboardLayout.ACTION_MODE_LETTERS.equals(key.action)) previewMode = KeyboardMode.LETTERS;
        else if (KeyboardLayout.ACTION_MODE_SYMBOLS.equals(key.action)) previewMode = KeyboardMode.SYMBOLS;
        else if (KeyboardLayout.ACTION_MODE_PROGRAMMER.equals(key.action)) previewMode = KeyboardMode.PROGRAMMER;
        else if (KeyboardLayout.ACTION_TAB.equals(key.action)) previewText.append('\t');
        renderPreview();
    }

    @Override public boolean onKeyLongPress(KeyView view, KeySpec key) {
        if (key != null && key.alternatives != null && !key.alternatives.isEmpty()) {
            previewText.append(key.alternatives.get(0));
            renderPreview();
            return true;
        }
        return false;
    }

    @Override public void onKeyGesture(KeySpec key, float dx, float dy) {
        if (dy < -Math.abs(dx)) previewShifted = true;
        else if (dy > Math.abs(dx)) previewShifted = false;
        else onKeyPress(key);
        renderPreview();
    }

    private void addSectionTitle(String title, String description) {
        TextView t = text(title, 20, true);
        t.setPadding(0, dp(14), 0, dp(2));
        content.addView(t, matchWrap());
        TextView d = text(description, 13, false);
        d.setAlpha(0.72f);
        d.setPadding(0, 0, 0, dp(8));
        content.addView(d, matchWrap());
    }

    private void addSwitch(LinearLayout parent, String label, boolean checked, BoolSetter setter) {
        Switch sw = new Switch(this);
        sw.setText(label);
        sw.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        sw.setTextColor(palette.keyText);
        sw.setPadding(0, dp(4), 0, dp(4));
        sw.setChecked(checked);
        sw.setOnCheckedChangeListener((buttonView, isChecked) -> setter.set(isChecked));
        parent.addView(sw, matchWrap());
    }

    private void addSlider(LinearLayout parent, String label, int value, int min, int max, IntSetter setter) {
        TextView t = text(label + ": " + value, 14, false);
        t.setPadding(0, dp(6), 0, 0);
        SeekBar bar = new SeekBar(this);
        bar.setMax(max - min);
        bar.setProgress(value - min);
        bar.getProgressDrawable().setColorFilter(palette.accent, PorterDuff.Mode.SRC_IN);
        bar.getThumb().setColorFilter(palette.accent, PorterDuff.Mode.SRC_IN);
        bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int v = min + progress;
                t.setText(label + ": " + v);
                setter.set(v);
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        parent.addView(t, matchWrap());
        parent.addView(bar, matchWrap());
    }

    private void addHexRow(LinearLayout parent, String label, int color, IntSetter setter) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        TextView l = text(label, 14, true);
        EditText hex = input("#RRGGBB", KeyboardPreferences.colorToHex(color));
        Button apply = pillButton("OK");
        apply.setOnClickListener(v -> {
            setter.set(KeyboardPreferences.parseColor(hex.getText().toString(), color));
            palette = KeyboardTheme.resolve(prefs);
            renderPreview();
        });
        row.addView(l, new LinearLayout.LayoutParams(0, dp(48), 0.9f));
        row.addView(hex, new LinearLayout.LayoutParams(0, dp(48), 1.4f));
        row.addView(apply, new LinearLayout.LayoutParams(0, dp(44), 0.7f));
        parent.addView(row, matchWrap());
    }

    private void addHslPicker(LinearLayout parent) {
        TextView title = text("Selector HSL para acento", 15, true);
        title.setPadding(0, dp(10), 0, 0);
        parent.addView(title, matchWrap());
        final int[] hue = {220};
        final int[] sat = {85};
        final int[] light = {55};
        IntSetter apply = ignored -> {
            float[] hsv = new float[]{hue[0], sat[0] / 100f, light[0] / 100f};
            prefs.setThemeId(KeyboardTheme.CUSTOM);
            prefs.setCustomAccentColor(Color.HSVToColor(hsv));
            renderPreview();
        };
        addSlider(parent, "H", hue[0], 0, 360, value -> { hue[0] = value; apply.set(value); });
        addSlider(parent, "S", sat[0], 0, 100, value -> { sat[0] = value; apply.set(value); });
        addSlider(parent, "L", light[0], 0, 100, value -> { light[0] = value; apply.set(value); });
    }

    private void addSwatches(LinearLayout parent) {
        HorizontalScrollView scroll = new HorizontalScrollView(this);
        LinearLayout row = new LinearLayout(this);
        row.setPadding(0, dp(8), 0, dp(4));
        scroll.addView(row);
        int[] colors = {0xFF4F7DFF, 0xFF00BFA6, 0xFFFF4D6D, 0xFFFFB703, 0xFF8A5CFF, 0xFF00C2FF, 0xFF111827};
        for (int c : colors) {
            View swatch = new View(this);
            GradientDrawable bg = rounded(c, dp(18));
            bg.setStroke(dp(1), palette.border);
            swatch.setBackground(bg);
            swatch.setOnClickListener(v -> { prefs.setThemeId(KeyboardTheme.CUSTOM); prefs.setCustomAccentColor(c); renderPreview(); });
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dp(42), dp(42));
            params.setMargins(0, 0, dp(8), 0);
            row.addView(swatch, params);
        }
        parent.addView(scroll, matchWrap());
    }

    private void updateToolbarAction(String id, boolean enabled) {
        List<String> ids = new ArrayList<>(prefs.getToolbarActions());
        if (enabled && !ids.contains(id)) ids.add(id);
        if (!enabled) ids.remove(id);
        prefs.setToolbarActions(ids);
    }

    private void addProfileButton(LinearLayout card, String name, String theme, String language, int height, KeyboardMode mode) {
        Button b = pillButton(name);
        b.setOnClickListener(v -> {
            prefs.setProfileName(name);
            prefs.setThemeId(theme);
            prefs.setLanguage(language);
            prefs.setKeyHeightDp(height);
            previewMode = mode;
            buildScreen();
        });
        card.addView(b, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(44)));
    }

    private void exportConfig() {
        try {
            exportBox.setText(prefs.exportJson());
        } catch (JSONException e) {
            Toast.makeText(this, "No se pudo exportar", Toast.LENGTH_SHORT).show();
        }
    }

    private void importConfig() {
        try {
            prefs.importJson(exportBox.getText().toString());
            Toast.makeText(this, "Configuración importada", Toast.LENGTH_SHORT).show();
            buildScreen();
        } catch (JSONException e) {
            Toast.makeText(this, "JSON inválido", Toast.LENGTH_LONG).show();
        }
    }

    private void shareConfig() {
        try {
            String json = prefs.exportJson();
            Intent send = new Intent(Intent.ACTION_SEND);
            send.setType("application/json");
            send.putExtra(Intent.EXTRA_SUBJECT, "theme.keyboardtheme");
            send.putExtra(Intent.EXTRA_TEXT, json);
            startActivity(Intent.createChooser(send, "Compartir tema"));
        } catch (JSONException e) {
            Toast.makeText(this, "No se pudo compartir", Toast.LENGTH_SHORT).show();
        }
    }

    private TextView text(String value, int sp, boolean bold) {
        TextView tv = new TextView(this);
        tv.setText(value);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, sp);
        tv.setTextColor(palette.keyText);
        tv.setTypeface(Typeface.create("sans", bold ? Typeface.BOLD : Typeface.NORMAL));
        tv.setLineSpacing(0, 1.08f);
        return tv;
    }

    private EditText input(String hint, String value) {
        EditText edit = new EditText(this);
        edit.setText(value);
        edit.setHint(hint);
        edit.setTextColor(palette.keyText);
        edit.setHintTextColor((palette.keyText & 0x00FFFFFF) | 0x66000000);
        edit.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        edit.setSingleLine(false);
        edit.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        edit.setPadding(dp(12), 0, dp(12), 0);
        GradientDrawable bg = rounded(palette.keyBackground, dp(14));
        bg.setStroke(dp(1), palette.border);
        edit.setBackground(bg);
        return edit;
    }

    private Button pillButton(String value) {
        Button b = new Button(this);
        b.setText(value);
        b.setAllCaps(false);
        b.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        b.setTextColor(palette.specialKeyText);
        b.setPadding(dp(8), 0, dp(8), 0);
        b.setBackground(rounded(palette.specialKeyBackground, dp(16)));
        return b;
    }

    private LinearLayout card() {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(12), dp(12), dp(12), dp(12));
        GradientDrawable bg = rounded(palette.toolbarBackground, dp(22));
        bg.setStroke(dp(1), palette.border);
        card.setBackground(bg);
        return card;
    }

    private GradientDrawable rounded(int color, int radius) {
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(color);
        bg.setCornerRadius(radius);
        return bg;
    }

    private LinearLayout.LayoutParams matchWrap() {
        return new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    private LinearLayout.LayoutParams matchWrapWithBottom(int bottomDp) {
        LinearLayout.LayoutParams params = matchWrap();
        params.setMargins(0, 0, 0, dp(bottomDp));
        return params;
    }

    private int dp(float value) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, getResources().getDisplayMetrics());
    }
}
