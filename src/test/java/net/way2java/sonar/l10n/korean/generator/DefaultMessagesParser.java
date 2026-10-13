/*
 * Copyright (C) 2026 jangb
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package net.way2java.sonar.l10n.korean.generator;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 웹앱 `default.ts`의 `defaultMessages` 객체 리터럴을 키와 값으로 읽습니다.
 *
 * <p>객체 리터럴에 쓰이는 표기만 다룹니다(문서 저장소 영문 번들 생성기 설계 §2). 다른 표기를 만나면 추측하지 않고 실패합니다.
 */
public final class DefaultMessagesParser {

  private static final String START = "export const defaultMessages";

  private final String source;
  private int pos;

  private DefaultMessagesParser(String source) {
    this.source = source;
  }

  /**
   * 원본의 순서대로 키와 값을 돌려줍니다.
   *
   * @throws IllegalArgumentException 객체를 찾지 못했거나 다루지 않는 표기를 만났을 때
   */
  public static Map<String, String> parse(String source) {
    return new DefaultMessagesParser(source).parseObject();
  }

  private Map<String, String> parseObject() {
    int start = source.indexOf(START);
    if (start < 0) {
      throw error("'" + START + "'를 찾지 못했습니다");
    }
    pos = source.indexOf('{', start);
    if (pos < 0) {
      throw error("defaultMessages 객체의 시작을 찾지 못했습니다");
    }
    pos++;
    Map<String, String> messages = new LinkedHashMap<>();
    while (true) {
      skipBlank();
      if (peek() == '}') {
        return messages;
      }
      String key = readKey();
      skipBlank();
      expect(':');
      String value = readValue();
      if (messages.put(key, value) != null) {
        throw error("키가 두 번 나옵니다: " + key);
      }
      skipBlank();
      if (peek() == ',') {
        pos++;
      } else if (peek() != '}') {
        throw error("값 뒤에 ',' 또는 '}'가 와야 합니다");
      }
    }
  }

  private String readKey() {
    char c = peek();
    if (c == '\'' || c == '"') {
      return readString();
    }
    int begin = pos;
    while (pos < source.length() && isIdentifierPart(source.charAt(pos))) {
      pos++;
    }
    if (begin == pos) {
      throw error("키가 와야 합니다");
    }
    return source.substring(begin, pos);
  }

  private String readValue() {
    StringBuilder value = new StringBuilder();
    while (true) {
      skipBlank();
      value.append(readString());
      skipBlank();
      if (peek() != '+') {
        return value.toString();
      }
      pos++;
    }
  }

  private String readString() {
    char quote = peek();
    if (quote != '\'' && quote != '"' && quote != '`') {
      throw error("문자열이 와야 합니다");
    }
    pos++;
    StringBuilder text = new StringBuilder();
    while (true) {
      char c = next();
      if (c == quote) {
        return text.toString();
      }
      if (quote == '`' && c == '$' && peek() == '{') {
        throw error("백틱 문자열의 ${} 치환은 다루지 않습니다");
      }
      if (c == '\\') {
        readEscape(text);
      } else if ((c == '\n' || c == '\r') && quote != '`') {
        throw error("따옴표 문자열 안에 줄바꿈이 있습니다");
      } else {
        text.append(c);
      }
    }
  }

  private void readEscape(StringBuilder text) {
    char c = next();
    switch (c) {
      case 'n' -> text.append('\n');
      case 't' -> text.append('\t');
      case 'r' -> text.append('\r');
      case 'b' -> text.append('\b');
      case 'f' -> text.append('\f');
      case 'v' -> text.append('\u000B');
      case '0' -> text.append('\0');
      case 'u' -> text.appendCodePoint(readUnicodeEscape());
      case '\r' -> {
        if (peek() == '\n') {
          pos++;
        }
      }
      case '\n' -> {
        // 줄 이음입니다. 문자열에 아무것도 더하지 않습니다
      }
      default -> text.append(c);
    }
  }

  private int readUnicodeEscape() {
    if (peek() == '{') {
      int end = source.indexOf('}', pos);
      if (end < 0) {
        throw error("\\u{...}가 닫히지 않았습니다");
      }
      int codePoint = Integer.parseInt(source.substring(pos + 1, end), 16);
      pos = end + 1;
      return codePoint;
    }
    if (pos + 4 > source.length()) {
      throw error("\\u 뒤에 16진수 네 자리가 와야 합니다");
    }
    int codePoint = Integer.parseInt(source.substring(pos, pos + 4), 16);
    pos += 4;
    return codePoint;
  }

  private void skipBlank() {
    while (pos < source.length()) {
      char c = source.charAt(pos);
      if (Character.isWhitespace(c)) {
        pos++;
      } else if (source.startsWith("//", pos)) {
        int end = source.indexOf('\n', pos);
        pos = end < 0 ? source.length() : end + 1;
      } else if (source.startsWith("/*", pos)) {
        int end = source.indexOf("*/", pos + 2);
        if (end < 0) {
          throw error("/* 주석이 닫히지 않았습니다");
        }
        pos = end + 2;
      } else {
        return;
      }
    }
  }

  private void expect(char expected) {
    if (peek() != expected) {
      throw error("'" + expected + "'가 와야 합니다");
    }
    pos++;
  }

  private char peek() {
    if (pos >= source.length()) {
      throw error("파일이 중간에 끝났습니다");
    }
    return source.charAt(pos);
  }

  private char next() {
    char c = peek();
    pos++;
    return c;
  }

  private static boolean isIdentifierPart(char c) {
    return Character.isLetterOrDigit(c) || c == '_' || c == '$';
  }

  private IllegalArgumentException error(String message) {
    int line = 1;
    for (int i = 0; i < Math.min(pos, source.length()); i++) {
      if (source.charAt(i) == '\n') {
        line++;
      }
    }
    return new IllegalArgumentException(line + "번째 줄: " + message);
  }
}
