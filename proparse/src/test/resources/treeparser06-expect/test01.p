PROGRAM_ROOT F0/0:0 -- Block -- Scope
  PROCEDURE F0/1:0 -- Block -- Scope
    IF F0/2:2
      THEN F0/2:10
        DO F0/2:15 -- Block
          IF F0/3:4
            THEN F0/3:32
              DO F0/3:37 -- Block
                MESSAGE F0/4:6
            ELSE F0/6:4
              MESSAGE F0/6:9
    MESSAGE F0/8:2
  DEFINE F0/11:0
  CASE F0/12:2
    WHEN F0/13:4
      THEN F0/13:11
        RETURN F0/13:16
    WHEN F0/14:4
      THEN F0/14:11
        RETURN F0/14:16
    WHEN F0/15:4
      THEN F0/15:11
        DO F0/15:16 -- Block
          MESSAGE F0/16:6
          RETURN F0/17:6
    OTHERWISE F0/19:4
      RETURN F0/19:14
  IF F0/22:0
    THEN F0/22:8
      CASE F0/23:2
        WHEN F0/24:4
          THEN F0/24:11
            RETURN F0/24:16
        WHEN F0/25:4
          THEN F0/25:11
            RETURN F0/25:16
        OTHERWISE F0/26:4
          RETURN F0/26:14
    ELSE F0/28:0
      CASE F0/29:2
        WHEN F0/30:4
          THEN F0/30:11
            RETURN F0/30:16
        WHEN F0/31:4
          THEN F0/31:11
            RETURN F0/31:16
        OTHERWISE F0/32:4
          RETURN F0/32:14

