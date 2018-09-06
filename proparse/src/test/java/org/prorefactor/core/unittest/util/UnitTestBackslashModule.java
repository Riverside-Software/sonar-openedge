/********************************************************************************
 * Copyright (c) 2015-2018 Riverside Software
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
package org.prorefactor.core.unittest.util;

import org.prorefactor.core.schema.ISchema;
import org.prorefactor.refactor.settings.IProparseSettings;

import com.google.inject.AbstractModule;

public class UnitTestBackslashModule extends AbstractModule {
  @Override
  protected void configure() {
    bind(IProparseSettings.class).to(UnitTestBackslashProparseSettings.class);
    bind(ISchema.class).to(SportsSchema.class);
  }
}
