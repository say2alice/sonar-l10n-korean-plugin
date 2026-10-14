/*
 * Copyright (C) 2026 jangb
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package net.way2java.sonar.l10n.korean;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;
import java.util.TreeSet;
import org.junit.jupiter.api.Test;

/** 영어 기본 번들에 없는 번역은 서버가 전달하지 않으므로 두 파일의 키가 같아야 합니다(문서 저장소 아키텍처 §8.5). */
class BundleKeySyncTest {

  // FR-TR-02, FR-TR-03
  @Test
  void translationHasExactlyTheEnglishKeys() {
    Set<String> english = Bundles.read(Bundles.ENGLISH).keySet();
    Set<String> translation = Bundles.read(Bundles.TRANSLATION).keySet();

    Set<String> missing = new TreeSet<>(english);
    missing.removeAll(translation);
    Set<String> extra = new TreeSet<>(translation);
    extra.removeAll(english);

    assertThat(english).as("영어 기본 번들이 비어 있습니다. scripts/english-bundle.sh generate를 실행하십시오").isNotEmpty();
    assertThat(missing).as("번역 파일에 없는 키 %s. scripts/english-bundle.sh generate로 키를 맞추십시오",
        Bundles.describe(missing)).isEmpty();
    assertThat(extra).as("기준 원본에 없는 키 %s. scripts/english-bundle.sh generate로 키를 맞추십시오",
        Bundles.describe(extra)).isEmpty();
  }
}
