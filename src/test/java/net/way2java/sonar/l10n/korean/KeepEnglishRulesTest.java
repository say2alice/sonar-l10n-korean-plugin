/*
 * Copyright (C) 2026 jangb
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package net.way2java.sonar.l10n.korean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class KeepEnglishRulesTest {

  // FR-TR-02
  @Test
  void keepsByKeyPrefixAndWholeValuePattern(@TempDir Path dir) throws IOException {
    KeepEnglishRules rules = rules(dir, "prefix alm.github.\npattern [A-Z]{2,5}\nkey github\n");

    assertThat(rules.keeps("alm.github.title", "Connect")).isTrue();
    assertThat(rules.keeps("alm.gitlab.title", "Connect")).isFalse();
    assertThat(rules.keeps("export", "JSON")).isTrue();
    assertThat(rules.keeps("export", "JSON file")).as("정규식은 값 전체와 일치해야 합니다").isFalse();
    assertThat(rules.keeps("github", "GitHub")).isTrue();
  }

  // FR-TR-02
  @Test
  void untranslatedOnlyWhenEqualToEnglishAndNotKept(@TempDir Path dir) throws IOException {
    KeepEnglishRules rules = rules(dir, "pattern [A-Z]{2,5}\n");

    assertThat(rules.untranslated("save", "Save", "Save")).isTrue();
    assertThat(rules.untranslated("save", "저장", "Save")).isFalse();
    assertThat(rules.untranslated("json", "JSON", "JSON")).isFalse();
  }

  // FR-TR-02
  @Test
  void skipsCommentsAndBlankLines(@TempDir Path dir) throws IOException {
    KeepEnglishRules rules = rules(dir, "# key save\n\n   \n");

    assertThat(rules.keeps("save", "Save")).isFalse();
  }

  // FR-TR-02
  @Test
  void rejectsMalformedLines(@TempDir Path dir) {
    assertThatThrownBy(() -> rules(dir, "suffix .title\n"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("1번째 줄")
        .hasMessageContaining("알 수 없는 규칙");
    assertThatThrownBy(() -> rules(dir, "# 주석\nprefix\n"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("2번째 줄");
    assertThatThrownBy(() -> rules(dir, "pattern [A-Z\n"))
        .isInstanceOf(IllegalArgumentException.class);
  }

  private static KeepEnglishRules rules(Path dir, String content) throws IOException {
    Path file = dir.resolve("keep-english.txt");
    Files.writeString(file, content);
    return KeepEnglishRules.load(file);
  }
}
