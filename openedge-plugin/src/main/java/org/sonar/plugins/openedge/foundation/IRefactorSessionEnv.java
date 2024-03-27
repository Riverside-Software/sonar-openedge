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
package org.sonar.plugins.openedge.foundation;

import javax.annotation.Nonnull;

import org.prorefactor.proparse.support.IProparseEnvironment;

public interface IRefactorSessionEnv {
  /**
   * Returns default RefactorSession. Always non-null
   */
  @Nonnull
  IProparseEnvironment getDefaultSession();

  /**
   * Returns RefactorSession given a file name. Always non-null (will return default session if name doesn't match any pattern).
   */
  @Nonnull
  IProparseEnvironment getSession(String fileName);
}
