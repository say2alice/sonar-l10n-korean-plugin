#!/usr/bin/env bash
# 빌드한 언어팩을 로컬 SonarQube 컨테이너에 설치해 실행합니다.
#
# 사용법: scripts/run-sonarqube.sh <이미지 태그>
# 예:     scripts/run-sonarqube.sh 26.10.0.133078-community
#
# 같은 이름의 컨테이너가 있으면 지우고 새로 만듭니다. 데이터는 보존하지 않습니다.
# 먼저 ./mvnw verify로 target/에 jar를 만들어 둡니다.
set -euo pipefail

CONTAINER=sonar-l10n-korean
PORT=9000
PLUGIN_DIR=/opt/sonarqube/extensions/plugins

if [ $# -ne 1 ]; then
  echo "사용법: $0 <이미지 태그>   예: $0 26.10.0.133078-community" >&2
  exit 1
fi
IMAGE="sonarqube:$1"

cd "$(dirname "$0")/.."
shopt -s nullglob
jars=(target/sonar-l10n-korean-plugin-*.jar)
if [ ${#jars[@]} -ne 1 ]; then
  echo "target/에 jar가 하나여야 합니다(찾은 수: ${#jars[@]}). ./mvnw clean verify를 먼저 실행하십시오." >&2
  exit 1
fi
JAR=${jars[0]}

wait_up() {
  local status=""
  for _ in $(seq 1 120); do
    status=$(curl -s "http://localhost:$PORT/api/system/status" | sed -n 's/.*"status":"\([A-Z_]*\)".*/\1/p' || true)
    if [ "$status" = "UP" ]; then
      return 0
    fi
    sleep 5
  done
  echo "10분 안에 서버가 UP 상태가 되지 않았습니다(마지막 상태: ${status:-응답 없음}). docker logs $CONTAINER로 확인하십시오." >&2
  return 1
}

docker rm -f "$CONTAINER" >/dev/null 2>&1 || true
echo "컨테이너 시작: $IMAGE"
docker run -d --name "$CONTAINER" -p "$PORT:9000" "$IMAGE" >/dev/null
wait_up

echo "플러그인 설치: $JAR"
docker cp "$JAR" "$CONTAINER:$PLUGIN_DIR/"
docker restart "$CONTAINER" >/dev/null
wait_up

echo "준비 완료: http://localhost:$PORT (처음 로그인 계정 admin / admin)"
