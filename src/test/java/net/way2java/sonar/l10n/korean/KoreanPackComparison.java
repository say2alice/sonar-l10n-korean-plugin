/*
 * Copyright (C) 2026 jangb
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package net.way2java.sonar.l10n.korean;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;
import net.way2java.sonar.l10n.korean.generator.BundleFile;

/**
 * 번역을 마친 작업 단위를 다른 한국어 팩의 번역과 비교합니다.
 *
 * <p>번역 중에는 쓰지 않고 단위를 마친 뒤 낱말 선택을 검토할 때 씁니다(문서 저장소 번역 지침 §1). 빌드에는 들어가지 않으며
 * `scripts/korean-pack-compare.sh`가 실행합니다. 쓰는 법은 DEVELOPMENT.md의 개발 작업 > 번역 절에 있습니다.
 */
public final class KoreanPackComparison {

  /**
   * 비교할 팩입니다. 같은 계열은 한 표로 셉니다. 클론할 저장소 목록은 DEVELOPMENT.md의 개발 작업 > 번역 절에 있습니다.
   *
   * <p>계열 안에서는 앞에 둔 팩의 번역을 그 계열의 번역으로 봅니다. LeeYudok 팩은 creatinov 팩을 원본으로 표기합니다.
   */
  static final List<KoreanPack> PACKS = List.of(
      new KoreanPack("leeyudok", "creatinov", "LeeYudok_sonar-l10n-ko"),
      new KoreanPack("creatinov", "creatinov", "SonarQubeCommunity_sonar-l10n-ko"),
      new KoreanPack("ronnie", "ronnie", "ronnie-neo-sonarsource_sonar-l10n-ko-plugin"),
      new KoreanPack("hoki", "hoki", "HokiCho8701_sonar-l10n-ko"));

  /** 우리와 다른 번역을 이만큼 많은 계열이 함께 쓰면 검토 대상으로 표시합니다. */
  static final int REVIEW_LINEAGES = 2;

  private KoreanPackComparison() {
  }

  /** `dir`은 이 저장소의 `local/reference/korean-repos/` 아래 `<소유자>_<저장소>` 폴더 이름입니다. */
  record KoreanPack(String name, String lineage, String dir) {
  }

  record Row(String key, String english, String korean, List<String> packs, String majority) {

    String toTsv() {
      StringJoiner line = new StringJoiner("\t");
      line.add(TranslationSheet.escape(key)).add(TranslationSheet.escape(english))
          .add(TranslationSheet.escape(korean));
      packs.forEach(value -> line.add(TranslationSheet.escape(value)));
      return line.add(TranslationSheet.escape(majority)).toString();
    }
  }

  public static void main(String[] args) throws IOException {
    if (args.length != 3 || !args[0].matches("[a-z][a-z-]*") || args[2].isBlank()) {
      System.err.println("사용법: <단위> <참고 자료 폴더> <접두사 목록>");
      System.exit(2);
    }
    String unit = args[0];
    Path repos = Path.of(args[1]).resolve("korean-repos");
    Set<String> prefixes = Set.of(args[2].strip().split("\\s+"));

    List<Map<String, String>> translations = new ArrayList<>();
    for (KoreanPack pack : PACKS) {
      Path file = repos.resolve(pack.dir()).resolve("src/main/resources/org/sonar/l10n/core_ko.properties");
      if (Files.notExists(file)) {
        System.err.println("경고: 한국어 팩이 없어 " + pack.name() + " 열을 비웁니다: " + file);
      }
      translations.add(BundleFile.readOrdered(file));
    }

    List<Row> rows = rows(BundleFile.readOrdered(Bundles.ENGLISH), BundleFile.readOrdered(Bundles.TRANSLATION),
        prefixes, translations);

    List<String> lines = new ArrayList<>();
    StringJoiner header = new StringJoiner("\t").add("key").add("english").add("korean");
    PACKS.forEach(pack -> header.add(pack.name()));
    lines.add(header.add("majority").toString());
    rows.forEach(row -> lines.add(row.toTsv()));
    Path out = Path.of("target/korean-pack-compare-" + unit + ".tsv");
    Files.createDirectories(out.getParent());
    Files.write(out, lines, StandardCharsets.UTF_8);

    System.out.printf("단위 %s: %d키. 비교표: %s%n", unit, rows.size(), out);
    for (int i = 0; i < PACKS.size(); i++) {
      int index = i;
      long has = rows.stream().filter(row -> !row.packs().get(index).isEmpty()).count();
      long same = rows.stream().filter(row -> row.packs().get(index).equals(row.korean())).count();
      System.out.printf("  %s: 단위의 키 %d, 같은 번역 %d%n", PACKS.get(i).name(), has, same);
    }
    List<Row> review = rows.stream().filter(row -> !row.majority().isEmpty()).toList();
    System.out.printf("다른 계열 %d곳 이상이 같은 번역을 쓰고 우리와 다른 키: %d%n", REVIEW_LINEAGES, review.size());
    review.forEach(row -> System.out.printf("  %s | %s | %s | %s%n", row.key(), row.english(), row.korean(),
        row.majority()));
  }

  /** `translations`는 {@link #PACKS}와 같은 순서입니다. */
  static List<Row> rows(Map<String, String> english, Map<String, String> translation, Set<String> prefixes,
      List<Map<String, String>> translations) {
    List<Row> rows = new ArrayList<>();
    translation.forEach((key, korean) -> {
      if (!TranslationSheet.inUnit(key, prefixes)) {
        return;
      }
      List<String> packs = translations.stream().map(t -> t.getOrDefault(key, "")).toList();
      rows.add(new Row(key, english.getOrDefault(key, ""), korean, packs, majority(korean, packs)));
    });
    return rows;
  }

  /** 계열마다 번역 하나를 골라, {@link #REVIEW_LINEAGES}곳 이상이 같고 우리와 다른 번역을 돌려줍니다. 없으면 빈 문자열입니다. */
  static String majority(String korean, List<String> packs) {
    Map<String, String> byLineage = new LinkedHashMap<>();
    for (int i = 0; i < PACKS.size(); i++) {
      if (!packs.get(i).isEmpty()) {
        byLineage.putIfAbsent(PACKS.get(i).lineage(), packs.get(i));
      }
    }
    Map<String, Integer> counts = new LinkedHashMap<>();
    byLineage.values().forEach(value -> counts.merge(value, 1, Integer::sum));
    return counts.entrySet().stream()
        .filter(e -> e.getValue() >= REVIEW_LINEAGES && !e.getKey().equals(korean))
        .map(Map.Entry::getKey)
        .findFirst()
        .orElse("");
  }
}
