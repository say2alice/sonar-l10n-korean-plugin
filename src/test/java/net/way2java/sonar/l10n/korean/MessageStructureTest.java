/*
 * Copyright (C) 2026 jangb
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package net.way2java.sonar.l10n.korean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class MessageStructureTest {

  // NFR-QL-03
  @Test
  void pluralReducedToOtherKeepsStructure() {
    assertThat(MessageStructure.of("{count, plural, other {이슈 #개}}"))
        .isEqualTo(MessageStructure.of("{count, plural, one {# issue} other {# issues}}"));
  }

  // NFR-QL-02, NFR-QL-03
  @Test
  void removingPluralChangesArguments() {
    assertThat(MessageStructure.of("이슈 {count}개").arguments())
        .isNotEqualTo(MessageStructure.of("{count, plural, one {# issue} other {# issues}}").arguments());
  }

  // NFR-QL-01, NFR-QL-02
  @Test
  void readsNumberedAndNamedArgumentsInsideBranches() {
    assertThat(MessageStructure.of("{0}: {resetDate, select, none {} other {재설정 {resetDate}}}").arguments())
        .containsExactly("0:NONE", "resetDate:NONE", "resetDate:SELECT");
  }

  // NFR-QL-03
  @Test
  void comparesSelectBranches() {
    assertThat(MessageStructure.of("{kind, select, a {가} other {나}}").selectBranches())
        .containsExactly("kind:a", "kind:other");
  }

  // NFR-QL-04
  @Test
  void countsTagsByKind() {
    assertThat(MessageStructure.of("<link>이동</link><br/> 줄 <b>굵게</b>").tags())
        .containsExactly("/b", "/link", "b", "link", "self:br");
  }

  // NFR-QL-03
  @Test
  void rejectsBrokenSyntax() {
    assertThatThrownBy(() -> MessageStructure.of("{count, plural, one {이슈}}"))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> MessageStructure.of("{name"))
        .isInstanceOf(IllegalArgumentException.class);
  }
}
