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
grammar ProfilerGrammar;

profiler:
  description module_data call_tree_data line_summary tracing_data coverage_data user_data;

description:
  NUMBER date=DATE desc=STRING time=TIME author=STRING NEWLINE CHR_DOT NEWLINE;

module_data:
  module_data_line* CHR_DOT NEWLINE;

module_data_line:
  id=NUMBER name=STRING debugListingFile=STRING crc=NUMBER NEWLINE;

call_tree_data:
  call_tree_data_line* CHR_DOT NEWLINE;

call_tree_data_line:
  callerId=NUMBER callerLineNum=NUMBER calleeId=NUMBER callCount=NUMBER NEWLINE;

line_summary:
  line_summary_line* CHR_DOT NEWLINE;

line_summary_line:
  moduleId=NUMBER lineNumber=NUMBER execCount=NUMBER actualTime=FLOAT cumulativeTime=FLOAT NEWLINE;

tracing_data:
  tracing_data_line* CHR_DOT NEWLINE;

tracing_data_line:
  moduleId=NUMBER lineNumber=NUMBER execTime=FLOAT timestamp=FLOAT NEWLINE;

coverage_data:
  coverage_section* CHR_DOT NEWLINE;

coverage_section:
  moduleId=NUMBER name=STRING lineCount=NUMBER NEWLINE coverage_section_line+ CHR_DOT NEWLINE;

/*coverage_section_lines:
  coverage_section_line+;*/
  
coverage_section_line:
  linenum=NUMBER NEWLINE;

user_data:
  user_data_line* CHR_DOT NEWLINE;

user_data_line:
  TIME STRING NEWLINE;

fragment INT:
  ('0'..'9');

NUMBER:
  INT+;

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
