create or replace package plog is

  procedure info(s varchar2, t varchar2);
  procedure debug(s varchar2, t varchar2);
  procedure warn(s varchar2, t varchar2);
  procedure error(s varchar2, t varchar2);

  level_debug constant number := 1;
  level_info  constant number := 2;
  level_warn  constant number := 3;
  level_error constant number := 4;

  log_level integer := level_warn;

end plog;
/
create or replace package body plog is

  /* this is the only real difference,
  *  in the whole file
  *  comments are ignored   */
  a VARCHAR2(200) = 'lower';

  procedure log_auto(l integer, s varchar2, t varchar2) is
    pragma autonomous_transaction;
    s0 varchar2(2000) := substr(s, 1, 2000);
    t0 varchar2(2000) := substr(t, 1, 2000);
  begin
    insert into tlog
      (id, date_, section, text, level_)
    values
      (slog.nextval, systimestamp, s0, t0, l);
    commit;
  end;

  procedure log(l integer, s varchar2, t varchar2) is
  begin
    if l >= log_level then
      log_auto(l, s, t);
    end if;
  end;

  procedure debug(s varchar2, t varchar2) is
  begin
    log(level_debug, s, t);
  end;
 procedure info(s varchar2, t varchar2) is
  begin
    log(level_info, s, t);
  end;
 procedure warn(s varchar2, t varchar2) is
  begin
    log(level_warn, s, t);
  end;
 procedure error(s varchar2, t varchar2) is
  begin
    log(level_error, s, t);
  end;

end plog;
/
