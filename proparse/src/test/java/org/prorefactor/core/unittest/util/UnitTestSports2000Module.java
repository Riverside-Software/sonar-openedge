/*******************************************************************************
 * Copyright (c) 2015 Gilles Querret
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Gilles Querret - initial API and implementation and/or initial documentation
 *******************************************************************************/ 
package org.prorefactor.core.unittest.util;

import org.prorefactor.core.schema.Schema;
import org.prorefactor.refactor.settings.IProgressSettings;
import org.prorefactor.refactor.settings.IProparseSettings;

import com.google.inject.AbstractModule;

public class UnitTestSports2000Module extends AbstractModule {
  @Override
  protected void configure() {
    bind(IProgressSettings.class).to(UnitTestProgressSettings.class);
    bind(IProparseSettings.class).to(UnitTestProparseSettings.class);
    bind(Schema.class).to(UnitTestSports2000Schema.class);
  }
}
