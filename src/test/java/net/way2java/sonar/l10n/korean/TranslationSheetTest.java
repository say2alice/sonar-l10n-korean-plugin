/*
 * Copyright (C) 2026 jangb
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package net.way2java.sonar.l10n.korean;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.way2java.sonar.l10n.korean.TranslationSheet.ReferencePack;
import net.way2java.sonar.l10n.korean.TranslationSheet.Row;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class TranslationSheetTest {

  private static final ReferencePack NO_PACK = new ReferencePack("ru", Map.of(), Map.of());

  // FR-TR-02
  @Test
  void selectsKeysByFirstSegmentInTranslationFileOrder(@TempDir Path dir) throws IOException {
    Map<String, String> translation = new LinkedHashMap<>();
    translation.put("save", "Save");
    translation.put("issues.title", "Issues");
    translation.put("issue.type", "Type");
    translation.put("about", "About");

    List<Row> common = TranslationSheet.rows(translation, translation, rules(dir, ""), Set.of("-"), NO_PACK, NO_PACK);
    List<Row> issue = TranslationSheet.rows(translation, translation, rules(dir, ""), Set.of("issue"), NO_PACK,
        NO_PACK);

    assertThat(common).extracting(Row::key).containsExactly("save", "about");
    assertThat(issue).extracting(Row::key).containsExactly("issue.type");
  }

  // FR-TR-02
  @Test
  void judgesStatusWithKeepEnglishRules(@TempDir Path dir) throws IOException {
    Map<String, String> english = Map.of("save", "Save", "json", "JSON", "cancel", "Cancel");
    Map<String, String> translation = new LinkedHashMap<>();
    translation.put("save", "저장");
    translation.put("json", "JSON");
    translation.put("cancel", "Cancel");

    List<Row> rows = TranslationSheet.rows(english, translation, rules(dir, "key json"), Set.of("-"), NO_PACK,
        NO_PACK);

    assertThat(rows).extracting(Row::status).containsExactly("번역", "영문 유지", "미번역");
  }

  // FR-TR-01
  @Test
  void marksReferenceTranslatedFromOtherEnglish(@TempDir Path dir) throws IOException {
    Map<String, String> english = Map.of("save", "Save", "close", "Close", "new_key", "New");
    ReferencePack japanese = new ReferencePack("ja",
        Map.of("save", "Save", "close", "Close window"),
        Map.of("save", "保存", "close", "ウィンドウを閉じる"));

    List<Row> rows = TranslationSheet.rows(english, new LinkedHashMap<>(english), rules(dir, ""), Set.of("-"),
        japanese, NO_PACK);

    assertThat(rows).filteredOn(row -> row.key().equals("save"))
        .singleElement().extracting(Row::japanese, Row::referenceNote).containsExactly("保存", "");
    assertThat(rows).filteredOn(row -> row.key().equals("close"))
        .singleElement().extracting(Row::referenceNote).isEqualTo("ja");
    assertThat(rows).filteredOn(row -> row.key().equals("new_key"))
        .singleElement().extracting(Row::japanese, Row::referenceNote).containsExactly("", "");
  }

  // FR-TR-02
  @Test
  void escapesTabsAndLineBreaksInCells() {
    Row row = new Row("k", "a\tb", "줄\n바꿈", "번역", "", "", "");

    assertThat(row.toTsv()).isEqualTo("k\ta\\tb\t줄\\n바꿈\t번역\t\t\t");
  }

  private static KeepEnglishRules rules(Path dir, String content) throws IOException {
    Path file = dir.resolve("keep-english.txt");
    Files.writeString(file, content);
    return KeepEnglishRules.load(file);
  }
}
