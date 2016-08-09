// Variable FOO is expanded by preprocessor, and will result in OutOfRange problem with CPD
// Expansion should be reverted for CPD.
&scoped-define FOO LongLongName
MESSAGE "{&FOO}" VIEW-AS 
  ALERT-BOX.
