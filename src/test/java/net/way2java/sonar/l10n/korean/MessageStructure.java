/*
 * Copyright (C) 2026 jangb
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package net.way2java.sonar.l10n.korean;

import com.ibm.icu.text.MessagePattern;
import com.ibm.icu.text.MessagePattern.ArgType;
import com.ibm.icu.text.MessagePattern.Part;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 메시지에서 번역 전후로 같아야 하는 구성을 뽑습니다(문서 저장소 아키텍처 §8.4, 번역 지침 §4).
 *
 * <p>인자는 이름과 형식(단순, plural, select)의 쌍으로 비교하므로 복수형 구문을 지우면 달라집니다. plural의 분기는 한국어에서
 * `other` 하나로 줄이므로 비교하지 않고, select의 분기는 의미를 가르므로 비교합니다.
 *
 * @param arguments `이름:형식`의 집합
 * @param selectBranches `인자 이름:분기 이름`의 집합
 * @param tags 여는 태그, 닫는 태그, 홀로 쓰는 태그를 구분한 태그 목록. 순서는 비교하지 않으므로 정렬해 둡니다
 */
record MessageStructure(Set<String> arguments, Set<String> selectBranches, List<String> tags) {

  private static final Pattern TAG = Pattern.compile("<(/?)([A-Za-z][A-Za-z0-9_-]*)\\b[^<>]*?(/?)>");

  /**
   * @throws IllegalArgumentException ICU 메시지 구문이 유효하지 않을 때
   */
  static MessageStructure of(String message) {
    MessagePattern pattern = new MessagePattern(message);
    Set<String> arguments = new TreeSet<>();
    Set<String> selectBranches = new TreeSet<>();
    Deque<String> selects = new ArrayDeque<>();
    Deque<ArgType> open = new ArrayDeque<>();
    for (int i = 0; i < pattern.countParts(); i++) {
      Part part = pattern.getPart(i);
      switch (part.getType()) {
        case ARG_START -> {
          String name = pattern.getSubstring(pattern.getPart(i + 1));
          arguments.add(name + ":" + part.getArgType());
          open.push(part.getArgType());
          if (part.getArgType() == ArgType.SELECT) {
            selects.push(name);
          }
        }
        case ARG_LIMIT -> {
          if (open.pop() == ArgType.SELECT) {
            selects.pop();
          }
        }
        case ARG_SELECTOR -> {
          if (open.peek() == ArgType.SELECT) {
            selectBranches.add(selects.peek() + ":" + pattern.getSubstring(part));
          }
        }
        default -> {
          // 나머지 부분은 비교 대상이 아닙니다
        }
      }
    }
    List<String> tags = new ArrayList<>();
    Matcher m = TAG.matcher(message);
    while (m.find()) {
      String kind = !m.group(1).isEmpty() ? "/" : !m.group(3).isEmpty() ? "self:" : "";
      tags.add(kind + m.group(2));
    }
    tags.sort(null);
    return new MessageStructure(arguments, selectBranches, tags);
  }

  /**
   * 번역문의 구성이 원문과 다른 점을 돌려줍니다. 같으면 빈 목록입니다.
   *
   * @throws IllegalArgumentException 원문의 ICU 메시지 구문이 유효하지 않을 때
   */
  static List<String> differences(String source, String translated) {
    MessageStructure expected = of(source);
    MessageStructure actual;
    try {
      actual = of(translated);
    } catch (IllegalArgumentException e) {
      return List.of("ICU 구문 오류 " + e.getMessage());
    }
    List<String> differences = new ArrayList<>();
    if (!expected.arguments().equals(actual.arguments())) {
      differences.add("인자 " + expected.arguments() + " → " + actual.arguments());
    }
    if (!expected.selectBranches().equals(actual.selectBranches())) {
      differences.add("select 분기 " + expected.selectBranches() + " → " + actual.selectBranches());
    }
    if (!expected.tags().equals(actual.tags())) {
      differences.add("태그 " + expected.tags() + " → " + actual.tags());
    }
    return differences;
  }
}
