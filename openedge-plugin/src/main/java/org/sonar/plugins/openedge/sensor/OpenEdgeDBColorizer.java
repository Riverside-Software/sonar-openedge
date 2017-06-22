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

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.Token;
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

import eu.rssw.antlr.database.DumpFileGrammarLexer;

public class OpenEdgeDBColorizer implements Sensor {
  private static final Logger LOG = Loggers.get(OpenEdgeDBColorizer.class);

  @Override
  public void describe(SensorDescriptor descriptor) {
    descriptor.onlyOnLanguage(Constants.DB_LANGUAGE_KEY).name(getClass().getSimpleName());
  }

  @Override
  public void execute(SensorContext context) {
    if (context.runtime().getProduct() == SonarProduct.SONARLINT)
      return;

    for (InputFile file : context.fileSystem().inputFiles(
        context.fileSystem().predicates().hasLanguage(Constants.DB_LANGUAGE_KEY))) {
      LOG.debug("DF syntax highlight on {}", file.relativePath());
      try {
        highlightFile(context, file);
      } catch (RuntimeException | IOException caught) {
        LOG.error("Unable to lex file " + file.relativePath(), caught);
      }
    }
  }

  private void highlightFile(SensorContext context, InputFile file) throws IOException {
    DumpFileGrammarLexer lexer = new DumpFileGrammarLexer(CharStreams.fromPath(file.path()));
    NewHighlighting highlighting = context.newHighlighting().onFile(file);

    Token tok = lexer.nextToken();
    Token nextTok = lexer.nextToken();

    while (tok.getType() != Token.EOF) {
      if (tok.getChannel() != Token.HIDDEN_CHANNEL) {
        // No whitespaces or new lines
        TypeOfText textType = null;
        if ((tok.getType() == DumpFileGrammarLexer.QUOTED_STRING)
            || (tok.getType() == DumpFileGrammarLexer.UNQUOTED_STRING)) {
          textType = TypeOfText.STRING;
        } else if (tok.getType() == DumpFileGrammarLexer.NUMBER) {
          textType = TypeOfText.CONSTANT;
        } else if (tok.getType() == DumpFileGrammarLexer.ANNOTATION_NAME) {
          textType = TypeOfText.ANNOTATION;
        } else {
          textType = TypeOfText.KEYWORD;
        }

        TextPointer start = file.newPointer(tok.getLine(), tok.getCharPositionInLine());
        TextPointer end;
        // If next token is at beginning of next line, then current token goes until end of line
        if (nextTok.getCharPositionInLine() == 0) {
          end = file.selectLine(nextTok.getLine() - 1).end();
        } else {
          end = file.newPointer(nextTok.getLine(), nextTok.getCharPositionInLine());
        }
        highlighting.highlight(file.newRange(start, end), textType);
      }
      tok = nextTok;
      nextTok = lexer.nextToken();
    }
    highlighting.save();
  }
}
