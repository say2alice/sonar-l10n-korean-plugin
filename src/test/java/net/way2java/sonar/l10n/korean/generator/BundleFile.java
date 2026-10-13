/*
 * Copyright (C) 2026 jangb
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package net.way2java.sonar.l10n.korean.generator;

import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * 번들 파일을 UTF-8 properties로 읽고 씁니다.
 *
 * <p>쓰기 규칙은 문서 저장소 영문 번들 생성기 설계 §3에 있습니다. 서버는 이 파일을 {@link Properties}로 읽으므로, 그 규칙에서
 * 뜻이 바뀌는 문자만 이스케이프합니다.
 */
public final class BundleFile {

  private BundleFile() {
  }

  /** 파일이 없으면 빈 {@link Properties}를 돌려줍니다. */
  public static Properties read(Path file) {
    Properties bundle = new Properties();
    if (Files.notExists(file)) {
      return bundle;
    }
    try (Reader in = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
      bundle.load(in);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
    return bundle;
  }

  /** {@link #read}와 같이 읽되 파일에 적힌 키 순서를 지킵니다. 파일이 없으면 빈 맵을 돌려줍니다. */
  public static Map<String, String> readOrdered(Path file) {
    Map<String, String> ordered = new LinkedHashMap<>();
    if (Files.notExists(file)) {
      return ordered;
    }
    // Properties.load()가 항목마다 put()을 부르므로, 이스케이프 해석은 Properties에 맡기고 순서만 따로 받습니다
    Properties bundle = new Properties() {
      @Override
      public synchronized Object put(Object key, Object value) {
        ordered.put((String) key, (String) value);
        return super.put(key, value);
      }
    };
    try (Reader in = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
      bundle.load(in);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
    return ordered;
  }

  /** 머리말 줄마다 `# `를 붙이고, 키와 값은 주어진 순서로 한 줄에 하나씩 씁니다. */
  public static void write(Path file, List<String> header, Map<String, String> entries) {
    List<String> lines = new ArrayList<>();
    for (String line : header) {
      lines.add("# " + line);
    }
    entries.forEach((key, value) -> lines.add(escapeKey(key) + "=" + escapeValue(value)));
    try {
      Files.createDirectories(file.toAbsolutePath().getParent());
      Files.write(file, lines, StandardCharsets.UTF_8);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  /** 머리말에서 `# <이름>: <값>` 줄의 값을 찾습니다. 없으면 `null`입니다. */
  public static String headerValue(Path file, String name) {
    String prefix = "# " + name + ": ";
    try {
      for (String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
        if (!line.startsWith("#")) {
          return null;
        }
        if (line.startsWith(prefix)) {
          return line.substring(prefix.length()).strip();
        }
      }
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
    return null;
  }

  static String escapeKey(String key) {
    StringBuilder out = new StringBuilder();
    for (char c : key.toCharArray()) {
      switch (c) {
        case '=', ':', '#', '!', ' ' -> out.append('\\').append(c);
        default -> appendCommon(out, c);
      }
    }
    return out.toString();
  }

  static String escapeValue(String value) {
    StringBuilder out = new StringBuilder();
    for (int i = 0; i < value.length(); i++) {
      char c = value.charAt(i);
      if (i == 0 && c == ' ') {
        // Properties.load()가 값의 앞 공백을 버립니다
        out.append("\\ ");
      } else {
        appendCommon(out, c);
      }
    }
    return out.toString();
  }

  private static void appendCommon(StringBuilder out, char c) {
    switch (c) {
      case '\\' -> out.append("\\\\");
      case '\n' -> out.append("\\n");
      case '\r' -> out.append("\\r");
      case '\t' -> out.append("\\t");
      case '\f' -> out.append("\\f");
      default -> out.append(c);
    }
  }
}
