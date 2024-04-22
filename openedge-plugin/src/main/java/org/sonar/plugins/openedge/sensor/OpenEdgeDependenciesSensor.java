/*
 * OpenEdge plugin for SonarQube
 * Copyright (c) 2015-2024 Riverside Software
 * contact AT riverside DASH software DOT fr
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.plugins.openedge.sensor;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.SonarProduct;
import org.sonar.api.batch.DependedUpon;
import org.sonar.api.batch.fs.FilePredicates;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.InputFile.Type;
import org.sonar.api.batch.sensor.Sensor;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.plugins.openedge.api.Constants;
import org.sonar.plugins.openedge.foundation.OpenEdgeComponents;
import org.sonar.plugins.openedge.foundation.OpenEdgeSettings;

@DependedUpon(value = "PctDependencies")
public class OpenEdgeDependenciesSensor implements Sensor {
  private static final Logger LOG = LoggerFactory.getLogger(OpenEdgeDependenciesSensor.class);

  // IoC
  private final OpenEdgeSettings settings;
  private final OpenEdgeComponents components;

  public OpenEdgeDependenciesSensor(OpenEdgeSettings settings,  OpenEdgeComponents components) {
    this.settings = settings;
    this.components = components;
  }

  @Override
  public void describe(SensorDescriptor descriptor) {
    descriptor.onlyOnLanguage(Constants.LANGUAGE_KEY).name(getClass().getSimpleName());
  }


  @Override
  public void execute(SensorContext context) {
    if (context.runtime().getProduct() == SonarProduct.SONARLINT)
      return;
    settings.init();

    int incImportNum = 0;
    FilePredicates predicates = context.fileSystem().predicates();
    for (InputFile file : context.fileSystem().inputFiles(
        predicates.and(predicates.hasLanguage(Constants.LANGUAGE_KEY), predicates.hasType(Type.MAIN)))) {
      LOG.debug("Looking for include file dependencies of {}", file);
      processFile(file);
      incImportNum++;
      if (context.isCancelled()) {
        LOG.info("Analysis cancelled...");
        return;
      }
    }
    LOG.info("{} files processed", incImportNum);
  }

  private void processFile(InputFile file) {
    Path incFile = settings.getPctIncludeFile(file);
    if ((incFile != null) && Files.isReadable(incFile)) {
      LOG.debug("Import include dependencies from {}", incFile);
      try {
        IncFileProcessor processor = new IncFileProcessor();
        Files.readAllLines(incFile, StandardCharsets.UTF_8).forEach(processor::processLine);
        components.addIncludeDependency(file.uri().toString(), processor.results);
      } catch (IOException caught) {
        // Nothing...
      }
    }
  }

  private class IncFileProcessor implements LineProcessor<List<String>> {
    private List<String> results = new ArrayList<>();

    @Override
    public boolean processLine(String line)  {
      // Line format "includeFileName" "resolvedToPath"
      if ((line == null) || (line.length() < 7) || (line.charAt(line.length() - 1) != '\"'))
        return false;
      int startQuotePos = line.substring(0, line.length() - 1).lastIndexOf('\"');
      if (startQuotePos == -1)
        return false;
      results.add(line.substring(startQuotePos + 1, line.length() - 1));

      return true;
    }

    @Override
    public List<String> getResult() {
      return results;
    }
  }

  public interface LineProcessor<T> {
    boolean processLine(String line);
    T getResult();
  }

}
