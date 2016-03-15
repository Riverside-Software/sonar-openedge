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

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.prorefactor.proparse.IntegerIndex;

import java.util.Set;

/**
 * Serialize objects to a blob optimized for random access. Objects of this class must only be used within a single
 * thread.
 * <p>
 * Several hours of searching the internet turned up many different tools for serialization, but none of them (that I
 * could find) were designed with direct, fast random access as the primary consideration.
 * <p>
 * This and Xferable are designed such that it is self-describing, and if the output class 'schemas' are used correctly,
 * there should never be a reason for the addition of new data fields added to the output to break any code using 'old'
 * versions of the schema. Each byte offset is written with a field-reference-name, and the schema is written to every
 * blob.
 */
public class DataXferStream {

  private int indexOffset;
  private int schemaMapOffset;

  private IntegerIndex<Object> objectIndexes = new IntegerIndex<Object>();

  private HashMap<Integer, Integer> index2offset = new HashMap<Integer, Integer>();

  private HashSet<Xferable> classExamples = new HashSet<Xferable>();
  private HashSet<Object> queue = new HashSet<Object>();

  private ByteArrayOutputStream bytes = new ByteArrayOutputStream();
  private DataOutputStream out = new DataOutputStream(bytes);

  private static final byte XNULL = 2;
  private static final byte XINT = 4;
  private static final byte XSTRING = 6;
  private static final byte XLIST = 8;
  private static final byte XMAP = 10;
  private static final byte XOBJECT = 12;
  private static final byte XTRUE = 14;
  private static final byte XBOOL = 15;
  private static final byte XFALSE = 16;
  private static final byte XNOREP = 18;
  private static final byte XSCHEMA = 20;
  private static final byte XSCHEMA_END = 22;

  /** May be used for building an xfer blob just for schema purposes. */
  public void addSchemaFor(Xferable x) {
    classIndex(x);
  }

  /** Must be called after all objects have been stored. */
  public void buildIndexes() throws IOException {
    // Write the schema records and a map of their indexes to offsets.
    TreeMap<Integer, Integer> schemaRecordOffsets = new TreeMap<Integer, Integer>();
    for (Xferable x : classExamples) {
      Class clas = x.getClass();
      int index = objectIndexes.getIndex(clas);
      int offset = out.size();
      index2offset.put(index, offset);
      schemaRecordOffsets.put(index, offset);
      writeSchemaRecord(clas, x);
    }
    // The schema adds a bunch of string records to the queue.
    processQueue();
    // Write the map of schema record indexes and offsets.
    schemaMapOffset = out.size();
    out.writeInt(schemaRecordOffsets.size());
    for (Map.Entry<Integer, Integer> entry : schemaRecordOffsets.entrySet()) {
      out.writeInt(entry.getKey());
      out.writeInt(entry.getValue());
    }
    // Write the index.
    indexOffset = out.size();
    int indexSize = objectIndexes.size();
    out.writeInt(indexSize);
    for (int i = 0; i < indexSize; ++i) {
      Integer offset = index2offset.get(i);
      out.writeInt(offset != null ? offset : -1);
    }
    // Use the end of the blob for those offsets...
    out.writeInt(schemaMapOffset);
    out.writeInt(indexOffset);
  }

  private int classIndex(Xferable x) {
    Class clas = x.getClass();
    int classIndex = objectIndexes.getIndex(clas);
    if (classIndex == -1) {
      classIndex = objectIndexes.add(clas);
      classExamples.add(x);
    }
    return classIndex;
  }

  /** Once buildIndex() has been called, then use this to get the blob. */
  public ByteArrayOutputStream getBytes() {
    return bytes;
  }

  public int getIndexOffset() {
    return indexOffset;
  }

  public int getSchemaMapOffset() {
    return schemaMapOffset;
  }

  /**
   * Get the offset of an object that's already been stored. Returns -1 if the object is not stored.
   */
  public int getOffsetOf(Object o) throws IOException {
    int index = objectIndexes.getIndex(o);
    if (index == -1)
      return -1;
    Integer ret = index2offset.get(index);
    if (ret == null)
      return -1;
    return ret;
  }

  private int indexAndQueue(Object o) {
    int index = objectIndexes.add(o);
    if (!index2offset.containsKey(index))
      queue.add(o);
    return index;
  }

  private void processQueue() throws IOException {
    while (queue.size() > 0) {
      Iterator it = queue.iterator();
      Object obj = it.next();
      it.remove();
      writeObjectRecord(obj);
    }
  }

  public void schemaBool(String fieldName) throws IOException {
    out.writeByte(XBOOL);
    writeRef(fieldName);
  }

  public void schemaInt(String fieldName) throws IOException {
    out.writeByte(XINT);
    writeRef(fieldName);
  }

  public void schemaRef(String fieldName) throws IOException {
    out.writeByte(XOBJECT);
    writeRef(fieldName);
  }

  /** Get the size in bytes. */
  public int size() {
    return out.size();
  }

