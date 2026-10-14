#!/usr/bin/env bash
# 번역을 마친 작업 단위를 다른 한국어 팩과 비교해 target/korean-pack-compare-<단위>.tsv로 씁니다.
# 낱말 선택을 검토할 때만 쓰며, 쓰는 법은 문서 저장소 번역 작업 단위 §3.2에 있습니다.
#
# 사용법: scripts/korean-pack-compare.sh <단위> <접두사>...
# 예:     scripts/korean-pack-compare.sh common -
#
# 한국어 팩은 L10N_REFERENCE_DIR(기본: 이 저장소를 담은 문서 저장소의 local/reference) 아래 korean-repos에서 읽습니다.
set -euo pipefail

cd "$(dirname "$0")/.."

if [ $# -lt 2 ]; then
  echo "사용법: $0 <단위> <접두사>..." >&2
  exit 1
fi

unit=$1
shift
reference=${L10N_REFERENCE_DIR:-../local/reference}

./mvnw -q test-compile exec:exec@compare -Dsheet.unit="$unit" -Dsheet.reference="$reference" -Dsheet.prefixes="$*"
