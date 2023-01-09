/********************************************************************************
 * Copyright (c) 2003-2015 John Green
 * Copyright (c) 2015-2023 Riverside Software
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
package org.prorefactor.macrolevel;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Abstract class for a macro reference. There are two subclasses:<ul>
 * <li>one for references to named macros (i.e. those named with &amp;global, &amp;scoped, or an include argument)
 * <li>one for references to include files.
 * </ul>
 */
public abstract class MacroRef implements MacroEvent {
  private final MacroRef parent;
  private final int refColumn;
  private final int refLine;
  private final int refEndLine;
  private final int refEndColumn;

  /** A list of macro references and defines that are in this macro's source */
  public final List<MacroEvent> macroEventList = new ArrayList<>();

  MacroRef(MacroRef parent, int line, int column, int endLine, int endColumn) {
    this.parent = parent;
    this.refLine = line;
    this.refColumn = column;
    this.refEndLine = endLine;
    this.refEndColumn = endColumn;
  }

  @Override
  public MacroRef getParent() {
    return parent;
  }

  public int getLine() {
    return refLine;
  }

  public int getColumn() {
    return refColumn;
  }

  public int getEndLine() {
    return refEndLine;
  }

  public int getEndColumn() {
    return refEndColumn;
  }

  /**
   * Find <i>external macro references</i>. An external macro is an include file, a &amp;GLOBAL or a &amp;SCOPED from
   * another file, and include args.
   * 
   * &amp;UNDEFINE of a &amp;GLOBAL or of a &amp;SCOPED from another file is considered a reference. &amp;UNDEFINE of an
   * include argument is considered a reference.
   * 
   * The subroutine is recursive, because a local define may incur an external reference.
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
      MacroPosition pos = next.getPosition();
      if (isInRange(pos.getLine(), pos.getColumn(), begin, end)) {
        findExternalMacroReferences(next, ret);
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
      if (def.getType() == MacroDefinitionType.UNDEFINE) {
        if (def.getUndefWhat().getType() == MacroDefinitionType.NAMEDARG) {
          list.add(def);
          return;
        }
        if (!isMine(def.getUndefWhat().getParent()))
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
      if (incl.getFileIndex() == fileIndex)
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
  public MacroPosition getPosition() {
    return new MacroPosition(parent == null ? 0 : parent.getFileIndex(), refLine, refColumn);
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
}
