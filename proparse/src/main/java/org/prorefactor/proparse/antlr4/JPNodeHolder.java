package org.prorefactor.proparse.antlr4;

import java.util.ArrayList;
import java.util.List;

import org.prorefactor.core.JPNode;

import com.google.common.collect.ImmutableList;

public class JPNodeHolder {
  private final List<JPNode> list = new ArrayList<>();
  
  public JPNodeHolder() {
  }

  public JPNodeHolder(JPNode node) {
    this.list.add(node);
  }

  public JPNodeHolder(JPNode node, JPNode... others) {
    this.list.add(node);
    for (JPNode o : others) {
      this.list.add(o);
    }
  }

  public void addNode(JPNode node) {
    this.list.add(node);
  }

  public void addHolder(JPNodeHolder holder) {
    for (JPNode n : holder.getNodes()) {
      this.list.add(n);
    }
  }

  public boolean isAlone() {
    return list.size() == 1;
  }

  public JPNode getFirstNode() {
    return list.get(0);
  }

  public JPNode[] getNodes() {
    return list.toArray(new JPNode[] {});
  }

  public List<JPNode> getNodesAsList() {
    return ImmutableList.copyOf(list);
  }
}
