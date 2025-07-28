/*
 * OpenEdge plugin for SonarQube
 * Copyright (c) 2015-2025 Riverside Software
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

public class Triplet<X, Y, Z> {
  private final X o1;
  private final Y o2;
  private final Z o3;

  public Triplet(X o1, Y o2, Z o3) {
    this.o1 = o1;
    this.o2 = o2;
    this.o3 = o3;
  }

  public static <A, B, C> Triplet<A, B, C> of(A x, B y, C z) {
    return new Triplet<>(x, y, z);
  }

  public X getO1() {
    return o1;
  }

  public Y getO2() {
    return o2;
  }

  public Z getO3() {
    return o3;
  }

  @Override
  public String toString() {
    return "<" + o1.toString() + ", " + o2.toString() + ", " + o3.toString() + ">";
  }
}
