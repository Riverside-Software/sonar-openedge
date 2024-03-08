/*
 * OpenEdge plugin for SonarQube
 * Copyright (c) 2015-2023 Riverside Software
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
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

import com.google.common.base.Joiner;

@DependedUpon(value = "PctDependencies")
public class OpenEdgeDependenciesSensor implements Sensor {
  private static final Logger LOGGER = LoggerFactory.getLogger(OpenEdgeDependenciesSensor.class);

  // IoC
  private final OpenEdgeSettings settings;
  private final OpenEdgeComponents components;
  // Internal use 
  private boolean useCache;

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
    useCache = context.isCacheEnabled() && settings.useCache();

    int incImportNum = 0;
    FilePredicates predicates = context.fileSystem().predicates();
    for (InputFile file : context.fileSystem().inputFiles(
        predicates.and(predicates.hasLanguage(Constants.LANGUAGE_KEY), predicates.hasType(Type.MAIN)))) {
      LOGGER.info("Looking for include file dependencies of {}", file);
      processFile(context, file);
      incImportNum++;
      if (context.isCancelled()) {
        LOGGER.info("Analysis cancelled...");
        return;
      }
    }
    LOGGER.info("{} files processed", incImportNum);
  }

  private void processFile(SensorContext context, InputFile file) {
    Path incFile = settings.getPctIncludeFile(file);
    LOGGER.info("Include file: {} for {}" , incFile , file.relativePath());

    if (useCache && (incFile != null) && context.previousCache().contains(file.relativePath())) {
      LOGGER.info("read from cache");
      List<String> lst = new ArrayList<>();
      try (InputStream stream = context.previousCache().read(file.relativePath());
          InputStreamReader r1 = new InputStreamReader(stream);
          BufferedReader r2 = new BufferedReader(new InputStreamReader(stream)) ) {
        String str = r2.readLine();
        while (str != null) {
          lst.add(str);
          str = r2.readLine();
        }
        components.addIncludeDependency(file.uri().toString(), lst);
        LOGGER.info("Add {} lines from previous version", lst.size());
      } catch (IOException caught) {
        LOGGER.error("IOExc", caught);
      }
    } else if ((incFile != null) && Files.isReadable(incFile)) {
      LOGGER.info("Import include dependencies from {}", incFile);
      try {
        IncFileProcessor processor = new IncFileProcessor();
        Files.readAllLines(incFile, StandardCharsets.UTF_8).forEach(processor::processLine);
        components.addIncludeDependency(file.uri().toString(), processor.results);
        if (useCache) {
          LOGGER.info("Write cache");
          context.nextCache().write(file.relativePath(),
              Joiner.on('\n').join(processor.results).getBytes(StandardCharsets.UTF_8));
        }
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
