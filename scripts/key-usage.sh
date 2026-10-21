#!/usr/bin/env bash
# 번역할 키가 웹앱 소스의 어디에서 쓰이는지 찾습니다. 뜻이 여럿인 낱말의 번역을 정할 때 씁니다.
# 웹앱 클론을 받는 방법은 DEVELOPMENT.md의 개발 작업 > 번역 절에 있습니다.
#
# 사용법: scripts/key-usage.sh <키>...
# 예:     scripts/key-usage.sh status open
#
# 웹앱 클론은 L10N_REFERENCE_DIR(기본: 이 저장소의 local/reference) 아래 sonarqube-webapp에서 읽습니다.
# 번역 함수의 호출과 id 속성만 찾으므로, 키 이름을 조합해 쓰는 곳은 나오지 않습니다.
set -euo pipefail

cd "$(dirname "$0")/.."

if [ $# -lt 1 ]; then
  echo "사용법: $0 <키>..." >&2
  exit 1
fi

webapp=${L10N_REFERENCE_DIR:-local/reference}/sonarqube-webapp
if [ ! -d "$webapp/.git" ]; then
  echo "웹앱 클론이 없습니다: $webapp. DEVELOPMENT.md의 개발 작업 > 번역 절의 방법으로 클론하십시오" >&2
  exit 1
fi
# 체크아웃하지 않은 클론은 검색할 파일이 없어 모든 키가 쓰임을 찾지 못한 것으로 나옵니다
if [ -z "$(git -C "$webapp" ls-files | head -1)" ]; then
  echo "웹앱 클론에 체크아웃된 파일이 없습니다: $webapp. DEVELOPMENT.md의 개발 작업 > 번역 절의 방법으로 다시 클론하십시오" >&2
  exit 1
fi

for key in "$@"; do
  echo "== $key"
  # 키의 점은 정규식에서 임의 문자이므로 이스케이프합니다
  pattern=${key//./\\.}
  git -C "$webapp" grep -nE "(translate[A-Za-z]*\(|id[:=] ?\{?)['\"]${pattern}['\"]" \
    -- '*.tsx' '*.ts' ':!*__tests__*' || echo "(쓰임을 찾지 못했습니다)"
done
