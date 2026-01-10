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
package eu.rssw.pct.elements;

import com.google.gson.annotations.JsonAdapter;

@JsonAdapter(ParameterAdapter.class)
public interface IParameter extends IElement {
  int getNum();
  String getName();
  int getExtent();
  DataType getDataType();
  ParameterMode getMode();
  ParameterType getParameterType();
  boolean isClassDataType();

  default String getSignature() {
    StringBuilder sb = new StringBuilder();
    switch (getMode()) {
      case INPUT_OUTPUT:
        sb.append('M');
        break;
      case OUTPUT:
        sb.append('O');
        break;
      case RETURN:
        sb.append('R');
        break;
      case BUFFER:
        return sb.append('B').toString();
      default:
        sb.append('I'); // INPUT
    }
    switch (getParameterType()) {
      case TABLE, BUFFER_TEMP_TABLE:
        sb.append('T');
        if (getDataType().getPrimitive() == PrimitiveDataType.HANDLE)
          sb.append('H');
        break;
      case DATASET:
        sb.append('D');
        if (getDataType().getPrimitive() == PrimitiveDataType.HANDLE)
          sb.append('H');
        break;
      case BROWSE:
        return sb.append('W').toString();
      case VARIABLE:
        if (isClassDataType())
          sb.append('Z').append(getDataType().getClassName());
        else
          sb.append(getDataType().getPrimitive().getSignature());
        break;
      default:
        sb.append("??");
    }
    if (getExtent() != 0)
      sb.append("[]");
    return sb.toString();
  }

  default String getIDESignature(boolean chronological) {
    StringBuilder sb = new StringBuilder();
    switch (getMode()) {
      case INPUT_OUTPUT:
        sb.append('⇅');
        break;
      case OUTPUT:
        sb.append(chronological ? '↑' : '↓');
        break;
      case RETURN:
        sb.append(chronological ? '⇈' : '⇊');
        break;
      case BUFFER:
        return sb.append("BUFFER").toString();
      default:
        sb.append(chronological ? '↓' : '↑'); // INPUT
    }
    switch (getParameterType()) {
      case TABLE, BUFFER_TEMP_TABLE:
        sb.append("TBL");
        if (getDataType().getPrimitive() == PrimitiveDataType.HANDLE)
          sb.append("-HDL");
        break;
      case DATASET:
        sb.append("DS");
        if (getDataType().getPrimitive() == PrimitiveDataType.HANDLE)
          sb.append("-HDL");
        break;
      case BROWSE:
        return sb.append("BRWS").toString();
      case VARIABLE:
        if (isClassDataType())
          sb.append(getDataType().getClassName());
        else
          sb.append(getDataType().getPrimitive().getIDESignature());
        break;
      default:
        sb.append("??");
    }
    if (getExtent() != 0)
      sb.append("[]");
    sb.append(' ').append(getName());

    return sb.toString();
  }

}
