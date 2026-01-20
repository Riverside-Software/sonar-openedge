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
package eu.rssw.pct.elements.fixed;

import eu.rssw.pct.elements.DataType;
import eu.rssw.pct.elements.IFunctionDocumentation;
import eu.rssw.pct.elements.IFunctionParameterList;

public class FunctionDocumentation implements IFunctionDocumentation {
  private final String name;
  private final String description;
  private final DataType returnType;
  private final IFunctionParameterList[] variants;

  public FunctionDocumentation(String name, String description, String returnType, IFunctionParameterList... variants) {
    this.name = name;
    this.description = description;
    this.returnType = DataType.get(returnType);
    this.variants = variants;
  }

  @Override
  public IFunctionParameterList[] getVariants() {
    return variants;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public String getDescription() {
    return description;
  }

  @Override
  public DataType getReturnType() {
    return returnType;
  }

  @Override
  public String toString() {
    return String.format("Function documentation of %s", name);
  }

}
