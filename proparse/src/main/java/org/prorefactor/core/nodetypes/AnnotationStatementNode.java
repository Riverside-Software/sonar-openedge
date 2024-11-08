/********************************************************************************
 * Copyright (c) 2015-2024 Riverside Software
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
package org.prorefactor.core.nodetypes;

import javax.annotation.Nonnull;

import org.prorefactor.core.JPNode;
import org.prorefactor.core.ProToken;

public class AnnotationStatementNode extends StatementNode {
  public AnnotationStatementNode(ProToken t, JPNode parent, int num, boolean hasChildren) {
    super(t, parent, num, hasChildren, null);
  }

  @Override
  public void addAnnotation(AnnotationStatementNode annotation) {
    throw new IllegalStateException("Annotations can't be added to annotations");
  }

  @Override
  public boolean hasAnnotation(String str) {
      return false;
  }

  @Nonnull
  public String getAnnotationText() {
    String text = getText();
    if (text == null)
      return "";
    if (getNumberOfChildren() > 1) {
      text += getFirstChild().getText();
    }
    return text;
  }
}
