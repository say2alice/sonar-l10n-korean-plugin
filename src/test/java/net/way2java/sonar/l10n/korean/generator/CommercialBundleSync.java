/*
 * Copyright (C) 2026 jangb
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package net.way2java.sonar.l10n.korean.generator;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * 상용 에디션 서버 번들의 영문 사본과 한국어 파일을 맞춥니다(문서 저장소 ADR-0016).
 *
 * <p>빌드에는 들어가지 않으며 `scripts/commercial-bundle.sh`가 실행합니다.
 */
public final class CommercialBundleSync {

  /** 영문 사본 머리말에서 사본을 꺼낸 상용 서버의 버전을 담는 항목의 이름입니다. */
  public static final String VERSION_HEADER = "상용 서버";

  private static final Pattern SERVER_JAR = Pattern.compile("sonar-application-(.+)\\.jar");
  /** 서버의 영어 번들입니다. `core`와 로캘 파일(`_ko` 등)은 상용 번들이 아닙니다. */
  private static final Pattern ENGLISH_BUNDLE = Pattern.compile("org/sonar/l10n/([a-z][a-z0-9-]*)\\.properties");

  private CommercialBundleSync() {
  }

  public static void main(String[] args) throws IOException {
    if (args.length == 3 && args[0].equals("sync")) {
      sync(Path.of(args[1]), Path.of(args[2]));
    } else if (args.length == 4 && args[0].equals("update")) {
      update(Path.of(args[1]), Path.of(args[2]), Path.of(args[3]));
    } else {
      System.err.println("사용법: sync <영문 사본 폴더> <번들 폴더>");
      System.err.println("        update <상용 서버 jar> <영문 사본 폴더> <번들 폴더>");
      System.exit(2);
    }
  }

  /** 영문 사본마다 한국어 파일을 다시 씁니다. */
  static void sync(Path copyDir, Path bundleDir) {
    Map<String, String> translation = translation(bundleDir);
    for (String bundle : bundles(copyDir)) {
      Map<String, String> english = BundleFile.readOrdered(copyDir.resolve(bundle + ".properties"));
      Path koreanFile = koreanFile(bundleDir, bundle);
      writeKorean(koreanFile, merge(english, BundleFile.readOrdered(koreanFile), translation));
      System.out.println(bundle + ": " + summary(english, BundleFile.readOrdered(koreanFile), translation));
    }
  }

  /** 상용 서버 jar의 번들로 영문 사본을 다시 쓰고 한국어 파일을 맞춥니다. jar에 없는 번들의 두 파일은 지웁니다. */
  static void update(Path serverJar, Path copyDir, Path bundleDir) throws IOException {
    Matcher name = SERVER_JAR.matcher(serverJar.getFileName().toString());
    if (!name.matches()) {
      throw new IllegalArgumentException("상용 서버 jar의 이름은 sonar-application-<버전>.jar여야 합니다: " + serverJar);
    }
    String version = name.group(1);
    Map<String, Map<String, String>> fromJar = readBundles(serverJar);
    if (fromJar.isEmpty()) {
      throw new IllegalStateException("상용 번들을 찾지 못했습니다: " + serverJar);
    }
    Map<String, String> translation = translation(bundleDir);
    System.out.println("상용 서버: " + version + ", 번들 " + fromJar.size() + "개");

    for (String bundle : bundles(copyDir)) {
      if (!fromJar.containsKey(bundle)) {
        Files.delete(copyDir.resolve(bundle + ".properties"));
        Files.deleteIfExists(koreanFile(bundleDir, bundle));
        System.out.println(bundle + ": jar에 없어 지웠습니다");
      }
    }
    for (Map.Entry<String, Map<String, String>> entry : fromJar.entrySet()) {
      String bundle = entry.getKey();
      Map<String, String> english = entry.getValue();
      Path copyFile = copyDir.resolve(bundle + ".properties");
      Map<String, String> oldEnglish = BundleFile.readOrdered(copyFile);

      BundleFile.write(copyFile, List.of(
          "상용 에디션 서버 jar에서 꺼낸 영문 번들의 사본입니다. 상용 번들 동기화 도구가 씁니다. 손으로 고치지 않습니다.",
          VERSION_HEADER + ": " + version,
          "원본: org/sonar/l10n/" + bundle + ".properties"), english);
      assertReadsBack(copyFile, english);

      // 번들에만 있는 키는 영문이 그대로인 키만 번역을 남깁니다
      Path koreanFile = koreanFile(bundleDir, bundle);
      Map<String, String> kept = TranslationSync.sync(oldEnglish, english, BundleFile.readOrdered(koreanFile));
      writeKorean(koreanFile, merge(english, kept, translation));
      System.out.println(bundle + ": " + KeyDiff.between(oldEnglish, english).summary() + ". "
          + summary(english, BundleFile.readOrdered(koreanFile), translation));
    }
  }

