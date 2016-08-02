/*
 * OpenEdge plugin for SonarQube
 * Copyright (C) 2013-2016 Riverside Software
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

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.rule.ActiveRule;
import org.sonar.api.batch.rule.ActiveRules;
import org.sonar.api.batch.sensor.Sensor;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.platform.Server;
import org.sonar.plugins.openedge.api.checks.OpenEdgeXrefCheck;
import org.sonar.plugins.openedge.foundation.OpenEdge;
import org.sonar.plugins.openedge.foundation.OpenEdgeComponents;
import org.sonar.plugins.openedge.foundation.OpenEdgeProjectHelper;
import org.sonar.plugins.openedge.foundation.OpenEdgeSettings;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class OpenEdgeXREFSensor implements Sensor {
  private static final Logger LOG = LoggerFactory.getLogger(OpenEdgeXREFSensor.class);

  // IoC
  private final FileSystem fileSystem;
  private final OpenEdgeSettings settings;
  private final ActiveRules activeRules;
  private final OpenEdgeComponents components;
  private final Server server;

  // Internal use
  private final DocumentBuilderFactory dbFactory;
  private final DocumentBuilder dBuilder;

  public OpenEdgeXREFSensor(OpenEdgeSettings settings, FileSystem fileSystem, ActiveRules activesRules, OpenEdgeComponents components, Server server) {
    this.fileSystem = fileSystem;
    this.settings = settings;
    this.activeRules = activesRules;
    this.components = components;
    this.server = server;

    this.dbFactory = DocumentBuilderFactory.newInstance();
    try {
      this.dBuilder = dbFactory.newDocumentBuilder();
    } catch (ParserConfigurationException caught) {
      throw new RuntimeException(caught);
    }
  }

  @Override
  public void describe(SensorDescriptor descriptor) {
    descriptor.onlyOnLanguage(OpenEdge.KEY).name(getClass().getSimpleName());
  }

  private File getXrefFile(File file) {
    String relPath = OpenEdgeProjectHelper.getPathRelativeToSourceDirs(file, settings.getSourceDirs());
    if (relPath == null)
      return null;
    return new File(settings.getPctDir(), relPath + ".xref");
  }

  @Override
  public void execute(SensorContext context) {
    int xrefNum = 0;
    Map<String, Long> ruleTime = new HashMap<>();
    long parseTime = 0L;
    components.initializeChecks(context);
    for (Map.Entry<ActiveRule, OpenEdgeXrefCheck> entry : components.getXrefRules().entrySet()) {
      ruleTime.put(entry.getKey().ruleKey().toString(), 0L);
    }

    for (InputFile file : fileSystem.inputFiles(fileSystem.predicates().hasLanguage(OpenEdge.KEY))) {
      LOG.debug("Looking for XREF of {}", file.relativePath());

      File xrefFile = getXrefFile(file.file());
      if ((xrefFile != null) && xrefFile.exists()) {
        LOG.debug("Parsing XML XREF file {}", xrefFile.getAbsolutePath());
        try {
          long startTime = System.currentTimeMillis();
          Document doc = dBuilder.parse(xrefFile);
          parseTime += (System.currentTimeMillis() - startTime);

          for (Map.Entry<ActiveRule, OpenEdgeXrefCheck> entry : components.getXrefRules().entrySet()) {
            LOG.debug("ActiveRule - Internal key {} - Repository {} - Rule {}",
                new Object[] {
                    entry.getKey().internalKey(), entry.getKey().ruleKey().repository(),
                    entry.getKey().ruleKey().rule()});
            startTime = System.currentTimeMillis();
            entry.getValue().execute(file, doc);
            ruleTime.put(entry.getKey().ruleKey().toString(),
                ruleTime.get(entry.getKey().ruleKey().toString()) + System.currentTimeMillis() - startTime);
          }

          xrefNum++;
        } catch (SAXException | IOException caught) {
          LOG.error("Unable to parse file " + xrefFile.getAbsolutePath(), caught);
        } catch (RuntimeException caught) {
          LOG.error("Runtime exception was caught '{}' - Please report this issue : ", caught.getMessage());
          for (StackTraceElement element : caught.getStackTrace()) {
            LOG.error("  {}", element.toString());
          }
        }
      }
    }

    LOG.info("{} XREF files imported", xrefNum);
    LOG.info("XREF DOM Parse | time={} ms", parseTime);
    for (Entry<String, Long> entry : ruleTime.entrySet()) {
      LOG.info("Rule {} | time={} ms", new Object[] {entry.getKey(), entry.getValue()});
    }
  }

  @Override
  public String toString() {
    return getClass().getSimpleName();
  }

}
