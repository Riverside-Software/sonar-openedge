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

/**
 * Values of the Reference-type attribute in XREF files. The XREF format isn't formally specified by Progress, and new
 * values can appear in newer OpenEdge releases, so {@link #fromValue(String)} falls back to {@link #UNKNOWN} instead of
 * failing when it encounters a value not listed here.
 */
public enum ReferenceType {
  ACCESS,
  ANNOTATION,
  BLOCK_LEVEL,
  CAST,
  CLASS,
  COMPILE,
  CONSTRUCTOR,
  CPINTERNAL,
  CPSTREAM,
  DATA_MEMBER,
  DATASET,
  DELETE_INSTANCE,
  DESTRUCTOR,
  EVENT,
  FUNCTION,
  INCLUDE,
  INVOKE,
  METHOD,
  NEW,
  NEW_SHR_DATASET,
  NEW_SHR_TEMPTABLE,
  NEW_SHR_VARIABLE,
  PRIVATE_PROCEDURE,
  PROCEDURE,
  PROPERTY,
  PUBLISH,
  REFERENCE,
  RUN,
  SEARCH,
  SHR_DATASET,
  SHR_TEMPTABLE,
  SORT_ACCESS,
  STRING,
  SUBSCRIBE,
  UNSUBSCRIBE,
  UPDATE,
  CREATE,
  DELETE,
  UNKNOWN;

  public static ReferenceType fromValue(String value) {
    if (value == null) {
      return UNKNOWN;
    }
    try {
      return valueOf(value.toUpperCase().replace('-', '_'));
    } catch (IllegalArgumentException uncaught) {
      return UNKNOWN;
    }
  }

  public String toValue() {
    return this == UNKNOWN ? "" : name().replace('_', '-');
  }
}
