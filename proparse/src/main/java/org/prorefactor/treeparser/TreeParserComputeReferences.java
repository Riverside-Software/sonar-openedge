/********************************************************************************
 * Copyright (c) 2015-2021 Riverside Software
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the Eclipse
 * Public License, v. 2.0 are satisfied: GNU Lesser General Public License v3.0
 * which is available at https://www.gnu.org/licenses/lgpl-3.0.txt
 *
 * SPDX-License-Identifier: EPL-2.0 OR LGPL-3.0
 ********************************************************************************/
package org.prorefactor.treeparser;

import org.prorefactor.core.JPNode;
import org.prorefactor.core.nodetypes.FieldRefNode;
import org.prorefactor.core.nodetypes.RecordNameNode;
import org.prorefactor.proparse.antlr4.Proparse.CatchStatementContext;
import org.prorefactor.proparse.antlr4.Proparse.ColonAttributeContext;
import org.prorefactor.proparse.antlr4.Proparse.Exprt2FieldContext;
import org.prorefactor.proparse.antlr4.Proparse.ExprtExprt2Context;
import org.prorefactor.proparse.antlr4.Proparse.ExprtWidNameContext;
import org.prorefactor.proparse.antlr4.Proparse.FieldContext;
import org.prorefactor.proparse.antlr4.Proparse.ParameterArgDatasetHandleContext;
import org.prorefactor.proparse.antlr4.Proparse.ParameterArgTableHandleContext;
import org.prorefactor.proparse.antlr4.Proparse.RecordContext;
import org.prorefactor.proparse.antlr4.Proparse.WidNameContext;
import org.prorefactor.proparse.antlr4.Proparse.WidattrExprt2Context;
import org.prorefactor.proparse.antlr4.Proparse.WidattrWidNameContext;
import org.prorefactor.treeparser.symbols.FieldBuffer;
import org.prorefactor.treeparser.symbols.TableBuffer;
import org.prorefactor.treeparser.symbols.Variable;

import com.google.inject.Inject;

public class TreeParserComputeReferences extends AbstractBlockProparseListener {

  @Inject
  public TreeParserComputeReferences(ParseUnit unit) {
    super(unit);
  }

  @Inject
  public TreeParserComputeReferences(AbstractBlockProparseListener listener) {
    super(listener);
  }

  @Override
  public void exitParameterArgTableHandle(ParameterArgTableHandleContext ctx) {
    noteReference(support.getNode(ctx.field()), contextQualifiers.get(ctx));
  }

  @Override
  public void exitParameterArgDatasetHandle(ParameterArgDatasetHandleContext ctx) {
    noteReference(support.getNode(ctx.field()), contextQualifiers.get(ctx));
  }

  @Override
  public void enterExprtWidName(ExprtWidNameContext ctx) {
    widattr(ctx.widName(), ctx.colonAttribute(), support.getNode(ctx), contextQualifiers.get(ctx));
  }

  @Override
  public void enterExprtExprt2(ExprtExprt2Context ctx) {
    if ((ctx.colonAttribute() != null) && (ctx.expressionTerm2() instanceof Exprt2FieldContext)
        && (ctx.colonAttribute().colonAttributeSub(0).OBJCOLON() != null)) {
      widattr((Exprt2FieldContext) ctx.expressionTerm2(), contextQualifiers.get(ctx),
          ctx.colonAttribute().colonAttributeSub(0).id.getText());
    }
  }

  @Override
  public void enterWidattrExprt2(WidattrExprt2Context ctx) {
    if ((ctx.expressionTerm2() instanceof Exprt2FieldContext)
        && (ctx.colonAttribute().colonAttributeSub(0).id != null)) {
      widattr((Exprt2FieldContext) ctx.expressionTerm2(), contextQualifiers.get(ctx),
          ctx.colonAttribute().colonAttributeSub(0).id.getText());
    }
  }

