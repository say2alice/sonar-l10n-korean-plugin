/*
 * Copyright (C) 2026 jangb
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package net.way2java.sonar.l10n.korean;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

/**
 * 번역 중에는 미번역을 보고만 하고, `translation.requireComplete`가 켜진 빌드에서만 실패합니다(문서 저장소 ADR-0014).
 */
class UntranslatedKeyTest {

  private static final Path REPORT = Path.of("target/untranslated-keys.txt");

  // FR-TR-02
  @Test
  void reportsUntranslatedKeys() throws IOException {
    Map<String, String> english = Bundles.read(Bundles.ENGLISH);
    KeepEnglishRules rules = KeepEnglishRules.load(Bundles.KEEP_ENGLISH);
    List<String> untranslated = new ArrayList<>();
    Bundles.read(Bundles.TRANSLATION).forEach((key, value) -> {
      if (rules.untranslated(key, value, english.get(key))) {
        untranslated.add(key);
      }
    });

    Files.createDirectories(REPORT.getParent());
    Files.write(REPORT, untranslated, StandardCharsets.UTF_8);
    System.out.printf("미번역 키 %d / %d. 목록: %s%n", untranslated.size(), english.size(), REPORT);

    if (Boolean.getBoolean("translation.requireComplete")) {
      assertThat(untranslated).as("미번역 키 %s", Bundles.describe(untranslated)).isEmpty();
    }
  }
}
