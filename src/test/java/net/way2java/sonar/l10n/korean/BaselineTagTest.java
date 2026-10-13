/*
 * Copyright (C) 2026 jangb
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package net.way2java.sonar.l10n.korean;

import static org.assertj.core.api.Assertions.assertThat;

import net.way2java.sonar.l10n.korean.generator.BundleFile;
import net.way2java.sonar.l10n.korean.generator.EnglishBundleGenerator;
import org.junit.jupiter.api.Test;

/** 기준 버전을 바꾸고 생성기 실행을 잊은 채 빌드되지 않게 합니다(문서 저장소 ADR-0012). */
class BaselineTagTest {

  // NFR-BD-05
  @Test
  void englishBundleWasGeneratedFromTheConfiguredTag() {
    String configured = System.getProperty("sonarqube.webapp.tag");
    assertThat(configured).as("pom.xml의 sonarqube.webapp.tag가 테스트에 전달되지 않았습니다").isNotBlank();

    String generated = BundleFile.headerValue(Bundles.ENGLISH, EnglishBundleGenerator.TAG_HEADER);

    assertThat(generated)
        .as("영어 기본 번들은 %s에서 만들어졌고 pom.xml은 %s입니다. scripts/english-bundle.sh generate를 실행하십시오",
            generated, configured)
        .isEqualTo(configured);
  }
}
