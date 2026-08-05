#!/usr/bin/env bash
#
# clean_repo.sh
# Removes files/folders from Git TRACKING that match .gitignore,
# without deleting them from your local disk.
#
# Usage:
#   ./clean_repo.sh          # dry-run, shows what would be untracked
#   ./clean_repo.sh --apply  # actually untracks + commits
#
set -euo pipefail

REPO_ROOT="$(git rev-parse --show-toplevel 2>/dev/null || true)"

if [ -z "$REPO_ROOT" ]; then
  echo "Error: not inside a git repository. cd into your repo first."
  exit 1
fi

cd "$REPO_ROOT"

if [ ! -f ".gitignore" ]; then
  echo "Error: no .gitignore found in $REPO_ROOT"
  echo "Copy the provided .gitignore into the repo root first."
  exit 1
fi

echo "Repo: $REPO_ROOT"
echo ""

# Find files that are currently tracked by git but match .gitignore
MATCHES="$(git ls-files -ci --exclude-standard || true)"

if [ -z "$MATCHES" ]; then
  echo "Nothing to clean — no tracked files match .gitignore."
  exit 0
fi

echo "The following tracked files/folders match .gitignore and will be UNTRACKED"
echo "(they will stay on your local disk, just removed from Git history going forward):"
echo "-----------------------------------------------------------------------------"
echo "$MATCHES"
echo "-----------------------------------------------------------------------------"
echo ""

if [ "${1:-}" != "--apply" ]; then
  echo "Dry run only. Re-run with:  ./clean_repo.sh --apply"
  exit 0
fi

echo "Untracking matched files..."
echo "$MATCHES" | git rm -r --cached --ignore-unmatch --quiet -- 2>/dev/null || {
  # Fallback: some git versions choke on passing many paths via a single arg block.
  echo "$MATCHES" | while IFS= read -r f; do
    [ -n "$f" ] && git rm -r --cached --ignore-unmatch --quiet -- "$f"
  done
}

git add .gitignore

echo ""
echo "Files untracked. Review with: git status"
echo ""
read -r -p "Commit these changes now? [y/N] " CONFIRM
if [[ "$CONFIRM" =~ ^[Yy]$ ]]; then
  git commit -m "chore: apply .gitignore, remove unnecessary tracked files"
  echo "Committed. Push with: git push origin <branch>"
else
  echo "Skipped commit. Your staged changes are ready — commit manually when ready."
fi
