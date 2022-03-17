/*
 * OpenEdge plugin for SonarQube
 * Copyright (c) 2015-2022 Riverside Software
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

import java.io.UncheckedIOException;
import java.util.Locale;

import org.antlr.v4.runtime.TokenSource;
import org.prorefactor.core.ABLNodeType;
import org.prorefactor.core.ProToken;
import org.prorefactor.core.ProparseRuntimeException;
import org.prorefactor.proparse.XCodedFileException;
import org.prorefactor.proparse.support.IProparseEnvironment;
import org.prorefactor.treeparser.ParseUnit;
import org.sonar.api.SonarProduct;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.TextRange;
import org.sonar.api.batch.sensor.Sensor;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.batch.sensor.cpd.NewCpdTokens;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.plugins.openedge.api.Constants;
import org.sonar.plugins.openedge.foundation.IRefactorSessionEnv;
import org.sonar.plugins.openedge.foundation.InputFileUtils;
import org.sonar.plugins.openedge.foundation.OpenEdgeSettings;

public class OpenEdgeCPDSensor implements Sensor {
  private static final Logger LOG = Loggers.get(OpenEdgeCPDSensor.class);

  // IoC
  private final OpenEdgeSettings settings;

  public OpenEdgeCPDSensor(OpenEdgeSettings settings) {
    this.settings = settings;
  }

  @Override
  public void describe(SensorDescriptor descriptor) {
    descriptor.onlyOnLanguage(Constants.LANGUAGE_KEY).name(getClass().getSimpleName()).onlyWhenConfiguration(
        config -> config.getBoolean(Constants.SKIP_PROPARSE_PROPERTY).orElse(false)
            && config.getBoolean(Constants.USE_SIMPLE_CPD).orElse(false));
  }

  @Override
  public void execute(SensorContext context) {
    if ((context.runtime().getProduct() == SonarProduct.SONARLINT) || !settings.useSimpleCPD())
      return;
    settings.init();
    IRefactorSessionEnv sessions = settings.getProparseSessions();

    for (InputFile file : context.fileSystem().inputFiles(
        context.fileSystem().predicates().hasLanguage(Constants.LANGUAGE_KEY))) {
      LOG.debug("CPD on {}", file);
      IProparseEnvironment session = sessions.getSession(file.relativePath());
      try {
        processFile(context, session, file);
      } catch (UncheckedIOException | ProparseRuntimeException caught) {
        if (caught.getCause() instanceof XCodedFileException) {
          LOG.error("Unable to process xcode'd file '{}", file);
        } else {
          LOG.error("Unable to lex file '{}'", file, caught);
        }
      }
    }
  }

  private void processFile(SensorContext context, IProparseEnvironment session, InputFile file) {
    TokenSource stream = new ParseUnit(InputFileUtils.getInputStream(file),
        InputFileUtils.getRelativePath(file, context.fileSystem()), session).lex();
    if (stream == null)
      return;

    NewCpdTokens cpdTokens = context.newCpdTokens().onFile(file);
    processTokenSource(file, cpdTokens, stream);
  }

  public static void processTokenSource(InputFile file, NewCpdTokens cpdTokens, TokenSource stream) {
    ProToken tok = (ProToken) stream.nextToken();
    boolean suspended = false;

    while (tok.getNodeType() != ABLNodeType.EOF_ANTLR4) {
      if ((tok.getNodeType() == ABLNodeType.AMPANALYZESUSPEND)
          && (tok.getText().startsWith("&ANALYZE-SUSPEND _CREATE-WINDOW")
              || tok.getText().startsWith("&ANALYZE-SUSPEND _UIB-CODE-BLOCK _PROCEDURE adm-create-objects"))) {
        suspended = true;
      } else if (tok.getNodeType() == ABLNodeType.AMPANALYZERESUME) {
        suspended = false;
      }

      if (!suspended) {
        processToken(file, cpdTokens, tok);
      }

      tok = (ProToken) stream.nextToken();
    }
    cpdTokens.save();
  }

  private static void processToken(InputFile file, NewCpdTokens cpdTokens, ProToken tok) {
    String str = tok.getNodeType() == ABLNodeType.NUMBER ? tok.getText() : tok.getNodeType().getText();
    // Identifiers are also using the same case
    if ((str == null) || (str.trim().length() == 0)) {
      if (tok.getNodeType() == ABLNodeType.ID) {
        str = tok.getText().toLowerCase(Locale.ENGLISH);
      } else {
        str = tok.getText().trim();
      }
    }

    try {
      TextRange range = file.newRange(tok.getLine(), tok.getCharPositionInLine() - 1, tok.getEndLine(),
          tok.getEndCharPositionInLine());
      cpdTokens.addToken(range, str);
    } catch (IllegalArgumentException | IllegalStateException uncaught) {
      LOG.debug("Unable to create CPD token at position {}:{} to {}:{} - Cause {}", tok.getLine(),
          tok.getCharPositionInLine(), tok.getEndLine(), tok.getEndCharPositionInLine(), uncaught.getMessage());
    }
  }
}
