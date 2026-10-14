/*
 * Copyright (C) 2026 jangb
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package net.way2java.sonar.l10n.korean.generator;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class KeyDiffTest {

  // NFR-MT-01
  @Test
  void separatesArgumentChangesFromValueChanges() {
    Map<String, String> before = new LinkedHashMap<>();
    before.put("gone", "Gone");
    before.put("wording", "{count} issues");
    before.put("args", "{count} issues");
    Map<String, String> after = new LinkedHashMap<>();
    after.put("wording", "{count} open issues");
    after.put("args", "{count, plural, one {# issue} other {# issues}} in {project}");
    after.put("new", "New");

    KeyDiff diff = KeyDiff.between(before, after);

    assertThat(diff.added()).containsExactly("new");
    assertThat(diff.removed()).containsExactly("gone");
    assertThat(diff.changed()).containsExactly("wording", "args");
    assertThat(diff.argumentsChanged()).containsExactly("args");
    assertThat(diff.summary()).isEqualTo("추가 1, 삭제 1, 값 변경 2, 인자 구성 변경 1");
  }

  // NFR-MT-01
  @Test
  void readsArgumentNamesIncludingIcuArguments() {
    assertThat(KeyDiff.argumentNames("{0} of {count, plural, other {# x}} by {user}"))
        .containsExactly("0", "count", "user");
  }
}
