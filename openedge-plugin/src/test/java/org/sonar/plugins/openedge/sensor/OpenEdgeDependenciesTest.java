package org.sonar.plugins.openedge.sensor;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;

import java.io.IOException;
import java.util.List;

import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.plugins.openedge.OpenEdgePluginTest;
import org.sonar.plugins.openedge.foundation.OpenEdgeComponents;
import org.sonar.plugins.openedge.foundation.OpenEdgeSettings;
import org.sonar.plugins.openedge.utils.TestProjectSensorContextExtra;
import org.testng.annotations.Test;

public class OpenEdgeDependenciesTest {

  @Test
  public void testBasicDependencies() throws IOException {
    SensorContextTester context = TestProjectSensorContextExtra.createContext();

    OpenEdgeSettings oeSettings = new OpenEdgeSettings(context.config(), context.fileSystem(),
        OpenEdgePluginTest.SONARQUBE_RUNTIME);
    OpenEdgeComponents components = new OpenEdgeComponents(context.config());
    OpenEdgeDependenciesSensor sensor = new OpenEdgeDependenciesSensor(oeSettings, components);
    sensor.execute(context);

    InputFile test3 = context.fileSystem().inputFile(
        context.fileSystem().predicates().hasRelativePath("src/procedures/test3.p"));
    assertNotNull(test3);
    List<String> deps = components.getIncludeDependencies(test3.uri().toString());
    assertFalse(deps.isEmpty());
    String str = deps.get(0);
    assertEquals(str, "src\\procedures\\test3.i");
  }
}
