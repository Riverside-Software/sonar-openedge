/*
 * OpenEdge plugin for SonarQube
 * Copyright (c) 2015-2018 Riverside Software
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
package org.sonar.plugins.openedge.checks;

import org.prorefactor.treeparser.ParseUnit;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.plugins.openedge.api.checks.OpenEdgeProparseCheck;
import org.sonar.plugins.openedge.foundation.OpenEdgeMetrics;

import com.progress.xref.CrossReference.Source;
import com.progress.xref.CrossReference.Source.Reference;

@Rule(priority = Priority.MAJOR, name = "Shared objects analyzer")
public class SharedObjectsAnalyzer extends OpenEdgeProparseCheck {

  @Override
  public void execute(InputFile file, ParseUnit unit) {
    if (unit.getXref() == null)
      return;

    int numShrTT = 0;
    int numShrDS = 0;
    int numShrVar = 0;
    for (Source src : unit.getXref().getSource()) {
      for (Reference ref : src.getReference()) {
        switch (ref.getReferenceType().toLowerCase()) {
          case "NEW-SHR-TEMPTABLE":
            numShrTT++;
            break;
          case "NEW-SHR-DATASET":
            numShrDS++;
            break;
          case "NEW-SHR-VARIABLE":
            numShrVar++;
            break;
          default:
            break;
        }
      }
    }

    reportMeasure(file, OpenEdgeMetrics.SHR_TT, numShrTT);
    reportMeasure(file, OpenEdgeMetrics.SHR_DS, numShrDS);
    reportMeasure(file, OpenEdgeMetrics.SHR_VAR, numShrVar);
  }

}
