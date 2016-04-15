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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.plugins.openedge.api.antlr.Token;
import org.sonar.plugins.openedge.api.antlr.TokenStream;
import org.sonar.plugins.openedge.api.antlr.TokenStreamException;
import org.sonar.plugins.openedge.api.org.prorefactor.core.NodeTypes;
import org.sonar.plugins.openedge.api.org.prorefactor.core.ProToken;
import org.sonar.plugins.openedge.api.org.prorefactor.core.ProparseRuntimeException;
import org.sonar.plugins.openedge.api.org.prorefactor.refactor.RefactorException;
import org.sonar.plugins.openedge.api.org.prorefactor.refactor.RefactorSession;
import org.sonar.plugins.openedge.api.org.prorefactor.treeparser.ParseUnit;

import net.sourceforge.pmd.cpd.SourceCode;
import net.sourceforge.pmd.cpd.TokenEntry;
import net.sourceforge.pmd.cpd.Tokenizer;
import net.sourceforge.pmd.cpd.Tokens;

public class OpenEdgeCpdTokenizer implements Tokenizer {
  private static final Logger LOG = LoggerFactory.getLogger(OpenEdgeCpdTokenizer.class);

  private final RefactorSession proparseSession;
  private final FileSystem fileSystem;
  private final boolean debug;

  public OpenEdgeCpdTokenizer(FileSystem fileSystem, RefactorSession session) {
    this(fileSystem, session, false);
  }

  public OpenEdgeCpdTokenizer(FileSystem fileSystem, RefactorSession session, boolean debug) {
    this.fileSystem = fileSystem;
    this.proparseSession = session;
    this.debug = debug;
  }

  @Override
  public final void tokenize(final SourceCode source, Tokens cpdTokens) {
    boolean appBuilderCode = false;
    boolean inUIBCode = false;

    InputFile inputFile = fileSystem.inputFile(fileSystem.predicates().hasAbsolutePath(source.getFileName()));
    if (inputFile == null) {
      return;
    }

    try {
      ParseUnit unit = new ParseUnit(new File(source.getFileName()), proparseSession);
      TokenStream stream = unit.lex();

      Token tok = stream.nextToken();
      appBuilderCode = ((tok.getType() == NodeTypes.AMPANALYZESUSPEND)
          && tok.getText().startsWith("&ANALYZE-SUSPEND _VERSION-NUMBER AB_"));
      if (appBuilderCode) {
        LOG.debug("AppBuilder generated code detected");
      }

      for (; tok.getType() != Token.EOF_TYPE; tok = stream.nextToken()) {
        if ((tok.getType() == NodeTypes.WS) || (tok.getType() == NodeTypes.COMMENT))
          continue;
        if (((ProToken) tok).getFileIndex() > 0)
          continue;
        if (appBuilderCode && (tok.getType() == NodeTypes.AMPANALYZESUSPEND)
            && (tok.getText().startsWith("&ANALYZE-SUSPEND _CREATE-WINDOW") || tok.getText().startsWith("&ANALYZE-SUSPEND _UIB-CODE-BLOCK _PROCEDURE adm-create-objects"))) {
          inUIBCode = true;
        } else if (appBuilderCode && inUIBCode && (tok.getType() == NodeTypes.AMPANALYZERESUME)) {
          inUIBCode = false;
          cpdTokens.add(new TokenEntry(UUID.randomUUID().toString(), inputFile.relativePath(), tok.getLine()));
          continue;
        }

        if (appBuilderCode && inUIBCode)
          continue;

        ProToken pTok = (ProToken) tok;
        if (pTok.getFileIndex() == 0) {
          // Using always same case and expanded version of the keywords
          String str = NodeTypes.getFullText(tok.getType());
          if ((str == null) || (str.trim().length() == 0)) {
            // Identifiers are also using the same case
            if (tok.getType() == NodeTypes.ID)
              str = tok.getText().toLowerCase();
            else
              str = tok.getText().trim();
          }
          cpdTokens.add(new TokenEntry(str, inputFile.relativePath(), tok.getLine()));
        }
      }
    } catch (TokenStreamException | RefactorException | ProparseRuntimeException caught) {
      LOG.error("Could not parse : " + inputFile.relativePath(), caught);
    } catch (RuntimeException caught) {
      LOG.error("Runtime exception was caught '{}' - Please report this issue : ", caught.getMessage());
      for (StackTraceElement element : caught.getStackTrace()) {
        LOG.error("  {}", element.toString());
      }
    }

    cpdTokens.add(TokenEntry.getEOF());
    if (debug) {
      File tokensDir = new File(fileSystem.baseDir(), ".tokens");
      File tokensFile = new File(tokensDir, inputFile.relativePath());
      tokensFile.getParentFile().mkdirs();
      try (BufferedWriter writer = new BufferedWriter(new FileWriter(tokensFile))) {
        for (TokenEntry entry : cpdTokens.getTokens()) {
          writer.write(entry.getBeginLine() + " - " + entry.getValue());
          writer.newLine();
        }
      } catch (IOException caught) {
        LOG.error("Unable to generate tokens file " + tokensFile.getAbsolutePath(), caught);
      }
    }
  }

}
