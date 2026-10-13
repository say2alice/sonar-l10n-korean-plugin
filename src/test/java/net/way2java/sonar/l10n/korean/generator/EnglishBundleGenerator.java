/*
 * Copyright (C) 2026 jangb
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package net.way2java.sonar.l10n.korean.generator;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * 웹앱 `default.ts`에서 영어 기본 번들을 만들고 번역 파일을 맞추거나, 두 웹앱 태그의 키 차이를 출력합니다.
 *
 * <p>빌드에는 들어가지 않으며 `scripts/english-bundle.sh`가 실행합니다(문서 저장소 ADR-0012, ADR-0015).
 */
public final class EnglishBundleGenerator {

  /** 영어 기본 번들 머리말에서 기준 태그를 담는 항목의 이름입니다. 기준 태그 검사가 읽습니다. */
  public static final String TAG_HEADER = "웹앱 태그";

  public static final String ENGLISH_BUNDLE = "l10nkorean.properties";
  public static final String TRANSLATION_BUNDLE = "l10nkorean_ko.properties";

  private static final String RAW_BASE = "https://raw.githubusercontent.com/SonarSource/sonarqube-webapp/";

  private EnglishBundleGenerator() {
  }

  public static void main(String[] args) throws IOException, InterruptedException {
    if (args.length == 4 && args[0].equals("generate")) {
      generate(args[1], args[2], Path.of(args[3]));
    } else if (args.length == 4 && args[0].equals("diff")) {
      diff(args[1], args[2], args[3]);
    } else {
      System.err.println("사용법: generate <웹앱 태그> <default.ts 경로> <번들 폴더>");
      System.err.println("        diff <default.ts 경로> <이전 태그> <새 태그>");
      System.exit(2);
    }
  }

  static void generate(String tag, String path, Path bundleDir) throws IOException, InterruptedException {
    Map<String, String> english = DefaultMessagesParser.parse(fetch(tag, path));
    Path englishFile = bundleDir.resolve(ENGLISH_BUNDLE);
    Path translationFile = bundleDir.resolve(TRANSLATION_BUNDLE);
    Map<String, String> oldEnglish = toMap(BundleFile.read(englishFile));
    Map<String, String> oldTranslation = toMap(BundleFile.read(translationFile));

    BundleFile.write(englishFile, List.of(
        "영문 번들 생성기가 만든 영어 기본 번들입니다. 손으로 고치지 않습니다.",
        TAG_HEADER + ": " + tag,
        "원본: " + path), english);
    assertReadsBack(englishFile, english);

    Map<String, String> translation = TranslationSync.sync(oldEnglish, english, oldTranslation);
    BundleFile.write(translationFile, List.of(
        "한국어 번역입니다. 값만 고칩니다. 키와 순서는 영문 번들 생성기가 맞추며, 다른 주석은 남지 않습니다."), translation);
    assertReadsBack(translationFile, translation);

    long kept = translation.entrySet().stream()
        .filter(e -> !e.getValue().equals(english.get(e.getKey())))
        .count();
    System.out.println("원본: " + tag + " " + english.size() + "키");
    System.out.println("이전 영어 기본 번들 대비: " + KeyDiff.between(oldEnglish, english).summary());
    System.out.println("번역 파일: 번역이 남은 키 " + kept + ", 영문 값인 키 " + (translation.size() - kept));
  }

  static void diff(String path, String fromTag, String toTag) throws IOException, InterruptedException {
    Map<String, String> before = DefaultMessagesParser.parse(fetch(fromTag, path));
    Map<String, String> after = DefaultMessagesParser.parse(fetch(toTag, path));
    System.out.println("# " + fromTag + " → " + toTag);
    System.out.print(KeyDiff.between(before, after).report());
  }

  private static String fetch(String tag, String path) throws IOException, InterruptedException {
    URI uri = URI.create(RAW_BASE + tag + "/" + path);
    HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(30)).build();
    HttpResponse<String> response = client.send(HttpRequest.newBuilder(uri).build(),
        HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
    if (response.statusCode() != 200) {
      // 경로가 바뀐 태그에서 다른 파일을 찾아 읽지 않도록 여기서 멈춥니다(문서 저장소 아키텍처 §10.1 위험 3)
      throw new IllegalStateException("원본을 받지 못했습니다(HTTP " + response.statusCode() + "): " + uri);
    }
    return response.body();
  }

  private static void assertReadsBack(Path file, Map<String, String> expected) {
    if (!toMap(BundleFile.read(file)).equals(expected)) {
      throw new IllegalStateException(file + "를 다시 읽은 결과가 쓴 값과 다릅니다");
    }
  }

  static Map<String, String> toMap(Properties properties) {
    Map<String, String> map = new HashMap<>();
    properties.forEach((key, value) -> map.put((String) key, (String) value));
    return map;
  }
}
