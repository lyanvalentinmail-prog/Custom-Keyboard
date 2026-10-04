# Arena Custom Keyboard

Teclado Android real (IME) moderno, fluido y altamente personalizable, inspirado en la experiencia visual/funcional de un Samsung Keyboard actual pero implementado con código, recursos e iconografía propios.

> No contiene assets propietarios de Samsung. Los iconos son `VectorDrawable` originales y la UI se dibuja con componentes nativos Android.

## Qué incluye

- `InputMethodService` real: `PremiumKeyboardService`.
- Manejo de `InputConnection`, `EditorInfo`, acciones IME, enter, borrar, composición de texto y selección contextual.
- Layout QWERTY con español/inglés:
  - Español: `QWERTY` + fila `A S D F G H J K L Ñ`.
  - Inglés: QWERTY sin Ñ.
- Modos: letras, números, símbolos y programador.
- Barra superior configurable con acciones: GIF, emojis, portapapeles, ajustes, búsqueda, traducción, selección, símbolos, idioma, micrófono, cerrar.
- Emojis por categorías con navegación vectorial, recientes y favoritos.
- Símbolos independientes: matemáticos, flechas, geométricos, monedas, técnicos, decorativos, tipográficos y letras especiales.
- Portapapeles local avanzado: historial, fijar, eliminar, pegar y opción para desactivarlo.
- Sugerencias/autocorrección local básica sin red y aprendizaje personal opcional.
- Gestos:
  - Deslizar sobre espacio: mover cursor.
  - Deslizar sobre borrar: borrar palabra.
  - Deslizar arriba/abajo: mayúsculas/minúsculas.
  - Long press en letras: popup de caracteres alternativos.
- Pantalla de configuración independiente con vista previa interactiva en tiempo real.
- Temas incluidos:
  1. Samsung-like Light
  2. Samsung-like Dark
  3. AMOLED
  4. Minimal
  5. Glass
  6. Midnight
  7. Custom
- Personalización de teclas: altura, separación, radio, tamaño de texto, escala de teclas especiales, alto contraste, espacio.
- Personalización de colores por HEX, paletas rápidas y selector H/S/L simplificado.
- Sonido, vibración, animaciones y velocidad.
- Perfiles: Trabajo, Gaming, Minimal, Programación, Emoji.
- Exportar/importar/compartir configuración JSON compatible con `.keyboardtheme`.
- Privacidad: no se declara permiso `INTERNET`; no se envían pulsaciones ni texto a servidores.

## Arquitectura

```text
app/src/main/java/com/arena/customkeyboard/
├── MainActivity.java                  # Configuración, vista previa, import/export
├── ime/
│   ├── PremiumKeyboardService.java    # IME real
│   ├── KeyboardSurface.java           # Render de filas y teclas
│   ├── KeyView.java                   # Tecla custom optimizada con feedback
│   ├── SuggestionStripView.java       # Barra de sugerencias
│   └── ToolbarView.java               # Toolbar configurable
├── model/
│   ├── KeyboardLayout.java            # QWERTY, números, símbolos, programador
│   ├── KeyboardMode.java
│   ├── KeySpec.java
│   ├── ToolbarAction.java
│   └── ToolbarActionRegistry.java
├── settings/
│   ├── KeyboardPreferences.java       # Config centralizada + JSON
│   ├── KeyboardTheme.java             # Temas predefinidos/custom
│   └── ThemePalette.java
├── suggestions/SuggestionEngine.java  # Sugerencias locales
├── clipboard/ClipboardRepository.java # Portapapeles local
└── emoji/
    ├── EmojiRepository.java
    └── SymbolRepository.java
```

Recursos clave:

```text
app/src/main/res/xml/method.xml        # Declaración IME/subtypes
app/src/main/AndroidManifest.xml       # Servicio BIND_INPUT_METHOD
app/src/main/res/drawable/ic_*.xml     # Iconografía vectorial propia
```

## Compilar

Requisitos:

- Android Studio Koala o superior, o Gradle compatible con Android Gradle Plugin 8.7.2.
- JDK 17.
- Android SDK 35 instalado.

Comandos:

```bash
# Desde la raíz del proyecto
gradle :app:assembleDebug
```

Si prefieres usar Android Studio:

1. Abre la carpeta del proyecto.
2. Deja que sincronice Gradle.
3. Ejecuta **Build → Make Project** o **Build APK(s)**.

> En este sandbox no está instalado Java/Android SDK, por lo que la validación local se limitó a revisión estática de estructura y XML.

## Instalar

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

## Activar el teclado

1. Instala la APK.
2. Abre **Arena Custom Keyboard**.
3. Pulsa **Activar teclado** o ve a:
   - **Ajustes → Sistema → Teclado → Teclado en pantalla**
4. Activa **Arena Samsung-like Keyboard**.
5. Pulsa **Seleccionar teclado** y elige el IME.

Android mostrará una advertencia estándar para cualquier teclado de terceros. Este teclado no tiene permiso de Internet y almacena la configuración localmente.

## Uso

- Escribe en cualquier app con campos de texto: WhatsApp, Telegram, Chrome, Discord, notas, formularios, etc.
- Cambia idioma desde la toolbar o desde la app de configuración.
- Mantén pulsada una vocal para acentos.
- Desliza el espacio para mover el cursor.
- Usa el botón de selección para copiar/cortar/pegar/seleccionar todo.
- Desactiva el portapapeles si no quieres historial local.

## Privacidad

La app **no declara `INTERNET`**. No puede enviar pulsaciones, conversaciones ni contraseñas a servidores.

Datos locales opcionales:

- Preferencias visuales y de comportamiento.
- Temas y perfiles.
- Emojis recientes/favoritos.
- Símbolos personalizados.
- Historial de portapapeles si está activado.
- Diccionario personal si el aprendizaje está activado.

Búsqueda/traducción abren el navegador solo después de que el usuario toque explícitamente el botón correspondiente.

## Notas de implementación

- GIF/stickers y micrófono están integrados como acciones configurables y paneles preparados. No descargan contenido ni conectan con servicios externos por privacidad. La infraestructura puede ampliarse con `InputConnection.commitContent` y paquetes locales/consentidos.
- El motor de sugerencias es local y ligero; puede sustituirse por un diccionario más grande o un modelo on-device.
- La UI evita dependencias externas para reducir peso, RAM y latencia del IME.
