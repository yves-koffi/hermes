#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
ENV_FILE="${ROOT_DIR}/.env"

if [[ ! -f "${ENV_FILE}" ]]; then
  echo "Missing ${ENV_FILE}. Create it before starting the app."
  exit 1
fi

set -a
source "${ENV_FILE}"
set +a

required_variables=(
  DB_HOST
  DB_PORT
  LIFE_PING_DB
  LIFE_PING_USER
  LIFE_PING_PASSWORD
)

missing_variables=()
for variable in "${required_variables[@]}"; do
  if [[ -z "${!variable:-}" ]]; then
    missing_variables+=("${variable}")
  fi
done

if (( ${#missing_variables[@]} > 0 )); then
  echo "Missing required environment variables: ${missing_variables[*]}"
  echo "Update .env, then retry."
  exit 1
fi

cd "${ROOT_DIR}"
exec ./mvnw -pl app quarkus:dev "$@"
