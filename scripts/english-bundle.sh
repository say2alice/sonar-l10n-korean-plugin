#!/usr/bin/env bash
# 영문 번들 생성기를 실행합니다. 기준 태그와 원본 경로는 pom.xml의 sonarqube.webapp.* 속성에서 읽습니다.
#
# 사용법: scripts/english-bundle.sh generate
#         scripts/english-bundle.sh diff <이전 태그> <새 태그>
# 예:     scripts/english-bundle.sh diff sqcb-25.12.0 sqcb-26.10.0
#
# generate는 영어 기본 번들을 만들고 번역 파일의 키를 맞춥니다. 결과 파일은 커밋합니다.
# diff는 차이를 출력하고 같은 내용을 target/diff-<이전 태그>-<새 태그>.txt에 저장합니다.
set -euo pipefail

cd "$(dirname "$0")/.."

case "${1:-}" in
  generate)
    if [ $# -ne 1 ]; then
      echo "사용법: $0 generate" >&2
      exit 1
    fi
    ./mvnw -q test-compile exec:exec@generate
    ;;
  diff)
    if [ $# -ne 3 ]; then
      echo "사용법: $0 diff <이전 태그> <새 태그>" >&2
      exit 1
    fi
    mkdir -p target
    ./mvnw -q test-compile exec:exec@diff -Ddiff.from="$2" -Ddiff.to="$3" | tee "target/diff-$2-$3.txt"
    ;;
  *)
    echo "사용법: $0 generate | diff <이전 태그> <새 태그>" >&2
    exit 1
    ;;
esac
