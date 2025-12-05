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
package eu.rssw.pct.elements.fixed;

import eu.rssw.pct.elements.DataType;
import eu.rssw.pct.elements.IAttributeElement;
import eu.rssw.pct.elements.IVariableElement;

public class AttributeElement implements IAttributeElement {
  protected static final String WRITE_ONLY = "Write-only";
  protected static final String READ_ONLY = "Read-only";
  protected static final String READ_WRITE = "Readable/Writable";

  private boolean readOnly;
  private boolean writeOnly;
  private String description;
  private final IVariableElement varElement;

  public AttributeElement(String name, DataType datatype, String access, String description) {
    varElement = new VariableElement(name, datatype);
    this.description = description;
    setAccess(access);
  }

  @Override
  public IVariableElement getVariable() {
    return varElement;
  }

  @Override
  public int getSizeInRCode() {
    throw new UnsupportedOperationException();
  }

  @Override
  public DataType getDataType() {
    return varElement.getDataType();
  }

  @Override
  public String getDescription() {
    return this.description;
  }

  @Override
  public boolean isReadOnly() {
    return readOnly;
  }

  @Override
  public boolean isWriteOnly() {
    return writeOnly;
  }

  @Override
  public boolean isNoUndo() {
    // Auto-generated method stub
    return false;
  }

  @Override
  public boolean baseIsDotNet() {
    // Auto-generated method stub
    return false;
  }

  @Override
  public boolean isProtected() {
    // Auto-generated method stub
    return false;
  }

  @Override
  public boolean isPublic() {
    // Auto-generated method stub
    return false;
  }

  @Override
  public boolean isPrivate() {
    // Auto-generated method stub
    return false;
  }

  @Override
  public boolean isAbstract() {
    // Auto-generated method stub
    return false;
  }

  @Override
  public boolean isStatic() {
    // Auto-generated method stub
    return false;
  }

  @Override
  public boolean isPackageProtected() {
    // Auto-generated method stub
    return false;
  }

  @Override
  public boolean isPackagePrivate() {
    // Auto-generated method stub
    return false;
  }

  @Override
  public String getName() {
    return varElement.getName();
  }

  @Override
  public int getExtent() {
    // Auto-generated method stub
    return 0;
  }

  private void setAccess(String access) {
    switch (access) {
      case READ_ONLY:
        this.readOnly = true;
        this.writeOnly = false;
        break;
      case WRITE_ONLY:
        this.readOnly = false;
        this.writeOnly = true;
        break;
      case READ_WRITE:
        this.readOnly = false;
        this.writeOnly = false;
        break;
      default:
        this.readOnly = true;
        this.writeOnly = true;
        throw new IllegalArgumentException("Unexpected value: " + access);
    }
  }

}
