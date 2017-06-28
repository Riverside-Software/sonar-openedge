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

import java.io.IOException;

import org.prorefactor.core.NodeTypes;
import org.prorefactor.core.ProToken;
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

import antlr.ANTLRException;
import antlr.Token;
import antlr.TokenStream;

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
      } catch (XCodedFileException caught) {
        LOG.error("Unable to highlight xcode'd file '" + caught.getFileName() + "'");
      } catch (RuntimeException | IOException | ANTLRException caught) {
        LOG.error("Unable to lex file " + file.relativePath(), caught);
      }
    }
  }

  private void highlightFile(SensorContext context, RefactorSession session, InputFile file)
      throws IOException, ANTLRException {
    TokenStream stream = new ParseUnit(file.file(), session).lex();

    ProToken tok = (ProToken) stream.nextToken();
    ProToken nextTok = (ProToken) stream.nextToken();
    NewHighlighting highlighting = context.newHighlighting().onFile(file);

    while (tok.getType() != Token.EOF_TYPE) {
      TypeOfText textType = null;
      if (tok.getType() == NodeTypes.QSTRING) {
        textType = TypeOfText.STRING;
      } else if (tok.getType() == NodeTypes.COMMENT) {
        textType = TypeOfText.COMMENT;
      } else if (NodeTypes.isKeywordType(tok.getType())) {
        textType = TypeOfText.KEYWORD;
      } else if ((tok.getType() == NodeTypes.INCLUDEDIRECTIVE)
          || ((tok.getType() >= NodeTypes.AMPANALYZESUSPEND) && (tok.getType() <= NodeTypes.AMPSCOPEDDEFINE))) {
        textType = TypeOfText.PREPROCESS_DIRECTIVE;
      } else if ((tok.getType() == NodeTypes.NUMBER) || (tok.getType() == NodeTypes.QUESTION)) {
        textType = TypeOfText.CONSTANT;
      } else if (tok.getType() == NodeTypes.ANNOTATION) {
        textType = TypeOfText.ANNOTATION;
      }

      if (textType != null) {
        TextPointer start = file.newPointer(tok.getLine(), tok.getColumn() - 1);
        int maxChar = file.selectLine(tok.getEndLine()).end().lineOffset();
        TextPointer end = file.newPointer(tok.getEndLine(),
            maxChar < tok.getEndColumn() ? maxChar - 1 : tok.getEndColumn());
        highlighting.highlight(file.newRange(start, end), textType);
      }

      tok = nextTok;
      nextTok = (ProToken) stream.nextToken();
    }
    highlighting.save();
  }
}
