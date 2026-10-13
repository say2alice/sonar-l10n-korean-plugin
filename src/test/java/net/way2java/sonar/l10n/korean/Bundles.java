/*
 * Copyright (C) 2026 jangb
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package net.way2java.sonar.l10n.korean;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;
import net.way2java.sonar.l10n.korean.generator.BundleFile;
import net.way2java.sonar.l10n.korean.generator.EnglishBundleGenerator;

/** 번들 검사가 함께 쓰는 파일 자리와 읽기입니다. 경로는 프로젝트 폴더 기준입니다. */
final class Bundles {

  static final Path DIR = Path.of("src/main/resources/org/sonar/l10n");
  static final Path ENGLISH = DIR.resolve(EnglishBundleGenerator.ENGLISH_BUNDLE);
  static final Path TRANSLATION = DIR.resolve(EnglishBundleGenerator.TRANSLATION_BUNDLE);
  static final Path GLOSSARY = Path.of("translation/glossary.tsv");
  static final Path KEEP_ENGLISH = Path.of("translation/keep-english.txt");

  private Bundles() {
  }

  /** 키 순으로 정렬해 돌려주므로 검사 결과의 순서가 실행마다 같습니다. */
  static Map<String, String> read(Path file) {
    Map<String, String> map = new TreeMap<>();
    BundleFile.read(file).forEach((key, value) -> map.put((String) key, (String) value));
    return map;
  }

  static String describe(Collection<String> lines) {
    return lines.size() + "건\n" + String.join("\n", lines);
  }
}
