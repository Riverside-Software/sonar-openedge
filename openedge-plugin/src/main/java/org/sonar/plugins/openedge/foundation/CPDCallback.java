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
package org.sonar.plugins.openedge.foundation;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.prorefactor.core.ABLNodeType;
import org.prorefactor.core.ICallback;
import org.prorefactor.core.IConstants;
import org.prorefactor.core.JPNode;
import org.prorefactor.core.ProToken;
import org.prorefactor.macrolevel.MacroEvent;
import org.prorefactor.macrolevel.NamedMacroRef;
import org.prorefactor.treeparser.ParseUnit;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.TextRange;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.cpd.NewCpdTokens;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

/**
 * This class generated CPD tokens for the CPD engine, by skipping tokens within annotated code blocks and also by
 * reverting preprocessor, as CPD engine doesn't want tokens to overlap.
 */
public class CPDCallback implements ICallback<NewCpdTokens> {
  private static final Logger LOG = Loggers.get(CPDCallback.class);

  private final NewCpdTokens cpdTokens;
  private final OpenEdgeSettings settings;
  private final InputFile file;
  private final ParseUnit unit;

  public CPDCallback(SensorContext context, InputFile file, OpenEdgeSettings settings, ParseUnit unit) {
    this.cpdTokens = context.newCpdTokens().onFile(file);
    this.file = file;
    this.settings = settings;
    this.unit = unit;
  }

  @Override
  public NewCpdTokens getResult() {
    return cpdTokens;
  }

  @Override
  public boolean visitNode(JPNode node) {
    // Periods, colons and CPD annotations not taken into account
    if ((node.getNodeType() == ABLNodeType.PERIOD) || (node.getNodeType() == ABLNodeType.OBJCOLON)) {
      return false;
    }
    if ((node.getNodeType() == ABLNodeType.ANNOTATION) && settings.skipCPD(node.getAnnotationName())) {
        return false;
    }
    if (preprocessorLookup(node)) {
      return false;
    }

    // Skip code blocks following parameterized annotations
    JPNode prevSibling = node.getPreviousSibling();
    while ((prevSibling != null) && (prevSibling.getNodeType() == ABLNodeType.ANNOTATION)) {
      if (settings.skipCPD(prevSibling.getAnnotationName())) {
        // Skipping nodes is not enough, as the content of the method would be considered blank lines.
        // So if this method is between two 'duplicate' methods, then all those blank lines would be
        // considered duplicates
        insertFakeNode(node);
        return false;
      }
      prevSibling = prevSibling.getPreviousSibling();
    }
    // Skip method matching parameterized names
    if (node.getNodeType() == ABLNodeType.METHOD) {
      JPNode methodName = node.findDirectChild(ABLNodeType.ID);
      if ((methodName != null) && (settings.skipMethod(methodName.getText()))) {
        insertFakeNode(node);
        return false;
      }
    }
    // Skip procedures and functions matching parameterized names
    if ((node.getNodeType() == ABLNodeType.PROCEDURE) || (node.getNodeType() == ABLNodeType.FUNCTION)) {
      JPNode procName = node.findDirectChild(ABLNodeType.ID);
      if ((procName != null) && (settings.skipProcedure(procName.getText()))) {
        insertFakeNode(node);
        return false;
      }
    }

    if (node.attrGet(IConstants.OPERATOR) == IConstants.TRUE) {
      // Consider that an operator only has 2 children
      visitNode(node.getFirstChild());
      visitCpdNode(node);
      visitNode(node.getFirstChild().getNextSibling());
      return false;
    } else {
      visitCpdNode(node);
    }
    return true;
  }

  /**
   * @return True if token is right after ANALYZE-SUSPEND _CREATE-WINDOW, meaning that block of code has to be skipped
   */
  private boolean preprocessorLookup(JPNode node) {
    for (ProToken n : node.getHiddenTokens()) {
      if ((n.getNodeType() == ABLNodeType.AMPANALYZESUSPEND) && (n.getText().startsWith("&ANALYZE-SUSPEND _CREATE-WINDOW")
          || n.getText().startsWith("&ANALYZE-SUSPEND _UIB-CODE-BLOCK _PROCEDURE adm-create-objects"))) {
        return true;
      }
    }
    return false;
  }

  private String undoPreprocessing(JPNode node, String str) {
    String foo = str;
    for (MacroEvent evt : unit.getMacroGraph().macroEventList) {
      if (evt instanceof NamedMacroRef) {
        NamedMacroRef nmr = (NamedMacroRef) evt;
        if (nmr.getLine() > node.getLine()) {
          break;
        }
        // Reduce expanded prepro variable back to {&VAR_NAME}
        if ((nmr.getMacroDef() != null) && NamedMacroRef.isInRange(nmr.getLine(), nmr.getColumn(),
            new int[] {node.getLine(), node.getColumn() - 1}, new int[] {node.getEndLine(), node.getEndColumn() - 1})) {
          foo = foo.replace(nmr.getMacroDef().getValue(), "{&" + nmr.getMacroDef().getName() + "}");
        }
      }
    }
    return foo;
  }

  private void visitCpdNode(JPNode node) {
    // We only take care of nodes in main file, and of real nodes
    if ((node.getFileIndex() > 0) || (node.getLine() <= 0) || (node.getFileIndex() != node.getEndFileIndex())){
      return;
    }
    if ((node.getLine() == node.getEndLine()) && (node.getColumn() == node.getEndColumn())) {
      return;
    }
    String str = node.getNodeType().getText();
    // Identifiers are also using the same case
    if ((str == null) || (str.trim().length() == 0)) {
      if (node.getNodeType() == ABLNodeType.ID) {
        str = node.getText().toLowerCase(Locale.ENGLISH);
      } else {
        str = node.getText().trim();
      }
    }
    str = undoPreprocessing(node, str);

    try {
      TextRange range = file.newRange(node.getLine(), node.getColumn(), node.getEndLine(), node.getEndColumn());
      cpdTokens.addToken(range, str);
    } catch (IllegalArgumentException uncaught) {
      LOG.debug("Unable to create CPD token at position {}:{} to {}:{} - Cause {}", node.getLine(), node.getColumn(),
          node.getEndLine(), node.getEndColumn(), uncaught.getMessage());
    }
  }

  private void insertFakeNode(JPNode node) {
    List<JPNode> children = node.getDirectChildren();
    JPNode lastSibling = children.isEmpty() ? node : children.get(children.size() - 1);
    try {
      TextRange range = file.newRange(node.getLine(), node.getColumn(), lastSibling.getEndLine(), lastSibling.getEndColumn());
      cpdTokens.addToken(range, UUID.randomUUID().toString());
    } catch (IllegalArgumentException uncaught) {
      LOG.debug("Unable to create CPD token at position {}:{} to {}:{} - Cause {}", node.getLine(), node.getColumn(),
          lastSibling.getEndLine(), lastSibling.getEndColumn(), uncaught.getMessage());
    }
  }
}
