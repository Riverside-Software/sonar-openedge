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
grammar ProfilerGrammar;

@parser::members {
  private int versionNumber = -1;
}

profiler:
  description
  moduleData
  callTreeData
  lineSummary
  tracingData
  coverageData
  statisticsData
  coverageData2
  userData;

description:
  version=NUMBER
    { try { versionNumber = Integer.parseInt($version.text) ; } catch (NumberFormatException uncaught) { } } 
  date=DATE desc=STRING time=TIME author=STRING jsonData CHR_DOT NEWLINE;

jsonData:
    { versionNumber < 3 }? NEWLINE
  | { versionNumber >= 3 }? JSON;

moduleData:
  moduleDataLine* CHR_DOT NEWLINE;

moduleDataLine:
  id=NUMBER name=STRING debugListingFile=STRING crc=NUMBER ( NUMBER STRING )? NEWLINE;

callTreeData:
  callTreeDataLine* CHR_DOT NEWLINE;

callTreeDataLine:
  callerId=NUMBER callerLineNum=NUMBER calleeId=NUMBER callCount=NUMBER NEWLINE;

lineSummary:
  lineSummaryLine* CHR_DOT NEWLINE;

lineSummaryLine:
  moduleId=NUMBER lineNumber=NUMBER execCount=NUMBER actualTime=FLOAT cumulativeTime=FLOAT NEWLINE;

tracingData:
  tracingDataLine* CHR_DOT NEWLINE;

tracingDataLine:
  moduleId=NUMBER lineNumber=NUMBER execTime=FLOAT timestamp=FLOAT NEWLINE;

coverageData:
  coverageSection* CHR_DOT NEWLINE;

coverageSection:
  moduleId=NUMBER name=STRING lineCount=NUMBER NEWLINE coverage_section_line+ CHR_DOT NEWLINE;

coverage_section_line:
  linenum=NUMBER NEWLINE;

coverageData2:
  | { versionNumber >= 3 }? coverageSection2Line* CHR_DOT NEWLINE;

coverageSection2Line:
  NUMBER NUMBER NUMBER NUMBER NUMBER FLOAT NUMBER* NEWLINE;

statisticsData:
  | { (versionNumber == 2) || (versionNumber == 4) }?
  stats1Data stats2Data stats3Data stats4Data;

stats1Data:
  stats1Line* CHR_DOT NEWLINE;

stats2Data:
  stats2Line* CHR_DOT NEWLINE;

stats3Data:
  stats3Line* CHR_DOT NEWLINE;

stats4Data:
  stats4Line CHR_DOT NEWLINE;

stats1Line:
  NUMBER NUMBER NUMBER STRING NEWLINE;

stats2Line:
  NUMBER FLOAT+ NEWLINE;

stats3Line:
  NUMBER+ NEWLINE;

stats4Line:
  NUMBER NUMBER STRING NEWLINE;

userData:
  userDataLine* CHR_DOT NEWLINE;

userDataLine:
  FLOAT STRING NEWLINE;

JSON:
  '{' .*? '}' WS* NEWLINE;

fragment INT:
  ('0'..'9');

NUMBER:
  '-'? INT+;

FLOAT:
  NUMBER '.' NUMBER;

DATE:
  ('0'..'9') ('0'..'9') '/' ('0'..'9') ('0'..'9') '/' ('0'..'9') ('0'..'9') ('0'..'9') ('0'..'9');

TIME:
  ('0'..'9') ('0'..'9') ':' ('0'..'9') ('0'..'9') ':' ('0'..'9') ('0'..'9');

STRING:
  '"' .*? '"' { setText(getText().substring(1, getText().length() - 1)); };

CHR_DOT:
  '.';

NEWLINE:
  '\r'? '\n';

WS:
  [ \t]+ -> channel(HIDDEN);