  /**
   * 영문 사본의 순서로 한국어 값을 돌려줍니다. 번역 파일에 있는 키는 번역 파일의 값, 번들에만 있는 키는 지금 값, 지금 값이 없으면 영문입니다.
   */
  static Map<String, String> merge(Map<String, String> english, Map<String, String> current,
      Map<String, String> translation) {
    Map<String, String> result = new LinkedHashMap<>();
    english.forEach((key, value) -> {
      if (translation.containsKey(key)) {
        result.put(key, translation.get(key));
      } else {
        result.put(key, current.getOrDefault(key, value));
      }
    });
    return result;
  }

  /** 영문 사본 폴더의 번들 이름을 이름 순으로 돌려줍니다. 폴더가 없으면 빈 목록입니다. */
  public static List<String> bundles(Path copyDir) {
    if (Files.notExists(copyDir)) {
      return List.of();
    }
    try (Stream<Path> files = Files.list(copyDir)) {
      return files.map(file -> file.getFileName().toString())
          .filter(file -> file.endsWith(".properties"))
          .map(file -> file.substring(0, file.length() - ".properties".length()))
          .sorted()
          .toList();
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  public static Path koreanFile(Path bundleDir, String bundle) {
    return bundleDir.resolve(bundle + "_ko.properties");
  }

  static Map<String, Map<String, String>> readBundles(Path serverJar) throws IOException {
    Map<String, Map<String, String>> bundles = new TreeMap<>();
    try (ZipFile jar = new ZipFile(serverJar.toFile())) {
      Enumeration<? extends ZipEntry> entries = jar.entries();
      while (entries.hasMoreElements()) {
        ZipEntry entry = entries.nextElement();
        Matcher m = ENGLISH_BUNDLE.matcher(entry.getName());
        if (!m.matches() || m.group(1).equals("core")) {
          continue;
        }
        try (Reader in = new InputStreamReader(jar.getInputStream(entry), StandardCharsets.UTF_8)) {
          bundles.put(m.group(1), BundleFile.readOrdered(in));
        }
      }
    }
    return bundles;
  }

  private static Map<String, String> translation(Path bundleDir) {
    Map<String, String> translation = BundleFile.readOrdered(bundleDir.resolve(EnglishBundleGenerator.TRANSLATION_BUNDLE));
    if (translation.isEmpty()) {
      throw new IllegalStateException("번역 파일이 없습니다: " + bundleDir);
    }
    return translation;
  }

  private static void writeKorean(Path file, Map<String, String> entries) {
    BundleFile.write(file, List.of(
        "상용 에디션 서버 번들의 한국어 번역입니다. 키와 순서는 상용 번들 동기화 도구가 영문 사본에 맞춥니다.",
        "번역 파일에도 있는 키는 번역 파일의 값으로 다시 쓰이므로 번역 파일에서 고칩니다. 번들에만 있는 키는 이 파일에서 값만 고칩니다."),
        entries);
    assertReadsBack(file, entries);
  }

  private static String summary(Map<String, String> english, Map<String, String> korean,
      Map<String, String> translation) {
    List<String> own = new ArrayList<>(english.keySet());
    own.removeIf(translation::containsKey);
    long untranslated = own.stream().filter(key -> korean.get(key).equals(english.get(key))).count();
    return "%d키, 번역 파일과 같은 키 %d, 번들에만 있는 키 %d(영문 값 %d)".formatted(
        english.size(), english.size() - own.size(), own.size(), untranslated);
  }

  private static void assertReadsBack(Path file, Map<String, String> expected) {
    if (!BundleFile.readOrdered(file).equals(expected)) {
      throw new IllegalStateException(file + "를 다시 읽은 결과가 쓴 값과 다릅니다");
    }
  }
}
