function Invoke-OracleDeployment {
    [CmdletBinding()]
    param(
        [Parameter(Mandatory = $true)]
        [object]$UnitObj,

        [Parameter(Mandatory = $true)]
        [string]$RemoteHost,

        [Parameter(Mandatory = $true)]
        [string]$RemoteUser,

        [Parameter(Mandatory = $true)]
        [string[]]$LocalSqlScripts,

        [Parameter(Mandatory = $true)]
        [string]$RemoteWorkDir,

        [Parameter(Mandatory = $true)]
        [string]$RemoteDmpFile,

        [Parameter(Mandatory = $true)]
        [string]$DbConnectString, # e.g., "sys/password@ORCL as sysdba" or user credentials

        [Parameter(Mandatory = $false)]
        [array]$SshBaseOptions
    )

    Write-Host "Starting Oracle database deployment and configuration pipeline..." -ForegroundColor Cyan

    # -------------------------------------------------------------------------
    # STEP 1: Process and Execute SQL Scripts
    # -------------------------------------------------------------------------
    foreach ($localSqlPath in $LocalSqlScripts) {
        if (-not (Test-Path -Path $localSqlPath)) {
            Write-Error "Local SQL script not found: $localSqlPath"
            continue
        }

        $scriptName = Split-Path -Leaf $localSqlPath
        $remoteSqlDestination = "$RemoteWorkDir/$scriptName"

        Write-Host "Processing placeholders in SQL script: $scriptName" -ForegroundColor Yellow
        $sqlContent = Get-Content -Path $localSqlPath -Raw

        # Replace @@CONFIG_KEY@@ patterns using values from $UnitObj or $global:AppConfig
        # Example token replacement: @@DB_USER@@ -> $UnitObj.users['runner'].username
        $sqlContent = $sqlContent -replace '@@RUNNER_USER@@', $UnitObj.users['runner'].username
        $sqlContent = $sqlContent -replace '@@RUNNER_PW@@', $UnitObj.users['runner'].password
        $sqlContent = $sqlContent -replace '@@WORKDIR@@', $UnitObj.workdir

        # Temporarily save processed script locally before pushing, or stream it
        $tempProcessedPath = [System.IO.Path]::GetTempFileName()
        Set-Content -Path $tempProcessedPath -Value $sqlContent -Encoding UTF8

        # Upload processed script to remote workdir
        $scpArgs = $SshBaseOptions + @($tempProcessedPath, "$RemoteUser@${RemoteHost}:$remoteSqlDestination")
        & scp @scpArgs
        Remove-Item -Path $tempProcessedPath -Force

        Write-Host "Executing $scriptName via sqlplus on remote host..." -ForegroundColor Cyan
        
        # Execute sqlplus and capture exit status/logs
        # Using WHENEVER SQLERROR EXIT SQL.SQLCODE to ensure error trapping
        $remoteSqlCmd = @"
cd "$RemoteWorkDir"
sqlplus -s "$DbConnectString" <<EOF
WHENEVER SQLERROR EXIT SQL.SQLCODE FAILURE
WHENEVER OSERROR EXIT 99 FAILURE
@$scriptName
EXIT;
EOF
"@

        $sshArgs = $SshBaseOptions + @("$RemoteUser@$RemoteHost", $remoteSqlCmd)
        $output = & ssh @sshArgs

        if ($LASTEXITCODE -ne 0) {
            Write-Error "SQL script execution failed for $scriptName. Output:`n$output"
            return $false
        } else {
            Write-Host "Successfully executed: $scriptName" -ForegroundColor Green
        }
    }

    # -------------------------------------------------------------------------
    # STEP 2: Import Data using Oracle 'imp'
    # -------------------------------------------------------------------------
    Write-Host "Executing Oracle data import (imp)..." -ForegroundColor Cyan
    
    # Example: imp core/password@core file=/path/to/file.dmp touser=core ignore=y
    $dbUser = $UnitObj.users['dbsys'].username
    $dbPass = $UnitObj.users['dbsys'].password
    
    $impCommand = "imp $dbUser/$dbPass file=$RemoteDmpFile touser=$dbUser ignore=y"
    
    $impArgs = $SshBaseOptions + @("$RemoteUser@$RemoteHost", $impCommand)
    $impOutput = & ssh @impArgs

    if ($LASTEXITCODE -ne 0) {
        Write-Error "Data import (imp) failed. Output:`n$impOutput"
        return $false
    }
    Write-Host "Data import completed successfully." -ForegroundColor Green

    # -------------------------------------------------------------------------
    # STEP 3: Create Database Links (DB_LINK)
    # -------------------------------------------------------------------------
    Write-Host "Creating Database Links..." -ForegroundColor Cyan

    $dbLinkScript = @"
sqlplus -s "$DbConnectString" <<EOF
WHENEVER SQLERROR EXIT SQL.SQLCODE FAILURE
-- Example DB Link creation statement using config values
CREATE DATABASE LINK remote_link 
CONNECT TO target_user IDENTIFIED BY target_password 
USING 'REMOTE_TNS_ALIAS';
EXIT;
EOF
"@

    $linkArgs = $SshBaseOptions + @("$RemoteUser@$RemoteHost", $dbLinkScript)
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
sqlplus -s "$DbConnectString" <<EOF
WHENEVER SQLERROR EXIT SQL.SQLCODE FAILURE
SET SERVEROUTPUT ON;
EXEC pkg_system_init.initialize_environment;
COMMIT;
EXIT;
EOF
"@

    $procArgs = $SshBaseOptions + @("$RemoteUser@$RemoteHost", $procScript)
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
