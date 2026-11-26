<!--
릴리스 노트 템플릿입니다. release-notes/v<버전>.md로 복사해 <...>를 채우고 이 주석을 지웁니다.
헤더, 헤더 순서, 표의 행 이름은 바꾸지 않습니다. 해당하는 내용이 없는 절은 지우지 않고 없다고 한 줄로 적습니다.
절 안에는 그 릴리스에만 해당하는 설명을 더할 수 있습니다.
수치는 scripts/english-bundle.sh diff <이전 태그> <새 태그>의 출력을 옮깁니다.
맨 끝의 Summary in English는 Marketplace 검토자와 영어 사용자를 위한 요약입니다(문서 저장소 ADR-0023).
절차: 문서 저장소 docs/operations/plugin-release.md §3
-->
SonarQube Community Build 한국어 언어팩 <버전>입니다. <이 릴리스의 성격을 한 문장으로>

## 변경 내용

- <이 릴리스에서 바뀐 것>

## 기준 원본과 지원 범위

| 항목 | 내용 |
| --- | --- |
| 기준 원본 | 웹앱 태그 `<기준 태그>`의 `default.ts`(<키 수>키) |
| 지원 범위 | Community Build <하한> 이상 |
| 상용 에디션 | 지원 범위 밖입니다. <상용 번들 설명> |

## 주의사항

- 기존 한국어팩(`l10nko`)을 포함해 다른 언어팩과 함께 설치하지 마세요. 이 언어팩은 웹 화면의 키를 자체 번들로 제공합니다. 그래서 함께 설치하면 다른 언어팩의 언어로 볼 때 화면 문구의 상당수가 영문으로 나옵니다. 다른 언어팩이 설치되어 있으면 먼저 지우세요.
- <하한>보다 낮은 버전에 설치하면 서버가 시작되지 않습니다. 로그에 `Plugin Korean Pack [l10nkorean] requires at least Sonar Plugin API version <API 버전>`이 남으며, jar를 지우면 다시 시작됩니다.

## 이전 릴리스 대비 키 변경

`<이전 릴리스의 기준 태그>` 대비 `<기준 태그>`의 차이는 추가 <수>키, 삭제 <수>키, 값 변경 <수>키입니다.

## 하위 버전 호환성

번역은 <기준 버전>을 기준으로 했으므로 <하한> ~ <기준 직전 버전>에서는 일부 문구가 다르게 나옵니다. <하한> ~ <기준 직전 버전> 서버에는 README의 서버 버전별 권장 릴리스(<권장 릴리스 요약>)를 권합니다. `<하한 태그>` 대비 `<기준 태그>`의 차이는 추가 <수>키, 삭제 <수>키, 값 변경 <수>키, 인자 구성 변경 <수>키입니다.

- <기준 버전>에서 지워진 키는 영문으로 나옵니다.
- 값이 바뀐 키는 옛 버전 화면에서도 <기준 버전> 문장의 번역으로 나옵니다.
- 아래 <수>키는 자리표시자(인자) 구성이 바뀌어, 옛 버전에서는 자리표시자에 값이 채워지지 않을 수 있습니다.

```text
<인자 구성 변경 키 목록>
```

## 설치

저장소 README의 설치와 사용 절을 따릅니다.

## Summary in English

SonarQube Community Build Korean language pack <버전>. <이 릴리스의 성격을 영어 한 문장으로>

| Item | Details |
| --- | --- |
| Baseline | `default.ts` of web app tag `<기준 태그>`: <번역한 키 수> of <키 수> messages translated into Korean |
| Supported versions | Community Build <하한> and later. Recommended for Community Build <권장 서버 버전> |
| Changes | <이 릴리스에서 바뀐 것을 영어로> |

Do not install this pack together with other language packs, including the existing Korean pack (`l10nko`). Installation steps are in the repository README (`README.en.md`).
