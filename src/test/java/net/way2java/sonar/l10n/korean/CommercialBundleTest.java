/*
 * Copyright (C) 2026 jangb
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package net.way2java.sonar.l10n.korean;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

/**
 * 상용 에디션 서버 번들의 한국어 파일을 검사합니다(문서 저장소 ADR-0016).
 *
 * <p>번역 파일에도 있는 키는 번역 파일의 검사가 맡으므로, 원문 비교와 미번역은 번들에만 있는 키만 봅니다.
 */
class CommercialBundleTest {

  private static final Path REPORT = Path.of("target/untranslated-commercial-keys.txt");

  // FR-TR-05
  @Test
  void koreanFilesHaveExactlyTheCopiedKeys() {
    List<String> bundles = Bundles.commercialBundles();
    assertThat(bundles).as("%s에 영문 사본이 없습니다", Bundles.COMMERCIAL).isNotEmpty();

    List<String> violations = new ArrayList<>();
    for (String bundle : bundles) {
      Map<String, String> english = Bundles.commercialEnglish(bundle);
      Map<String, String> korean = Bundles.commercialKorean(bundle);
      if (!english.keySet().equals(korean.keySet())) {
        violations.add(bundle + ": 영문 사본 " + english.size() + "키, 한국어 파일 " + korean.size() + "키");
      }
    }

    assertThat(violations).as("키가 영문 사본과 다른 한국어 파일 %s. scripts/commercial-bundle.sh sync를 실행하십시오",
        Bundles.describe(violations)).isEmpty();
  }

  // FR-TR-05
  @Test
  void sharedKeysMatchTranslationFile() {
    Map<String, String> translation = Bundles.read(Bundles.TRANSLATION);
    List<String> violations = new ArrayList<>();
    for (String bundle : Bundles.commercialBundles()) {
      Bundles.commercialKorean(bundle).forEach((key, value) -> {
        if (translation.containsKey(key) && !translation.get(key).equals(value)) {
          violations.add(bundle + "_ko " + key);
        }
      });
    }

    assertThat(violations).as("번역 파일과 값이 다른 키 %s. scripts/commercial-bundle.sh sync를 실행하십시오",
        Bundles.describe(violations)).isEmpty();
  }

  // NFR-QL-01, NFR-QL-02, NFR-QL-03, NFR-QL-04
  @Test
  void ownKeysKeepArgumentsSyntaxAndTags() {
    List<String> violations = new ArrayList<>();
    forEachOwnKey((bundle, key, english, korean) -> {
      if (!english.equals(korean)) {
        MessageStructure.differences(english, korean)
            .forEach(difference -> violations.add(bundle + "_ko " + key + ": " + difference));
      }
    });

    assertThat(violations).as("원문과 구성이 다른 번역 %s", Bundles.describe(violations)).isEmpty();
  }

  // FR-TR-05
  @Test
  void reportsUntranslatedOwnKeys() throws IOException {
    KeepEnglishRules rules = KeepEnglishRules.load(Bundles.KEEP_ENGLISH);
    List<String> untranslated = new ArrayList<>();
    forEachOwnKey((bundle, key, english, korean) -> {
      if (rules.untranslated(key, korean, english)) {
        untranslated.add(bundle + "_ko " + key);
      }
    });

    Files.createDirectories(REPORT.getParent());
    Files.write(REPORT, untranslated, StandardCharsets.UTF_8);
    System.out.printf("상용 번들 미번역 키 %d. 목록: %s%n", untranslated.size(), REPORT);

    if (Boolean.getBoolean("translation.requireComplete")) {
      assertThat(untranslated).as("상용 번들 미번역 키 %s", Bundles.describe(untranslated)).isEmpty();
    }
  }

  private interface OwnKey {
    void accept(String bundle, String key, String english, String korean);
  }

  /** 번역 파일에 없는, 번들에만 있는 키를 돕니다. */
  private static void forEachOwnKey(OwnKey action) {
    Map<String, String> translation = Bundles.read(Bundles.TRANSLATION);
    for (String bundle : Bundles.commercialBundles()) {
      Map<String, String> korean = Bundles.commercialKorean(bundle);
      Bundles.commercialEnglish(bundle).forEach((key, english) -> {
        if (!translation.containsKey(key) && korean.containsKey(key)) {
          action.accept(bundle, key, english, korean.get(key));
        }
      });
    }
  }
}
