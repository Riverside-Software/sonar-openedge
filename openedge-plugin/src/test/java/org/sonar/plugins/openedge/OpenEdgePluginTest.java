package org.sonar.plugins.openedge;

import org.sonar.api.Plugin;
import org.sonar.api.SonarQubeSide;
import org.sonar.api.SonarRuntime;
import org.sonar.api.internal.SonarRuntimeImpl;
import org.sonar.api.utils.Version;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class OpenEdgePluginTest {

  @Test
  public void testExtensionsSonarLint() {
    SonarRuntime runtime = SonarRuntimeImpl.forSonarLint(Version.parse("6.2"));
    Plugin.Context context = new Plugin.Context(runtime);
    new OpenEdgePlugin().define(context);
    assertThat(context.getExtensions()).hasSize(25);
  }

  @Test
  public void testExtensionsSonarQube() {
    SonarRuntime runtime = SonarRuntimeImpl.forSonarQube(Version.parse("6.2"), SonarQubeSide.SCANNER);
    Plugin.Context context = new Plugin.Context(runtime);
    new OpenEdgePlugin().define(context);
    assertThat(context.getExtensions()).hasSize(27);
  }

}
