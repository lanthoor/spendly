#!/bin/bash
# Usage: ./scripts/release.sh 0.5.0

VERSION="$1"
TAG="v$VERSION"

if [ -z "$VERSION" ]; then
  echo "Usage: $0 <version>"
  echo "Example: $0 0.5.0"
  exit 1
fi

# Pull latest CHANGELOG updates
echo "📥 Pulling latest changes..."
git pull origin main

# Update build.gradle.kts
echo "📝 Updating version in build.gradle.kts..."
sed -i '' "s/versionName = \"[^\"]*\"/versionName = \"$VERSION\"/" app/build.gradle.kts

# Get current version code and increment
CURRENT_CODE=$(grep -oP 'versionCode = \K\d+' app/build.gradle.kts)
NEW_CODE=$((CURRENT_CODE + 1))
sed -i '' "s/versionCode = [0-9]\+/versionCode = $NEW_CODE/" app/build.gradle.kts

echo "  versionName: $VERSION"
echo "  versionCode: $NEW_CODE"

# Commit (NO [skip ci])
echo "💾 Committing version bump..."
git add app/build.gradle.kts
git commit -m "chore: bump version to $VERSION"
git push origin main

# Tag
echo "🏷️  Creating and pushing tag..."
git tag "$TAG"
git push origin "$TAG"

echo ""
echo "✅ Released $TAG"
echo "📊 Monitor: https://github.com/lanthoor/spendly/actions"
