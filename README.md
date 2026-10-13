# SonarQube 한국어 언어팩

SonarQube Community Build의 웹 화면을 한국어로 표시하는 언어팩 플러그인입니다.

## 구성

jar에 들어가는 것은 진입점 클래스 하나와 번들 파일 셋입니다. 나머지는 번역 파일을 만들고 검사하는 도구입니다.

```text
├── pom.xml                         # 빌드 설정. 기준 웹앱 태그 등 기준 버전은 이 파일의 속성에만 둡니다
├── mvnw, .mvn/wrapper/             # Maven Wrapper
├── .gitlab-ci.yml                  # CI 검증 잡
├── scripts/
│   ├── english-bundle.sh           # 영문 번들 생성기 실행
│   ├── translation-sheet.sh        # 번역 대조표 생성
│   └── run-sonarqube.sh            # 로컬 SonarQube 컨테이너에 jar 설치
├── translation/
│   ├── glossary.tsv                # 용어집
│   └── keep-english.txt            # 영문 그대로 두는 값의 규칙
└── src/
    ├── main/                       # jar에 들어가는 부분
    │   ├── java/.../KoreanPackPlugin.java
    │   └── resources/org/sonar/l10n/
    │       ├── l10nkorean.properties
    │       ├── l10nkorean_ko.properties
    │       └── core_ko.properties
    └── test/                       # 번들 검사와 도구. jar에 들어가지 않습니다
        └── java/.../korean/
            ├── generator/          # 영문 번들 생성기
            ├── TranslationSheet.java  # 번역 대조표 도구
            └── *Test.java          # 번들 검사
```

jar에 들어가는 파일입니다.

| 파일 | 역할 | 고치는 방법 |
| --- | --- | --- |
| `KoreanPackPlugin.java` | SonarQube가 플러그인을 적재하는 진입점입니다. 확장을 등록하지 않습니다 | 바꿀 일이 거의 없습니다 |
| `l10nkorean.properties` | 영어 기본 번들입니다. 서버가 이 파일로 번들의 키를 등록합니다 | 영문 번들 생성기가 만듭니다. 손으로 고치지 않습니다 |
| `l10nkorean_ko.properties` | 화면에 나오는 한국어 번역입니다. 번역하지 않은 키는 영문 값입니다 | 값만 고칩니다. 키와 순서는 영문 번들 생성기가 맞춥니다 |
| `core_ko.properties` | 서버가 한국어 로캘로 응답하게 합니다. 키를 넣지 않습니다 | 고치지 않습니다 |

번역의 원본은 서버의 `core.properties`가 아니라 웹앱의 `default.ts`이므로, 번역을 `core_ko.properties`가 아닌 플러그인 자체 번들 `l10nkorean`에 둡니다.

도구는 jar에 넣지 않으면서 빌드와 같은 JDK로 실행하려고 테스트 소스에 두며, `scripts/`의 스크립트가 실행합니다. 번들 검사는 `./mvnw verify`에 포함되어 다음을 확인합니다.

| 검사 | 확인하는 것 |
| --- | --- |
| 번들 동기화 | 번역 파일과 영어 기본 번들의 키가 같습니다 |
| 기준 태그 | 영어 기본 번들을 만든 웹앱 태그가 `pom.xml`의 기준 태그와 같습니다 |
| 번역 품질 | 인자, ICU 구문, 태그가 원문과 같습니다 |
| 미번역 | 미번역 키의 수와 목록을 `target/untranslated-keys.txt`에 씁니다. `-Dtranslation.requireComplete=true`일 때만 실패합니다 |
| 용어 | 번역에 용어집의 금지 표기가 없습니다 |
| 플러그인, `core_ko.properties` | 확장을 등록하지 않고, `core_ko.properties`에 키가 없습니다 |

설계와 결정의 근거는 문서 저장소의 아키텍처와 결정 기록(ADR)에 있습니다.

## 빌드

