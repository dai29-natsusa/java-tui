# Features

## Restrictions

- Everything **MUST** be and working under encoding = `UTF-8`
- Java 8

## Actor

1. Deployer - the main user of the application
1. User - Linux's users, have their own role and goal
    1. Executor User - main user for running the Application's services
    1. DB User - sysadmin user for execute Oracle DB operations
    1. Root - root user who could perform Admin Privilege commands
1. Unit - the remote server that are target of deployment, multiple installation to make Services to approach HA (High Availability)

## Procedures / Steps

### xmcs_server instance -> Both Unit 01 & 02

1. Can pre-defined users - VSMC case has 3 users: `xmcs`:system runner, `oracle`: db sysadmin, `root`: root
    - Predefined users **REQUIRED**
1. Can validate existence of the users above
    - Error or Exit when validation failed
1. Can create SSH Keys and inject into $user remote
1. Can upload a {xmcs}.zip file into $user remote via SCP-like protocol with pure Java with generated keys from above
    - Or thinking like Deployer put the final/production `.zip` file -> extract it and upload the extracted into target
    - Optional: might require password in some cases
1. Can auto-modify files into Linux based => all text file's newline=`\r`, `.sh` script file has `u+x` permission
1. Can modified `.bash_profile` profile of Main User (default: `xmcs`) with Environment Variable (e.g. `XMCS_HOME`) and list of `alias`
1. With DB user - Can operate DB command for server setup/initialize database, tables, init data (default db=oracle)
    1. Can run/execute **user's** `.sql` files, this is for create/init DB's users
    1. Can confirm success of `.sql` execution above
    1. Can unload `.dmp` files into database
    1. Can confirm success of load `.dmp` files
    1. Can create DB Link
    1. Can run/execute 'Procedure' on specific name 'SP_DEL_INS_PART' with specific PARAM['W_FORCE_EDITION'] = 'SE' and need to confirm that execution result is OK
1. With Executor User - can do Deployment and execute many functions/scripts to setup the server to run target application (xMCS - server/emulator/monitor)
    1. Can check and confirm installation of JDK8 -> error or exit when confirmation failed
    1. Can edit or auto-edit the specific properties in `jdbc.properties` file -> `xmcs.db.hostname`, `xmcs.db.user`, `xmcs.db.passwd`
    1. Can edit or auto-edit the specific properties in `jdbc-history.properties` file -> `xmcs.db.hostname`, `xmcs.db.user`, `xmcs.db.passwd` (same properties' name with above but difference DB's user that intiated from when executed DB users init)
    1. Can edit or auto-edit the specific properties in `xmcs-nodes.properties` file -> `xmcs.node.ip.1={unit01_ip}:5701`, `xmcs.node.ip.2={unit02_ip}:5701`, `xmcs.node.port.auto.increment=false`
    1. Can edit or auto-edit the specific shell script in `xmcscommon.sh` file -> `DEF_BIND_ADDR={unitxx_alias_or_hostname}`, `JG_BIND_ADDR={unitxx_alias_or_hostname}` (**xx** is depends on unit id)
    1. Can edit or auto-edit the specific shell script in `amsh.sh` file -> `HSMS_LOCAL_BIND_ADDR={unit01_ip}`
    1. Can edit or auto-edit the specific shell script in `client.sh` file -> `BIND_ADDR={unit01_ip}`
    1. Can edit or auto-edit the specific shell script in `core.sh` file -> `JAVA_OPTS=${JAVA_OPTS} -Dxmcs.core.hazelcast.type=client -Dxmcs.event.log.union=true`
    1. Can edit or auto-edit the specific shell script in `check-if.sh` file -> `${XMCS_MONITOR}/scripts/xmcs-monitor.sh status`
    1. **ONLY Unit02** Can edit or auto-edit the specific shell script in `amsh.sh`, `client-if.sh`, `core.sh`, `cache.sh`, `host-if.sh`, `layout-man.sh` for set `MODULE_ID={MODULE}_2` (`MODULE` is depends on shell files name like `client-if.sh`=>`MODULE='CLIENT-IF'`)
    1. Can edit or auto-edit the specific shell script in `amsh.sh`, `client-if.sh`, `core.sh`, `cache.sh`, `host-if.sh`, `layout-man.sh` -> `MEM_MIN` and `MEM_MAX` as following table
        
        | Module  | Min  | Max   |
        |---------|------|-------|
        | CACHE   | 512M | 2048M |
        | CORE    | 512M | 1024M |
        | CLIENT  | 512M | 2048M |
        | AMSH    | 128M | 256M  |
        | HOST    | 128M | 256M  |
        | MONITOR | 512M | 1024M |
        | LAYOUT  | 256M | 256M  |
        
    1. Can start each shell script file -> validate each result -> can raise the **CAUSE** of exception
        1. Information specified in each script are correct
        1. database user/pass correctly defined in `jdbc.properties` and `jdbc-history.properties` files
        1. Jar files in `lib` directory has correctly filename and matched the parameters `DEP_JARS`
        1. Confirm with output file log name like `{MODULE}.out` file is come out matched to those scripts
    1. Can run/execute scripts `startAll.sh`, `check-if.sh`, `stopAll.sh`
1. Can use `root` user to register service
    1. 
1. **Optional** App can read `.env` file when running and tread variables inside as Environment Variable, or App can register like `export VAR_NAME={value}` with reading `.env` file and do export each variables
