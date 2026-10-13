/*
 * Copyright (C) 2026 jangb
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package net.way2java.sonar.l10n.korean;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import org.junit.jupiter.api.Test;

class CoreKoBundleTest {

  private static final String BUNDLE = "/org/sonar/l10n/core_ko.properties";

  // FR-TR-03
  @Test
  void bundleExistsWithoutKeys() throws IOException {
    try (InputStream in = CoreKoBundleTest.class.getResourceAsStream(BUNDLE)) {
      assertThat(in).as("%s가 있어야 합니다", BUNDLE).isNotNull();
      Properties bundle = new Properties();
      bundle.load(new InputStreamReader(in, StandardCharsets.UTF_8));
      assertThat(bundle).as("%s에는 키를 넣지 않습니다", BUNDLE).isEmpty();
    }
  }
}
