#!/usr/bin/env bash
# 상용 번들 동기화 도구를 실행합니다(문서 저장소 ADR-0016).
#
# 사용법: scripts/commercial-bundle.sh sync
#         scripts/commercial-bundle.sh update <상용 에디션 이미지 태그>
# 예:     scripts/commercial-bundle.sh update 2026.5.2-enterprise
#
# sync는 번역 파일의 값을 상용 번들 한국어 파일에 다시 씁니다. 번역 파일을 고친 뒤에 실행합니다.
# update는 공식 이미지에서 서버 jar를 target/에 꺼내 translation/commercial/의 영문 사본을 다시 쓰고 sync를 실행합니다.
# Docker가 필요합니다. 결과 파일은 커밋합니다.
set -euo pipefail

cd "$(dirname "$0")/.."

case "${1:-}" in
  sync)
    if [ $# -ne 1 ]; then
      echo "사용법: $0 sync" >&2
      exit 1
    fi
    ./mvnw -q test-compile exec:exec@commercial-sync
    ;;
  update)
    if [ $# -ne 2 ]; then
      echo "사용법: $0 update <상용 에디션 이미지 태그>   예: $0 update 2026.5.2-enterprise" >&2
      exit 1
    fi
    image="sonarqube:$2"
    name=$(docker run --rm --entrypoint sh "$image" -c 'basename /opt/sonarqube/lib/sonar-application-*.jar')
    mkdir -p target
    # docker cp는 큰 파일에서 멈추는 일이 있어 표준 출력으로 꺼냅니다
    docker run --rm --entrypoint cat "$image" "/opt/sonarqube/lib/$name" > "target/$name"
    echo "서버 jar: target/$name"
    ./mvnw -q test-compile exec:exec@commercial-update -Dcommercial.jar="$PWD/target/$name"
    ;;
  *)
    echo "사용법: $0 sync | update <상용 에디션 이미지 태그>" >&2
    exit 1
    ;;
esac
