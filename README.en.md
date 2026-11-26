# SonarQube Korean Language Pack

[한국어](README.md) | English

A language pack plugin that displays the SonarQube Community Build web interface in Korean.

| Item | Details |
| --- | --- |
| Supported versions | Community Build 26.1 and later |
| Download | <https://gitlab.way2java.net/jangb/sonar-l10n-korean-plugin/-/releases><br />GitHub mirror: <https://github.com/say2alice/sonar-l10n-korean-plugin/releases> |
| Plugin key | `l10nkorean`. This is a different plugin from the existing Korean pack `l10nko` |
| Contact | GitHub issues <https://github.com/say2alice/sonar-l10n-korean-plugin/issues> |

To install and use the plugin, read the [User guide](#user-guide). To fix translations or develop the plugin, read the [Developer guide](#developer-guide) and the [development guide](DEVELOPMENT.md) (in Korean).

## User guide

### Before you install

- Install on Community Build 26.1 or later. On an earlier version the server does not start and the log shows `Plugin Korean Pack [l10nkorean] requires at least Sonar Plugin API version 13.4.3.4290`. Remove the jar and the server starts again.
- Do not install together with another language pack, including the existing Korean pack (`l10nko`). With both installed, much of the interface appears in English when viewed in the other pack's language. Remove other language packs in Administration > Marketplace, or delete their jars from `extensions/plugins/`.
- Keep only one version of the jar. With two versions the server logs `Found two versions of the plugin 'Korean Pack'` and does not start.
- Commercial editions (Developer, Enterprise, Data Center) are not supported.

### Download

Download `sonar-l10n-korean-plugin-<version>.jar` from the release that matches your server's Community Build version.

| Server (Community Build) | Release |
| --- | --- |
| 26.11 | `v26.11.1` |
| 26.1 ~ 26.10 | `v26.10.1` |

Other releases also install on 26.1 and later, but because they translate a different version's text, some text appears in English or as a different sentence.

### Installation

#### Docker container

Install on a server running from the official Docker image. In the commands, `sonarqube` is the container name and `26.11.1` is the version to install. Replace both with your actual values.

1. Download the jar.

    ```bash
    curl -fLO https://gitlab.way2java.net/api/v4/projects/105/packages/generic/sonar-l10n-korean-plugin/26.11.1/sonar-l10n-korean-plugin-26.11.1.jar
    ```

    `sonar-l10n-korean-plugin-26.11.1.jar` should appear in the current folder.

2. Copy the jar to the container's plugin folder.

    ```bash
    docker cp sonar-l10n-korean-plugin-26.11.1.jar sonarqube:/opt/sonarqube/extensions/plugins/
    ```

    No output means success. When you type the command in a terminal, a line starting with `Successfully copied` appears.

3. Restart the container.

    ```bash
    docker restart sonarqube
    ```

    `sonarqube` should appear. The server takes 1 ~ 2 minutes to restart.

4. Check that the plugin was deployed.

    ```bash
    docker logs sonarqube | grep "Korean Pack"
    ```

    A line containing `Deploy Korean Pack / 26.11.1` should appear. If not, check the path in step 2 and the server version.

5. The first time you log in as an administrator, a plugin installation risk consent screen appears. Accept it.

If `/opt/sonarqube/extensions` is not on a volume, the plugin is lost when the container is recreated. Repeat from step 2.

#### Server installed from ZIP

1. Download the jar from the release page.
2. Copy the jar to `extensions/plugins/` in the SonarQube installation folder.
3. Restart the server. On Linux, run `bin/linux-x86-64/sonar.sh restart`.
4. Check that `logs/web.log` has a line containing `Deploy Korean Pack`.

### Viewing in Korean

SonarQube follows the browser language setting. Put Korean at the top of the browser's language list and reload the SonarQube page. In Chrome, use Settings > Languages. In Firefox, use Settings > General > Language.

### Update and removal

- Update: delete the previous jar from `extensions/plugins/`, copy the new jar, and restart the server. When you upgrade the SonarQube server, also switch this pack to the release that matches the server version.
- Removal: delete the jar from `extensions/plugins/` and restart the server.

## Developer guide

An overview for those who fork this repository to fix translations or develop the plugin. The detailed [development guide](DEVELOPMENT.md) (in Korean) covers environment setup, repository layout, translation guidelines, procedures, and checks.

### Contact and contributions

Report bugs, translation errors, and suggestions in GitHub issues <https://github.com/say2alice/sonar-l10n-korean-plugin/issues>. The GitHub repository is a mirror of the maintainer's GitLab repository, so pull requests are not merged. Write your translation fix in an issue and the maintainer will apply it.

| Issue type | What to include |
| --- | --- |
| Translation error, suggestion | The screen (menu path or URL), the English source text, the current translation, and your proposed translation. Include the key if you know it |
| Bug | SonarQube version and edition, language pack version, symptoms, and the relevant server log lines |

### Overview

| Item | Details |
| --- | --- |
| Translation source | `default.ts` in the SonarQube web app repository <https://github.com/SonarSource/sonarqube-webapp>. The baseline tag is `sonarqube.webapp.tag` in `pom.xml` |
| Translation file | `src/main/resources/org/sonar/l10n/l10nkorean_ko.properties`. Edit values only. The English bundle generator keeps the keys and their order |
| Translation rules | Style, notation, and message syntax rules are in the translation guidelines of the development guide. Terms are in `translation/glossary.tsv`, and rules for values kept in English are in `translation/keep-english.txt` |
| Build and checks | Build with JDK 25 and the Maven Wrapper. `./mvnw verify` checks key synchronization, placeholders and tags, terms, and untranslated keys |
| Checking screens | `scripts/run-sonarqube.sh` installs the built jar in a container from the official Docker image |

Register JDK 25 in `~/.m2/toolchains.xml`, then clone and build. Registration is described in the environment setup section of the development guide.

```bash
git clone https://github.com/say2alice/sonar-l10n-korean-plugin.git
cd sonar-l10n-korean-plugin
./mvnw clean verify
```

`BUILD SUCCESS` should appear and `target/sonar-l10n-korean-plugin-<version>.jar` should be created.

## License

GNU LGPL v3. The full text is in [LICENSE](LICENSE). Because LGPL v3 supplements GPL v3, the full text of GPL v3 is also included in [LICENSE.GPL](LICENSE.GPL).
