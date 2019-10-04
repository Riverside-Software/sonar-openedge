/*
 * OpenEdge plugin for SonarQube
 * Copyright (c) 2015-2019 Riverside Software
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
package eu.rssw.antlr.profiler;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.DefaultErrorStrategy;
import org.antlr.v4.runtime.FailedPredicateException;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.tree.ParseTree;

public final class ProfilerUtils {

  private ProfilerUtils() {
    // No-op
  }

  public static final ProfilerSession getProfilerSession(File file) throws IOException {
    return getProfilerSession(new FileInputStream(file));
  }

  public static final ProfilerSession getProfilerSession(InputStream input) throws IOException {
    ProfilerGrammarLexer lexer = new ProfilerGrammarLexer(CharStreams.fromStream(input));
    CommonTokenStream tokens = new CommonTokenStream(lexer);
    ProfilerGrammarParser parser = new ProfilerGrammarParser(tokens);
    parser.setErrorHandler(new NoFailedPredicateErrorStrategy());
    ParseTree tree = parser.profiler();

    ProfilerSessionVisitor visitor = new ProfilerSessionVisitor();
    visitor.visit(tree);

    return visitor.getSession();
  }

  private static class NoFailedPredicateErrorStrategy extends DefaultErrorStrategy {
    @Override
    protected void reportFailedPredicate(Parser recognizer, FailedPredicateException e) {
      // Nothing here...
    }
  }
}
