#!/usr/bin/env bash
set -euo pipefail

REPO="${1:-descargaintelectual3/NativeWeb}"
CREDENTIALS_FILE="${2:-credentials.env}"
KEYSTORE_FILE="${3:-webnative-release-keystore.jks}"

if [[ ! -f "$CREDENTIALS_FILE" || ! -f "$KEYSTORE_FILE" ]]; then
  echo "Uso: $0 OWNER/REPO credentials.env webnative-release-keystore.jks" >&2
  exit 2
fi

# shellcheck disable=SC1090
source "$CREDENTIALS_FILE"
: "${ANDROID_KEY_ALIAS:?Falta ANDROID_KEY_ALIAS}"
: "${ANDROID_KEYSTORE_PASSWORD:?Falta ANDROID_KEYSTORE_PASSWORD}"
: "${ANDROID_KEY_PASSWORD:?Falta ANDROID_KEY_PASSWORD}"

base64 -w 0 "$KEYSTORE_FILE" | gh secret set ANDROID_KEYSTORE_BASE64 --repo "$REPO"
printf '%s' "$ANDROID_KEYSTORE_PASSWORD" | gh secret set ANDROID_KEYSTORE_PASSWORD --repo "$REPO"
printf '%s' "$ANDROID_KEY_PASSWORD" | gh secret set ANDROID_KEY_PASSWORD --repo "$REPO"
printf '%s' "$ANDROID_KEY_ALIAS" | gh secret set ANDROID_KEY_ALIAS --repo "$REPO"

echo "Secretos de firma configurados en $REPO."
echo "No borres ni subas el keystore al repositorio."
