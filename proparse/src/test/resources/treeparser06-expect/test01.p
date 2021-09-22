PROGRAM_ROOT F0/0:0 -- Block -- Scope
  PROCEDURE F0/1:1 -- Block -- Scope
    IF F0/2:3
      THEN F0/2:11
        DO F0/2:16 -- Block
          IF F0/3:5
            THEN F0/3:33
              DO F0/3:38 -- Block
                MESSAGE F0/4:7
            ELSE F0/6:5
              MESSAGE F0/6:10
    MESSAGE F0/8:3
  DEFINE F0/11:1
  CASE F0/12:3
    WHEN F0/13:5
      THEN F0/13:12
        RETURN F0/13:17
    WHEN F0/14:5
      THEN F0/14:12
        RETURN F0/14:17
    WHEN F0/15:5
      THEN F0/15:12
        DO F0/15:17 -- Block
          MESSAGE F0/16:7
          RETURN F0/17:7
    OTHERWISE F0/19:5
      RETURN F0/19:15
  IF F0/22:1
    THEN F0/22:9
      CASE F0/23:3
        WHEN F0/24:5
          THEN F0/24:12
            RETURN F0/24:17
        WHEN F0/25:5
          THEN F0/25:12
            RETURN F0/25:17
        OTHERWISE F0/26:5
          RETURN F0/26:15
    ELSE F0/28:1
      CASE F0/29:3
        WHEN F0/30:5
          THEN F0/30:12
            RETURN F0/30:17
        WHEN F0/31:5
          THEN F0/31:12
            RETURN F0/31:17
        OTHERWISE F0/32:5
          RETURN F0/32:15

