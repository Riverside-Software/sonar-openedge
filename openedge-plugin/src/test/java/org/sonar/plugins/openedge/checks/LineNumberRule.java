/*
 * OpenEdge plugin for SonarQube
 * Copyright (c) 2019-2026 Riverside Software
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

import java.nio.file.Path;

import org.prorefactor.treeparser.ParseUnit;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.sonar.plugins.openedge.api.checks.OpenEdgeProparseCheck;

@Rule(priority = Priority.BLOCKER, name = "Test rule #1")
public class LineNumberRule extends OpenEdgeProparseCheck {
  // Test rule which reports issues on file num/line num. List is passed as parameter as a list of
  // file num,line num with semicolon separator, e.g. 0,5;3,4 for line 5 of main file, and line 4 of file #3
  @RuleProperty
  public String fileNums = "";

  @Override
  public void execute(InputFile file, ParseUnit unit) {
    for (var pair : fileNums.split(";")) {
      var fileNum = Integer.parseInt(pair.substring(0, pair.indexOf(',')));
      var lineNum = Integer.parseInt(pair.substring(pair.indexOf(',') + 1));
      if (fileNum == 0) {
        reportIssue(file, lineNum, "File " + fileNum + " - Line " + lineNum);
      } else {
        // Not possible (for now) to use getInputFile from this test case, so use workaround
        // to find the right include file
        var fName = Path.of(unit.getIncludeFileName(fileNum)).getFileName().toString();
        var pred = getContext().fileSystem().predicates().hasFilename(fName);
        var inputFile = getContext().fileSystem().inputFile(pred);
        if (inputFile != null) {
          reportIssue(inputFile, lineNum, "File " + fileNum + " - Line " + lineNum);
        }
      }
    }
  }

}
