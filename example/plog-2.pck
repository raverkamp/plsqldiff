CREATE OR REPLACE PACKAGE Plog IS

  PROCEDURE Info(s VARCHAR2,
                 t VARCHAR2);

  PROCEDURE Debug(s VARCHAR2,
                  t VARCHAR2);

  PROCEDURE Warn(s VARCHAR2,
                 t VARCHAR2);

  PROCEDURE Error(s VARCHAR2,
                  t VARCHAR2);

  Level_Debug CONSTANT NUMBER := 1;

  Level_Info CONSTANT NUMBER := 2;

  Level_Warn CONSTANT NUMBER := 3;

  Level_Error CONSTANT NUMBER := 4;

  Log_Level INTEGER := Level_Warn;

END Plog;
/
CREATE OR REPLACE PACKAGE BODY Plog IS

  -- this is the only difference
  a VARCHAR2(200) = 'UPPER';

  PROCEDURE Log_Auto(l INTEGER,
                     s VARCHAR2,
                     t VARCHAR2) IS
    PRAGMA AUTONOMOUS_TRANSACTION;
    S0 VARCHAR2(2000) := Substr(s
                               ,1
                               ,2000);
    T0 VARCHAR2(2000) := Substr(t
                               ,1
                               ,2000);
  BEGIN
    INSERT INTO Tlog
      (Id, Date_, Section, Text, Level_)
    VALUES
      (Slog.Nextval, Systimestamp, S0, T0, l);
    COMMIT;
  END;

  PROCEDURE Log(l INTEGER,
                s VARCHAR2,
                t VARCHAR2) IS
  BEGIN
    IF l >= Log_Level
    THEN
      Log_Auto(l
              ,s
              ,t);
    END IF;
  END;

  PROCEDURE Debug(s VARCHAR2,
                  t VARCHAR2) IS
  BEGIN
    Log(Level_Debug
       ,s
       ,t);
  END;

  PROCEDURE Info(s VARCHAR2,
                 t VARCHAR2) IS
  BEGIN
    Log(Level_Info
       ,s
       ,t);
  END;

  PROCEDURE Warn(s VARCHAR2,
                 t VARCHAR2) IS
  BEGIN
    Log(Level_Warn
       ,s
       ,t);
  END;

  PROCEDURE Error(s VARCHAR2,
                  t VARCHAR2) IS
  BEGIN
    Log(Level_Error
       ,s
       ,t);
  END;

END Plog;
/
