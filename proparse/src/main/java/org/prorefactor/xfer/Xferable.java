/*******************************************************************************
 * Copyright (c) 2003-2015 John Green
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    John Green - initial API and implementation and/or initial documentation
 *******************************************************************************/ 
package org.prorefactor.xfer;

import java.io.IOException;

/** The interface for an Object which can be written to a DataXferStream. */
public interface Xferable {

  /**
   * Writes this object's fields to the DataXferStream. Calls DataXferStream write*(fieldname) for each field.
   */
  public void writeXferBytes(DataXferStream out) throws IOException;

  /**
   * Write's this object's class's schema to the DataXferStream. Calls DataXferStream schema*(fieldname) for each field.
   */
  public void writeXferSchema(DataXferStream out) throws IOException;

}
