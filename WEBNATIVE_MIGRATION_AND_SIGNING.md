# WebNative 5.x: migración, firma y CI/CD

## Estado de la firma

La firma original de WebNative 4.4.8 no pudo recuperarse. Se generó una nueva clave estable para la cadena 5.x con alias `webnative-release`. La huella pública SHA-256 del certificado se encuentra en el paquete cifrado de custodia y no debe modificarse.

La clave privada **no se guarda en este repositorio**, no se integra dentro del APK y no debe pegarse en chats. El workflow usa cuatro secretos protegidos de GitHub Actions:

| Secreto | Contenido |
| --- | --- |
| `ANDROID_KEYSTORE_BASE64` | Keystore JKS convertido a Base64 |
| `ANDROID_KEYSTORE_PASSWORD` | Contraseña del almacén |
| `ANDROID_KEY_PASSWORD` | Contraseña de la clave |
| `ANDROID_KEY_ALIAS` | `webnative-release` |

El token de integración de esta sesión puede leer y publicar código, pero no puede modificar Actions Secrets. Por ello, estos cuatro secretos deben añadirse una sola vez desde **GitHub → Settings → Secrets and variables → Actions → New repository secret**, o ejecutando el script `scripts/configure-github-signing.sh` desde un entorno autorizado con GitHub CLI. Después, cada `commit` y `push` a `main` compilará y publicará automáticamente una release firmada.

## Migración desde 4.4.8

La nueva 5.x no puede instalarse encima de 4.4.8 si la aplicación antigua fue firmada con una clave que ya no existe. Antes de desinstalar la versión 4.4.8, abre el Centro de Actualizaciones, selecciona el Método 15 y usa **Guardar archivo** para crear un backup JSON. Ese backup conserva aplicaciones, URLs, iconos, categorías, estilos CSS/JS, preferencias OTA, endpoint de Sheets, estado de auto-comprobación, notificaciones, repositorio GitHub y el token configurado.

Después instala el primer APK 5.x mediante el instalador del teléfono. Abre el Centro de Actualizaciones, selecciona el Método 15, pulsa **Importar archivo** y elige el backup JSON. La restauración fusiona por URL y no duplica aplicaciones si se importa más de una vez.

El backup contiene configuraciones sensibles, incluido el token de GitHub si estaba guardado en la aplicación. Debe mantenerse privado y no compartirse mediante enlaces públicos.

## Versionado

Las versiones posteriores deben incrementar ambos valores de forma coherente:

```text
5.0.1 → versionCode 501
5.0.2 → versionCode 502
5.1.0 → versionCode 510
```

La versión se declara en `version_manifest.json` y `package.json`; Gradle la recibe mediante `-PversionName` y `-PversionCode` durante el workflow.

## Regla de custodia

Conservar al menos dos copias privadas del paquete cifrado de firma. No subir nunca el archivo JKS, sus contraseñas ni un backup sensible sin cifrar al repositorio. Si la clave estable se pierde, las versiones futuras dejarán de poder instalarse como actualización sobre la última versión firmada con ella.
