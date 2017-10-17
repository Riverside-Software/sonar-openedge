/*
 * OpenEdge plugin for SonarQube
 * Copyright (C) 2017 Riverside Software
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

import org.antlr.v4.runtime.TokenSource;
import org.prorefactor.core.ABLNodeType;
import org.prorefactor.proparse.antlr4.ProToken;
import org.prorefactor.proparse.antlr4.XCodedFileException;
import org.prorefactor.refactor.RefactorSession;
import org.prorefactor.treeparser.ParseUnit;
import org.sonar.api.SonarProduct;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.TextPointer;
import org.sonar.api.batch.sensor.Sensor;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.batch.sensor.highlighting.NewHighlighting;
import org.sonar.api.batch.sensor.highlighting.TypeOfText;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.plugins.openedge.api.Constants;
import org.sonar.plugins.openedge.foundation.OpenEdgeSettings;

public class OpenEdgeCodeColorizer implements Sensor {
  private static final Logger LOG = Loggers.get(OpenEdgeCodeColorizer.class);

  // IoC
  private final OpenEdgeSettings settings;
  
  public OpenEdgeCodeColorizer(OpenEdgeSettings settings) {
    this.settings = settings;
  }

  @Override
  public void describe(SensorDescriptor descriptor) {
    descriptor.onlyOnLanguage(Constants.LANGUAGE_KEY).name(getClass().getSimpleName());
  }

  @Override
  public void execute(SensorContext context) {
    if (context.runtime().getProduct() == SonarProduct.SONARLINT)
      return;

    RefactorSession session = settings.getProparseSession(context.runtime().getProduct() == SonarProduct.SONARLINT);

    for (InputFile file : context.fileSystem().inputFiles(
        context.fileSystem().predicates().hasLanguage(Constants.LANGUAGE_KEY))) {
      LOG.debug("Syntax highlight on {}", file.relativePath());
      try {
        highlightFile(context, session, file);
      } catch (UncheckedIOException caught) {
        if (caught.getCause() instanceof XCodedFileException) {
          LOG.error("Unable to highlight xcode'd file '{}", file.relativePath());
        } else {
          LOG.error("Unable to lex file '{}'", file.relativePath(), caught);
        }
      }
    }
  }

  private void highlightFile(SensorContext context, RefactorSession session, InputFile file) {
    TokenSource stream = new ParseUnit(file.file(), session).lex4();

    ProToken tok = (ProToken) stream.nextToken();
    ProToken nextTok = (ProToken) stream.nextToken();
    NewHighlighting highlighting = context.newHighlighting().onFile(file);

    while (tok.getNodeType() != ABLNodeType.EOF_ANTLR4) {
      TypeOfText textType = null;
      if (tok.getNodeType() == ABLNodeType.QSTRING) {
        textType = TypeOfText.STRING;
      } else if (tok.getNodeType() == ABLNodeType.COMMENT) {
        textType = TypeOfText.COMMENT;
      } else if (tok.getNodeType().isKeyword()) {
        textType = TypeOfText.KEYWORD;
      } else if ((tok.getNodeType() == ABLNodeType.INCLUDEDIRECTIVE) || tok.getNodeType().isPreprocessor()) {
        textType = TypeOfText.PREPROCESS_DIRECTIVE;
      } else if ((tok.getNodeType() == ABLNodeType.NUMBER) || (tok.getNodeType() == ABLNodeType.QUESTION)) {
        textType = TypeOfText.CONSTANT;
      } else if (tok.getNodeType() == ABLNodeType.ANNOTATION) {
        textType = TypeOfText.ANNOTATION;
      }

      if (textType != null) {
        try {
          TextPointer start = file.newPointer(tok.getLine(), tok.getCharPositionInLine() - 1);
          int maxChar = file.selectLine(tok.getEndLine()).end().lineOffset();
          TextPointer end = file.newPointer(tok.getEndLine(),
              maxChar < tok.getEndCharPositionInLine() ? maxChar - 1 : tok.getEndCharPositionInLine());

          highlighting.highlight(file.newRange(start, end), textType);
        } catch (IllegalArgumentException caught) {
          LOG.error("File {} - Unable to highlight token type {} - Start {}:{} - End {}:{} - Remaining tokens skipped",
              file.relativePath(), textType, tok.getLine(), tok.getCharPositionInLine(), tok.getEndLine(),
              tok.getEndCharPositionInLine());
          return;
        }
      }

      tok = nextTok;
      nextTok = (ProToken) stream.nextToken();
    }
    highlighting.save();
  }
}
