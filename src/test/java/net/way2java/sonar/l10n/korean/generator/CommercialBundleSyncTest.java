/*
 * Copyright (C) 2026 jangb
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package net.way2java.sonar.l10n.korean.generator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class CommercialBundleSyncTest {

  // FR-TR-05
  @Test
  void mergeTakesSharedKeysFromTranslationFile() {
    Map<String, String> english = new LinkedHashMap<>();
    english.put("shared", "Save");
    english.put("own", "Licence");
    english.put("own_new", "Edition");
    Map<String, String> current = Map.of("shared", "옛 값", "own", "라이선스");
    Map<String, String> translation = Map.of("shared", "저장", "other", "기타");

    assertThat(CommercialBundleSync.merge(english, current, translation)).containsExactly(
        Map.entry("shared", "저장"), Map.entry("own", "라이선스"), Map.entry("own_new", "Edition"));
  }

  // FR-TR-05
  @Test
  void updateRewritesCopiesFromServerJar(@TempDir Path dir) throws IOException {
    Path jar = dir.resolve("sonar-application-2026.5.2.1.jar");
    try (ZipOutputStream out = new ZipOutputStream(Files.newOutputStream(jar))) {
      entry(out, "org/sonar/l10n/core.properties", "shared=Save\n");
      entry(out, "org/sonar/l10n/governance.properties", "shared=Save\nkept=Portfolio\nchanged=New text\nadded=Added\n");
      entry(out, "org/sonar/l10n/governance_fr.properties", "shared=Enregistrer\n");
    }
    Path copyDir = Files.createDirectories(dir.resolve("commercial"));
    Path bundleDir = Files.createDirectories(dir.resolve("l10n"));
    BundleFile.write(copyDir.resolve("governance.properties"), List.of(),
        map("shared", "Save", "kept", "Portfolio", "changed", "Old text"));
    BundleFile.write(copyDir.resolve("gone.properties"), List.of(), map("gone", "Gone"));
    BundleFile.write(bundleDir.resolve("gone_ko.properties"), List.of(), map("gone", "사라짐"));
    BundleFile.write(bundleDir.resolve("governance_ko.properties"), List.of(),
        map("shared", "저장", "kept", "포트폴리오", "changed", "옛 문구"));
    BundleFile.write(bundleDir.resolve(EnglishBundleGenerator.TRANSLATION_BUNDLE), List.of(), map("shared", "저장"));

    CommercialBundleSync.update(jar, copyDir, bundleDir);

    assertThat(CommercialBundleSync.bundles(copyDir)).containsExactly("governance");
    assertThat(bundleDir.resolve("gone_ko.properties")).doesNotExist();
    assertThat(BundleFile.headerValue(copyDir.resolve("governance.properties"), CommercialBundleSync.VERSION_HEADER))
        .isEqualTo("2026.5.2.1");
    assertThat(BundleFile.readOrdered(CommercialBundleSync.koreanFile(bundleDir, "governance"))).containsExactly(
        Map.entry("shared", "저장"),
        Map.entry("kept", "포트폴리오"),
        Map.entry("changed", "New text"),
        Map.entry("added", "Added"));
  }

  // FR-TR-05
  @Test
  void rejectsJarWithoutVersionInName(@TempDir Path dir) {
    assertThatThrownBy(() -> CommercialBundleSync.update(dir.resolve("server.jar"), dir, dir))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("sonar-application-<버전>.jar");
  }

  private static void entry(ZipOutputStream out, String name, String content) throws IOException {
    out.putNextEntry(new ZipEntry(name));
    out.write(content.getBytes(StandardCharsets.UTF_8));
    out.closeEntry();
  }

  private static Map<String, String> map(String... keysAndValues) {
    Map<String, String> map = new LinkedHashMap<>();
    for (int i = 0; i < keysAndValues.length; i += 2) {
      map.put(keysAndValues[i], keysAndValues[i + 1]);
    }
    return map;
  }
}
