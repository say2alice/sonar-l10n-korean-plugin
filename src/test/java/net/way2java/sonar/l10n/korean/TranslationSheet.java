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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;
import net.way2java.sonar.l10n.korean.generator.BundleFile;

/**
 * 번역 작업 단위의 키마다 원문, 지금 번역, 참고 팩의 번역을 나란히 둔 대조표를 만듭니다.
 *
 * <p>빌드에는 들어가지 않으며 `scripts/translation-sheet.sh`가 실행합니다. 열과 쓰는 법은 문서 저장소 번역 작업 단위 §3.1에
 * 있습니다.
 */
public final class TranslationSheet {

  /** 점이 없는 키(작업 단위 1)를 가리키는 접두사 인자입니다. */
  static final String NO_DOT = "-";

  static final String HEADER = "key\tenglish\tkorean\tstatus\tjapanese\trussian\treference_note";

  private TranslationSheet() {
  }

  record Row(String key, String english, String korean, String status, String japanese, String russian,
      String referenceNote) {

    String toTsv() {
      StringJoiner line = new StringJoiner("\t");
      for (String cell : List.of(key, english, korean, status, japanese, russian, referenceNote)) {
        line.add(escape(cell));
      }
      return line.toString();
    }
  }

  /** 다른 언어 팩의 번역과, 그 팩이 옮긴 영문입니다. 팩이 없으면 두 맵이 비어 있습니다. */
  record ReferencePack(String name, Map<String, String> english, Map<String, String> translation) {

    static ReferencePack load(Path referenceDir, String language) {
      String name = "sonar-l10n-" + language;
      Path dir = referenceDir.resolve(name).resolve("src/main/resources/org/sonar/l10n");
      if (Files.notExists(dir)) {
        System.err.println("경고: 참고 팩이 없어 " + language + " 열을 비웁니다: " + dir);
      }
      return new ReferencePack(language, BundleFile.readOrdered(dir.resolve("core.properties")),
          BundleFile.readOrdered(dir.resolve("core_" + language + ".properties")));
    }

    String translationOf(String key) {
      return translation.getOrDefault(key, "");
    }

    /** 이 팩의 번역이 지금 원문과 다른 영문을 옮긴 것이면 `true`입니다. */
    boolean translatedOtherEnglish(String key, String currentEnglish) {
      return translation.containsKey(key) && !currentEnglish.equals(english.get(key));
    }
  }

  public static void main(String[] args) throws IOException {
    if (args.length != 3 || !args[0].matches("[a-z][a-z-]*") || args[2].isBlank()) {
      System.err.println("사용법: <단위> <참고 팩 폴더> <접두사 목록>");
      System.err.println("        접두사 목록은 공백으로 구분하며, " + NO_DOT + "는 점이 없는 키입니다");
      System.exit(2);
    }
    String unit = args[0];
    Path referenceDir = Path.of(args[1]);
    Set<String> prefixes = Set.of(args[2].strip().split("\\s+"));

    List<Row> rows = rows(BundleFile.readOrdered(Bundles.ENGLISH), BundleFile.readOrdered(Bundles.TRANSLATION),
        KeepEnglishRules.load(Bundles.KEEP_ENGLISH), prefixes,
        ReferencePack.load(referenceDir, "ja"), ReferencePack.load(referenceDir, "ru"));

    List<String> lines = new ArrayList<>();
    lines.add(HEADER);
    rows.forEach(row -> lines.add(row.toTsv()));
    Path out = Path.of("target/translation-sheet-" + unit + ".tsv");
    Files.createDirectories(out.getParent());
    Files.write(out, lines, StandardCharsets.UTF_8);

    long untranslated = rows.stream().filter(row -> row.status().equals("미번역")).count();
    System.out.printf("단위 %s: %d키, 미번역 %d키. 대조표: %s%n", unit, rows.size(), untranslated, out);
  }

  /** 번역 파일의 순서로 단위의 키를 골라 대조표의 행을 만듭니다. */
  static List<Row> rows(Map<String, String> english, Map<String, String> translation, KeepEnglishRules rules,
      Set<String> prefixes, ReferencePack japanese, ReferencePack russian) {
    List<Row> rows = new ArrayList<>();
    translation.forEach((key, korean) -> {
      if (!inUnit(key, prefixes)) {
        return;
      }
      String source = english.getOrDefault(key, "");
      StringJoiner note = new StringJoiner(",");
      for (ReferencePack pack : List.of(japanese, russian)) {
        if (pack.translatedOtherEnglish(key, source)) {
          note.add(pack.name());
        }
      }
      rows.add(new Row(key, source, korean, status(key, korean, source, rules),
          japanese.translationOf(key), russian.translationOf(key), note.toString()));
    });
    return rows;
  }

  /** 작업 단위는 키의 첫 `.` 앞부분으로 정합니다. 점이 없는 키와 이름이 같은 접두사도 접두사로 정합니다. */
  static boolean inUnit(String key, Set<String> prefixes) {
    return prefixes.contains(prefixOf(key));
  }

  /** 키의 첫 `.` 앞부분입니다. 점이 없는 키는 {@link #NO_DOT}입니다(문서 저장소 번역 작업 단위 §1). */
  static String prefixOf(String key) {
    int dot = key.indexOf('.');
    return dot < 0 ? NO_DOT : key.substring(0, dot);
  }

  private static String status(String key, String korean, String english, KeepEnglishRules rules) {
    if (rules.untranslated(key, korean, english)) {
      return "미번역";
    }
    return korean.equals(english) ? "영문 유지" : "번역";
  }

  /** 값 안의 탭과 줄바꿈이 TSV의 열과 행을 나누지 않도록 properties와 같은 형태로 적습니다. */
  static String escape(String cell) {
    return cell.replace("\\", "\\\\").replace("\t", "\\t").replace("\n", "\\n").replace("\r", "\\r");
  }
}
