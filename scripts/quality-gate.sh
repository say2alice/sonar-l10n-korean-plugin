#!/usr/bin/env bash
# 릴리스 태그 커밋의 SonarQube Cloud 분석이 있고 품질 게이트가 통과인지 확인합니다.
# release.sh가 jar를 올리기 전에 부르며, 따로 실행해도 됩니다(문서 저장소 ADR-0023).
#
# 사용법: scripts/quality-gate.sh v<버전>
# 예:     scripts/quality-gate.sh v26.11.1
#
# 태그 커밋을 분석한 결과를 main과 release/<메이저>.<마이너> 브랜치의 분석 목록에서 찾습니다. 분석은 그 커밋의
# 브랜치 파이프라인(verify 잡)이 하므로, 태그는 그 파이프라인이 끝난 뒤 붙입니다. 분석이 아직 없으면 최대 10분 기다립니다.
# 공개 프로젝트라 토큰 없이 조회하며, SONAR_TOKEN이 있으면 함께 보냅니다. jq가 필요합니다.
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

# 브랜치마다 분석 목록을 받아 태그 커밋을 분석한 결과의 키를 찾습니다. 없는 브랜치는 건너뜁니다
find_analysis() {
  local branch key
  for branch in main "release/$line"; do
    key=$(api -G "$host/api/project_analyses/search" \
      --data-urlencode "project=$project" --data-urlencode "branch=$branch" --data-urlencode "ps=100" 2> /dev/null \
      | jq -r --arg c "$commit" '[.analyses[] | select(.revision == $c)][0].key // empty') || key=
    if [ -n "$key" ]; then
      echo "$branch $key"
      return
    fi
  done
}

echo "품질 게이트 확인: $project $tag ($commit)"
waited=0
while :; do
  found=$(find_analysis)
  if [ -n "$found" ]; then
    break
  fi
  if [ "$waited" -ge "$wait_seconds" ]; then
    echo "SonarQube Cloud에 커밋 $commit의 분석이 없습니다(main, release/$line, ${wait_seconds}초 기다림)." >&2
    echo "그 커밋의 브랜치 파이프라인에서 verify 잡의 분석이 끝났는지 확인하십시오" >&2
    exit 1
  fi
  sleep 30
  waited=$((waited + 30))
done
branch=${found%% *}
analysis=${found#* }

status=$(api -G "$host/api/qualitygates/project_status" --data-urlencode "analysisId=$analysis")
result=$(jq -r '.projectStatus.status' <<< "$status")
if [ "$result" != "OK" ]; then
  echo "품질 게이트가 통과가 아닙니다: $result (브랜치 $branch, 분석 $analysis)" >&2
  jq -r '.projectStatus.conditions[]? | select(.status != "OK") | "  \(.metricKey): \(.actualValue // "-") (기준 \(.comparator) \(.errorThreshold))"' <<< "$status" >&2
  exit 1
fi
echo "품질 게이트 통과: 브랜치 $branch, 분석 $analysis"
