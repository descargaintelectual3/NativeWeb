# 📊 Plantilla Oficial de Google Sheets para WebNative Backend & Live Sync

Esta plantilla permite gestionar toda la base de datos de aplicaciones y sitios web de **WebNative** directamente desde una hoja de cálculo en **Google Sheets**, funcionando como un backend en tiempo real.

---

## 🚀 Cómo Usar esta Plantilla con Google Sheets

1. **Crear / Importar Hoja de Cálculo**:
   - Abre [Google Sheets](https://sheets.google.com).
   - Crea una nueva hoja e importa el archivo `google_sheets_template.csv` o copia la tabla de 15 columnas que se muestra abajo.
2. **Publicar en la Web como CSV**:
   - En Google Sheets, ve a **Archivo (File)** > **Compartir (Share)** > **Publicar en la Web (Publish to web)**.
   - En la pestaña *Enlace*, selecciona **Página principal** y formato **Valores separados por comas (.csv)**.
   - Haz clic en **Publicar** y copia el enlace generado (ejemplo: `https://docs.google.com/spreadsheets/d/e/.../pub?output=csv`).
3. **Sincronizar en WebNative**:
   - En la app Android WebNative, pulsa el botón **Sheets Sync** en la pestaña de Mis Apps.
   - Pega tu URL de Google Sheets y presiona **Sincronizar Apps**.
   - ¡Listo! Todas las aplicaciones, iconos, colores y configuraciones de hardware se actualizarán automáticamente.

---

## 📋 Estructura de las 15 Columnas de Metadata

| # | Columna | Tipo | Descripción | Ejemplo |
|---|---|---|---|---|
| 1 | `id` | Texto / Entero | Identificador único de la app | `1` |
| 2 | `name` | Texto | Nombre visible de la app | `Bene Cloud` |
| 3 | `url` | URL HTTPS | Enlace destino del sitio o sistema | `https://bene.civer.cloud/` |
| 4 | `category` | Texto | Categoría (Cloud & Empresa, Productividad, IA, etc.) | `Cloud & Empresa` |
| 5 | `icon_type` | EMOJI / URL | Tipo de icono | `EMOJI` |
| 6 | `icon_value` | Emoji o URL | Emoji representativo o URL del favicon | `🌟` |
| 7 | `accent_color` | HEX / Color | Color primario en hexadecimal | `#D0BCFF` |
| 8 | `fullscreen` | Booleano | Modo pantalla completa nativo | `TRUE` |
| 9 | `hardware_boost` | Booleano | Aceleración gráfica por hardware (GPU) | `TRUE` |
| 10 | `ad_block` | Booleano | Motor de bloqueo de publicidad y rastreadores | `TRUE` |
| 11 | `battery_bypass` | Booleano | Evita suspensión por ahorro de batería | `TRUE` |
| 12 | `desktop_mode` | Booleano | Forzar versión de escritorio / User-Agent | `FALSE` |
| 13 | `oled_black_mode` | Booleano | Modo oscuro puro OLED de contraste | `FALSE` |
| 14 | `cpu_priority` | Texto | Prioridad de ejecución (`NORMAL` / `TURBO`) | `TURBO` |
| 15 | `custom_css` | Texto CSS | Inyección de estilos CSS personalizados | `/* custom */` |

---

## 🏢 Aplicaciones del Ecosistema Civer Cloud Preconfiguradas

1. **Bene Cloud**
   - URL: `https://bene.civer.cloud/`
   - Icono: `🌟`
   - Categoría: `Cloud & Empresa`
   - Color: `#D0BCFF`

2. **Manager Cloud**
   - URL: `https://manager.civer.cloud/`
   - Icono: `📊`
   - Categoría: `Cloud & Empresa`
   - Color: `#A6EECA`

3. **ControlDroid Cloud**
   - URL: `https://controldroid.civer.cloud/`
   - Icono: `🤖`
   - Categoría: `Cloud & Empresa`
   - Color: `#FFD999`
