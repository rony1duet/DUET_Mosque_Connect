#!/usr/bin/env bash
#
# remove_folders.sh
# Permanently removes app/src/androidTest, app/src/test, and
# app/src/main/keepRules from both the working directory and Git tracking.
#
# Usage:
#   ./remove_folders.sh          # dry-run, shows what would be removed
#   ./remove_folders.sh --apply  # actually removes + commits
#
set -euo pipefail

REPO_ROOT="$(git rev-parse --show-toplevel 2>/dev/null || true)"

if [ -z "$REPO_ROOT" ]; then
  echo "Error: not inside a git repository. cd into your repo first."
  exit 1
fi

cd "$REPO_ROOT"

TARGETS=(
  "app/src/androidTest"
  "app/src/test"
  "app/src/main/keepRules"
)

echo "Repo: $REPO_ROOT"
echo ""
echo "The following will be permanently deleted (local disk + git history going forward):"
for t in "${TARGETS[@]}"; do
  if [ -e "$t" ]; then
    echo "  - $t"
  else
    echo "  - $t (not found, skipping)"
  fi
done
echo ""

if [ "${1:-}" != "--apply" ]; then
  echo "Dry run only. Re-run with:  ./remove_folders.sh --apply"
  exit 0
fi

for t in "${TARGETS[@]}"; do
  if [ -e "$t" ]; then
    git rm -r --quiet -- "$t"
    echo "Removed: $t"
  fi
done

echo ""
read -r -p "Commit these changes now? [y/N] " CONFIRM
if [[ "$CONFIRM" =~ ^[Yy]$ ]]; then
  git commit -m "chore: remove androidTest, test, and keepRules"
  echo "Committed. Push with: git push origin <branch>"
else
  echo "Skipped commit. Changes are staged — commit manually when ready."
fi
