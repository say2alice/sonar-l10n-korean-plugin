/*
 * Copyright (C) 2026 jangb
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package net.way2java.sonar.l10n.korean;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.junit.jupiter.api.Test;

/** 용어집의 금지 표기가 번역문과 상용 번들 한국어 파일에 없는지 봅니다. 용어집 형식은 문서 저장소 번역 지침 §6에 있습니다. */
class GlossaryTest {

  private static final List<String> HEADER = List.of("english", "korean", "forbidden", "note");

  // NFR-QL-05
  @Test
  void translationsAvoidForbiddenTerms() throws IOException {
    List<String> lines = Files.readAllLines(Bundles.GLOSSARY, StandardCharsets.UTF_8);
    assertThat(List.of(lines.get(0).split("\t", -1))).as("%s의 첫 줄", Bundles.GLOSSARY).isEqualTo(HEADER);

    Map<String, String> translation = Bundles.read(Bundles.TRANSLATION);
    // 상용 번들 한국어 파일은 번들에만 있는 키만 봅니다. 나머지 키는 번역 파일과 값이 같습니다
    Map<String, String> values = new TreeMap<>(translation);
    for (String bundle : Bundles.commercialBundles()) {
      Bundles.commercialKorean(bundle).forEach((key, value) -> {
        if (!translation.containsKey(key)) {
          values.put(bundle + "_ko " + key, value);
        }
      });
    }
    List<String> violations = new ArrayList<>();
    for (String line : lines.subList(1, lines.size())) {
      if (line.isBlank()) {
        continue;
      }
      String[] columns = line.split("\t", -1);
      assertThat(columns).as("열이 네 개여야 합니다: %s", line).hasSize(HEADER.size());
      if (columns[2].isBlank()) {
        continue;
      }
      for (String forbidden : columns[2].split("\\|")) {
        values.forEach((key, value) -> {
          if (value.contains(forbidden.strip())) {
            violations.add(key + ": \"" + forbidden.strip() + "\" 대신 \"" + columns[1] + "\"");
          }
        });
      }
    }

    assertThat(violations).as("용어집의 금지 표기 %s", Bundles.describe(violations)).isEmpty();
  }
}
