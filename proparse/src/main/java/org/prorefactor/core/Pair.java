/**
 * ABL Language Server implementation
 *
 * This source code is not part of an open-source package.
 * Copyright (c) 2021-2023 Riverside Software
 * contact@riverside-software.fr
 */
package org.prorefactor.core;

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
}
