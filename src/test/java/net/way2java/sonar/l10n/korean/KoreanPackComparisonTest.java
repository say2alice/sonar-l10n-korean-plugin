/*
 * Copyright (C) 2026 jangb
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package net.way2java.sonar.l10n.korean;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.way2java.sonar.l10n.korean.KoreanPackComparison.Row;
import org.junit.jupiter.api.Test;

class KoreanPackComparisonTest {

  // NFR-QL-05
  @Test
  void countsSameLineageOnce() {
    // leeyudok와 creatinov는 한 계열이므로 둘이 같아도 한 표입니다
    assertThat(KoreanPackComparison.majority("재설정", List.of("초기화", "초기화", "재설정", ""))).isEmpty();
  }

  // NFR-QL-05
  @Test
  void reportsTranslationSharedByTwoLineages() {
    assertThat(KoreanPackComparison.majority("다시 시작", List.of("재시작", "", "재시작", "다시 시작")))
        .isEqualTo("재시작");
    assertThat(KoreanPackComparison.majority("재시작", List.of("재시작", "", "재시작", "재시작"))).isEmpty();
  }

  // NFR-QL-05
  @Test
  void comparesOnlyUnitKeysInTranslationFileOrder() {
    Map<String, String> translation = new LinkedHashMap<>();
    translation.put("save", "저장");
    translation.put("issue.type", "유형");
    translation.put("about", "정보");
    List<Map<String, String>> packs = List.of(Map.of("save", "저장"), Map.of(), Map.of("about", "소개"), Map.of());

    List<Row> rows = KoreanPackComparison.rows(translation, translation, Set.of("-"), packs);

    assertThat(rows).extracting(Row::key).containsExactly("save", "about");
    assertThat(rows.get(1).packs()).containsExactly("", "", "소개", "");
  }
}
