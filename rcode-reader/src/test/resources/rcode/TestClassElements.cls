CLASS rcode.TestClassElements:
  DEFINE PROTECTED TEMP-TABLE tt1
    FIELD fld1 AS INT
    FIELD fld2 AS CHAR
    INDEX idx1 IS PRIMARY UNIQUE fld1.
  DEFINE PROTECTED TEMP-TABLE tt2
    FIELD fld3 AS INT
    FIELD fld4 AS CHAR
    INDEX idx2 IS PRIMARY UNIQUE fld3.

  DEFINE PROTECTED DATASET ds1 FOR tt1, tt2.

  CONSTRUCTOR PUBLIC TestClassElements():
  
  END CONSTRUCTOR.

  DESTRUCTOR PUBLIC TestClassElements():
  
  END DESTRUCTOR.

  METHOD PUBLIC INT EXTENT testMethod(TABLE tt1, OUTPUT DATASET ds1, xx AS INT EXTENT 3):
  
  END METHOD.

  METHOD PUBLIC INT EXTENT testMethod2(xx AS INT):
  
  END METHOD.
  METHOD PUBLIC INT testMethod2(xx AS INT EXTENT):
  
  END METHOD.

  METHOD PUBLIC INT EXTENT testMethod3(TABLE-HANDLE htt1, INPUT-OUTPUT DATASET-HANDLE hds1):

  END METHOD.

  METHOD PUBLIC INT testMethod4(INPUT x1 AS CHAR,
                   INPUT-OUTPUT x2 AS Progress.Lang.Object,
                   OUTPUT x3 AS DATE,
                   INPUT x4 AS DATETIME,
                   INPUT x5 AS DATETIME-TZ,
                   OUTPUT x6 AS DECIMAL,
                   INPUT x7 AS HANDLE,
                   INPUT x8 AS INT64,
                   INPUT x9 AS LOGICAL,
                   INPUT x10 AS LONGCHAR,
                   OUTPUT x11 AS MEMPTR,
                   INPUT x12 AS RAW,
                   INPUT x13 AS RECID,
                   INPUT x14 AS ROWID):

  END METHOD.

  METHOD PUBLIC Progress.Lang.Object testMethod5(INPUT x1 AS Progress.Lang.Enum):

  END METHOD.

END CLASS.
