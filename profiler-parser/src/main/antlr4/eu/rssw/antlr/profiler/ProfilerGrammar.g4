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
