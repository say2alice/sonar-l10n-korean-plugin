/*
 * Copyright (C) 2026 jangb
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package net.way2java.sonar.l10n.korean.generator;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class TranslationSyncTest {

  // NFR-MT-02
  @Test
  void keepsOnlyTranslationsWhoseEnglishIsUnchanged() {
    Map<String, String> oldEnglish = Map.of("same", "Save", "changed", "Delete", "removed", "Old");
    Map<String, String> newEnglish = new LinkedHashMap<>();
    newEnglish.put("added", "New");
    newEnglish.put("changed", "Delete project");
    newEnglish.put("same", "Save");
    Map<String, String> oldTranslation = Map.of("same", "저장", "changed", "삭제", "removed", "옛 값");

    Map<String, String> result = TranslationSync.sync(oldEnglish, newEnglish, oldTranslation);

    assertThat(result).containsExactly(
        Map.entry("added", "New"),
        Map.entry("changed", "Delete project"),
        Map.entry("same", "저장"));
  }

  // NFR-MT-02
  @Test
  void writesEnglishWhenThereIsNoTranslationFile() {
    Map<String, String> newEnglish = Map.of("about", "About");

    assertThat(TranslationSync.sync(Map.of(), newEnglish, Map.of())).isEqualTo(newEnglish);
  }
}
