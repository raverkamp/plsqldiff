CREATE OR REPLACE PACKAGE BODY csv IS

  g_p INTEGER;

  --  k_past_end    integer :=4;

  k_clob    INTEGER := 1;
  k_varchar INTEGER := 2;

  g_what INTEGER;

  g_clob CLOB;

  g_quote_char VARCHAR2(1);
  g_delim_char VARCHAR2(1);

  g_varchar2 VARCHAR2(32000);

  PROCEDURE p(x VARCHAR2) IS
  BEGIN
    NULL;
    dbms_output.put_line(x);
  END;

  FUNCTION instr2(y VARCHAR2
                 ,p INTEGER) RETURN INTEGER IS
  BEGIN
    IF g_what = k_clob
    THEN
      RETURN dbms_lob.instr(lob_loc => g_clob
                           ,pattern => y
                           ,offset  => p);
    ELSIF g_what = k_varchar
    THEN
      RETURN instr(g_varchar2
                  ,y
                  ,p);
    END IF;
  END;

  FUNCTION substrclob(i_from INTEGER
                     ,i_to1  INTEGER) RETURN VARCHAR2 IS
  BEGIN
    IF i_from + 4000 >= i_to1
    THEN
      RETURN dbms_lob.substr(lob_loc => g_clob
                            ,amount  => i_to1 - i_from
                            ,offset  => i_from);
    ELSE
      DECLARE
        a INTEGER := trunc((i_to1 + i_from) / 2);
      BEGIN
        RETURN substrclob(i_from
                         ,a) || substrclob(a
                                          ,i_to1);
      END;
    END IF;
  END;

  FUNCTION substr2(i_from INTEGER
                  ,i_to1  INTEGER) RETURN varchar2x IS
  BEGIN
    IF g_what = k_clob
    THEN
      RETURN substrclob(i_from
                       ,i_to1);
    ELSIF g_what = k_varchar
    THEN
      RETURN substr(g_varchar2
                   ,i_from
                   ,i_to1 - i_from);
    END IF;
  END;

  FUNCTION len2 RETURN INTEGER IS
  BEGIN
    IF g_what = k_clob
    THEN
      RETURN nvl(dbms_lob.getlength(g_clob)
                ,0);
    ELSIF g_what = k_varchar
    THEN
      RETURN nvl(length(g_varchar2)
                ,0);
    END IF;
  END;

  PROCEDURE clob_init(i_clob  IN CLOB
                     ,i_delim VARCHAR2
                     ,i_quote VARCHAR2) IS
  BEGIN
    g_what := k_clob;
    g_clob := i_clob;
    g_p    := 1;
    set_chars(i_delim
             ,i_quote);
  END;

  PROCEDURE varchar_init(i_varchar2 IN VARCHAR2
                        ,i_delim    VARCHAR2
                        ,i_quote    VARCHAR2) IS
  BEGIN
    g_what     := k_varchar;
    g_p        := 1;
    g_varchar2 := i_varchar2;
    set_chars(i_delim
             ,i_quote);
  END;

  PROCEDURE set_chars(i_delim VARCHAR2
                     ,i_quote VARCHAR2) IS
  BEGIN
    g_delim_char := i_delim;
    g_quote_char := i_quote;
  END;

  PROCEDURE reset(p INTEGER) IS
  BEGIN
    g_p := p;
    --   if p + g_p - 1 > nvl(length(g_buff), 0) + 1 then
    --     raise_application_error(-20001, 'bug');
    --    end if;
    --   g_buff := substr(g_buff, p + g_p - 1); --, nvl(length(g_buff), 0) - p + 1);
    --   g_p    := 1;
  END;

  FUNCTION past_end(x INTEGER) RETURN BOOLEAN IS
  BEGIN
    RETURN x > len2;
  END;

  PROCEDURE skip_to_end(i_pos   INTEGER
                       ,o_state OUT VARCHAR2) IS
    -- i_pos is one after end of quotet field
    -- skip to pos after end of line
  
    posd  INTEGER;
    posnl INTEGER;
  BEGIN
    posd  := instr2(g_delim_char
                   ,i_pos);
    posnl := instr2(chr(10)
                   ,i_pos);
    IF posd > 0 OR posnl > 0
    THEN
      -- an end was found !
      IF (posd > 0 AND (posnl = 0 OR posnl > posd))
      THEN
        -- separator found
        reset(posd + 1);
        o_state := k_found_delim;
        RETURN;
      ELSE
        -- end of line found
        reset(posnl + 1);
        o_state := k_found_nl;
        RETURN;
      END IF;
    ELSE
      o_state := k_found_eof;
      reset(len2 + 1);
    END IF;
  END;

  PROCEDURE next_quoted_field(o_field OUT VARCHAR2
                             ,o_state OUT INTEGER) IS
    x     INTEGER;
    q_pos INTEGER;
  BEGIN
    p('q ' || g_p);
    -- first char is quote char => v is not null!
    x := g_p + 1;
    LOOP
      --  if past_end(x) then
      --    -- end of file
      --    -- quoted field is not closed  ... play nice
      --     o_field := replace(substr2(2, len2 + 1),
      --                        g_quote_char || g_quote_char,
      --                        g_quote_char);
      --    o_state := k_found_nl;
      --     reset(x); -- := length(v)+1;
      --     return;
      --   end if;
      q_pos := instr2(g_quote_char
                     ,x);
      IF q_pos = 0
      THEN
        -- not closed 
        o_field := REPLACE(substr(g_p + 1
                                 ,len2 + 1)
                          ,g_quote_char || g_quote_char
                          ,g_quote_char);
        o_state := k_found_eof;
        RETURN;
      END IF;
      -- found quote char
      -- we need the next char ...
      IF past_end(q_pos + 1)
      THEN
        -- no next char
        o_field := REPLACE(substr2(g_p + 1
                                  ,q_pos)
                          ,g_quote_char || g_quote_char
                          ,g_quote_char);
        o_state := k_found_eof;
        reset(q_pos + 1);
        RETURN;
      END IF;
      IF substr2(q_pos + 1
                ,q_pos + 2) != g_quote_char
      THEN
        -- at the end of field
        p('---' || q_pos);
        o_field := REPLACE(substr2(g_p + 1
                                  ,q_pos)
                          ,g_quote_char || g_quote_char
                          ,g_quote_char);
        skip_to_end(q_pos + 1
                   ,o_state);
        RETURN;
      ELSE
        x := q_pos + 2;
      END IF;
    END LOOP;
  END;

  -- buffer is string
  -- start position is integer
  -- on next field both is set
  -- at end if 
  -- g_p> length(buffer)
  -- g_buff and g_p can change with calls to getchars
  -- getchars always works, returns if chars where found
  -- reset set
  PROCEDURE next_field(o_field OUT VARCHAR2
                      ,o_state OUT INTEGER) IS
    res   VARCHAR2(32000);
    posd  INTEGER;
    posnl INTEGER;
    --  posq integer;
  BEGIN
    p('NF:' || g_p);
    IF past_end(g_p)
    THEN
      o_field := NULL;
      o_state := k_found_eof;
      RETURN;
    END IF;
  
    -- if the first char is quote it is a quotet field,
    -- may be less conditions
    IF substr2(g_px
              ,g_p + 1) = g_quote_char
    THEN
      next_quoted_field(o_field
                       ,o_state);
    
      --sasa
      --assa
      --assassa
    
      RETURN;
    END IF;
    posd  := instr2(g_delim_char
                   ,g_p);
    posnl := instr2(chr(10)
                   ,g_p);
    p('pos: ' || posd || ' ' || posnl);
    IF posd = 0 AND posnl = 0
    THEN
    END IF;
    IF posd != 0 AND (posd < posnl OR posnl = 0)
    THEN
      -- found sep and later no or later NL
      res     := substr2(g_p
                        ,posd);
      o_field := res;
      o_state := k_found_delim;
      reset(posd + 1);
      RETURN;
    ELSE
      res     := substr2(g_p
                        ,posnl);
      o_field := res;
      reset(posnl + 1);
      o_state := xyz k_found_nl;
      RETURN;
    END IF;
  END;

END csv;
/
