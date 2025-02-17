/*
 * OpenEdge plugin for SonarQube
 * Copyright (c) 2015-2025 Riverside Software
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import eu.rssw.antlr.profiler.ProfilerGrammarParser.CallTreeDataContext;
import eu.rssw.antlr.profiler.ProfilerGrammarParser.CallTreeDataLineContext;
import eu.rssw.antlr.profiler.ProfilerGrammarParser.CoverageSection2LineContext;
import eu.rssw.antlr.profiler.ProfilerGrammarParser.CoverageSectionContext;
import eu.rssw.antlr.profiler.ProfilerGrammarParser.Coverage_section_lineContext;
import eu.rssw.antlr.profiler.ProfilerGrammarParser.DescriptionContext;
import eu.rssw.antlr.profiler.ProfilerGrammarParser.JsonDataContext;
import eu.rssw.antlr.profiler.ProfilerGrammarParser.LineSummaryLineContext;
import eu.rssw.antlr.profiler.ProfilerGrammarParser.ModuleDataLineContext;
import eu.rssw.antlr.profiler.ProfilerGrammarParser.Stats1LineContext;
import eu.rssw.antlr.profiler.ProfilerGrammarParser.TracingDataLineContext;
import eu.rssw.antlr.profiler.ProfilerGrammarParser.UserDataLineContext;

public class ProfilerSessionVisitor extends ProfilerGrammarBaseVisitor<Void> {
  private final List<Module> modules;
  private ProfilerSession session;
  private Module lastModule;

  public ProfilerSessionVisitor() {
    modules = new ArrayList<>();
  }

  public ProfilerSession getSession() {
    return session;
  }

  @Override
  public Void visitDescription(DescriptionContext ctx) {
    if (session != null)
      throw new IllegalStateException("Session already created");

    session = new ProfilerSession(ctx.desc.getText(), ctx.author.getText(),
        ctx.date.getText() + " " + ctx.time.getText(), ctx.version.getText());

    return visitChildren(ctx);
  }

  @Override
  public Void visitJsonData(JsonDataContext ctx) {
    if (ctx.JSON() != null)
      session.setJsonDescription(ctx.JSON().getText().trim());
    else
      session.setJsonDescription("");
    return null;
  }

  @Override
  public Void visitModuleDataLine(ModuleDataLineContext ctx) {
    modules.add(new Module(Integer.parseInt(ctx.id.getText()), ctx.name.getText(), ctx.debugListingFile.getText(),
        Integer.parseInt(ctx.crc.getText())));

    return null;
  }

  @Override
  public Void visitCallTreeData(CallTreeDataContext ctx) {
    // Modules are parsed, adding them to ProfilerSession
    Collections.sort(modules);
    for (Module m : modules) {
      session.addModule(m);
    }
    session.initializeCallTreeMatrix();

    return visitChildren(ctx);
  }

  @Override
  public Void visitCallTreeDataLine(CallTreeDataLineContext ctx) {
    session.addCall(Integer.parseInt(ctx.callerId.getText()), Integer.parseInt(ctx.calleeId.getText()),
        Integer.parseInt(ctx.callCount.getText()));

    return null;
  }

  @Override
  public Void visitLineSummaryLine(LineSummaryLineContext ctx) {
    Module module = session.getModuleById(Integer.parseInt(ctx.moduleId.getText()));
    if (module != null) {
      module.addLineSummary(
          new LineData(Integer.parseInt(ctx.lineNumber.getText()), Integer.parseInt(ctx.execCount.getText()),
              Float.parseFloat(ctx.actualTime.getText()), Float.parseFloat(ctx.cumulativeTime.getText())));
    }
    return null;
  }

  @Override
  public Void visitTracingDataLine(TracingDataLineContext ctx) {
    try {
      session.addTraceLine(Integer.parseInt(ctx.moduleId.getText()), Integer.parseInt(ctx.lineNumber.getText()),
          Float.parseFloat(ctx.execTime.getText()), Float.parseFloat(ctx.timestamp.getText()));
    } catch (NumberFormatException uncaught) {
      //
    }
    return null;
  }

  @Override
  public Void visitCoverageSection(CoverageSectionContext ctx) {
    lastModule = session.getModuleById(Integer.parseInt(ctx.moduleId.getText()));

    return visitChildren(ctx);
  }

  @Override
  public Void visitCoverage_section_line(Coverage_section_lineContext ctx) {
    lastModule.addLineToCover(Integer.parseInt(ctx.linenum.getText()));
    return null;
  }

  @Override
  public Void visitCoverageSection2Line(CoverageSection2LineContext ctx) {
    session.addModuleInfo(Integer.parseInt(ctx.NUMBER(0).getText()));
    return null;
  }

  @Override
  public Void visitStats1Line(Stats1LineContext ctx) {
    session.addStats1(ctx.STRING().getText());
    return null;
  }

  @Override
  public Void visitUserDataLine(UserDataLineContext ctx) {
    session.addUserData(Float.parseFloat(ctx.FLOAT().getText()), ctx.STRING().getText());
    return null;
  }

}
