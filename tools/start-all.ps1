<#
.SYNOPSIS
    Levanta todo el stack en orden: MariaDB, Apache, login server, game server.

.DESCRIPTION
    Nada de XAMPP arranca solo en esta maquina: no hay servicios instalados, ni
    claves Run, ni entradas en la carpeta de Inicio. Los servidores tampoco.
    Despues de un reinicio habia que abrir el panel de XAMPP y los .vbs a mano.

    El orden no es decorativo. El login y el game abren su pool de HikariCP al
    arrancar, asi que sin MariaDB escuchando fallan; y el game server se registra
    contra el login, asi que sin login queda reintentando cada 5 segundos. Cada
    paso espera a que el anterior conteste antes de seguir, y corta la cadena si
    no contesta, en vez de arrancar cosas condenadas a fallar.

    Es idempotente: lo que ya este escuchando su puerto no se toca. Se puede
    correr a mano en cualquier momento para levantar lo que falte.

    Los procesos se crean con Win32_Process.Create y no con Start-Process, para
    que queden colgando de WmiPrvSE y no del arbol de quien los lanzo. Eso los
    hace sobrevivir a que termine la tarea programada, y ademas hereda la sesion
    del que llama, que es lo que mantiene las ventanas de los servidores en el
    escritorio.

.PARAMETER SkipApache
    No levanta Apache. Solo sirve para phpMyAdmin: el juego no lo necesita.

.PARAMETER DryRun
    Informa que levantaria, sin arrancar nada.
#>
param(
    [switch]$SkipApache,
    [switch]$DryRun
)

$ErrorActionPreference = 'Stop'

# --- Configuracion --------------------------------------------------------
$ServerRoot = 'C:\Users\Man_z\Desktop\servidor\build'
$LogFile    = Join-Path $ServerRoot 'autostart.log'
$MaxLogKB   = 512

# Cmd lleva la ruta completa del ejecutable a proposito: Win32_Process.Create
# NO busca el binario en CurrentDirectory, resuelve por PATH. Con solo
# "mysqld.exe" devuelve 9 (ruta no encontrada). wscript.exe andaria igual por
# estar en System32, pero se escribe completo para no depender de eso.
$Steps = @(
    @{ Name = 'MariaDB'; Port = 3306
       Cmd  = '"C:\xampp\mysql\bin\mysqld.exe" --defaults-file="C:\xampp\mysql\bin\my.ini" --standalone'
       Dir  = 'C:\xampp\mysql\bin'; Wait = 60 }
    @{ Name = 'Apache'; Port = 80
       Cmd  = '"C:\xampp\apache\bin\httpd.exe"'
       Dir  = 'C:\xampp\apache\bin'; Wait = 30; Optional = $true }
    @{ Name = 'Login server'; Port = 2106
       Cmd  = '"C:\Windows\System32\wscript.exe" LoginServer.vbs'
       Dir  = (Join-Path $ServerRoot 'login'); Wait = 60 }
    @{ Name = 'Game server'; Port = 7777
       Cmd  = '"C:\Windows\System32\wscript.exe" GameServer.vbs'
       Dir  = (Join-Path $ServerRoot 'game'); Wait = 180 }
)

# --- Utilidades -----------------------------------------------------------
function Write-Log {
    param([string]$Message, [string]$Level = 'INFO')
    $line = '{0} [{1}] {2}' -f (Get-Date -Format 'yyyy-MM-dd HH:mm:ss'), $Level, $Message
    try {
        if ((Test-Path $LogFile) -and ((Get-Item $LogFile).Length -gt ($MaxLogKB * 1KB))) {
            Move-Item $LogFile "$LogFile.old" -Force
        }
        Add-Content -Path $LogFile -Value $line -Encoding utf8
    } catch { }
    # Write-Host y no Write-Output: dentro de una funcion, Write-Output se suma
    # al valor de retorno y un array no vacio evalua como verdadero.
    Write-Host $line
}

function Test-Port {
    param([int]$Port)
    $conn = Get-NetTCPConnection -LocalPort $Port -State Listen -ErrorAction SilentlyContinue |
            Select-Object -First 1
    return [bool]$conn
}

function Start-Step {
    param([hashtable]$Step)

    if (Test-Port $Step.Port) {
        Write-Log ('{0}: ya esta escuchando en {1}, no lo toco' -f $Step.Name, $Step.Port)
        return $true
    }

    if (-not (Test-Path $Step.Dir)) {
        Write-Log ('{0}: no existe el directorio {1}' -f $Step.Name, $Step.Dir) 'ERROR'
        return $false
    }

    if ($DryRun) {
        Write-Log ('-DryRun: aca arrancaria {0} ({1})' -f $Step.Name, $Step.Cmd)
        return $true
    }

    Write-Log ('{0}: arrancando' -f $Step.Name)
    $created = Invoke-CimMethod -ClassName Win32_Process -MethodName Create -Arguments @{
        CommandLine      = $Step.Cmd
        CurrentDirectory = $Step.Dir
    }
    if ($created.ReturnValue -ne 0) {
        Write-Log ('{0}: Win32_Process.Create devolvio {1}' -f $Step.Name, $created.ReturnValue) 'ERROR'
        return $false
    }

    for ($i = 1; $i -le $Step.Wait; $i++) {
        Start-Sleep -Seconds 1
        if (Test-Port $Step.Port) {
            Write-Log ('{0}: escuchando en {1} ({2}s)' -f $Step.Name, $Step.Port, $i)
            return $true
        }
    }

    Write-Log ('{0}: no llego a escuchar en {1} tras {2}s' -f $Step.Name, $Step.Port, $Step.Wait) 'ERROR'
    return $false
}

# --- Trabajo --------------------------------------------------------------
Write-Log '--- arranque del stack ---'
$failed = $null

foreach ($step in $Steps) {
    if ($SkipApache -and ($step.Name -eq 'Apache')) {
        Write-Log 'Apache: salteado por -SkipApache'
        continue
    }

    $ok = Start-Step $step

    if (-not $ok) {
        if ($step.Optional) {
            # Apache solo sirve para phpMyAdmin. Que no levante no es razon para
            # dejar el servidor de juego abajo.
            Write-Log ('{0} fallo, pero es opcional; sigo' -f $step.Name) 'WARN'
            continue
        }
        $failed = $step.Name
        break
    }
}

if ($failed) {
    Write-Log ('cadena cortada en "{0}"; no arranco lo que viene despues porque dependeria de el' -f $failed) 'ERROR'
    exit 1
}

Write-Log 'stack arriba'
exit 0
