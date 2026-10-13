/*
 * Copyright (C) 2026 jangb
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package net.way2java.sonar.l10n.korean.generator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 두 원본 사이의 키 차이입니다(문서 저장소 영문 번들 생성기 설계 §4).
 *
 * @param added 새 원본에만 있는 키
 * @param removed 이전 원본에만 있는 키
 * @param changed 양쪽에 있고 값이 다른 키
 * @param argumentsChanged {@code changed} 중 인자 이름의 집합이 다른 키
 */
public record KeyDiff(List<String> added, List<String> removed, List<String> changed, List<String> argumentsChanged) {

  private static final Pattern ARGUMENT = Pattern.compile("\\{\\s*([A-Za-z0-9_]+)");

  /** 키 순서는 각 원본의 순서를 따릅니다. 추가와 변경은 새 원본, 삭제는 이전 원본의 순서입니다. */
  public static KeyDiff between(Map<String, String> before, Map<String, String> after) {
    List<String> added = new ArrayList<>();
    List<String> changed = new ArrayList<>();
    List<String> argumentsChanged = new ArrayList<>();
    after.forEach((key, value) -> {
      String old = before.get(key);
      if (old == null) {
        added.add(key);
      } else if (!Objects.equals(old, value)) {
        changed.add(key);
        if (!argumentNames(old).equals(argumentNames(value))) {
          argumentsChanged.add(key);
        }
      }
    });
    List<String> removed = before.keySet().stream().filter(key -> !after.containsKey(key)).toList();
    return new KeyDiff(added, removed, changed, argumentsChanged);
  }

  static Set<String> argumentNames(String message) {
    Set<String> names = new TreeSet<>();
    Matcher m = ARGUMENT.matcher(message);
    while (m.find()) {
      names.add(m.group(1));
    }
    return names;
  }

  /** 구분별 키 수를 한 줄로 돌려줍니다. */
  public String summary() {
    return "추가 %d, 삭제 %d, 값 변경 %d, 인자 구성 변경 %d".formatted(
        added.size(), removed.size(), changed.size(), argumentsChanged.size());
  }

  /** 요약 뒤에 구분별 키 목록을 붙인 보고서입니다. */
  public String report() {
    StringBuilder out = new StringBuilder(summary()).append('\n');
    section(out, "추가", added);
    section(out, "삭제", removed);
    section(out, "값 변경", changed);
    section(out, "인자 구성 변경", argumentsChanged);
    return out.toString();
  }

  private static void section(StringBuilder out, String title, List<String> keys) {
    out.append('\n').append("## ").append(title).append(" (").append(keys.size()).append(")\n");
    keys.forEach(key -> out.append(key).append('\n'));
  }
}