  /**
   * Store an object and any object hierarchy that comes with it. The process that creates a DataXferStream would
   * typically call this next, and this might be called multiple times, depending on how many object hierarchies are to
   * be stored. After all object hierarchies have been stored, then buildIndex() should be called before getting the
   * byte array. This is NOT TO BE CALLED FROM Xferable.writeXferBytes().
   * 
   * @return The offset of the stored object record.
   */
  public int store(Object o) throws IOException {
    int ret = bytes.size();
    writeObjectRecord(o);
    processQueue();
    return ret;
  }

  /**
   * Called by Xferable.writeXferBytes() for boolean fields. Writes an single byte encoding for true or false.
   */
  public void writeBool(boolean b) throws IOException {
    out.writeByte(b ? XTRUE : XFALSE);
  }

  private void writeBooleanRecord(Boolean b) throws IOException {
    out.writeByte(b ? XTRUE : XFALSE);
  }

  /**
   * Called by Xferable.writeXferBytes() for int fields. Writes a 4 byte int.
   */
  public void writeInt(int i) throws IOException {
    out.writeInt(i);
  }

  private void writeIntegerRecord(Integer i) throws IOException {
    out.writeByte(XINT);
    out.writeInt(i);
  }

  private void writeList(List list) throws IOException {
    out.writeInt(list.size());
    for (Object o : list)
      writeRef(o);
  }

  /** Also used for arrays: Arrays.asList(theArray). */
  private void writeListRecord(List list) throws IOException {
    out.writeByte(XLIST);
    writeList(list);
  }

  private void writeMap(Map map) throws IOException {
    out.writeInt(map.size());
    for (Object o : map.entrySet()) {
      Map.Entry entry = (Map.Entry) o;
      writeRef(entry.getKey());
      writeRef(entry.getValue());
    }
  }

  private void writeMapRecord(Map map) throws IOException {
    out.writeByte(XMAP);
    writeMap(map);
  }

  /**
   * Write a record of a referenced object that has no Xferable representation. Every object reference is an integer
   * that must appear in the index-to-offset data. That's true even if it's a reference to an object that is not stored.
   * In this record, we store the index to the name of the object's class.
   */
  private void writeNoRepRecord(Object o) throws IOException {
    out.writeByte(XNOREP);
    out.writeInt(indexAndQueue(o.getClass().getName()));
  }

  private void writeNullRecord() throws IOException {
    out.writeByte(XNULL);
  }

  /** This is called by store(Object) and for storage of objects in the queue. */
  private void writeObjectRecord(Object o) throws IOException {

    int index = objectIndexes.getIndex(o);
    index2offset.put(index, out.size());

    if (o == null)
      writeNullRecord();
    else if (o instanceof Integer)
      writeIntegerRecord((Integer) o);
    else if (o instanceof Boolean)
      writeBooleanRecord((Boolean) o);
    else if (o instanceof String)
      writeStringRecord((String) o);
    else if (o instanceof Xferable)
      writeXferableRecord((Xferable) o);
    else if (o instanceof Map)
      writeMapRecord((Map) o);
    else if (o instanceof List)
      writeListRecord((List) o);
    else if (o.getClass().isArray())
      writeListRecord(Arrays.asList((Object[]) o));
    else if (o instanceof Set)
      writeListRecord(Arrays.asList(((Set) o).toArray()));
    else
      /*
       * We will have lists of objects where we want to write all of the Xferable objects and just skip over the ones
       * that aren't Xferable.
       */
      writeNoRepRecord(o);
  }

  /**
   * Called by Xferable.writeXferBytes(). Used for Strings, Collections, and any other Object references. Writes -1 for
   * null, otherwise the index to the object's record (4 byte int).
   */
  public void writeRef(Object o) throws IOException {
    if (o == null)
      out.writeInt(-1);
    else
      out.writeInt(indexAndQueue(o));
  }

  private void writeSchemaRecord(Class clas, Xferable example) throws IOException {
    out.writeByte(XSCHEMA);
    writeString(clas.getName());
    example.writeXferSchema(this);
    out.writeByte(XSCHEMA_END);
  }

  private void writeString(String s) throws IOException {
    byte[] b = s.getBytes();
    out.writeInt(b.length);
    out.write(b);
  }

  private void writeStringRecord(String s) throws IOException {
    out.writeByte(XSTRING);
    writeString(s);
  }

  private void writeXferableRecord(Xferable x) throws IOException {
    out.writeByte(XOBJECT);
    out.writeInt(classIndex(x));
    x.writeXferBytes(this);
  }

}

/*
 * Notes
 *
 * One idea was to use permanantly fixed field positions in the byte output for each class. New fields could be added to
 * the end, but never inserted or shuffled. That didn't quite work out, because a sub-class wouldn't be able to refer to
 * the super-class's writeXferBytes(), in case the super class added fields at any point. Also the subclass couldn't
 * just write the super's fields itself, because in many cases the super had private fields. As a result, I decided to
 * have the 'schema' for each class written to the binary file instead. This takes care of documentation, field order,
 * calling the super, no more problem with accessing private fields from super, etc. By hard-coding the field reference
 * name written to the schema (rather than using reflection) we aren't putting undue restrictions on changes to the Java
 * class source code and internals.
 *
 */