JDK 25가 `~/.m2/toolchains.xml`에 등록되어 있어야 합니다. Maven은 설치하지 않고 Wrapper로 실행합니다. Wrapper가 Maven 배포본을 처음 내려받을 때 `unzip`과 `sha256sum`(또는 `shasum`)이 필요합니다.

1. jar를 만듭니다.

    ```bash
    ./mvnw clean verify
    ```

    `BUILD SUCCESS`가 나오고 `target/sonar-l10n-korean-plugin-<버전>.jar`가 생겨야 합니다.

## 번역

번역은 `src/main/resources/org/sonar/l10n/l10nkorean_ko.properties`의 값을 고칩니다. 키와 순서는 영문 번들 생성기가 맞추므로 손으로 바꾸지 않습니다. 번역 규칙과 용어는 문서 저장소의 번역 지침, 용어집은 `translation/glossary.tsv`, 영문 그대로 두는 값의 규칙은 `translation/keep-english.txt`에 있습니다.

1. 번역 작업 단위의 대조표를 만듭니다. 인자는 단위 이름과 키 접두사이며 `-`는 점이 없는 키입니다. 단위별 접두사는 문서 저장소의 번역 작업 단위에 있습니다.

    ```bash
    scripts/translation-sheet.sh common -
    ```

    `단위 common: <키 수>키, 미번역 <수>키`가 나오고, 키마다 원문, 지금 번역, 일본어 팩과 러시아어 팩의 번역을 나란히 둔 `target/translation-sheet-common.tsv`가 생겨야 합니다. 참고 팩은 문서 저장소의 `local/reference/`에서 읽으며, 다른 자리에 두었으면 `L10N_REFERENCE_DIR`로 지정합니다.

2. 빌드해 미번역 키를 확인합니다.

    ```bash
    ./mvnw verify
    ```

    `미번역 키 <수> / <전체 키 수>`가 나오고, 목록이 `target/untranslated-keys.txt`에 생겨야 합니다.

## 기준 원본 갱신

기준 원본은 `pom.xml`의 `sonarqube.webapp.tag`, `sonarqube.webapp.messages.path`입니다. 이 값을 바꾼 뒤 영어 기본 번들을 다시 만듭니다. GitHub에 접속할 수 있어야 합니다. 전체 절차는 문서 저장소의 기준 원본 갱신 절차에 있습니다.

1. 두 태그 사이의 키 차이를 봅니다. 태그는 비교할 버전으로 바꿔 입력합니다.

    ```bash
    scripts/english-bundle.sh diff sqcb-25.12.0 sqcb-26.10.0
    ```

    둘째 줄에 `추가`, `삭제`, `값 변경`, `인자 구성 변경`의 키 수가 나와야 합니다. 같은 내용이 `target/diff-sqcb-25.12.0-sqcb-26.10.0.txt`에 저장됩니다.

2. 영어 기본 번들을 만들고 번역 파일의 키를 맞춥니다.

    ```bash
    scripts/english-bundle.sh generate
    ```

    `원본: <태그> <키 수>키`로 시작하는 세 줄이 나와야 합니다.

## 로컬 실행

Docker와 `curl`이 필요합니다. 빌드한 jar를 공식 이미지의 컨테이너에 설치합니다. 인자는 이미지 태그이며 확인할 버전에 맞게 바꿉니다.

1. 컨테이너를 실행하고 jar를 설치합니다.

    ```bash
    scripts/run-sonarqube.sh 26.10.0.133078-community
    ```

    `준비 완료: http://localhost:9000`이 나와야 합니다.

2. 플러그인이 설치되었는지 확인합니다.

    ```bash
    docker logs sonar-l10n-korean | grep "Korean Pack"
    ```

    `Deploy Korean Pack`으로 시작하는 줄이 나와야 합니다.

## 라이선스

GNU LGPL v3입니다. 전문은 [LICENSE](LICENSE)에 있습니다. LGPL v3는 GPL v3를 보충하는 라이선스이므로 GPL v3 전문을 [LICENSE.GPL](LICENSE.GPL)에 함께 둡니다.
