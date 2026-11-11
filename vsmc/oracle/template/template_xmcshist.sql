--for xmcshist

/*
 * ・各種変数は以下SQLで定義されています。（事前実行要）
 * @../../define.sql
 */

/* Create xmcshist profile */
BEGIN
    EXECUTE IMMEDIATE 'CREATE PROFILE XMCSHIST_PROFILE LIMIT
    SESSIONS_PER_USER 1100
    CONNECT_TIME UNLIMITED
    IDLE_TIME UNLIMITED
    CPU_PER_SESSION UNLIMITED
    CPU_PER_CALL UNLIMITED
    LOGICAL_READS_PER_SESSION UNLIMITED
    LOGICAL_READS_PER_CALL UNLIMITED
    PRIVATE_SGA UNLIMITED
    COMPOSITE_LIMIT UNLIMITED
    PASSWORD_LIFE_TIME UNLIMITED
    FAILED_LOGIN_ATTEMPTS UNLIMITED';
EXCEPTION
    WHEN OTHERS THEN
        IF SQL_CODE = -1184 THEN
            DBMS_OUTPUT.PUT_LINE('Profile already existing');
        ELSE
            RAISE;
        END IF;
END;
/


/* Create xmcshist user */
BEGIN
    EXECUTE IMMEDIATE 'CREATE USER @@USR@@
    IDENTIFIED BY @@PSW@@
    DEFAULT TABLESPACE HISTORY_DATA
    TEMPORARY TABLESPACE TEMP
    PROFILE XMCSHIST_PROFILE';
EXCEPTION
   WHEN OTHERS THEN
      IF SQLCODE = -1920 THEN
         DBMS_OUTPUT.PUT_LINE('User already exists.');
      ELSE
         RAISE;
      END IF;
END;
/
GRANT connect,resource TO @@USR@@ WITH ADMIN OPTION
/
GRANT DBA TO @@USR@@ WITH ADMIN OPTION
/
GRANT SELECT ANY DICTIONARY TO @@USR@@ WITH ADMIN OPTION
/
GRANT UNLIMITED TABLESPACE TO @@USR@@ WITH ADMIN OPTION
/
GRANT CREATE ANY TABLE TO @@USR@@ WITH ADMIN OPTION
/
GRANT CREATE ANY INDEX TO @@USR@@ WITH ADMIN OPTION
/
GRANT ALTER ANY TABLE TO @@USR@@ WITH ADMIN OPTION
/
GRANT ALTER ANY INDEX TO @@USR@@ WITH ADMIN OPTION
/
GRANT DROP ANY TABLE TO @@USR@@ WITH ADMIN OPTION
/
GRANT DROP ANY INDEX TO @@USR@@ WITH ADMIN OPTION
/
GRANT CREATE ANY PROCEDURE TO @@USR@@ WITH ADMIN OPTION
/
GRANT CREATE ANY SYNONYM TO @@USR@@ WITH ADMIN OPTION
/
GRANT CREATE ANY VIEW TO @@USR@@ WITH ADMIN OPTION
/
GRANT CREATE EVALUATION CONTEXT TO @@USR@@ WITH ADMIN OPTION
/
GRANT CREATE RULE TO @@USR@@ WITH ADMIN OPTION
/
GRANT ALTER SYSTEM TO @@USR@@ WITH ADMIN OPTION
/
GRANT EXECUTE ON DBMS_LOCK TO @@USR@@
/

/*
下記も必要なので別途OS操作を実施しておくこと（OS User = oracle）

# cd /opt/oracle/diag/rdbms/xmcs/xmcs
# chmod 755 trace
# cd /opt/oracle/diag/rdbms/xmcshist/xmcshist
# chmod 755 trace
# cd /opt/oracle/diag/rdbms
# chmod 755 xmcs
# chmod 755 xmcshist
# chmod 755 xmcs/xmcs
# chmod 755 xmcshist/xmcshist

# cd /opt/oracle/diag/rdbms/xmcs/xmcs
# chmod 755 trace
# cd trace
# chmod 755 *

# cd /opt/oracle/diag/rdbms/xmcshist/xmcshist
# chmod 755 trace
# cd trace
# chmod 755 *

*/

--oracle profile change & check
alter profile default limit password_life_time unlimited
/
alter profile default limit failed_login_attempts unlimited
/
select resource_name,limit from dba_profiles where profile='DEFAULT'
/
select username, expiry_date from dba_users
/

