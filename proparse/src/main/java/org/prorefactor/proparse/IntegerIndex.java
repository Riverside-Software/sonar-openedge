/*******************************************************************************
 * Copyright (c) 2003-2015 John Green
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    John Green - initial API and implementation and/or initial documentation
 *    Gilles Querret - Switched to BiMap implementation
 *******************************************************************************/ 
package org.prorefactor.proparse;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import java.util.ArrayList;
import java.util.List;

/**
 * Generic class for working with integer indexed data of type T. ie: You want to be able to lookup the value at integer
 * index n, and you also want to be able to find the integer index of some data of type T. Integer indexes start from
 * zero, as in Java array indexes.
 * 
 * Not thread-safe
 */
public class IntegerIndex<T> {

  private final BiMap<Integer, T> biMap = HashBiMap.create();
  private int nextIndex;

  /**
   * Add the value if it's not already there. Returns the new or existing index.
   */
  public int add(T val) {
    Integer ret = biMap.inverse().get(val);
    if (ret != null) {
      return ret;
    }

    ret = nextIndex++;
    biMap.put(ret, val);

    return ret;
  }

  public void clear() {
    biMap.clear();
    nextIndex = 0;
  }

  /**
   * Returns -1 if not found
   */
  public int getIndex(T val) {
    Integer ret = biMap.inverse().get(val);
    if (ret == null) {
      return -1;
    }

    return ret;
  }

  /**
   * Returns null if not found
   */
  public T getValue(int index) {
    return biMap.get(index);
  }

  /**
   * Returns an array list in order from zero to number of indexes of all the values
   */
  public List<T> getValues() {
    List<T> list = new ArrayList<T>(nextIndex);
    for (int i = 0; i < nextIndex; ++i) {
      list.add(biMap.get(i));
    }

    return list;
  }

  public boolean hasIndex(int index) {
    return biMap.containsKey(index);
  }

  public boolean hasValue(T value) {
    return biMap.containsValue(value);
  }

  public int size() {
    return nextIndex;
  }
}
