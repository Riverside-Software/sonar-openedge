/********************************************************************************
 * Copyright (c) 2003-2015 John Green
 * Copyright (c) 2015-2026 Riverside Software
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
package org.prorefactor.treeparser.symbols;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import org.prorefactor.core.ABLNodeType;
import org.prorefactor.core.nodetypes.IStatement;
import org.prorefactor.core.nodetypes.IStatementBlock;
import org.prorefactor.core.nodetypes.IfStatementNode;
import org.prorefactor.treeparser.ExecutionGraph;
import org.prorefactor.treeparser.Parameter;
import org.prorefactor.treeparser.TreeParserSymbolScope;

import eu.rssw.pct.elements.DataType;
import eu.rssw.pct.elements.IMethodElement;
import eu.rssw.pct.elements.PrimitiveDataType;

/**
 * Represents the definition of a Routine. Is a Symbol - used as an entry in the symbol table. A Routine is a
 * Program_root, PROCEDURE, FUNCTION, or METHOD.
 */
public class Routine extends Symbol {
  private static final Predicate<IStatement> IS_BLOCK = it -> (it.getNodeType() == ABLNodeType.FUNCTION)
      || (it.getNodeType() == ABLNodeType.PROCEDURE) || (it.getNodeType() == ABLNodeType.METHOD)
      || (it.getNodeType() == ABLNodeType.ON);

  private final TreeParserSymbolScope routineScope;
  private final List<Parameter> parameters = new ArrayList<>();
  private DataType returnDatatypeNode = null;
  private ABLNodeType progressType;
  private ExecutionGraph graph;
  // Pointer to FORWARDS declaration of FUNCTION. Can be null.
  private Routine fwdDeclaration;
  private boolean isFwdDeclaration;

  public Routine(String name, TreeParserSymbolScope definingScope, TreeParserSymbolScope routineScope) {
    super(name, definingScope);
    this.routineScope = routineScope;
    this.routineScope.setRoutine(this);
  }

  /** Called by the tree parser. */
  public void addParameter(Parameter p) {
    parameters.add(p);
  }

  /** @see org.prorefactor.treeparser.symbols.Symbol#fullName() */
  @Override
  public String fullName() {
    return getName();
  }

  public String getSignature() {
    StringBuilder val = new StringBuilder(getName()).append('(');
    boolean first = true;
    for (Parameter p : parameters) {
      if (first) {
        first = false;
      } else {
        val.append(',');
      }
      val.append(p.getSignatureString());
    }
    val.append(')');
    return val.toString();
  }

  public String getIDESignature() {
    return getIDESignature(false);
  }
  
  public String getIDESignature(boolean chronological) {
    StringBuilder retVal = new StringBuilder(getName()).append('(');
    boolean first = true;
    for (Parameter p : parameters) {
      if (first) {
        first = false;
      } else {
        retVal.append(", ");
      }
      retVal.append(p.getIDESignature(chronological));
    }
    retVal.append(')');

    if (returnDatatypeNode != null) {
      retVal.append(" : ").append(getReturnDataType());
    }

    return retVal.toString();
  }

  public String getIDEInsertElement(boolean upperCase) {
    StringBuilder retVal = new StringBuilder(getName()).append('(');
    int cnt = 1;
    for (Parameter p : parameters) {
      if (cnt > 1) {
        retVal.append(", ");
      }
      String mode = "";
      if (p.getDirectionNode() == ABLNodeType.INPUTOUTPUT)
        mode = "input-output ";
      else if ((p.getDirectionNode() == ABLNodeType.OUTPUT) || (p.getDirectionNode() == ABLNodeType.RETURN))
        mode = "output ";
      if (upperCase)
        mode = mode.toUpperCase();
      retVal.append(mode).append("${" + cnt + ":");
      if ((p.getSymbol() != null) && (p.getSymbol().getName() != null))
        retVal.append(p.getSymbol().getName());
      else
        retVal.append("arg" + cnt);
      retVal.append("}");
      cnt++;
    }
    retVal.append(")$0");
    return retVal.toString();
  }

  public String getReturnDataType() {
    if (returnDatatypeNode != null) {
      if (returnDatatypeNode.getPrimitive() == PrimitiveDataType.CLASS) {
        return returnDatatypeNode.getClassName();
      } else {
        return returnDatatypeNode.getPrimitive().getIDESignature();
      }
    } else {
      return "N/A";
    }
  }

  public List<Parameter> getParameters() {
    return parameters;
  }

  /**
   * Return Program_root, PROCEDURE, FUNCTION, or METHOD.
   */
  @Override
  public int getProgressType() {
    return progressType.getType();
  }

  /**
   * Return PROGRAM_ROOT, PROCEDURE, FUNCTION, or METHOD.
   */
  @Override
  public ABLNodeType getNodeType() {
    return progressType;
  }

  /**
   * Null for PROCEDURE, node of the datatype for FUNCTION or METHOD. For a Class return value, won't be the CLASS node,
   * but the TYPE_NAME node.
   */
  public DataType getReturnDatatypeNode() {
    return returnDatatypeNode;
  }

  public TreeParserSymbolScope getRoutineScope() {
    return routineScope;
  }

  public IMethodElement getMethodElement() {
    if (progressType == ABLNodeType.METHOD) {
      for (var method : routineScope.getRootScope().getTypeInfo().getMethods()) {
        if (hasSameSignature(this, method)) {
          return method;
        }
      }
    }
    return null;
  }

  public Routine getForwardDeclaration() {
    return fwdDeclaration;
  }

  public boolean isForwardDeclaration() {
    return isFwdDeclaration;
  }

