/*
 * Copyright (C) 2026 jangb
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package net.way2java.sonar.l10n.korean;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.sonar.api.Plugin;
import org.sonar.api.SonarEdition;
import org.sonar.api.SonarQubeSide;
import org.sonar.api.internal.SonarRuntimeImpl;
import org.sonar.api.utils.Version;

class KoreanPackPluginTest {

  // FR-PK-03
  @Test
  void defineRegistersNoExtension() {
    Plugin.Context context = new Plugin.Context(
        SonarRuntimeImpl.forSonarQube(Version.create(25, 12), SonarQubeSide.SERVER, SonarEdition.COMMUNITY));

    new KoreanPackPlugin().define(context);

    assertThat(context.getExtensions()).isEmpty();
  }
}
