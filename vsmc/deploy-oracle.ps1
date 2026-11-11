. "$PSScriptRoot/configs.ps1"

$appConfig = $Global:AppConfig
foreach ($unitKey in $appConfig.unit.units.Keys) {
    $UnitConfig = $appConfig.unit.units[$unitKey]

    if ($UnitConfig.status -eq "offline") {
        Write-Host "$unitKey is OFFLINE" -ForegroundColor DarkYellow
        continue
    }

    $userMap = $UnitConfig.users
    if ($userMap -and -not $userMap.ContainsKey("dbsys")) {
        continue
    }

    Write-Host "Starting Oracle database deployment and configuration pipeline..." -ForegroundColor Cyan

    # -------------------------------------------------------------------------
    # STEP 1: Process and Execute SQL Scripts
    # -------------------------------------------------------------------------
    $dbSysUser = $UnitConfig.users["dbsys"]
    $dbUsers = $dbSysUser.database['db-users']
    $targetUserIp = "$($dbSysUser.username)@$($UnitConfig.ip)"
    foreach ($dbKey in $dbUsers.Keys) {
        $dbUsername = $dbUsers[$dbKey].u
        $dbPassword = $dbUsers[$dbKey].p
        $tSqlPath = Join-Path $(Join-Path $PSScriptRoot $dbSysUser.localdir) "template/template_$($dbUsername).sql"
        $localSqlPath = Join-Path $(Join-Path $PSScriptRoot $dbSysUser.localdir) $dbUsers[$dbKey].script
        $localDumpPath = Join-Path $(Join-Path $PSScriptRoot $dbSysUser.localdir) $dbUsers[$dbKey].dump
        if (-not (Test-Path -Path $tSqlPath)) {
            Write-Error "Local SQL script not found: $tSqlPath"
            continue
        }

        Write-Host "Processing placeholders in SQL script: $dbUsername.sql" -ForegroundColor Yellow
        $sqlContent = Get-Content -Path $tSqlPath -Raw
        # Replace @@CONFIG_KEY@@ patterns using values from $global:AppConfig
        $sqlContent = $sqlContent -replace '@@USR@@', $dbUsers[$dbKey].u
        $sqlContent = $sqlContent -replace '@@PSW@@', $dbUsers[$dbKey].p

        # Write to file without newline=CRLF
        [System.IO.File]::WriteAllText($localSqlPath, $sqlContent, [System.Text.Encoding]::UTF8)

        # Upload processed script to remote workdir
        $remoteSqlDestination = "$($dbSysUser.workdir)/$($dbUsers[$dbKey].script)"
        $scpArgs = $sshBase + @($localSqlPath, "$($targetUserIp):$remoteSqlDestination")
        & scp @scpArgs

        Write-Host "Executing $dbUsername.sql via sqlplus on remote." -ForegroundColor Cyan
        $dbConnectString = $dbSysUser.database['connection-string']
        
        # Execute sqlplus and capture exit status/logs
        # Using WHENEVER SQLERROR EXIT SQL.SQLCODE to ensure error trapping
        $remoteSqlCmd = @"
WHENEVER SQLERROR EXIT SQL.SQLCODE FAILURE
WHENEVER OSERROR EXIT 99 FAILURE
@$remoteSqlDestination
EXIT;
"@

        $output = $remoteSqlCmd -replace "`r`n", "`n" | ssh @sshBase $targetUserIp "tr -d '\r' | sqlplus -s $dbConnectString"

        if ($LASTEXITCODE -ne 0) {
            Write-Error "SQL script execution failed for $($dbUsers[$dbKey].u). Output:`n$output"
            return $false
        } else {
            Write-Host "Successfully executed: $($dbUsers[$dbKey].u)" -ForegroundColor Green
        }

        # -------------------------------------------------------------------------
        # STEP 2: Import Data using Oracle 'imp'
        # -------------------------------------------------------------------------
        Write-Host "Import data with dump files." -ForegroundColor Cyan

        # Upload processed dump to remote workdir
        $remoteDumpDestination = "$($dbSysUser.workdir)/$($dbUsers[$dbKey].dump)"
        $scpArgs = $sshBase + @($localDumpPath, "$($targetUserIp):$remoteDumpDestination")
        & scp @scpArgs

        $databaseSid = $dbSysUser.database.sid
        $dbFromUser = $dbUsers[$dbKey].fromuser
        $impCommand = "imp $dbUsername/$dbPassword@$databaseSid file=$remoteDumpDestination fromuser=$dbFromUser touser=$dbUsername ignore=y"
        
        $impArgs = $sshBase + @($targetUserIp, $impCommand)
        $impOutput = & ssh @impArgs

        if ($LASTEXITCODE -ne 0) {
            Write-Error "Data import (imp) failed. Output:`n$impOutput"
            return $false
        }
        Write-Host "Data import completed successfully." -ForegroundColor Green
    }

    # -------------------------------------------------------------------------
    # STEP 3: Create Database Links (DB_LINK)
    # -------------------------------------------------------------------------
    Write-Host "Creating Database Links..." -ForegroundColor Cyan

    $coreConnectionString = "$($dbUsers.core.u)/$($dbUsers.core.p)@$($UnitConfig.ip):1521/$($dbSysUser.database.sid)"
    $coreToHistScript = @"
sqlplus -s "$coreConnectionString" <<EOF
WHENEVER SQLERROR EXIT SQL.SQLCODE FAILURE
WHENEVER OSERROR EXIT 99 FAILURE

DECLARE
  v_count NUMBER;
BEGIN
  SELECT COUNT(*) INTO v_count FROM user_db_links WHERE db_link = 'XMCS_HIST_LINK';
  IF v_count > 0 THEN
    EXECUTE IMMEDIATE 'DROP DATABASE LINK XMCS_HIST_LINK';
  END IF;
END;
/

CREATE DATABASE LINK XMCS_HIST_LINK 
CONNECT TO $($dbUsers.hist.u) IDENTIFIED BY $($dbUsers.hist.p) 
USING '$($UnitConfig.ip):1521/$($dbSysUser.database.sid)';
EXIT;
EOF
"@
    $histConnectionString = "$($dbUsers.hist.u)/$($dbUsers.hist.p)@$($UnitConfig.ip):1521/$($dbSysUser.database.sid)"
    $histToCoreScript = @"
sqlplus -s "$histConnectionString" <<EOF
WHENEVER SQLERROR EXIT SQL.SQLCODE FAILURE
WHENEVER OSERROR EXIT 99 FAILURE

DECLARE
  v_count NUMBER;
BEGIN
  SELECT COUNT(*) INTO v_count FROM user_db_links WHERE db_link = 'XMCS_LINK';
  IF v_count > 0 THEN
    EXECUTE IMMEDIATE 'DROP DATABASE LINK XMCS_LINK';
  END IF;
END;
/

CREATE DATABASE LINK XMCS_LINK 
CONNECT TO $($dbUsers.core.u) IDENTIFIED BY $($dbUsers.core.p) 
USING '$($UnitConfig.ip):1521/$($dbSysUser.database.sid)';
EXIT;
EOF
"@

    # CORE
    $linkArgs = $sshBase + @($targetUserIp, $($coreToHistScript -replace "`r`n", "`n"))
    & ssh @linkArgs

    # HIST
    $linkArgs = $sshBase + @($targetUserIp, $($histToCoreScript -replace "`r`n", "`n"))
    & ssh @linkArgs

    if ($LASTEXITCODE -eq 0) {
        Write-Host "Database links established successfully." -ForegroundColor Green
    } else {
        Write-Warning "Database link creation encountered issues or already exists."
    }

    # -------------------------------------------------------------------------
    # STEP 4: Run Specific Stored Procedure
    # -------------------------------------------------------------------------
    Write-Host "Executing target stored procedure..." -ForegroundColor Cyan

    $procScript = @"
sqlplus -s "$histConnectionString" <<EOF
WHENEVER SQLERROR EXIT SQL.SQLCODE FAILURE
WHENEVER OSERROR EXIT 99 FAILURE

SET SERVEROUTPUT ON;

DECLARE
  W_OLD_PARTITION_FORCE_MAKE VARCHAR2(200);
  W_FORCE_EDITION VARCHAR2(200);
BEGIN
  W_OLD_PARTITION_FORCE_MAKE := NULL;
  W_FORCE_EDITION := NULL;

  SP_DEL_INS_PART(
    W_OLD_PARTITION_FORCE_MAKE => W_OLD_PARTITION_FORCE_MAKE,
    W_FORCE_EDITION => W_FORCE_EDITION
  );

  COMMIT;
END;
/

EXIT;
EOF
"@

    $procArgs = $sshBase + @($targetUserIp, $($procScript -replace "`r`n", "`n"))
    & ssh @procArgs

    if ($LASTEXITCODE -eq 0) {
        Write-Host "Stored procedure executed successfully." -ForegroundColor Green
    } else {
        Write-Error "Stored procedure execution failed."
        return $false
    }

    Write-Host "Oracle database deployment pipeline completed successfully!" -ForegroundColor Green
    return $true
}
