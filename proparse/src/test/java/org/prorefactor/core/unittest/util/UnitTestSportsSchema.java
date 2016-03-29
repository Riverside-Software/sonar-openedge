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

import java.io.IOException;

import org.prorefactor.core.schema.Schema;

public class UnitTestSportsSchema extends Schema {
  public UnitTestSportsSchema() throws IOException {
    super("src/test/resources/projects/unittest/proparse.schema", true);
  }
}
