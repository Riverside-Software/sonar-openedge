/*
 * OpenEdge plugin for SonarQube
 * Copyright (C) 2013-2014 Riverside Software
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
package org.sonar.plugins.openedge.cpd;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import net.sourceforge.pmd.cpd.SourceCode;
import net.sourceforge.pmd.cpd.TokenEntry;
import net.sourceforge.pmd.cpd.Tokenizer;
import net.sourceforge.pmd.cpd.Tokens;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.Token;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.rssw.antlr.openedge.OpenEdgeKeywords;
import eu.rssw.antlr.openedge.OpenEdgeLexer;

public class OpenEdgeCpdTokenizer implements Tokenizer {
  private static final Logger LOG = LoggerFactory.getLogger(OpenEdgeCpdTokenizer.class);

  @Override
  public final void tokenize(final SourceCode source, Tokens cpdTokens) {
    LOG.debug("Running CPD Tokenizer for " + source.getFileName());

    boolean appBuilderCode = false;
    boolean inUIBCode = false;

    InputStream input = null;

    try {
      input = new FileInputStream(source.getFileName());
      OpenEdgeLexer lexer = new OpenEdgeLexer(new ANTLRInputStream(input));

      // Add custom error listener in order to throw RuntimeException when first error is caught
      lexer.removeErrorListeners();
      lexer.addErrorListener(new BaseErrorListener() {
        @Override
        public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine,
            String msg, RecognitionException e) {
          LOG.error("Skipping CPD on {} due to lexer error at line {} column {} : {}", new Object[] {source.getFileName(), line,
              charPositionInLine, msg});
          LOG.debug("Lexer exception caught : ", e);
          throw new LexerException();
        }
      });

      Token tok = lexer.nextToken();
      appBuilderCode = tok.getText().startsWith("&ANALYZE-SUSPEND _VERSION-NUMBER AB_");
      if (appBuilderCode)
        LOG.debug(source.getFileName()
            + " is AppBuilder managed code - Code duplication sensor won't run outside of &ANALYZE-SUSPEND _UIB-CODE-BLOCK");

      for (; tok.getType() != Token.EOF; tok = lexer.nextToken()) {
        if (appBuilderCode) {
          if ((tok.getChannel() == Token.HIDDEN_CHANNEL)
              && (tok.getText().startsWith("&ANALYZE-SUSPEND _UIB-CODE-BLOCK"))) {
            inUIBCode = true;
          }
          if ((tok.getChannel() == Token.HIDDEN_CHANNEL) && inUIBCode && (tok.getText().startsWith("&ANALYZE-RESUME"))) {
            inUIBCode = false;
            // Insert fake token to fool CPD
            cpdTokens.add(new TokenEntry(UUID.randomUUID().toString(), source.getFileName(), tok.getLine()));
          }
        }
        if (tok.getChannel() == Token.HIDDEN_CHANNEL)
          continue;
        if (appBuilderCode && !inUIBCode)
          continue;

        // Using always same case and expanded version of the keywords
        String str = OpenEdgeKeywords.inv_keywords.get(tok.getType());
        if (str == null) {
          // Identifiers are also using the same case
          if (tok.getType() == OpenEdgeLexer.IDENT)
            str = tok.getText().toLowerCase();
          else
            str = tok.getText().trim();
        }
        cpdTokens.add(new TokenEntry(str, source.getFileName(), tok.getLine()));
      }
    } catch (IOException caught) {
      LOG.error("Could not find : " + source.getFileName(), caught);
    } catch (LexerException uncaught) {
    } finally {
      IOUtils.closeQuietly(input);
    }

    cpdTokens.add(TokenEntry.getEOF());
  }

  private static class LexerException extends RuntimeException {
    private static final long serialVersionUID = -2162150815971985701L;

  }
}
