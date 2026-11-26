# SonarQube 한국어 언어팩 개발 안내

이 저장소를 포크해 번역을 고치거나 플러그인을 개발하는 분을 위한 상세 안내입니다. 개요와 문의 방법은 [README](README.md#개발자-안내)에 있습니다.

## 개발 환경 구성

저장소를 클론합니다. 포크했다면 포크한 저장소의 주소로 바꿉니다.

```bash
git clone https://github.com/say2alice/sonar-l10n-korean-plugin.git
cd sonar-l10n-korean-plugin
```

| 요건 | 쓰는 곳 |
| --- | --- |
| JDK 25 | 컴파일과 테스트. `~/.m2/toolchains.xml`에 등록합니다. 셸의 기본 JDK는 바꾸지 않아도 됩니다 |
| `unzip`, `sha256sum`(또는 `shasum`) | Maven Wrapper가 Maven 배포본을 처음 내려받아 검증할 때. Maven은 설치하지 않습니다 |
| Docker, `curl` | 컨테이너에 설치해 확인할 때 |
| GitHub 접속 | 영문 번들 생성기가 웹앱 원본을 받을 때. 빌드에는 필요 없습니다 |
| 참고 자료(선택) | 번역 대조표의 참고 열과 키의 쓰임 찾기. 받는 방법은 [번역](#번역) 절에 있습니다 |

참고 자료와 `.env`는 `local/` 아래에 두며 Git으로 추적하지 않습니다.

`~/.m2/toolchains.xml`에는 JDK 25 항목이 있어야 합니다. `jdkHome`만 PC에 맞게 바꿉니다.

```xml
<toolchains>
  <toolchain>
    <type>jdk</type>
    <provides>
      <version>25</version>
    </provides>
    <configuration>
      <jdkHome>/path/to/jdk25</jdkHome>
    </configuration>
  </toolchain>
</toolchains>
```

구성을 마쳤으면 빌드해 확인합니다.

1. jar를 만듭니다.

    ```bash
    ./mvnw clean verify
    ```

    `BUILD SUCCESS`가 나오고 `target/sonar-l10n-korean-plugin-<버전>.jar`가 생겨야 합니다. 이 jar를 [설치](README.md#설치) 절차대로 서버에 넣어 쓸 수 있습니다.

## 저장소 구성

SonarQube 웹 화면 문구의 원본은 서버의 `core.properties`에서 웹앱 저장소의 `default.ts`로 변경되었습니다. 그래서 이 언어팩은 웹앱 `default.ts`의 키 전체를 `core_ko.properties`가 아닌 플러그인 자체 번들 `l10nkorean`에 담아 번역합니다.

jar에 들어가는 것은 진입점 클래스 하나와 번들 파일입니다. 나머지는 번역 파일을 만들고 검사하는 도구입니다.

```text
├── README.md, README.en.md         # 사용자 안내와 개발 개요(한국어, 영어)
├── DEVELOPMENT.md                  # 이 개발 안내
├── pom.xml                         # 빌드 설정. 기준 웹앱 태그 등 기준 버전은 이 파일의 속성에만 둡니다
├── mvnw, .mvn/wrapper/             # Maven Wrapper
├── .gitlab-ci.yml                  # 관리자 GitLab의 CI 검증 잡과 릴리스 잡
├── release-notes/                  # 릴리스 노트(v<버전>.md)와 템플릿
├── scripts/
│   ├── english-bundle.sh           # 영문 번들 생성기 실행
│   ├── commercial-bundle.sh        # 상용 번들 동기화 도구 실행
│   ├── translation-sheet.sh        # 번역 대조표 생성
│   ├── key-usage.sh                # 웹앱 원본에서 키의 쓰임 검색
│   ├── korean-pack-compare.sh      # 다른 한국어 팩과 번역 비교
│   ├── run-sonarqube.sh            # 로컬 SonarQube 컨테이너에 jar 설치
│   ├── release.sh                  # 공식 릴리스 생성(관리자용)
│   └── github-release.sh           # GitHub 저장소에 같은 릴리스 생성(관리자용)
├── translation/
│   ├── glossary.tsv                # 용어집
│   ├── keep-english.txt            # 영문 그대로 두는 값의 규칙
│   └── commercial/                 # 상용 에디션 서버 번들의 영문 사본. jar에 들어가지 않습니다
└── src/
    ├── main/                       # jar에 들어가는 부분
    │   ├── java/.../KoreanPackPlugin.java
    │   └── resources/org/sonar/l10n/
    │       ├── l10nkorean.properties
    │       ├── l10nkorean_ko.properties
    │       ├── core_ko.properties
    │       └── <상용 번들>_ko.properties  # governance_ko.properties 등 일곱 개
    └── test/                       # 번들 검사와 도구. jar에 들어가지 않습니다
        └── java/.../korean/
            ├── generator/          # 영문 번들 생성기, 상용 번들 동기화 도구
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
| `<상용 번들>_ko.properties` | 상용 에디션 서버 본체의 번들(`governance` 등)이 우리 번역을 가리지 않게 합니다. 키는 `translation/commercial/`의 영문 사본과 같습니다 | 번역 파일에도 있는 키는 번역 파일에서 고치고 `scripts/commercial-bundle.sh sync`로 옮깁니다. 번들에만 있는 키는 이 파일에서 값만 고칩니다 |

도구는 jar에 넣지 않으면서 빌드와 같은 JDK로 실행하려고 테스트 소스에 두며, `scripts/`의 스크립트가 실행합니다.


## 번역 지침

번역 파일 `l10nkorean_ko.properties`의 값을 고칠 때 지키는 규칙입니다. 원문은 기준 웹앱 태그의 `default.ts`이고, 번역 파일의 영문 값이 원문과 같습니다.

### 문체

화면에서 읽히는 한국어를 우선합니다. 원문의 어순을 따르지 않아도 되지만, 원문에 없는 정보를 더하거나 있는 정보를 빼지 않습니다.

| 원문의 유형 | 규칙 | 원문 | 번역 |
| --- | --- | --- | --- |
| 문장 | 합니다체. 해요체와 해라체는 쓰지 않습니다 | `A fix is required.` | 수정이 필요합니다. |
| 안내, 요청 | "-세요". "Please"는 옮기지 않습니다 | `Please contact the instance administrator.` | 인스턴스 관리자에게 문의하세요. |
| 확인 질문 | "-시겠습니까?" | `Are you sure you want to delete the "{0}" link?` | "{0}" 링크를 삭제하시겠습니까? |
| 버튼, 메뉴, 제목 | 명사형. 마침표를 붙이지 않습니다 | `Back up` | 백업 |
| 상태 값 | "-됨" 또는 명사 | `Acknowledged` | 확인됨 |
| 완료 알림 | "successfully" 같은 수식어는 옮기지 않습니다 | `Quality gate has been successfully updated.` | 품질 게이트를 업데이트했습니다. |

사용자를 가리키는 "you", "your"는 대부분 옮기지 않습니다.

### 표기

- 같은 원문 낱말은 같은 한국어로 씁니다. 기준은 용어집 `translation/glossary.tsv`입니다. 빌드는 번역에 용어집의 `forbidden` 열(쓰지 않는 표기)이 있으면 실패합니다.
- 음차 표기가 널리 쓰이는 용어는 음차로(이슈, 브랜치, 품질 게이트), 널리 쓰이는 표기가 없는 용어는 영문으로(Code Smell) 씁니다. 어느 쪽인지는 용어집에 있습니다.
- 마침표, 물음표, 콜론, 말줄임표는 원문을 따릅니다.
- 따옴표는 큰따옴표(`"`)만 씁니다. 작은따옴표(`'`)는 메시지 구문의 이스케이프 문자입니다.
- 자리표시자(`{name}`) 바로 뒤에 조사를 붙이지 않습니다. 문장을 바꿔 피하고, 바꿀 수 없을 때만 "을(를)", "이(가)"처럼 적습니다. 예: `Are you sure you want to delete branch "{name}"?` → "{name}" 브랜치를 삭제하시겠습니까?

### 메시지 구문

번역문은 원문의 자리표시자, ICU 구문, 태그를 그대로 남깁니다. 빌드의 번역 품질 검사가 확인합니다.

| 구성 | 규칙 |
| --- | --- |
| 자리표시자 | 이름과 개수를 바꾸지 않습니다. 순서는 바꿔도 됩니다 |
| 복수형(`plural`) | `other` 분기 하나만 남깁니다. 예: `{count, plural, other {결과 #개}}`. 분기마다 태그가 있으면 `one` 분기도 번역해 남깁니다 |
| `select` | 분기를 모두 남기고 분기 안의 문구만 번역합니다 |
| 태그(`<link>` 등) | 종류와 개수를 바꾸지 않고 태그 안의 문구를 번역합니다 |

### 영문으로 두는 값

제품명(SonarQube, Community Build), 다른 회사의 상표(GitHub, Jenkins), 언어와 도구, 형식의 이름(Java, JSON), 코드와 설정 키, 약어 그 자체가 표기인 낱말(IDE, API)은 영문으로 둡니다. 튜토리얼이 가리키는 외부 도구 화면의 버튼과 메뉴 이름도 영문으로 둡니다. 문장 안에서는 그 낱말만 영문으로 남기고 나머지를 번역합니다.

값 전체를 영문으로 두는 키는 `translation/keep-english.txt`에 규칙을 더해야 미번역으로 보고되지 않습니다. 규칙은 넓게 잡지 않습니다.

```text
# 키 접두사
prefix alm.github.
# 값 전체와 일치하는 정규식
pattern [A-Z]{2,5}
# 키 하나
key github
```

### 번역 파일 다루기

- `=` 뒤의 값만 고칩니다. 키와 키 순서는 영문 번들 생성기가 맞추므로 손으로 바꾸지 않습니다.
- 한 키를 한 줄에 씁니다. 주석을 두지 않습니다. 생성기가 파일을 다시 쓸 때 사라집니다.
- 원문 값이 공백으로 시작하면 번역도 `\ `로 시작합니다.

## 개발 작업

### 번역

| 파일 | 고치는 때 |
| --- | --- |
| `src/main/resources/org/sonar/l10n/l10nkorean_ko.properties` | 값을 번역할 때 |
| `translation/glossary.tsv` | 새 용어를 정했을 때. 여러 화면에서 되풀이되는 기능 용어와 번역이 갈릴 수 있는 동작, 상태 낱말만 올립니다. 탭으로 구분한 열은 `english`, `korean`, `forbidden`(쓰지 않는 표기, 여럿이면 `\|`로 구분), `note`(쓰임과 근거)입니다 |
| `translation/keep-english.txt` | 값 전체를 영문으로 두기로 했을 때 |

번역 대조표는 키마다 원문, 지금 번역, 중국어 팩과 일본어 팩의 번역을 나란히 둔 TSV입니다. 참고 팩은 이 저장소의 `local/reference/`에 클론합니다. 없으면 참고 열만 비웁니다.

```bash
git clone --depth 1 https://github.com/xuhuisheng/sonar-l10n-zh.git local/reference/sonar-l10n-zh
git clone --depth 1 https://github.com/cresco-sonar/sonar-l10n-ja.git local/reference/sonar-l10n-ja
```

1. 고칠 키의 접두사(키의 첫 `.` 앞부분)로 대조표를 만듭니다. 첫 인자는 결과 파일의 이름이고, 그 뒤가 접두사입니다. `-`는 점이 없는 키입니다.

    ```bash
    scripts/translation-sheet.sh issues issues
    ```

    `단위 issues: <키 수>키, 미번역 <수>키`가 나오고 `target/translation-sheet-issues.tsv`가 생겨야 합니다. 참고 팩의 번역은 참고용이며 원문에서 번역합니다.

    | 열 | 내용 |
    | --- | --- |
    | `key` | 키. 번역 파일의 순서를 따릅니다 |
    | `english` | 원문 |
    | `korean` | 번역 파일의 지금 값 |
    | `status` | `미번역`, `번역`, `영문 유지`. 빌드의 미번역 검사와 같은 규칙으로 판정합니다 |
    | `chinese`, `japanese` | 참고 팩의 번역. 그 팩에 키가 없으면 비웁니다 |
    | `reference_note` | 참고 팩이 옮긴 영문이 지금 원문과 다르면 그 팩을 적습니다. 그 번역은 옛 원문의 것입니다 |

2. 뜻이 여럿인 짧은 낱말(예: `Open`, `Status`)은 웹앱 소스에서 그 키가 쓰이는 화면을 찾아 정합니다. 웹앱 저장소를 기준 태그(`pom.xml`의 `sonarqube.webapp.tag`)로 얕게 클론한 뒤 찾습니다.

    ```bash
    git clone --depth 1 --branch sqcb-26.11.0 https://github.com/SonarSource/sonarqube-webapp.git local/reference/sonarqube-webapp
    scripts/key-usage.sh status open
    ```

    키마다 `== <키>` 줄 다음에 그 키를 쓰는 소스 파일과 줄 번호가 나와야 합니다. 키 이름을 조합해 쓰는 곳은 나오지 않습니다.

3. 번역 파일의 값을 고치고 빌드해 검사를 통과하는지 확인합니다.

    ```bash
    ./mvnw verify
    ```

    `BUILD SUCCESS`와 `미번역 키 <수> / <전체 키 수>`가 나와야 합니다. 미번역 목록은 `target/untranslated-keys.txt`에 있습니다.

번역을 마친 접두사는 다른 한국어 팩과 비교해 낱말 선택을 검토할 수 있습니다. 번역하는 중에는 쓰지 않습니다. 비교할 팩 네 곳을 이 저장소의 `local/reference/korean-repos/` 아래 `<소유자>_<저장소>` 폴더에 클론합니다.

```bash
mkdir -p local/reference/korean-repos && cd local/reference/korean-repos
for r in SonarQubeCommunity/sonar-l10n-ko ronnie-neo-sonarsource/sonar-l10n-ko-plugin HokiCho8701/sonar-l10n-ko LeeYudok/sonar-l10n-ko; do
  git clone --depth 1 "https://github.com/$r.git" "${r/\//_}"
done
cd -
```

1. 대조표와 같은 인자로 비교합니다.

    ```bash
    scripts/korean-pack-compare.sh issues issues
    ```

    `단위 issues: <키 수>키. 비교표: <파일>` 다음에 팩마다 키 수와 같은 번역의 수가 나오고, 다른 계열 두 곳 이상이 같은 번역을 쓰는데 이 팩과 다른 키의 목록이 나와야 합니다. 전체 비교표는 `target/korean-pack-compare-issues.tsv`입니다. LeeYudok 팩은 creatinov 팩을 원본으로 표기하므로 둘은 한 계열로 셉니다.

### 기준 원본 갱신

기준 원본은 `pom.xml`의 `sonarqube.webapp.tag`, `sonarqube.webapp.messages.path`입니다. 새 Community Build가 나오면 이 값을 그 버전의 웹앱 태그로 바꾼 뒤 영어 기본 번들을 다시 만듭니다. GitHub에 접속할 수 있어야 합니다.

1. 두 태그 사이의 키 차이를 봅니다. 태그는 비교할 버전으로 바꿔 입력합니다.

    ```bash
    scripts/english-bundle.sh diff sqcb-26.10.0 sqcb-26.11.0
    ```

    둘째 줄에 `추가`, `삭제`, `값 변경`, `인자 구성 변경`의 키 수가 나와야 합니다. 같은 내용이 `target/diff-sqcb-26.10.0-sqcb-26.11.0.txt`에 저장됩니다.

2. 영어 기본 번들을 만들고 번역 파일의 키를 맞춥니다.

    ```bash
    scripts/english-bundle.sh generate
    ```

    `원본: <태그> <키 수>키`로 시작하는 세 줄이 나와야 합니다. 추가된 키와 값이 바뀐 키는 영문 값이 되어 미번역으로 보고됩니다.

### 상용 번들 갱신

상용 에디션 서버 본체의 번들 일곱 개(`governance` 등)는 같은 키가 있으면 우리 번역을 가립니다. 그래서 번들마다 한국어 파일을 둡니다. 영문 사본은 `translation/commercial/`에, 한국어 파일은 `src/main/resources/org/sonar/l10n/<번들>_ko.properties`에 있습니다.

번역 파일의 값을 고쳤거나 기준 원본을 갱신한 뒤에는 한국어 파일을 맞춥니다.

1. 번역 파일의 값을 상용 번들 한국어 파일에 옮깁니다.

    ```bash
    scripts/commercial-bundle.sh sync
    ```

    번들마다 `<번들>: <키 수>키, 번역 파일과 같은 키 <수>, 번들에만 있는 키 <수>(영문 값 <수>)` 줄이 나와야 합니다.

새 상용 에디션 버전의 번들로 바꿀 때는 공식 이미지에서 서버 jar를 꺼내 영문 사본을 다시 씁니다. Docker가 필요하며, 이미지 태그는 바꿀 버전에 맞게 바꿉니다.

1. 영문 사본과 한국어 파일을 다시 씁니다.

    ```bash
    scripts/commercial-bundle.sh update 2026.5.2-enterprise
    ```

    `상용 서버: <버전>, 번들 <수>개` 다음에 번들마다 키 추가, 삭제, 값 변경 수가 나와야 합니다. 영문이 바뀐 키는 영문 값으로 되돌아가 미번역으로 보고됩니다.

2. 번들에만 있는 키 중 미번역을 확인합니다.

    ```bash
    ./mvnw verify
    ```

    `상용 번들 미번역 키 <수>`가 나오고, 목록이 `target/untranslated-commercial-keys.txt`에 생겨야 합니다.

## 검사와 화면 확인

### 번들 검사

`./mvnw verify`는 다음을 검사합니다. 미번역 외의 검사에서 하나라도 어긋나면 빌드가 실패합니다.

| 검사 | 확인하는 것 |
| --- | --- |
| 번들 동기화 | 번역 파일과 영어 기본 번들의 키가 같습니다 |
| 기준 태그 | 영어 기본 번들을 만든 웹앱 태그가 `pom.xml`의 기준 태그와 같습니다 |
| 번역 품질 | 자리표시자, ICU 구문, 태그가 원문과 같습니다 |
| 미번역 | 미번역 키의 수와 목록을 `target/untranslated-keys.txt`에 씁니다 |
| 용어 | 번역에 용어집의 금지 표기가 없습니다 |
| 상용 번들 | 한국어 파일의 키가 영문 사본과 같고, 번역 파일에도 있는 키의 값이 번역 파일과 같습니다 |

모든 키가 번역되었는지까지 확인하려면 미번역을 실패로 처리합니다.

1. 미번역을 실패로 처리해 빌드합니다.

    ```bash
    ./mvnw clean verify -Dtranslation.requireComplete=true
    ```

    `미번역 키 0 / <전체 키 수>`, `상용 번들 미번역 키 0`, `BUILD SUCCESS`가 나와야 합니다.

### Docker 컨테이너에서 화면 확인

고친 번역을 화면에서 볼 때 씁니다. 빌드한 jar를 공식 이미지의 컨테이너(`sonar-l10n-korean`, 포트 9000)에 설치합니다. 인자는 이미지 태그이며 확인할 버전에 맞게 바꿉니다. 같은 이름의 컨테이너가 있으면 지우고 새로 만듭니다.

1. jar를 만든 뒤 컨테이너를 실행하고 jar를 설치합니다.

    ```bash
    ./mvnw verify
    scripts/run-sonarqube.sh 26.11.0.134175-community
    ```

    `준비 완료: http://localhost:9000`이 나와야 합니다.

2. 플러그인이 설치되었는지 확인합니다.

    ```bash
    docker logs sonar-l10n-korean | grep "Korean Pack"
    ```

    `Deploy Korean Pack`으로 시작하는 줄이 나와야 합니다. 브라우저 언어를 한국어로 두고 <http://localhost:9000>을 엽니다.
