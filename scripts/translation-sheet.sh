#!/usr/bin/env bash
# 번역 작업 단위의 번역 대조표를 target/translation-sheet-<단위>.tsv로 만듭니다.
# 열과 쓰는 법은 문서 저장소 번역 작업 단위 §3.1에 있습니다.
#
# 사용법: scripts/translation-sheet.sh <단위> <접두사>...
# 예:     scripts/translation-sheet.sh common -
#         scripts/translation-sheet.sh issues cct coding_rules hotspot
#
# 접두사 -는 점이 없는 키입니다. 참고 팩은 L10N_REFERENCE_DIR 아래의 sonar-l10n-ja, sonar-l10n-ru에서 읽으며,
# 기본값은 이 저장소의 local/reference입니다.
set -euo pipefail

cd "$(dirname "$0")/.."

if [ $# -lt 2 ]; then
  echo "사용법: $0 <단위> <접두사>..." >&2
  exit 1
fi

unit=$1
shift
reference=${L10N_REFERENCE_DIR:-local/reference}

./mvnw -q test-compile exec:exec@sheet -Dsheet.unit="$unit" -Dsheet.reference="$reference" -Dsheet.prefixes="$*"
