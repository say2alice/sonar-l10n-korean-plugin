/*
 * Copyright (C) 2026 jangb
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package net.way2java.sonar.l10n.korean.generator;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class BundleFileTest {

  // NFR-BD-05
  @Test
  void writtenFileReadsBackAsParsed(@TempDir Path dir) throws IOException {
    Map<String, String> source = DefaultMessagesParserTest.sample();
    Path file = dir.resolve("bundle.properties");

    BundleFile.write(file, List.of("머리말"), source);

    assertThat(EnglishBundleGenerator.toMap(BundleFile.read(file))).isEqualTo(source);
  }

  // NFR-BD-05
  @Test
  void escapesOnlyWhatPropertiesWouldChange(@TempDir Path dir) {
    Map<String, String> source = new LinkedHashMap<>();
    source.put("a b", " lead\\back\nline");
    source.put("plain", "한글 = 값: #1");
    Path file = dir.resolve("bundle.properties");

    BundleFile.write(file, List.of(), source);

    assertThat(file).hasContent("a\\ b=\\ lead\\\\back\\nline\nplain=한글 = 값: #1");
  }

  // NFR-BD-05
  @Test
  void readsHeaderValue(@TempDir Path dir) {
    Path file = dir.resolve("bundle.properties");
    BundleFile.write(file, List.of("설명", "웹앱 태그: sqcb-26.10.0"), Map.of("k", "v"));

    assertThat(BundleFile.headerValue(file, "웹앱 태그")).isEqualTo("sqcb-26.10.0");
    assertThat(BundleFile.headerValue(file, "없는 항목")).isNull();
  }
}
