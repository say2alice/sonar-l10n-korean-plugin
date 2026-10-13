/*
 * Copyright (C) 2026 jangb
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package net.way2java.sonar.l10n.korean.generator;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 번역 파일의 키를 새 영어 기본 번들에 맞춥니다(문서 저장소 영문 번들 생성기 설계 §5, ADR-0014).
 */
public final class TranslationSync {

  private TranslationSync() {
  }

  /**
   * 새 영어 기본 번들의 순서로 번역 파일의 값을 돌려줍니다.
   *
   * <p>영문 값이 그대로인 키만 기존 번역을 남깁니다. 새 키와 영문 값이 바뀐 키는 새 영문 값이 되어 미번역 검사에 다시 잡힙니다.
   */
  public static Map<String, String> sync(Map<String, String> oldEnglish, Map<String, String> newEnglish,
      Map<String, String> oldTranslation) {
    Map<String, String> result = new LinkedHashMap<>();
    newEnglish.forEach((key, english) -> {
      String translation = oldTranslation.get(key);
      boolean unchanged = Objects.equals(oldEnglish.get(key), english);
      result.put(key, unchanged && translation != null ? translation : english);
    });
    return result;
  }
}
