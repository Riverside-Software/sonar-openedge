/*
 * OpenEdge plugin for SonarQube
 * Copyright (c) 2015-2026 Riverside Software
 * contact AT riverside DASH software DOT fr
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.prorefactor.core;

import java.util.Objects;

public class Pair<X, Y> {
  private final X o1;
  private final Y o2;

  public Pair(X o1, Y o2) {
    this.o1 = o1;
    this.o2 = o2;
  }

  public static <A, B> Pair<A, B> of(A x, B y) {
    return new Pair<>(x, y);
  }

  public X getO1() {
    return o1;
  }

  public Y getO2() {
    return o2;
  }

  @Override
  public String toString() {
    return "<" + o1.toString() + ", " + o2.toString() + ">";
  }

  @Override
  public boolean equals(Object other) {
    if (other instanceof Pair pair2) {
      return Objects.equals(o1, pair2.o1) && Objects.equals(o2, pair2.o2);
    } else
      return false;
  }

  @Override
  public int hashCode() {
    return Objects.hash(o1, o2);
  }
}
