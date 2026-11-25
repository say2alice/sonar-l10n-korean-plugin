#!/usr/bin/env bash
# GitLab 릴리스와 같은 릴리스를 GitHub 미러 저장소에 만듭니다. CI의 github-release 잡과 로컬에서 함께 씁니다(문서 저장소 ADR-0021).
#
# 사용법: scripts/github-release.sh v<버전>
# 예:     scripts/github-release.sh v26.10.0
#
# GitLab 릴리스가 먼저 있어야 합니다. 본문은 GitLab 릴리스의 본문을 그대로 쓰고, jar는 다시 빌드하지 않고
# GitLab 패키지 저장소에서 받아 그대로 첨부합니다. 태그는 GitLab의 푸시 미러가 GitHub에 옮기므로 GitHub에 나타날 때까지 기다리고,
# 그 태그가 가리키는 커밋이 GitLab의 태그와 같은지 확인합니다. 이미 있는 릴리스와 첨부 파일은 건너뛰므로 다시 실행해도 됩니다.
# 인증은 GITHUB_TOKEN을 씁니다. CI에서는 프로젝트의 CI/CD 변수로 받습니다. 로컬에서 없으면 .env를 이 저장소, 이 저장소를 담은
# 문서 저장소의 순서로 찾아 처음 찾은 파일 하나를 읽습니다. jq가 필요합니다.
set -euo pipefail

cd "$(dirname "$0")/.."

if [ $# -ne 1 ] || [[ ! "$1" =~ ^v[0-9]+\.[0-9]+\.[0-9]+$ ]]; then
  echo "사용법: $0 v<버전>   예: $0 v26.10.0" >&2
  exit 1
fi
tag=$1
version=${tag#v}
package=sonar-l10n-korean-plugin
file="$package-$version.jar"

repo=${GITHUB_MIRROR_REPO:-say2alice/sonar-l10n-korean-plugin}
github_api=https://api.github.com
github_uploads=https://uploads.github.com
gitlab_api=${CI_API_V4_URL:-https://gitlab.way2java.net/api/v4}
project=${CI_PROJECT_ID:-jangb%2Fsonar-l10n-korean-plugin}
# 미러는 태그 푸시 뒤 보통 1분 안에 동기화합니다. 기다리는 시간의 상한입니다
tag_wait_seconds=${GITHUB_TAG_WAIT_SECONDS:-600}

if ! command -v jq > /dev/null; then
  echo "jq가 없습니다. 설치한 뒤 다시 실행하십시오" >&2
  exit 1
fi
if [ -z "${GITHUB_TOKEN:-}" ] && [ -z "${CI:-}" ]; then
  for env_file in .env ../.env; do
    if [ -f "$env_file" ]; then
      set -a
      . "$env_file"
      set +a
      break
    fi
  done
fi
if [ -z "${GITHUB_TOKEN:-}" ]; then
  echo "GITHUB_TOKEN이 없습니다. CI에서는 프로젝트의 CI/CD 변수를, 로컬에서는 .env(이 저장소 또는 문서 저장소)를 확인하십시오" >&2
  exit 1
fi
if [ -n "${CI_COMMIT_SHA:-}" ]; then
  commit=$CI_COMMIT_SHA
else
  if ! commit=$(git rev-parse --verify --quiet "$tag^{commit}"); then
    echo "로컬에 태그 $tag가 없습니다. git fetch --tags로 받은 뒤 다시 실행하십시오" >&2
    exit 1
  fi
fi

github() {
  curl -sS --header "Authorization: Bearer $GITHUB_TOKEN" \
    --header "Accept: application/vnd.github+json" \
    --header "X-GitHub-Api-Version: 2022-11-28" "$@"
}

echo "GitLab 릴리스 확인: $tag"
if ! gitlab_release=$(curl -sS --fail "$gitlab_api/projects/$project/releases/$tag"); then
  echo "GitLab 릴리스가 없습니다. release 잡(scripts/release.sh)이 끝난 뒤 실행하십시오" >&2
  exit 1
fi

mkdir -p target/github-release
jar="target/github-release/$file"
echo "jar 받기: $file"
curl -sS --fail -o "$jar" "$gitlab_api/projects/$project/packages/generic/$package/$version/$file"
size=$(wc -c < "$jar" | tr -d ' ')

echo "태그 확인: $repo $tag"
waited=0
while :; do
  github_commit=$(github "$github_api/repos/$repo/commits/$tag" | jq -r '.sha // empty')
  if [ -n "$github_commit" ]; then
    break
  fi
  if [ "$waited" -ge "$tag_wait_seconds" ]; then
    echo "GitHub에 태그 $tag가 없습니다(${tag_wait_seconds}초 기다림). GitLab의 저장소 미러링 상태를 확인하십시오" >&2
    exit 1
  fi
  sleep 15
  waited=$((waited + 15))
done
if [ "$github_commit" != "$commit" ]; then
  echo "GitHub의 태그 $tag가 다른 커밋을 가리킵니다(GitHub $github_commit, GitLab $commit)" >&2
  exit 1
fi

release=$(github "$github_api/repos/$repo/releases/tags/$tag")
release_id=$(jq -r '.id // empty' <<< "$release")
if [ -n "$release_id" ]; then
  echo "릴리스 있음: $tag"
else
  echo "릴리스 만들기: $tag"
  if ! release=$(jq '{tag_name: .tag_name, name: .name, body: .description}' <<< "$gitlab_release" \
    | github --fail-with-body --request POST --data @- "$github_api/repos/$repo/releases"); then
    echo "릴리스를 만들지 못했습니다: $release" >&2
    exit 1
  fi
  release_id=$(jq -r '.id' <<< "$release")
fi

asset_size=$(github "$github_api/repos/$repo/releases/$release_id/assets" \
  | jq -r --arg name "$file" '.[] | select(.name == $name) | .size')
if [ -z "$asset_size" ]; then
  echo "jar 첨부: $file"
  if ! uploaded=$(github --fail-with-body --request POST --header "Content-Type: application/java-archive" \
    --data-binary "@$jar" "$github_uploads/repos/$repo/releases/$release_id/assets?name=$file"); then
    echo "jar를 첨부하지 못했습니다: $uploaded" >&2
    exit 1
  fi
elif [ "$asset_size" = "$size" ]; then
  echo "jar 첨부 있음: $file"
else
  echo "릴리스에 크기가 다른 $file이 있습니다(GitHub $asset_size, GitLab $size). 릴리스 페이지에서 지우고 다시 실행하십시오" >&2
  exit 1
fi

echo "완료: https://github.com/$repo/releases/tag/$tag"
