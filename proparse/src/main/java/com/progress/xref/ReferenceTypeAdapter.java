/********************************************************************************
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
package com.progress.xref;

import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * Maps the Reference-type attribute to {@link ReferenceType}, falling back to {@link ReferenceType#UNKNOWN} instead
 * of failing the whole document when an unrecognized value is found.
 */
public class ReferenceTypeAdapter extends XmlAdapter<String, ReferenceType> {

  @Override
  public ReferenceType unmarshal(String value) {
    return ReferenceType.fromValue(value);
  }

  @Override
  public String marshal(ReferenceType value) {
    return value == null ? null : value.toValue();
  }
}
