# SonarQube 한국어 언어팩

SonarQube Community Build의 웹 화면을 한국어로 표시하는 언어팩 플러그인입니다.

| 항목 | 내용 |
| --- | --- |
| 지원 범위 | Community Build 26.1 이상 |
| 내려받기 | <https://gitlab.way2java.net/project/sonar-l10n-korean-plugin/-/releases> |
| 플러그인 키 | `l10nkorean`. 기존 한국어팩 `l10nko`와 다른 플러그인입니다 |

플러그인을 설치해 쓰려면 [사용자 안내](#사용자-안내)를, 번역을 고치거나 플러그인을 개발하려면 [개발자 안내](#개발자-안내)를 읽습니다.

## 사용자 안내

### 주의사항

설치하기 전에 확인합니다.

- Community Build 26.1 이상에 설치합니다. 26.1보다 낮은 버전에 설치하면 서버가 시작되지 않고 로그에 `Plugin Korean Pack [l10nkorean] requires at least Sonar Plugin API version 13.4.3.4290`이 남습니다. 이때는 jar를 지우면 다시 시작됩니다.
- 다른 언어팩과 함께 설치하지 않습니다. 기존 한국어팩(`l10nko`)도 마찬가지입니다. 이 언어팩은 웹 화면의 키를 자체 번들로 제공합니다. 그래서 함께 설치하면 다른 언어팩의 언어로 볼 때 화면 문구의 상당수가 영문으로 나옵니다. 다른 언어팩이 있으면 관리 > 마켓플레이스에서 제거하거나 `extensions/plugins/`에서 jar를 지웁니다.
- 이 언어팩의 jar는 한 버전만 둡니다. 두 버전이 함께 있으면 서버가 `Found two versions of the plugin 'Korean Pack'`을 남기고 시작되지 않습니다.
- 상용 에디션(Developer, Enterprise, Data Center)은 지원 범위 밖입니다. 상용 에디션 서버 본체의 번들 때문에 번역 대신 영문이 나오지 않도록 그 번들의 한국어 파일을 함께 넣었지만, 상용 기능 화면은 모두 확인하지 않았습니다.

### 내려받기

릴리스 페이지 <https://gitlab.way2java.net/project/sonar-l10n-korean-plugin/-/releases>에서 jar를 내려받습니다. 로그인하지 않아도 받을 수 있습니다. 릴리스마다 기준 원본과 지원 범위, 주의사항을 적은 릴리스 노트가 있습니다.

| 항목 | 내용 |
| --- | --- |
| 파일 이름 | `sonar-l10n-korean-plugin-<버전>.jar` |
| 버전 | `<기준 Community Build 메이저>.<마이너>.<재릴리스 번호>`. 예를 들어 `26.10.0`은 Community Build 26.10의 화면 문구를 번역한 첫 릴리스입니다 |
| 고르는 방법 | 가장 최근 릴리스를 받습니다. 서버 버전이 지원 범위 안이면 서버 버전과 릴리스 버전이 달라도 됩니다 |

### 설치

#### Docker 컨테이너

공식 Docker 이미지로 실행 중인 서버에 설치하는 절차입니다. 명령의 `sonarqube`는 컨테이너 이름이고, `26.10.0`은 설치할 버전입니다. 둘 다 실제 값으로 바꿔 입력합니다.

1. 릴리스의 jar를 내려받습니다. 릴리스 페이지의 링크로 받아도 됩니다.

    ```bash
    curl -fLO https://gitlab.way2java.net/api/v4/projects/105/packages/generic/sonar-l10n-korean-plugin/26.10.0/sonar-l10n-korean-plugin-26.10.0.jar
    ```

    현재 폴더에 `sonar-l10n-korean-plugin-26.10.0.jar`가 생겨야 합니다.

2. jar를 컨테이너의 플러그인 폴더에 복사합니다.

    ```bash
    docker cp sonar-l10n-korean-plugin-26.10.0.jar sonarqube:/opt/sonarqube/extensions/plugins/
    ```

    `Successfully copied`로 시작하는 줄이 나와야 합니다.

3. 컨테이너를 다시 시작합니다.

    ```bash
    docker restart sonarqube
    ```

    `sonarqube`가 나와야 합니다. 서버가 다시 뜨는 데 1 ~ 2분이 걸립니다.

4. 플러그인이 배포되었는지 확인합니다.

    ```bash
    docker logs sonarqube | grep "Korean Pack"
    ```

    `Deploy Korean Pack / 26.10.0`이 든 줄이 나와야 합니다. 아무것도 나오지 않으면 2단계의 경로와 서버 버전을 확인합니다.

5. 관리자로 로그인하면 처음 한 번 플러그인 설치 위험 동의 화면이 나옵니다. 내용을 확인하고 동의합니다.

컨테이너를 새로 만들 때 `/opt/sonarqube/extensions`가 볼륨에 있지 않으면 플러그인이 사라지므로 2단계부터 다시 합니다.

#### ZIP으로 설치한 서버

1. 릴리스 페이지에서 jar를 내려받습니다.
2. jar를 SonarQube 설치 폴더의 `extensions/plugins/`에 복사합니다.
3. 서버를 다시 시작합니다. 리눅스에서는 `bin/linux-x86-64/sonar.sh restart`입니다.
4. `logs/web.log`에 `Deploy Korean Pack`이 든 줄이 있는지 확인합니다.

### 한국어로 보기

SonarQube는 브라우저 언어 설정을 따릅니다. 브라우저의 언어 목록에서 한국어를 맨 위에 둡니다. Chrome은 설정 > 언어에서, Firefox는 설정 > 일반 > 언어에서 바꿉니다. 바꾼 뒤 SonarQube 화면을 새로 고칩니다.

### 업데이트와 제거

- 업데이트: `extensions/plugins/`에서 이전 버전의 jar를 지우고 새 jar를 복사한 뒤 서버를 다시 시작합니다.
- 제거: `extensions/plugins/`에서 jar를 지우고 서버를 다시 시작합니다. 화면은 영어로 돌아갑니다.

## 개발자 안내

설계, 결정 기록, 번역 규칙, 진행 현황은 Home 저장소 <https://gitlab.way2java.net/project/ai-managed-project>의 `docs/sonar-l10n-ko/`에 있습니다. 이 문서에서 "문서 저장소의 ○○"는 그 폴더의 문서 이름이며, 그 폴더의 목차에서 찾을 수 있습니다.

### 개발 환경 구성

이 저장소는 Home 저장소의 `project/` 아래에 클론해 작업합니다. 작업 단위와 번역 규칙이 Home 저장소의 문서에 있기 때문입니다. `project/`는 Home이 추적하지 않는 폴더라 클론한 뒤 만듭니다.

```bash
git clone https://gitlab.way2java.net/project/ai-managed-project.git
mkdir ai-managed-project/project
cd ai-managed-project/project
git clone https://gitlab.way2java.net/project/sonar-l10n-korean-plugin.git
```

| 요건 | 쓰는 곳 |
| --- | --- |
| JDK 25 | 컴파일과 테스트. `~/.m2/toolchains.xml`에 등록합니다. 셸의 기본 JDK는 바꾸지 않아도 됩니다 |
| `unzip`, `sha256sum`(또는 `shasum`) | Maven Wrapper가 Maven 배포본을 처음 내려받아 검증할 때. Maven은 설치하지 않습니다 |
| Docker, `curl` | 컨테이너에 설치해 확인할 때 |
| GitHub 접속 | 영문 번들 생성기가 웹앱 원본을 받을 때. 빌드에는 필요 없습니다 |
| 일본어 팩, 러시아어 팩 클론 | 번역 대조표의 참고 열. `https://github.com/cresco-sonar/sonar-l10n-ja`, `https://github.com/1c-syntax/sonar-l10n-ru`를 이 저장소의 `local/reference/sonar-l10n-ja/`, `local/reference/sonar-l10n-ru/`에 클론합니다 |

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

구성을 마쳤으면 코드 저장소 폴더에서 빌드해 확인합니다.

1. jar를 만듭니다.

    ```bash
    ./mvnw clean verify
    ```

    `BUILD SUCCESS`가 나오고 `target/sonar-l10n-korean-plugin-<버전>.jar`가 생겨야 합니다.

### 저장소 구성

SonarQube 웹 화면 문구의 원본은 서버의 `core.properties`에서 웹앱 저장소의 `default.ts`로 변경되었습니다. 그래서 이 언어팩은 웹앱 `default.ts`의 키 전체를 `core_ko.properties`가 아닌 플러그인 자체 번들 `l10nkorean`에 담아 번역합니다.

jar에 들어가는 것은 진입점 클래스 하나와 번들 파일입니다. 나머지는 번역 파일을 만들고 검사하는 도구입니다.

```text
├── pom.xml                         # 빌드 설정. 기준 웹앱 태그 등 기준 버전은 이 파일의 속성에만 둡니다
├── mvnw, .mvn/wrapper/             # Maven Wrapper
├── .gitlab-ci.yml                  # CI 검증 잡과 릴리스 잡
├── release-notes/                  # 릴리스 노트(v<버전>.md)와 템플릿
├── scripts/
│   ├── english-bundle.sh           # 영문 번들 생성기 실행
│   ├── commercial-bundle.sh        # 상용 번들 동기화 도구 실행
│   ├── translation-sheet.sh        # 번역 대조표 생성
│   ├── key-usage.sh                # 웹앱 원본에서 키의 쓰임 검색
│   ├── korean-pack-compare.sh      # 다른 한국어 팩과 번역 비교
│   ├── run-sonarqube.sh            # 로컬 SonarQube 컨테이너에 jar 설치
│   └── release.sh                  # 릴리스 생성
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

설계와 결정의 근거는 문서 저장소의 아키텍처와 결정 기록(ADR)에 있습니다.

### 작업 흐름

브랜치와 병합 요청은 문서 저장소의 Git 브랜치 전략을 따릅니다. 요약하면 다음과 같습니다.

| 브랜치 | 쓰임 |
| --- | --- |
| `main` | 확인을 마친 코드입니다. `main`에 병합하는 것이 곧 릴리스입니다 |
| `develop` | 일상 개발의 통합 지점입니다. 직접 커밋하지 않습니다. 단, README 같은 문서만 고칠 때는 직접 커밋합니다 |
| `feature/<설명>`, `fix/<설명>` | `develop`에서 분기해 병합 요청으로 `develop`에 병합합니다. 병합하면 지웁니다 |

- 커밋 메시지는 Conventional Commit 형식(`type(scope): 설명`)이며 한국어로 씁니다.
- 병합 요청 본문에는 변경 요약, 확인 방법, 관련 요구사항을 적습니다.
- CI는 병합 요청과 `develop`, `main` 푸시에서 `./mvnw verify`를 실행합니다. 미번역 키는 번역 중에는 보고만 하고, `main` 대상 병합 요청에서만 실패로 처리합니다.

### 개발 작업

#### 번역

번역은 작업 단위로 나눠 합니다. 작업 단위는 키 접두사(키의 첫 `.` 앞부분)의 묶음이며, 단위마다 `feature/translate-<단위>` 브랜치와 병합 요청 하나입니다. 단위 목록과 접두사, 단위마다 할 일은 문서 저장소의 번역 작업 단위에, 문체와 표기 규칙은 번역 지침에 있습니다.

| 파일 | 고치는 때 |
| --- | --- |
| `src/main/resources/org/sonar/l10n/l10nkorean_ko.properties` | 값을 번역할 때. 키와 순서는 영문 번들 생성기가 맞추므로 손으로 바꾸지 않습니다 |
| `translation/glossary.tsv` | 새 용어를 정했을 때. 번역과 같은 병합 요청에 넣습니다 |
| `translation/keep-english.txt` | 값 전체를 영문 그대로 두기로 했을 때. 규칙이 없으면 미번역으로 보고됩니다 |

1. 번역 작업 단위의 대조표를 만듭니다. 인자는 단위 이름과 키 접두사이며 `-`는 점이 없는 키입니다. 단위별 접두사는 문서 저장소의 번역 작업 단위에 있습니다.

    ```bash
    scripts/translation-sheet.sh common -
    ```

    `단위 common: <키 수>키, 미번역 <수>키`가 나오고, 키마다 원문, 지금 번역, 일본어 팩과 러시아어 팩의 번역을 나란히 둔 `target/translation-sheet-common.tsv`가 생겨야 합니다. 참고 팩은 이 저장소의 `local/reference/`에서 읽으며, 다른 자리에 두었으면 `L10N_REFERENCE_DIR`로 지정합니다.

2. 대조표를 보며 단위의 값을 약 100키씩 묶어 번역합니다.

3. 묶음마다 빌드해 검사를 통과하면 커밋합니다.

    ```bash
    ./mvnw verify
    ```

    `BUILD SUCCESS`와 `미번역 키 <수> / <전체 키 수>`가 나오고, 목록이 `target/untranslated-keys.txt`에 생겨야 합니다. 단위를 마치면 1의 명령을 다시 실행해 단위의 미번역이 0키인지 확인합니다.

#### 기준 원본 갱신

기준 원본은 `pom.xml`의 `sonarqube.webapp.tag`, `sonarqube.webapp.messages.path`입니다. 이 값을 바꾼 뒤 영어 기본 번들을 다시 만듭니다. GitHub에 접속할 수 있어야 합니다. 전체 절차는 문서 저장소의 기준 원본 갱신 절차에 있습니다.

1. 두 태그 사이의 키 차이를 봅니다. 태그는 비교할 버전으로 바꿔 입력합니다.

    ```bash
    scripts/english-bundle.sh diff sqcb-26.1.0 sqcb-26.10.0
    ```

    둘째 줄에 `추가`, `삭제`, `값 변경`, `인자 구성 변경`의 키 수가 나와야 합니다. 같은 내용이 `target/diff-sqcb-26.1.0-sqcb-26.10.0.txt`에 저장됩니다.

2. 영어 기본 번들을 만들고 번역 파일의 키를 맞춥니다.

    ```bash
    scripts/english-bundle.sh generate
    ```

    `원본: <태그> <키 수>키`로 시작하는 세 줄이 나와야 합니다.

#### 상용 번들 갱신

상용 에디션 서버 본체의 번들 일곱 개(`governance` 등)는 같은 키가 있으면 우리 번역을 가립니다. 그래서 번들마다 한국어 파일을 둡니다. 영문 사본은 `translation/commercial/`에, 한국어 파일은 `src/main/resources/org/sonar/l10n/<번들>_ko.properties`에 있습니다. 근거는 문서 저장소의 ADR-0016에 있습니다.

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

### 테스트

#### 번들 검사

번들 검사는 `./mvnw verify`에 포함되어 다음을 확인합니다.

| 검사 | 확인하는 것 |
| --- | --- |
| 번들 동기화 | 번역 파일과 영어 기본 번들의 키가 같습니다 |
| 기준 태그 | 영어 기본 번들을 만든 웹앱 태그가 `pom.xml`의 기준 태그와 같습니다 |
| 번역 품질 | 인자, ICU 구문, 태그가 원문과 같습니다 |
| 미번역 | 미번역 키의 수와 목록을 `target/untranslated-keys.txt`에 씁니다. `-Dtranslation.requireComplete=true`일 때만 실패합니다 |
| 용어 | 번역에 용어집의 금지 표기가 없습니다 |
| 상용 번들 | 한국어 파일의 키가 영문 사본과 같고, 번역 파일에도 있는 키의 값이 번역 파일과 같습니다. 번들에만 있는 키의 구성과 미번역도 봅니다 |
| 플러그인, `core_ko.properties` | 확장을 등록하지 않고, `core_ko.properties`에 키가 없습니다 |

릴리스와 같은 조건으로 확인할 때는 미번역을 실패로 처리합니다.

1. 미번역을 실패로 처리해 빌드합니다.

    ```bash
    ./mvnw clean verify -Dtranslation.requireComplete=true
    ```

    `미번역 키 0 / <전체 키 수>`, `상용 번들 미번역 키 0`, `BUILD SUCCESS`가 나와야 합니다.

#### Docker 컨테이너에 설치해 확인

개발 중 화면 확인과 릴리스 전 호환 확인(지원 하한 26.1과 최신 버전)에 씁니다. 사용자용 설치 안내가 아닙니다.

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

### 릴리스

릴리스는 `main`에 `v<버전>` 태그를 푸시하면 CI의 `release` 잡이 만듭니다. 전체 절차와 실패할 때의 대응은 문서 저장소의 플러그인 빌드와 릴리스 절차에 있습니다.

| 순서 | 하는 일 |
| ---: | --- |
| 1 | 릴리스 준비 브랜치에서 `pom.xml` 버전의 `-SNAPSHOT`을 떼고, `release-notes/template.md`를 복사해 `release-notes/v<버전>.md`를 씁니다. 템플릿의 헤더와 순서, 표의 행 이름은 바꾸지 않습니다 |
| 2 | 릴리스와 같은 조건으로 빌드하고, 지원 하한과 최신 버전의 공식 이미지에 설치해 확인합니다 |
| 3 | `develop`에 병합한 뒤 `develop`을 `main`에 병합합니다 |
| 4 | `main`에 `v<버전>` 태그를 붙여 푸시합니다. `release` 잡이 `scripts/release.sh`로 빌드하고, jar를 패키지 저장소에 올리고, 릴리스 노트를 본문으로 릴리스를 만듭니다 |
| 5 | 버전은 릴리스 뒤 다음 작업의 첫 병합 요청에서 다음 재릴리스 버전(`<버전>`의 재릴리스 번호 + 1, `-SNAPSHOT`)으로 올립니다. 기준 원본을 갱신할 때는 그 버전으로 바꿉니다 |

러너를 쓸 수 없을 때는 같은 스크립트를 로컬에서 실행합니다. 인증은 `.env`의 `GITLAB_TOKEN`을 씁니다. `.env`는 이 저장소, Home의 `project/`, Home 루트의 순서로 찾습니다.

## 라이선스

GNU LGPL v3입니다. 전문은 [LICENSE](LICENSE)에 있습니다. LGPL v3는 GPL v3를 보충하는 라이선스이므로 GPL v3 전문을 [LICENSE.GPL](LICENSE.GPL)에 함께 둡니다.
