/*
 * OpenEdge plugin for SonarQube
 * Copyright (c) 2015-2021 Riverside Software
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
package eu.rssw.antlr.profiler;

import java.util.Comparator;

public class LineData {
  private final int lineNumber;
  private final int execCount;
  private final float actualTime;
  private final float cumulativeTime;

  public LineData(int lineNumber, int execCount, float actualTime, float cumulativeTime) {
    this.lineNumber = lineNumber;
    this.execCount = execCount;
    this.actualTime = actualTime;
    this.cumulativeTime = cumulativeTime;
  }

  public int getLineNumber() {
    return lineNumber;
  }

  public int getExecCount() {
    return execCount;
  }

  public float getActualTime() {
    return actualTime;
  }

  public float getCumulativeTime() {
    return cumulativeTime;
  }

  @Override
  public int hashCode() {
    return lineNumber;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (this.getClass() == obj.getClass()) {
      return ((LineData) obj).lineNumber == lineNumber;
    }
    return false;
  }

  @Override
  public String toString() {
    return lineNumber + (execCount == 0 ? "" : " x" + execCount);
  }

  public static class CumulativeTimeComparator implements Comparator<LineData> {

    @Override
    public int compare(LineData o1, LineData o2) {
      return ((int) (o1.getCumulativeTime() * 1000000)) - ((int) (o2.getCumulativeTime() * 1000000));
    }

  }
}
