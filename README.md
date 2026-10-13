# SonarQube 한국어 언어팩

SonarQube Community Build의 웹 화면을 한국어로 표시하는 언어팩 플러그인입니다.

## 빌드

JDK 25가 `~/.m2/toolchains.xml`에 등록되어 있어야 합니다. Maven은 설치하지 않고 Wrapper로 실행합니다. Wrapper가 Maven 배포본을 처음 내려받을 때 `unzip`과 `sha256sum`(또는 `shasum`)이 필요합니다.

1. jar를 만듭니다.

    ```bash
    ./mvnw clean verify
    ```

    `BUILD SUCCESS`가 나오고 `target/sonar-l10n-korean-plugin-<버전>.jar`가 생겨야 합니다.

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
