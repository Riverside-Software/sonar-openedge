/*******************************************************************************
 * Copyright (c) 2015-2018 Riverside Software
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

public class SportsSchema extends Schema {
  public SportsSchema() throws IOException {
    super("src/test/resources/projects/sports2000/sports2000.cache", true);
  }
}
