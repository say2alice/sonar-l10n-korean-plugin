#!/usr/bin/env bash
# GitLab 릴리스를 만듭니다. CI의 태그 파이프라인과 로컬에서 함께 씁니다(문서 저장소 ADR-0018).
#
# 사용법: scripts/release.sh v<버전>
# 예:     scripts/release.sh v26.10.0
#
# 태그와 pom.xml의 버전, 릴리스 노트 release-notes/<태그>.md를 확인하고, 미번역을 실패로 처리하는 빌드를 한 뒤,
# jar를 Generic 패키지 저장소에 올리고 릴리스를 만듭니다. 태그는 원격에 먼저 올라가 있어야 합니다.
# 인증은 CI에서는 CI_JOB_TOKEN을 씁니다. 로컬에서는 GITLAB_TOKEN을 쓰며, 없으면 .env를 이 저장소, 이 저장소를 담은
# 문서 저장소의 순서로 찾아 처음 찾은 파일 하나를 읽습니다.
set -euo pipefail

cd "$(dirname "$0")/.."

if [ $# -ne 1 ] || [[ ! "$1" =~ ^v[0-9]+\.[0-9]+\.[0-9]+$ ]]; then
  echo "사용법: $0 v<버전>   예: $0 v26.10.0" >&2
  exit 1
fi
tag=$1
version=${tag#v}
notes="release-notes/$tag.md"
package=sonar-l10n-korean-plugin
file="$package-$version.jar"

api=${CI_API_V4_URL:-https://gitlab.way2java.net/api/v4}
project=${CI_PROJECT_ID:-jangb%2Fsonar-l10n-korean-plugin}
if [ -n "${CI_JOB_TOKEN:-}" ]; then
  auth="JOB-TOKEN: $CI_JOB_TOKEN"
else
  if [ -z "${GITLAB_TOKEN:-}" ]; then
    for env_file in .env ../.env; do
      if [ -f "$env_file" ]; then
        set -a
        . "$env_file"
        set +a
        break
      fi
    done
  fi
  if [ -z "${GITLAB_TOKEN:-}" ]; then
    echo "GITLAB_TOKEN이 없습니다. .env(이 저장소 또는 문서 저장소)에 두거나 환경 변수로 넘기십시오" >&2
    exit 1
  fi
  auth="PRIVATE-TOKEN: $GITLAB_TOKEN"
fi

pom_version=$(./mvnw -q help:evaluate -Dexpression=project.version -DforceStdout)
if [ "$pom_version" != "$version" ]; then
  echo "태그와 pom.xml의 버전이 다릅니다(태그 $tag, pom.xml $pom_version). 릴리스 준비 병합 요청에서 -SNAPSHOT을 뗐는지 확인하십시오" >&2
  exit 1
fi
if [ ! -f "$notes" ]; then
  echo "릴리스 노트가 없습니다: $notes" >&2
  exit 1
fi

echo "빌드: $version"
./mvnw -B clean verify -Dtranslation.requireComplete=true

url="$api/projects/$project/packages/generic/$package/$version/$file"
echo "jar 올리기: $url"
curl -sS --fail-with-body --header "$auth" --upload-file "target/$file" "$url" > /dev/null

echo "릴리스 만들기: $tag"
curl -sS --fail-with-body --header "$auth" \
  --data-urlencode "tag_name=$tag" \
  --data-urlencode "name=$tag" \
  --data-urlencode "description@$notes" \
  "$api/projects/$project/releases" > /dev/null
# 링크는 따로 더합니다. 릴리스 생성에 함께 넘기려면 중첩 배열을 써야 해서 JSON 도구 없이 다루기 어렵습니다
curl -sS --fail-with-body --header "$auth" \
  --data-urlencode "name=$file" \
  --data-urlencode "url=$url" \
  --data-urlencode "link_type=package" \
  "$api/projects/$project/releases/$tag/assets/links" > /dev/null

echo "완료: ${CI_PROJECT_URL:-https://gitlab.way2java.net/jangb/sonar-l10n-korean-plugin}/-/releases/$tag"
