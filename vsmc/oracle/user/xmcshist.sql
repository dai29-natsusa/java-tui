--for xmcshist

/*
 * ・各種変数は以下SQLで定義されています。（事前実行要）
 * @../../define.sql
 */

/* Create xmcshist profile */
CREATE PROFILE XMCSHIST_PROFILE LIMIT
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
FAILED_LOGIN_ATTEMPTS UNLIMITED
/


/* Create xmcshist user */
CREATE USER @@HIST_USER@@
IDENTIFIED BY @@HIST_PW@@
DEFAULT TABLESPACE HISTORY_DATA
TEMPORARY TABLESPACE TEMP
PROFILE XMCSHIST_PROFILE
/
GRANT connect,resource TO @@HIST_USER@@ WITH ADMIN OPTION
/
GRANT DBA TO @@HIST_USER@@ WITH ADMIN OPTION
/
GRANT SELECT ANY DICTIONARY TO @@HIST_USER@@ WITH ADMIN OPTION
/
GRANT UNLIMITED TABLESPACE TO @@HIST_USER@@ WITH ADMIN OPTION
/
GRANT CREATE ANY TABLE TO @@HIST_USER@@ WITH ADMIN OPTION
/
GRANT CREATE ANY INDEX TO @@HIST_USER@@ WITH ADMIN OPTION
/
GRANT ALTER ANY TABLE TO @@HIST_USER@@ WITH ADMIN OPTION
/
GRANT ALTER ANY INDEX TO @@HIST_USER@@ WITH ADMIN OPTION
/
GRANT DROP ANY TABLE TO @@HIST_USER@@ WITH ADMIN OPTION
/
GRANT DROP ANY INDEX TO @@HIST_USER@@ WITH ADMIN OPTION
/
GRANT CREATE ANY PROCEDURE TO @@HIST_USER@@ WITH ADMIN OPTION
/
GRANT CREATE ANY SYNONYM TO @@HIST_USER@@ WITH ADMIN OPTION
/
GRANT CREATE ANY VIEW TO @@HIST_USER@@ WITH ADMIN OPTION
/
GRANT CREATE EVALUATION CONTEXT TO @@HIST_USER@@ WITH ADMIN OPTION
/
GRANT CREATE RULE TO @@HIST_USER@@ WITH ADMIN OPTION
/
GRANT ALTER SYSTEM TO @@HIST_USER@@ WITH ADMIN OPTION
/
GRANT EXECUTE ON DBMS_LOCK TO @@HIST_USER@@
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

