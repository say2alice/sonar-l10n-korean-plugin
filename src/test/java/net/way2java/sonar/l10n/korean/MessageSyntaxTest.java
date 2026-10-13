/*
 * Copyright (C) 2026 jangb
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package net.way2java.sonar.l10n.korean;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

/** 구문이 깨진 번역은 웹앱 화면에서 바로 드러나므로 빌드에서 막습니다(문서 저장소 아키텍처 §8.4). */
class MessageSyntaxTest {

  // NFR-QL-01, NFR-QL-02, NFR-QL-03, NFR-QL-04
  @Test
  void translationsKeepArgumentsSyntaxAndTags() {
    Map<String, String> english = Bundles.read(Bundles.ENGLISH);
    List<String> violations = new ArrayList<>();
    Bundles.read(Bundles.TRANSLATION).forEach((key, translated) -> {
      String source = english.get(key);
      if (source == null || source.equals(translated)) {
        return;
      }
      MessageStructure expected = MessageStructure.of(source);
      MessageStructure actual;
      try {
        actual = MessageStructure.of(translated);
      } catch (IllegalArgumentException e) {
        violations.add(key + ": ICU 구문 오류 " + e.getMessage());
        return;
      }
      if (!expected.arguments().equals(actual.arguments())) {
        violations.add(key + ": 인자 " + expected.arguments() + " → " + actual.arguments());
      }
      if (!expected.selectBranches().equals(actual.selectBranches())) {
        violations.add(key + ": select 분기 " + expected.selectBranches() + " → " + actual.selectBranches());
      }
      if (!expected.tags().equals(actual.tags())) {
        violations.add(key + ": 태그 " + expected.tags() + " → " + actual.tags());
      }
    });

    assertThat(violations).as("원문과 구성이 다른 번역 %s", Bundles.describe(violations)).isEmpty();
  }

  // NFR-QL-03
  @Test
  void everySourceMessageIsValidIcu() {
    List<String> invalid = new ArrayList<>();
    Bundles.read(Bundles.ENGLISH).forEach((key, source) -> {
      try {
        MessageStructure.of(source);
      } catch (IllegalArgumentException e) {
        invalid.add(key + ": " + e.getMessage());
      }
    });

    assertThat(invalid).as("ICU 구문으로 읽히지 않는 원문 %s", Bundles.describe(invalid)).isEmpty();
  }
}
