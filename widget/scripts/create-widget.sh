#!/usr/bin/env bash
# Create a widget on a running PleaseBookMe server and write widget/.env.local.
#
# Why a script: the key pair must be minted by the platform (the create endpoint
# enforces a pbm_pk_/pbm_sk_ format, so you cannot invent one), the secret is
# returned exactly once, and Next.js inlines NEXT_PUBLIC_* at dev-server start —
# so doing this by hand means copying a secret under time pressure and then
# remembering to restart. This does all of it in one go and is safe to re-run.
#
#   Usage:  ./scripts/create-widget.sh <username> <password> [origin] [api-url]
#   e.g.    ./scripts/create-widget.sh harrington 'secret123'
#
set -euo pipefail

USERNAME="${1:?usage: create-widget.sh <username> <password> [origin] [api-url]}"
PASSWORD="${2:?usage: create-widget.sh <username> <password> [origin] [api-url]}"
ORIGIN="${3:-http://localhost:3002}"
API_URL="${4:-http://localhost:8080}"

HERE="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
ENV_FILE="$HERE/.env.local"

json() { python3 -c "import sys,json;d=json.load(sys.stdin);print(d.get('$1',''))"; }

echo "→ server $API_URL"
curl -sf -o /dev/null "$API_URL/api/v1/public/__healthcheck__" 2>/dev/null || true
if ! curl -s -o /dev/null -w '%{http_code}' "$API_URL/api/v1/widgets" | grep -qE '^(401|403|200)$'; then
  echo "✗ $API_URL is not answering. Start the server first (cd server && ./gradlew bootRun)." >&2
  exit 1
fi

echo "→ signing in as $USERNAME"
LOGIN=$(curl -s -X POST "$API_URL/api/v1/auth/login" \
  -H 'Content-Type: application/json' \
  -d "$(python3 -c "import json,sys;print(json.dumps({'username':sys.argv[1],'password':sys.argv[2]}))" "$USERNAME" "$PASSWORD")")
TOKEN=$(printf '%s' "$LOGIN" | json accessToken)
if [ -z "$TOKEN" ]; then
  echo "✗ login failed: $LOGIN" >&2
  exit 1
fi

echo "→ minting a key pair"
CREDS=$(curl -s -X POST "$API_URL/api/v1/widgets/credentials" -H "Authorization: Bearer $TOKEN")
PUBLIC_KEY=$(printf '%s' "$CREDS" | json publicKey)
SECRET_KEY=$(printf '%s' "$CREDS" | json secretKey)
if [ -z "$PUBLIC_KEY" ] || [ -z "$SECRET_KEY" ]; then
  echo "✗ could not generate credentials: $CREDS" >&2
  exit 1
fi

NAME="Harness $(date +%H%M%S)"
echo "→ creating widget \"$NAME\" for origin $ORIGIN"
BODY=$(python3 -c "
import json,sys
print(json.dumps({
  'name': sys.argv[1],
  'type': 'INLINE',
  'origin': sys.argv[2],
  'credentials': {'publicKey': sys.argv[3], 'secretKey': sys.argv[4]},
}))" "$NAME" "$ORIGIN" "$PUBLIC_KEY" "$SECRET_KEY")
CREATED=$(curl -s -X POST "$API_URL/api/v1/widgets" \
  -H "Authorization: Bearer $TOKEN" -H 'Content-Type: application/json' -d "$BODY")
STATUS=$(printf '%s' "$CREATED" | json status)
if [ "$STATUS" != "ACTIVE" ]; then
  echo "✗ widget was not created: $CREATED" >&2
  exit 1
fi

if [ -f "$ENV_FILE" ]; then
  cp "$ENV_FILE" "$ENV_FILE.bak"
  echo "→ existing .env.local backed up to .env.local.bak"
fi
cat > "$ENV_FILE" <<EOF
# Written by scripts/create-widget.sh on $(date -u +%Y-%m-%dT%H:%M:%SZ)
# Widget: $NAME · origin $ORIGIN
NEXT_PUBLIC_PBM_API_URL=$API_URL
NEXT_PUBLIC_PBM_PUBLIC_KEY=$PUBLIC_KEY
NEXT_PUBLIC_PBM_SECRET_KEY=$SECRET_KEY
EOF

echo
echo "✓ widget is ACTIVE and .env.local written"
echo "  public key  $PUBLIC_KEY"
echo "  origin      $ORIGIN"
echo
echo "Next: restart the harness so Next.js picks up the new env vars —"
echo "  npm run dev      then open $ORIGIN/barbershop/kinetic"
