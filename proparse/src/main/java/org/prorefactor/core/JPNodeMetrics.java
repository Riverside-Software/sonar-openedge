/********************************************************************************
 * Copyright (c) 2015-2025 Riverside Software
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
package org.prorefactor.core;

public class JPNodeMetrics {
  private final int loc;
  private final int comments;
  private final int directives;

  public JPNodeMetrics(int loc, int comments, int directives) {
    this.loc = loc;
    this.comments = comments;
    this.directives = directives;
  }

  public int getLoc() {
    return loc;
  }

  public int getComments() {
    return comments;
  }

  public int getDirectives() {
    return directives;
  }
}