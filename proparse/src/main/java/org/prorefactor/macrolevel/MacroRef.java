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
package org.prorefactor.macrolevel;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.io.IOException;

import org.prorefactor.refactor.RefactorException;
import org.prorefactor.xfer.DataXferStream;

/**
 * Abstract class for a macro reference. There are two subclasses: one for references to named macros (i.e. those named
 * with &global, &scoped, or an include argument), and one for references to include files.
 */
public abstract class MacroRef implements MacroEvent {
  private static final long serialVersionUID = -3732504160384813887L;

  int listingFileLine;
  public int refColumn;
  public int refLine;
  public MacroRef parent = null;

  /** A list of macro references and defines that are in this macro's source */
  public List<MacroEvent> macroEventList = new ArrayList<>();

  /** Only to be used for persistence/serialization. */
  protected MacroRef() {
  }

  MacroRef(int listingFileLine) {
    this.listingFileLine = listingFileLine;
  }

  public int getListingFileLine() {
    return listingFileLine;
  }

  @Override
  public MacroRef getParent() {
    return parent;
  }

  /**
   * Find <i>external macro references</i>. An external macro is an include file, a &GLOBAL or a &SCOPED from another
   * file, and include args.
   * 
   * TODO: (Jan 26) This doesn't seem right to me anymore. An &UNDEFINE only affects the local scope. If re-implemented
   * after building a pseudoprocessor, consider dropping this. &UNDEFINE of a &GLOBAL or of a &SCOPED from another file
   * is considered a reference. &UNDEFINE of an include argument is considered a reference.
   * 
   * The subroutine is recursive, because a local define may incur an external reference!
   * 
   * @return An array of objects: MacroRef and MacroDef (for UNDEFINE).
   */
  public List<MacroEvent> findExternalMacroReferences() {
    List<MacroEvent> ret = new ArrayList<>();
    for (Iterator<MacroEvent> it = macroEventList.iterator(); it.hasNext();) {
      findExternalMacroReferences(it.next(), ret);
    }
    return ret;
  }

  /**
   * @see #findExternalMacroReferences()
   * @param begin An array of two integers to indicate the beginning line/column. May be null to indicate the beginning
   *          of the range is open ended.
   * @param end An array of two integers to indicate the ending line/column. May be null to indicate the ending of the
   *          range is open ended.
   */
  public List<MacroEvent> findExternalMacroReferences(int[] begin, int[] end) {
    List<MacroEvent> ret = new ArrayList<>();
    for (Iterator<MacroEvent> it = macroEventList.iterator(); it.hasNext();) {
      MacroEvent next = it.next();
      if (next instanceof MacroRef) {
        MacroRef ref = (MacroRef) next;
        if (isInRange(ref.refLine, ref.refColumn, begin, end)) {
          findExternalMacroReferences(ref, ret);
        }
        continue;
      }
      if (next instanceof MacroDef) {
        MacroDef def = (MacroDef) next;
        if (isInRange(def.line, def.column, begin, end))
          findExternalMacroReferences(def, ret);
      }
    }
    return ret;
  }

  private void findExternalMacroReferences(MacroEvent obj, List<MacroEvent> list) {
    if (obj == null)
      return;
    if (obj instanceof IncludeRef) {
      list.add(obj);
      return;
    }
    if (obj instanceof MacroDef) {
      MacroDef def = (MacroDef) obj;
      if (def.type == MacroDef.UNDEFINE) {
        if (def.undefWhat.type == MacroDef.NAMEDARG) {
          list.add(def);
          return;
        }
        if (!isMine(def.undefWhat.parent))
          list.add(def);
      }
      return;
    }
    // Only one last type we're interested in...
    if (!(obj instanceof NamedMacroRef))
      return;
    NamedMacroRef ref = (NamedMacroRef) obj;
    if (!isMine(ref)) {
      list.add(ref);
      return;
    }
    // It's possible for an internal macro to refer to an external macro
    for (Iterator<MacroEvent> it = ref.macroEventList.iterator(); it.hasNext();) {
      findExternalMacroReferences(it.next(), list);
    }
  }

  /**
   * Find references to an include file by the include file's file index number. Search is recursive, beginning at this
   * MacroRef object.
   * 
   * @param fileIndex The fileIndex for the include file we want references to.
   * @return An array of IncludeRef objects.
   */
  public List<IncludeRef> findIncludeReferences(int fileIndex) {
    List<IncludeRef> ret = new ArrayList<>();
    findIncludeReferences(fileIndex, this, ret);
    return ret;
  }

  private void findIncludeReferences(int fileIndex, MacroRef ref, List<IncludeRef> list) {
    if (ref == null)
      return;
    if (ref instanceof IncludeRef) {
      IncludeRef incl = (IncludeRef) ref;
      if (incl.fileIndex == fileIndex)
        list.add(incl);
    }
    for (Iterator<MacroEvent> it = ref.macroEventList.iterator(); it.hasNext();) {
      MacroEvent next = it.next();
      if (next instanceof MacroRef)
        findIncludeReferences(fileIndex, (MacroRef) next, list);
    }
  }

  public abstract int getFileIndex();

  @Override
  public MacroPosition getPosition() throws RefactorException {
    return new MacroPosition(parent.getFileIndex(), refLine, refColumn);
  }

  /** Is a macro ref/def myself, or, a child of mine? */
  private boolean isMine(MacroEvent obj) {
    if (obj == null)
      return false;
    if (obj == this)
      return true;
    return isMine(obj.getParent());
  }

  /**
   * Assuming an x,y range, this function returns whether an input x and y are within the specified range of x,y begin
   * and x,y end. We use this primarily for checking if a line/column are within the specified range. The "range" may be
   * open ended, see parameter descriptions.
   * 
   * @param x The x value to check that it is within range
   * @param y The y value to check that it is within range
   * @param begin An array of 2 integers to specify the beginning of the x,y range. May be null to indicate that the
   *          beginning is open ended.
   * @param end An array of 2 integers to specify the ending of the x,y range. May be null to indicate that the
   *          beginning is open ended.
   * @return
   */
  public static boolean isInRange(int x, int y, int[] begin, int[] end) {
    if ((begin != null) && ((x < begin[0]) || ((x == begin[0]) && (y < begin[1])))) {
      return false;
    }
    if ((end != null) && ((x > end[0]) || ((x == end[0]) && (y > end[1])))) {
      return false;
    }
    return true;
  }

  @Override
  public void writeXferBytes(DataXferStream out) throws IOException {
    out.writeInt(listingFileLine);
    out.writeRef(macroEventList);
    out.writeRef(parent);
    out.writeInt(refColumn);
    out.writeInt(refLine);
  }

  @Override
  public void writeXferSchema(DataXferStream out) throws IOException {
    out.schemaInt("listingFileLine");
    out.schemaRef("macroEventList");
    out.schemaRef("parent");
    out.schemaInt("refColumn");
    out.schemaInt("refLine");
  }
}
