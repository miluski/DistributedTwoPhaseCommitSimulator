#!/usr/bin/env bash

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"

SECRETS_DIR="$ROOT/secrets"
ENV_FILE="$ROOT/.env"
ENV_EXAMPLE="$ROOT/.env.example"

if [[ -n "${KEYSTORE_PASSWORD:-}" ]]; then
  PASSWORD="$KEYSTORE_PASSWORD"
elif [[ -f "$ENV_FILE" ]] && grep -q "^COORDINATOR_KEYSTORE_PASSWORD=.\+" "$ENV_FILE" \
     && [[ -f "$SECRETS_DIR/coordinator-keystore.p12" ]]; then
  PASSWORD="$(grep "^COORDINATOR_KEYSTORE_PASSWORD=" "$ENV_FILE" | cut -d'=' -f2-)"
else
  PASSWORD="$(openssl rand -base64 32 | tr -d '/+=\n' | head -c 32)"
fi
VALIDITY=3650

green()  { echo -e "\033[32m[OK]\033[0m  $*"; }
yellow() { echo -e "\033[33m[SKIP]\033[0m $*"; }
blue()   { echo -e "\033[34m[...]\033[0m $*"; }
red()    { echo -e "\033[31m[ERR]\033[0m $*" >&2; }

detect_os() {
  case "$(uname -s)" in
    Linux*)  echo linux ;;
    Darwin*) echo mac   ;;
    MINGW*|MSYS*|CYGWIN*) echo windows ;;
    *) echo unknown ;;
  esac
}

OS=$(detect_os)
blue "Detected OS: $OS"

if ! command -v keytool &>/dev/null; then
  red "keytool not found. Make sure a JDK is installed and on PATH."
  if [[ "$OS" == "windows" ]]; then
    red "On GitBash ensure JAVA_HOME is set, e.g.:"
    red "  export JAVA_HOME='/c/Program Files/Eclipse Adoptium/jdk-21'"
    red "  export PATH=\"\$JAVA_HOME/bin:\$PATH\""
  fi
  exit 1
fi

mkdir -p "$SECRETS_DIR"

write_env_passwords() {
  local target="$1"
  sed -i.bak \
    -e "s|^COORDINATOR_KEYSTORE_PASSWORD=.*|COORDINATOR_KEYSTORE_PASSWORD=${PASSWORD}|" \
    -e "s|^PARTICIPANT_KEYSTORE_PASSWORD=.*|PARTICIPANT_KEYSTORE_PASSWORD=${PASSWORD}|" \
    "$target"
  rm -f "${target}.bak"
}

if [[ -f "$ENV_FILE" ]]; then
  write_env_passwords "$ENV_FILE"
  green ".env passwords updated with generated values"
else
  if [[ -f "$ENV_EXAMPLE" ]]; then
    cp "$ENV_EXAMPLE" "$ENV_FILE"
    write_env_passwords "$ENV_FILE"
    green ".env created from .env.example with generated passwords"
  else
    cat >"$ENV_FILE" <<EOF
COORDINATOR_KEYSTORE_PASSWORD=${PASSWORD}
PARTICIPANT_KEYSTORE_PASSWORD=${PASSWORD}
EOF
    green ".env created with generated passwords"
  fi
fi

generate_keystore() {
  local alias="$1"
  local cn="$2"
  local out="$3"
  local ext="${4:-}"

  if [[ -f "$out" ]]; then
    yellow "Keystore already exists: $out"
    return
  fi

  blue "Generating keystore: $out (CN=$cn)"

  local dname="CN=${cn}, OU=Dev, O=TwoPC, L=Local, ST=Local, C=PL"
  local ext_args=()
  if [[ -n "$ext" ]]; then
    ext_args=("-ext" "$ext")
  fi

  keytool -genkeypair \
    -alias   "$alias" \
    -keyalg  RSA \
    -keysize 2048 \
    -validity "$VALIDITY" \
    -dname   "$dname" \
    -storetype PKCS12 \
    -keystore  "$out" \
    -storepass "$PASSWORD" \
    -keypass   "$PASSWORD" \
    -noprompt \
    "${ext_args[@]}"

  green "Created: $out"
}

participant_cert_has_sans() {
  local keystore="$1"
  keytool -list -v \
    -keystore "$keystore" \
    -storepass "$PASSWORD" \
    2>/dev/null | grep -qi "SubjectAlternativeName"
}

PARTICIPANT_SANS="SAN=dns:participant,dns:participant-1,dns:participant-2,dns:participant-3,dns:participant-4,dns:participant-5,dns:participant-6,dns:localhost"

generate_keystore "coordinator" "coordinator" "$SECRETS_DIR/coordinator-keystore.p12"

if [[ -f "$SECRETS_DIR/participant-keystore.p12" ]] && ! participant_cert_has_sans "$SECRETS_DIR/participant-keystore.p12"; then
  blue "Participant keystore missing Docker SANs — regenerating..."
  rm -f "$SECRETS_DIR/participant-keystore.p12" "$SECRETS_DIR/trust-store.p12"
fi

generate_keystore "participant" "participant" "$SECRETS_DIR/participant-keystore.p12" "$PARTICIPANT_SANS"

generate_trust_store() {
  local truststore="$SECRETS_DIR/trust-store.p12"

  if [[ -f "$truststore" ]]; then
    yellow "Trust store already exists: $truststore"
    return
  fi

  blue "Generating trust store: $truststore"

  local tmpdir; tmpdir=$(mktemp -d)

  keytool -export -alias coordinator \
    -keystore "$SECRETS_DIR/coordinator-keystore.p12" \
    -storepass "$PASSWORD" \
    -file "$tmpdir/coordinator.crt" \
    -noprompt

  keytool -export -alias participant \
    -keystore "$SECRETS_DIR/participant-keystore.p12" \
    -storepass "$PASSWORD" \
    -file "$tmpdir/participant.crt" \
    -noprompt

  keytool -import -alias coordinator-ca \
    -file "$tmpdir/coordinator.crt" \
    -keystore "$truststore" \
    -storepass "$PASSWORD" \
    -storetype PKCS12 \
    -noprompt

  keytool -import -alias participant-ca \
    -file "$tmpdir/participant.crt" \
    -keystore "$truststore" \
    -storepass "$PASSWORD" \
    -storetype PKCS12 \
    -noprompt

  rm -rf "$tmpdir"
  green "Created: $truststore"
}

generate_trust_store

generate_frontend_cert() {
  local cert="$SECRETS_DIR/frontend.crt"
  local key="$SECRETS_DIR/frontend.key"

  if [[ -f "$cert" && -f "$key" ]]; then
    yellow "Frontend certificate already exists: $cert"
    return
  fi

  blue "Generating frontend TLS certificate: $cert"
  openssl req -x509 -nodes -newkey rsa:2048 \
    -keyout "$key" \
    -out    "$cert" \
    -days   3650 \
    -subj   "/CN=localhost/O=TwoPC/OU=Dev/C=PL" 2>/dev/null
  chmod 644 "$cert" "$key"
  green "Created: $cert (and frontend.key)"
}

generate_frontend_cert

echo ""
green "Dev environment initialised."
echo ""
echo "  Secrets directory : $SECRETS_DIR"
echo "  Keystore password : $PASSWORD"
echo ""
echo "Next steps:"
echo "  1. Review .env and update passwords if needed."
echo "  2. docker compose up --build"
