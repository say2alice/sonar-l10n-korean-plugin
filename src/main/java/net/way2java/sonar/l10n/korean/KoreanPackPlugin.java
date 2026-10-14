/*
 * Copyright (C) 2026 jangb
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package net.way2java.sonar.l10n.korean;

import org.sonar.api.Plugin;

/**
 * 한국어 언어팩의 진입점입니다. 확장을 등록하지 않고, SonarQube가 jar 안의 번역 번들을 직접 읽습니다.
 */
public final class KoreanPackPlugin implements Plugin {

  @Override
  public void define(Context context) {
    // 등록할 확장이 없습니다
  }
}