  /**
   * Add pointer to FORWARDS declaration routine
   * @param fwdRoutine
   */
  public void addForwardDeclaration(Routine fwdRoutine) {
    this.fwdDeclaration = fwdRoutine;
  }

  /**
   * Set routine as a FORWARDS declaration
   */
  public void setForwardDeclaration() {
    this.isFwdDeclaration = true;
  }

  public Routine setProgressType(ABLNodeType t) {
    progressType = t;
    return this;
  }

  /** Set by TreeParser for functions and methods. */
  public void setReturnDatatypeNode(DataType n) {
    this.returnDatatypeNode = n;
  }

  public ExecutionGraph getExecutionGraph() {
    if (graph == null) {
      this.graph = createExecutionGraph();
    }

    return graph;
  }
  
  private ExecutionGraph createExecutionGraph() {
    var g2 = new ExecutionGraph();
    if (routineScope.getRootBlock().getNode().isIStatementBlock()) {
      addVerticesAndEdges(g2, routineScope.getRootBlock().getNode().asIStatementBlock(), null);
    }

    return g2;
  }

  private IStatement getNextNonRoutineStatement(IStatement stmt) {
    var currStmt = stmt.getNextStatement();
    while ((currStmt != null) && IS_BLOCK.test(currStmt)) {
      currStmt = currStmt.getNextStatement();
    }
    return currStmt;
  }

  private void addVerticesAndEdges(ExecutionGraph graph, IStatementBlock block, IStatement exitStmt) {
    // Init navigation
    var currStmt = block.getFirstStatement();
    var prevStmt = block.asJPNode();

    // Add block vertex
    graph.addVertex(block.asJPNode());

    while (currStmt != null) {
      if (IS_BLOCK.test(currStmt)) {
        currStmt = currStmt.getNextStatement();
        continue;
      }

      if (currStmt instanceof IfStatementNode ifStmt) {
        var tmp = getNextNonRoutineStatement(currStmt);
        addVertices(graph, ifStmt, tmp == null ? exitStmt : tmp);
      } else if (currStmt instanceof IStatementBlock stmtBlock) {
        var tmp = getNextNonRoutineStatement(currStmt);
        addVerticesAndEdges(graph, stmtBlock, tmp == null ? exitStmt : tmp);
      } else {
        graph.addVertex(currStmt.asJPNode());
      }
      graph.addEdge(prevStmt, currStmt.asJPNode());

      prevStmt = currStmt.asJPNode();
      currStmt = currStmt.getNextStatement();
    }
    if ((exitStmt != null) && (prevStmt != null)) {
      graph.addVertex(exitStmt.asJPNode());
      graph.addEdge(prevStmt, exitStmt.asJPNode());
    }
  }

  private void addVertices(ExecutionGraph graph, IfStatementNode ifNode, IStatement exitStmt) {
    graph.addVertex(ifNode.asJPNode());

    if (ifNode.getThenBlockOrNode() instanceof IfStatementNode ifNode2)
      addVertices(graph, ifNode2, exitStmt);
    else if (ifNode.getThenBlockOrNode() instanceof IStatementBlock stmtBlock) {
      addVerticesAndEdges(graph, stmtBlock, exitStmt);
    } else {
      graph.addVertex(ifNode.getThenBlockOrNode().asJPNode());
      if (exitStmt != null) {
        graph.addVertex(exitStmt.asJPNode());
        graph.addEdge(ifNode.getThenBlockOrNode().asJPNode(), exitStmt.asJPNode());
      }
    }
    graph.addEdge(ifNode.asJPNode(), ifNode.getThenBlockOrNode().asJPNode());

    if (ifNode.getElseBlockOrNode() != null) {
      if (ifNode.getElseBlockOrNode() instanceof IfStatementNode ifNode2)
        addVertices(graph, ifNode2, exitStmt);
      else if (ifNode.getElseBlockOrNode() instanceof IStatementBlock stmtBlock)
        addVerticesAndEdges(graph, stmtBlock, exitStmt);
      else {
        graph.addVertex(ifNode.getElseBlockOrNode().asJPNode());
        if (exitStmt != null) {
          graph.addVertex(exitStmt.asJPNode());
          graph.addEdge(ifNode.getElseBlockOrNode().asJPNode(), exitStmt.asJPNode());
        }
      }

      graph.addEdge(ifNode.asJPNode(), ifNode.getElseBlockOrNode().asJPNode());
    }

    if (exitStmt != null) {
      graph.addVertex(exitStmt.asJPNode());
      graph.addEdge(ifNode.asJPNode(), exitStmt.asJPNode());
    }
  }

  /**
   * 
   */
  private static boolean hasSameSignature(Routine routine, IMethodElement method) {
    // Different name, no match...
    if (!method.getName().equalsIgnoreCase(routine.getName()))
      return false;
    // Different number of parameters, no match...
    if (method.getParameters().length != routine.getParameters().size())
      return false;
    // Compare parameter data type one by one
    int zz = 0;
    for (var prm : method.getParameters()) {
      var prm2 = routine.getParameters().get(zz).getSymbol();
      if (prm2 instanceof Variable var2) {
        if (prm.isClassDataType()) {
          if (!prm.getDataType().getClassName().equalsIgnoreCase(var2.getDataType().getClassName()))
            return false;
        } else if (!prm.getDataType().equals(var2.getDataType()))
          return false;
      } else {
        return false;
      }
      zz++;
    }
    return true;
  }

  @Override
  public Stream copy(TreeParserSymbolScope newScope) {
    throw new UnsupportedOperationException("Routine objects can't be copied");
  }

}
