#!/usr/bin/env bash
# 릴리스 태그 커밋의 SonarQube Cloud 분석이 있고 품질 게이트가 통과인지 확인합니다.
# release.sh가 jar를 올리기 전에 부르며, 따로 실행해도 됩니다(문서 저장소 ADR-0023).
#
# 사용법: scripts/quality-gate.sh v<버전>
# 예:     scripts/quality-gate.sh v26.11.1
#
# 환경 변수: QUALITY_GATE_WAIT_SECONDS(기다리는 시간, 기본 600), QUALITY_GATE_BRANCHES(찾을 브랜치, 기본 "main release/<메이저>.<마이너>")
#
# main과 release/<메이저>.<마이너> 브랜치 중 마지막 분석 커밋이 태그 커밋인 브랜치를 찾아 그 품질 게이트를 봅니다.
# 브랜치 목록 API는 단기 브랜치에도 마지막 분석의 커밋과 품질 게이트를 주지만, 분석 목록 API는 장기 브랜치만 됩니다.
# 그래서 태그는 브랜치의 끝 커밋에 붙이고, 그 커밋의 브랜치 파이프라인(verify 잡의 분석)이 끝난 뒤 붙입니다.
# 분석이 아직 없으면 최대 10분 기다립니다. 공개 프로젝트라 토큰 없이 조회하며, SONAR_TOKEN이 있으면 함께 보냅니다. jq가 필요합니다.
set -euo pipefail

cd "$(dirname "$0")/.."

if [ $# -ne 1 ] || [[ ! "$1" =~ ^v[0-9]+\.[0-9]+\.[0-9]+$ ]]; then
  echo "사용법: $0 v<버전>   예: $0 v26.11.1" >&2
  exit 1
fi
tag=$1
version=${tag#v}
line=${version%.*}
host=${SONAR_HOST_URL:-https://sonarcloud.io}
project=$(sed -n 's:.*<sonar.projectKey>\(.*\)</sonar.projectKey>.*:\1:p' pom.xml)
wait_seconds=${QUALITY_GATE_WAIT_SECONDS:-600}
# 태그 커밋을 찾을 브랜치. 공백으로 나눕니다
branches=${QUALITY_GATE_BRANCHES:-main release/$line}

if ! command -v jq > /dev/null; then
  echo "jq가 없습니다. 설치한 뒤 다시 실행하십시오" >&2
  exit 1
fi
if [ -z "$project" ]; then
  echo "pom.xml에 sonar.projectKey가 없습니다" >&2
  exit 1
fi
if [ -n "${CI_COMMIT_SHA:-}" ]; then
  commit=$CI_COMMIT_SHA
elif ! commit=$(git rev-parse --verify --quiet "$tag^{commit}"); then
  echo "로컬에 태그 $tag가 없습니다. git fetch --tags로 받은 뒤 다시 실행하십시오" >&2
  exit 1
fi

api() {
  if [ -n "${SONAR_TOKEN:-}" ]; then
    curl -fsS -u "$SONAR_TOKEN:" "$@"
  else
    curl -fsS "$@"
  fi
}

# 확인할 브랜치 중 마지막 분석 커밋이 태그 커밋인 브랜치의 이름을 찾습니다
find_branch() {
  api -G "$host/api/project_branches/list" --data-urlencode "project=$project" 2> /dev/null \
    | jq -r --arg c "$commit" --arg b "$branches" \
      '($b | split(" ")) as $names | [.branches[] | select((.name | IN($names[])) and .commit.sha == $c) | .name][0] // empty' || true
}

echo "품질 게이트 확인: $project $tag ($commit)"
waited=0
while :; do
  branch=$(find_branch)
  if [ -n "$branch" ]; then
    break
  fi
  if [ "$waited" -ge "$wait_seconds" ]; then
    echo "SonarQube Cloud에 커밋 $commit을 마지막으로 분석한 브랜치가 없습니다($branches, ${wait_seconds}초 기다림)." >&2
    echo "그 커밋의 브랜치 파이프라인에서 verify 잡의 분석이 끝났는지, 태그가 브랜치의 끝 커밋에 붙었는지 확인하십시오" >&2
    exit 1
  fi
  sleep 30
  waited=$((waited + 30))
done

status=$(api -G "$host/api/qualitygates/project_status" --data-urlencode "projectKey=$project" --data-urlencode "branch=$branch")
result=$(jq -r '.projectStatus.status' <<< "$status")
if [ "$result" != "OK" ]; then
  echo "품질 게이트가 통과가 아닙니다: $result (브랜치 $branch, 커밋 $commit)" >&2
  jq -r '.projectStatus.conditions[]? | select(.status != "OK") | "  \(.metricKey): \(.actualValue // "-") (기준 \(.comparator) \(.errorThreshold))"' <<< "$status" >&2
  exit 1
fi
echo "품질 게이트 통과: 브랜치 $branch, 커밋 $commit"