  @Override
  public void enterWidattrWidName(WidattrWidNameContext ctx) {
    widattr(ctx.widName(), ctx.colonAttribute(), support.getNode(ctx.getParent()), contextQualifiers.get(ctx));
  }

  @Override
  public void enterWidName(WidNameContext ctx) {
    if ((ctx.BUFFER() != null) || (ctx.TEMPTABLE() != null)) {
      TableBuffer tableBuffer = currentScope.lookupBuffer(ctx.filn().getText());
      if (tableBuffer != null) {
        tableBuffer.noteReference(support.getNode(ctx), ContextQualifier.SYMBOL);
      }
    }
  }

  @Override
  public void enterCatchStatement(CatchStatementContext ctx) {
    super.enterCatchStatement(ctx);
    currentScope.getVariable(ctx.n.getText()).noteReference(support.getNode(ctx), ContextQualifier.UPDATING);
  }

  @Override
  public void enterRecord(RecordContext ctx) {
    RecordNameNode node = (RecordNameNode) support.getNode(ctx);
    if ((node != null) && (node.getTableBuffer() != null))
      node.getTableBuffer().noteReference(node, node.getQualifier());
  }

  @Override
  public void exitField(FieldContext ctx) {
    FieldRefNode refNode = (FieldRefNode) support.getNode(ctx);
    ContextQualifier qual = contextQualifiers.get(ctx);
    if (qual == null)
      qual = ContextQualifier.REF;

    if (refNode.getSymbol() != null) {
      refNode.getSymbol().noteReference(refNode, refNode.getQualifier());
      if (refNode.getSymbol() instanceof FieldBuffer) {
        FieldBuffer fb = (FieldBuffer) refNode.getSymbol();
        if (fb.getBuffer() != null) {
          fb.getBuffer().noteReference(refNode, qual);
        }
      }
    }
  }

  private void noteReference(JPNode node, ContextQualifier cq) {
    if ((node.getSymbol() != null)
        && ((cq == ContextQualifier.UPDATING) || (cq == ContextQualifier.REFUP) || (cq == ContextQualifier.OUTPUT))) {
      node.getSymbol().noteReference(node, cq);
    }
  }

  private void widattr(WidNameContext ctx, ColonAttributeContext ctx2, JPNode node, ContextQualifier cq) {
    if ((ctx == null) || (ctx2 == null))
      return;
    if ((ctx.systemHandleName() != null) && (ctx.systemHandleName().THISOBJECT() != null)) {
      FieldLookupResult result = rootScope.getRootBlock().lookupField(ctx2.colonAttributeSub(0).id.getText(), true);
      if (result == null)
        return;

      if (result.getSymbol() instanceof Variable) {
        result.getSymbol().noteReference(node, cq);
        // If using chained expression, then we add a REF reference
        if ((cq != ContextQualifier.REF) && (ctx2.colonAttributeSub().size() > 1))
          result.getSymbol().noteReference(node, ContextQualifier.REF);
      }
    }
  }

  // Called from expressionTerm rule (expressionTerm2 option) and widattr rule (widattrExprt2 option)
  // Tries to add references to variables/properties of current class, or references to static classes
  private void widattr(Exprt2FieldContext ctx2, ContextQualifier cq, String right) {
    String clsRef = ctx2.field().getText();
    String clsName = rootScope.getClassName();
    if ((clsRef != null) && (clsName != null) && (clsRef.indexOf('.') == -1) && (clsName.indexOf('.') != -1))
      clsName = clsName.substring(clsName.indexOf('.') + 1);

    if ((clsRef != null) && (clsName != null) && clsRef.equalsIgnoreCase(clsName)) {
      FieldLookupResult result = currentBlock.lookupField(right, true);
      if (result == null)
        return;

      // Variable
      if (result.getSymbol() instanceof Variable) {
        result.getSymbol().noteReference(support.getNode(ctx2.getParent().getParent()), cq);
      }
    }
  }

}
