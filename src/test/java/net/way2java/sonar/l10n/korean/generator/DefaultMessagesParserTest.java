/*
 * Copyright (C) 2026 jangb
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package net.way2java.sonar.l10n.korean.generator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.junit.jupiter.api.Test;

class DefaultMessagesParserTest {

  static Map<String, String> sample() throws IOException {
    try (InputStream in = DefaultMessagesParserTest.class.getResourceAsStream("/generator/default-sample.ts")) {
      return DefaultMessagesParser.parse(new String(in.readAllBytes(), StandardCharsets.UTF_8));
    }
  }

  // NFR-BD-05
  @Test
  void readsEveryNotationInOrder() throws IOException {
    assertThat(sample()).containsExactly(
        Map.entry("about", "About"),
        Map.entry("about_x", "About {x}"),
        Map.entry("double_quoted", "Say \"hi\""),
        Map.entry("facet_might_have_more_results", "This facet might have more results."),
        Map.entry("onboarding.analysis.jsts.step.footer", "Please visit the {link} for more details."),
        Map.entry("open_in_ide.safari.not_supported.description", "Safari doesn't support it"),
        Map.entry("alerts.operator.=", "equals"),
        Map.entry("alerts.operator.!=", "is not"),
        Map.entry("metric_domain.Integration Tests", "Integration Tests"),
        Map.entry("hotspots.find_in_status_filter_x", " You can find it again."),
        Map.entry("trailing_space", "Ends with space "),
        Map.entry("since_previous_version.short", "Δ version"),
        Map.entry("with_hash", "Value with # and : and = inside"));
  }

  // NFR-BD-05
  @Test
  void rejectsTemplateSubstitution() {
    assertThatThrownBy(() -> DefaultMessagesParser.parse("export const defaultMessages = { a: `x ${y}` };"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("${}");
  }

  // NFR-BD-05
  @Test
  void rejectsDuplicateKey() {
    assertThatThrownBy(() -> DefaultMessagesParser.parse("export const defaultMessages = { a: 'x', 'a': 'y' };"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("두 번");
  }
}
