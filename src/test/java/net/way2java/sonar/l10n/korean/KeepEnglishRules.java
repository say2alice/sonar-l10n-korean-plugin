/*
 * Copyright (C) 2026 jangb
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package net.way2java.sonar.l10n.korean;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * 번역하지 않고 영문 그대로 두는 값의 규칙입니다. 형식은 문서 저장소 번역 지침 §5에 있습니다.
 */
final class KeepEnglishRules {

  private final List<String> prefixes = new ArrayList<>();
  private final List<Pattern> patterns = new ArrayList<>();
  private final Set<String> keys = new HashSet<>();

  /**
   * @throws IllegalArgumentException 알 수 없는 규칙이나 잘못된 정규식이 있을 때
   */
  static KeepEnglishRules load(Path file) {
    KeepEnglishRules rules = new KeepEnglishRules();
    List<String> lines;
    try {
      lines = Files.readAllLines(file, StandardCharsets.UTF_8);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
    for (int i = 0; i < lines.size(); i++) {
      String line = lines.get(i).strip();
      if (line.isEmpty() || line.startsWith("#")) {
        continue;
      }
      String[] rule = line.split("\\s+", 2);
      if (rule.length != 2) {
        throw new IllegalArgumentException(file + " " + (i + 1) + "번째 줄: 규칙과 값이 함께 있어야 합니다");
      }
      switch (rule[0]) {
        case "prefix" -> rules.prefixes.add(rule[1]);
        case "pattern" -> rules.patterns.add(Pattern.compile(rule[1]));
        case "key" -> rules.keys.add(rule[1]);
        default -> throw new IllegalArgumentException(file + " " + (i + 1) + "번째 줄: 알 수 없는 규칙 " + rule[0]);
      }
    }
    return rules;
  }

  /** 값 정규식은 값 전체와 일치해야 합니다. */
  boolean keeps(String key, String value) {
    return keys.contains(key)
        || prefixes.stream().anyMatch(key::startsWith)
        || patterns.stream().anyMatch(p -> p.matcher(value).matches());
  }
}
