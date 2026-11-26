# SonarQube 한국어 언어팩

한국어 | [English](README.en.md)

SonarQube Community Build의 웹 화면을 한국어로 표시하는 언어팩 플러그인입니다.

| 항목 | 내용 |
| --- | --- |
| 지원 범위 | Community Build 26.1 이상 |
| 내려받기 | <https://gitlab.way2java.net/jangb/sonar-l10n-korean-plugin/-/releases><br />GitHub 미러: <https://github.com/say2alice/sonar-l10n-korean-plugin/releases> |
| 플러그인 키 | `l10nkorean`. 기존 한국어팩 `l10nko`와 다른 플러그인입니다 |
| 문의 | GitHub 이슈 <https://github.com/say2alice/sonar-l10n-korean-plugin/issues> |

플러그인을 설치해 쓰려면 [사용자 안내](#사용자-안내)를, 번역을 고치거나 플러그인을 개발하려면 [개발자 안내](#개발자-안내)와 [개발 안내](DEVELOPMENT.md)를 읽습니다.

## 사용자 안내

### 주의사항

- Community Build 26.1 이상에 설치합니다. 26.1보다 낮은 버전에서는 서버가 시작되지 않고 로그에 `Plugin Korean Pack [l10nkorean] requires at least Sonar Plugin API version 13.4.3.4290`이 남습니다. jar를 지우면 다시 시작됩니다.
- 다른 언어팩과 함께 설치하지 않습니다. 기존 한국어팩(`l10nko`)도 마찬가지입니다. 함께 설치하면 다른 언어팩의 언어로 볼 때 화면 문구의 상당수가 영문으로 나옵니다. 다른 언어팩은 관리 > 마켓플레이스에서 제거하거나 `extensions/plugins/`에서 jar를 지웁니다.
- jar는 한 버전만 둡니다. 두 버전이 있으면 서버가 `Found two versions of the plugin 'Korean Pack'`을 남기고 시작되지 않습니다.
- 상용 에디션(Developer, Enterprise, Data Center)은 지원하지 않습니다.

### 내려받기

서버의 Community Build 버전에 맞는 릴리스에서 `sonar-l10n-korean-plugin-<버전>.jar`를 내려받습니다.

| 서버(Community Build) | 릴리스 |
| --- | --- |
| 26.11 | `v26.11.1` |
| 26.1 ~ 26.10 | `v26.10.1` |

다른 릴리스도 26.1 이상에 설치되지만, 서버 버전과 번역 기준이 달라 일부 문구가 영문이나 다른 문장으로 나옵니다.

### 설치

#### Docker 컨테이너

공식 Docker 이미지로 실행 중인 서버에 설치합니다. 명령의 `sonarqube`는 컨테이너 이름, `26.11.1`은 설치할 버전입니다. 실제 값으로 바꿔 입력합니다.

1. jar를 내려받습니다.

    ```bash
    curl -fLO https://gitlab.way2java.net/api/v4/projects/105/packages/generic/sonar-l10n-korean-plugin/26.11.1/sonar-l10n-korean-plugin-26.11.1.jar
    ```

    현재 폴더에 `sonar-l10n-korean-plugin-26.11.1.jar`가 생겨야 합니다.

2. jar를 컨테이너의 플러그인 폴더에 복사합니다.

    ```bash
    docker cp sonar-l10n-korean-plugin-26.11.1.jar sonarqube:/opt/sonarqube/extensions/plugins/
    ```

    아무것도 출력되지 않으면 됩니다. 터미널에서 직접 입력하면 `Successfully copied`로 시작하는 줄이 나옵니다.

3. 컨테이너를 다시 시작합니다.

    ```bash
    docker restart sonarqube
    ```

    `sonarqube`가 나와야 합니다. 서버가 다시 시작되는 데 1 ~ 2분이 걸립니다.

4. 플러그인이 배포되었는지 확인합니다.

    ```bash
    docker logs sonarqube | grep "Korean Pack"
    ```

    `Deploy Korean Pack / 26.11.1`이 든 줄이 나와야 합니다. 나오지 않으면 2단계의 경로와 서버 버전을 확인합니다.

5. 관리자로 처음 로그인하면 플러그인 설치 위험 동의 화면이 나옵니다. 동의합니다.

`/opt/sonarqube/extensions`를 볼륨에 두지 않으면 컨테이너를 새로 만들 때 플러그인이 사라집니다. 이때는 2단계부터 다시 합니다.

#### ZIP으로 설치한 서버

1. 릴리스 페이지에서 jar를 내려받습니다.
2. jar를 SonarQube 설치 폴더의 `extensions/plugins/`에 복사합니다.
3. 서버를 다시 시작합니다. 리눅스에서는 `bin/linux-x86-64/sonar.sh restart`입니다.
4. `logs/web.log`에 `Deploy Korean Pack`이 든 줄이 있는지 확인합니다.

### 한국어로 보기

SonarQube는 브라우저 언어 설정을 따릅니다. 브라우저 언어 목록의 맨 위에 한국어를 두고 SonarQube 화면을 새로 고칩니다. Chrome은 설정 > 언어, Firefox는 설정 > 일반 > 언어에서 바꿉니다.

### 업데이트와 제거

- 업데이트: `extensions/plugins/`에서 이전 jar를 지우고 새 jar를 복사한 뒤 서버를 다시 시작합니다. SonarQube 서버를 올리면 언어팩도 서버 버전에 맞는 릴리스로 바꿉니다.
- 제거: `extensions/plugins/`에서 jar를 지우고 서버를 다시 시작합니다.

## 개발자 안내

이 저장소를 포크해 번역을 고치거나 플러그인을 개발하는 분을 위한 개요입니다. 개발 환경 구성, 저장소 구성, 번역 지침, 작업 절차, 검사 방법은 [개발 안내](DEVELOPMENT.md)에 있습니다.

### 문의와 기여

버그, 번역 오류, 개선 제안은 GitHub 이슈 <https://github.com/say2alice/sonar-l10n-korean-plugin/issues>에 남깁니다. GitHub 저장소는 관리자의 GitLab 저장소를 옮겨 오는 미러라서 풀 리퀘스트를 병합하지 않습니다. 고친 번역은 이슈에 적어 주시면 관리자가 반영합니다.

| 이슈 종류 | 적을 것 |
| --- | --- |
| 번역 오류, 제안 | 화면(메뉴 경로나 주소), 영문 원문, 지금 번역, 제안하는 번역. 키를 알면 키도 적습니다 |
| 버그 | SonarQube 버전과 에디션, 언어팩 버전, 증상, 서버 로그의 관련 줄 |

### 개요

| 항목 | 내용 |
| --- | --- |
| 번역 원본 | SonarQube 웹앱 저장소 <https://github.com/SonarSource/sonarqube-webapp>의 `default.ts`입니다. 기준 태그는 `pom.xml`의 `sonarqube.webapp.tag`입니다 |
| 번역 파일 | `src/main/resources/org/sonar/l10n/l10nkorean_ko.properties`. 값만 고치고, 키와 순서는 영문 번들 생성기가 맞춥니다 |
| 번역 규칙 | 문체, 표기, 메시지 구문 규칙은 개발 안내의 번역 지침에, 용어는 `translation/glossary.tsv`에, 영문으로 두는 값의 규칙은 `translation/keep-english.txt`에 있습니다 |
| 빌드와 검사 | JDK 25와 Maven Wrapper로 빌드합니다. `./mvnw verify`가 키 동기화, 자리표시자와 태그, 용어, 미번역을 검사합니다 |
| 화면 확인 | `scripts/run-sonarqube.sh`가 빌드한 jar를 공식 Docker 이미지의 컨테이너에 설치합니다 |

JDK 25를 `~/.m2/toolchains.xml`에 등록한 뒤 클론해 빌드합니다. 등록 방법은 개발 안내의 개발 환경 구성에 있습니다.

```bash
git clone https://github.com/say2alice/sonar-l10n-korean-plugin.git
cd sonar-l10n-korean-plugin
./mvnw clean verify
```

`BUILD SUCCESS`가 나오고 `target/sonar-l10n-korean-plugin-<버전>.jar`가 생겨야 합니다.

## 라이선스

GNU LGPL v3입니다. 전문은 [LICENSE](LICENSE)에 있습니다. LGPL v3는 GPL v3를 보충하는 라이선스이므로 GPL v3 전문을 [LICENSE.GPL](LICENSE.GPL)에 함께 둡니다.
