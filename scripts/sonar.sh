#!/usr/bin/env bash

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"

green()  { echo -e "\033[32m[OK]\033[0m  $*"; }
blue()   { echo -e "\033[34m[...]\033[0m $*"; }
red()    { echo -e "\033[31m[ERR]\033[0m $*" >&2; }

if [[ -z "${SONAR_HOST_URL:-}" ]]; then
  read -r -p "SonarQube URL (e.g. https://host:9443): " SONAR_HOST_URL
fi

if [[ -z "${SONAR_TOKEN:-}" ]]; then
  read -r -s -p "SonarQube token: " SONAR_TOKEN
  echo
fi

if [[ -z "$SONAR_TOKEN" || -z "$SONAR_HOST_URL" ]]; then
  red "Both SonarQube URL and token are required."
  exit 1
fi

blue "Step 1/3 – Backend: build, test, JaCoCo coverage"
cd "$ROOT/backend"
mvn --batch-mode --no-transfer-progress install -Dcheckstyle.skip=true
mvn --batch-mode --no-transfer-progress dependency:copy-dependencies \
  -DincludeScope=runtime
green "Backend tests and coverage complete"

blue "Step 2/3 – Frontend: install, test, lcov coverage"
cd "$ROOT/frontend"
npm ci --silent
npm run test -- --coverage --run
green "Frontend tests and coverage complete"

blue "Step 3/3 – SonarQube scanner"

SONAR_HOST=$(echo "$SONAR_HOST_URL" | sed 's|https://||' | cut -d: -f1)
SONAR_PORT=$(echo "$SONAR_HOST_URL" | sed 's|https://||' | cut -d: -f2)
SONAR_CERT_TMP=$(mktemp /tmp/sonar-cert-XXXXXX.pem)
SONAR_TRUST_TMP=$(mktemp /tmp/sonar-trust-XXXXXX.jks)

cleanup() { rm -f "$SONAR_CERT_TMP" "$SONAR_TRUST_TMP"; }
trap cleanup EXIT

blue "  Fetching server certificate from ${SONAR_HOST}:${SONAR_PORT}"
openssl s_client -connect "${SONAR_HOST}:${SONAR_PORT}" -showcerts </dev/null 2>/dev/null \
  | openssl x509 -outform PEM > "$SONAR_CERT_TMP" || true

if [[ ! -s "$SONAR_CERT_TMP" ]]; then
  red "Could not retrieve TLS certificate from ${SONAR_HOST}:${SONAR_PORT}. Check the URL and that SonarQube is reachable."
  exit 1
fi

JAVA_HOME_DIR=$(java -XshowSettings:properties -version 2>&1 | grep 'java.home' | sed 's/.*= //' | head -1)
JAVA_CACERTS="${JAVA_HOME_DIR}/lib/security/cacerts"

cp "$JAVA_CACERTS" "$SONAR_TRUST_TMP"
chmod 600 "$SONAR_TRUST_TMP"
keytool -importcert -noprompt -alias sonarqube-local \
  -file "$SONAR_CERT_TMP" \
  -keystore "$SONAR_TRUST_TMP" \
  -storepass changeit 2>/dev/null || true
blue "  Certificate imported into temporary truststore"

export SONAR_SCANNER_OPTS="-Djavax.net.ssl.trustStore=$SONAR_TRUST_TMP -Djavax.net.ssl.trustStorePassword=changeit"

cd "$ROOT"
npx sonar-scanner \
  -Dsonar.host.url="$SONAR_HOST_URL" \
  -Dsonar.token="$SONAR_TOKEN" \
  -Dsonar.scanner.trustAllCertificates=true
green "SonarQube scan complete — open $SONAR_HOST_URL/dashboard?id=DistributedTwoPhaseCommitSimulator"
