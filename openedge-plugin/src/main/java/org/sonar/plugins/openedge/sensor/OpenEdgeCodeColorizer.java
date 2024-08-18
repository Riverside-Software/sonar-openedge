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

import java.io.UncheckedIOException;

import org.antlr.v4.runtime.TokenSource;
import org.prorefactor.core.ABLNodeType;
import org.prorefactor.core.ProToken;
import org.prorefactor.core.ProparseRuntimeException;
import org.prorefactor.proparse.XCodedFileException;
import org.prorefactor.proparse.support.IProparseEnvironment;
import org.prorefactor.treeparser.ParseUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.SonarProduct;
import org.sonar.api.batch.DependsUpon;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.TextPointer;
import org.sonar.api.batch.sensor.Sensor;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.batch.sensor.highlighting.NewHighlighting;
import org.sonar.api.batch.sensor.highlighting.TypeOfText;
import org.sonar.plugins.openedge.api.Constants;
import org.sonar.plugins.openedge.foundation.IRefactorSessionEnv;
import org.sonar.plugins.openedge.foundation.InputFileUtils;
import org.sonar.plugins.openedge.foundation.OpenEdgeComponents;
import org.sonar.plugins.openedge.foundation.OpenEdgeSettings;

@DependsUpon(value = {"PctDependencies"})
public class OpenEdgeCodeColorizer implements Sensor {
  private static final Logger LOG = LoggerFactory.getLogger(OpenEdgeCodeColorizer.class);

  // IoC
  private final OpenEdgeSettings settings;
  private final OpenEdgeComponents components;

  public OpenEdgeCodeColorizer(OpenEdgeSettings settings, OpenEdgeComponents components) {
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
    IRefactorSessionEnv sessions = settings.getProparseSessions();
    boolean skipUnchangedFiles = settings.skipUnchangedFiles();
    for (InputFile file : context.fileSystem().inputFiles(
        context.fileSystem().predicates().hasLanguage(Constants.LANGUAGE_KEY))) {
      if (skipUnchangedFiles && !components.isChanged(context, file)) {
        LOG.debug("Skip {} as it is unchanged in this branch", file);
        continue;
      }
      LOG.debug("Syntax highlight on {}", file);
      IProparseEnvironment session = sessions.getSession(file.toString());
      try {
        highlightFile(context, session, file);
      } catch (UncheckedIOException | ProparseRuntimeException caught) {
        if (caught.getCause() instanceof XCodedFileException) {
          LOG.error("Unable to highlight xcode'd file '{}", file);
        } else {
          LOG.error("Unable to lex file '{}'", file, caught);
        }
      }
    }
  }

  private void highlightFile(SensorContext context, IProparseEnvironment session, InputFile file) {
    TokenSource stream = new ParseUnit(InputFileUtils.getInputStream(file),
        InputFileUtils.getRelativePath(file, context.fileSystem()), session, file.charset()).lex();

    ProToken tok = (ProToken) stream.nextToken();
    ProToken nextTok = (ProToken) stream.nextToken();
    NewHighlighting highlighting = context.newHighlighting().onFile(file);

    while (tok.getNodeType() != ABLNodeType.EOF_ANTLR4) {
      TypeOfText textType = null;
      if (tok.getNodeType() == ABLNodeType.QSTRING) {
        textType = TypeOfText.STRING;
      } else if (tok.getNodeType() == ABLNodeType.COMMENT) {
        textType = TypeOfText.COMMENT;
      } else if (tok.getNodeType().isKeyword()
          || ((tok.getNodeType() == ABLNodeType.ID) && (ABLNodeType.isFormerUnreservedKeyword(tok.getText())))) {
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
          TextPointer end = file.newPointer(tok.getEndLine(), maxChar < tok.getEndCharPositionInLine()
              ? (maxChar > 0 ? maxChar - 1 : 0) : tok.getEndCharPositionInLine());

          highlighting.highlight(file.newRange(start, end), textType);
        } catch (IllegalArgumentException caught) {
          LOG.error("File {} - Unable to highlight token type {} - Start {}:{} - End {}:{} - Remaining tokens skipped",
              file, textType, tok.getLine(), tok.getCharPositionInLine(), tok.getEndLine(),
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
