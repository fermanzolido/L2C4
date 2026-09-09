<#
.SYNOPSIS
    Reinicia el login server cuando cambia la IP publica, para que vuelva a
    resolver el hostname de No-IP.

.DESCRIPTION
    El cliente de L2 recibe la direccion del game server como cuatro bytes
    crudos dentro del packet ServerList, no como un nombre. Esos bytes salen de
    GameServerThread._gameExternalIP, que se resuelve una sola vez: cuando el
    game server se registra contra el login. O sea que No-IP puede tener el DNS
    perfectamente al dia y el login seguir repartiendo la IP vieja.

    Reiniciar el login fuerza al game server a reconectar (cada 5 segundos) y
    re-registrarse, y en ese re-registro el login resuelve el hostname de nuevo.
    El game server no se toca: el mundo, los spawns y los timers siguen vivos.

    El disparador es el cambio del REGISTRO DNS, no el de la IP publica. Es
    deliberado: si reaccionara a la IP publica podria reiniciar el login antes
    de que No-IP haya propagado, y el login volveria a congelar la IP vieja.
    Esperando al DNS, el valor nuevo ya esta disponible cuando reiniciamos.

.PARAMETER DryRun
    Informa que haria, sin matar ni arrancar nada.

.PARAMETER Force
    Reinicia aunque la IP no haya cambiado. Solo para probar el camino completo.
#>
param(
    [switch]$DryRun,
    [switch]$Force
)

$ErrorActionPreference = 'Stop'

# --- Configuracion --------------------------------------------------------
$TargetHost = 'l2jsaked.servegame.com'
$LoginDir   = 'C:\Users\Man_z\Desktop\servidor\build\login'
$LoginVbs   = 'LoginServer.vbs'
$LoginPort  = 2106
$Resolvers  = @('8.8.8.8', '1.1.1.1')
$StateFile  = Join-Path $LoginDir 'log\ddns-state.txt'
$LogFile    = Join-Path $LoginDir 'log\ddns-watch.log'
$MaxLogKB   = 512
$BootWait   = 30   # segundos a esperar a que el 2106 vuelva a escuchar

# --- Utilidades -----------------------------------------------------------
function Write-Log {
    param([string]$Message, [string]$Level = 'INFO')
    $line = '{0} [{1}] {2}' -f (Get-Date -Format 'yyyy-MM-dd HH:mm:ss'), $Level, $Message
    try {
        if ((Test-Path $LogFile) -and ((Get-Item $LogFile).Length -gt ($MaxLogKB * 1KB))) {
            Move-Item $LogFile "$LogFile.old" -Force
        }
        Add-Content -Path $LogFile -Value $line -Encoding utf8
    } catch {
        # Si no se puede escribir el log, no es motivo para no hacer el trabajo.
    }
    # Write-Host y no Write-Output: dentro de una funcion, Write-Output se suma
    # al valor de retorno. Restart-LoginServer devolvia @(lineas..., $false) y
    # un array no vacio es verdadero, asi que un reinicio fallido se reportaba
    # como exito y encima guardaba el estado, matando el reintento.
    Write-Host $line
}

# Consulta a resolvers publicos a proposito: el cache de Windows serviria un
# valor viejo justo cuando nos importa que sea nuevo.
function Resolve-Current {
    foreach ($resolver in $Resolvers) {
        try {
            $answer = Resolve-DnsName -Name $TargetHost -Type A -Server $resolver `
                        -DnsOnly -NoHostsFile -ErrorAction Stop |
                      Where-Object { $_.Type -eq 'A' }
            if ($answer) {
                # Ordenado para que varios registros A no se vean como un cambio.
                return (($answer.IPAddress | Sort-Object) -join ',')
            }
        } catch {
            Write-Log ("resolver {0} no contesto: {1}" -f $resolver, $_.Exception.Message) 'WARN'
        }
    }
    return $null
}

function Get-LoginPid {
    $conn = Get-NetTCPConnection -LocalPort $LoginPort -State Listen -ErrorAction SilentlyContinue |
            Select-Object -First 1
    if ($conn) { return $conn.OwningProcess }
    return $null
}

function Restart-LoginServer {
    # El supervisor primero: si sigue vivo cuando muere el java, muestra un
    # MsgBox de "terminated abnormally" que se queda esperando un click.
    $supervisors = Get-CimInstance Win32_Process -Filter "Name='wscript.exe'" -ErrorAction SilentlyContinue |
                   Where-Object { $_.CommandLine -match 'LoginServer' }
    foreach ($s in $supervisors) {
        Write-Log ("matando supervisor wscript pid {0}" -f $s.ProcessId)
        Stop-Process -Id $s.ProcessId -Force -ErrorAction SilentlyContinue
    }

    $loginPid = Get-LoginPid
    if ($loginPid) {
        Write-Log ("matando login server pid {0}" -f $loginPid)
        Stop-Process -Id $loginPid -Force -ErrorAction SilentlyContinue
    }

    Start-Sleep -Seconds 2
    Write-Log 'arrancando LoginServer.vbs'
    # Win32_Process.Create y no Start-Process: asi el login queda colgando de
    # WmiPrvSE y no del arbol de la tarea programada, que el Programador de
    # tareas puede terminar entero cuando la corrida concluye. Con Start-Process
    # el login recien levantado se moriria junto con la corrida que lo levanto.
    $created = Invoke-CimMethod -ClassName Win32_Process -MethodName Create -Arguments @{
        CommandLine      = "wscript.exe $LoginVbs"
        CurrentDirectory = $LoginDir
    }
    if ($created.ReturnValue -ne 0) {
        Write-Log ("Win32_Process.Create devolvio {0}" -f $created.ReturnValue) 'ERROR'
        return $false
    }

    for ($i = 1; $i -le $BootWait; $i++) {
        Start-Sleep -Seconds 1
        if (Get-LoginPid) {
            Write-Log ("login server escuchando en {0} otra vez ({1}s)" -f $LoginPort, $i)
            return $true
        }
    }
    Write-Log ("el login server NO volvio a escuchar en {0} tras {1}s" -f $LoginPort, $BootWait) 'ERROR'
    return $false
}

# --- Trabajo --------------------------------------------------------------
$current = Resolve-Current
if (-not $current) {
    Write-Log 'ningun resolver contesto; no se toca nada' 'WARN'
    exit 0
}

$previous = $null
if (Test-Path $StateFile) {
    $previous = (Get-Content $StateFile -Raw -ErrorAction SilentlyContinue).Trim()
}

if (-not $previous) {
    Set-Content -Path $StateFile -Value $current -Encoding ascii
    Write-Log ("primera corrida, guardo {0} como referencia" -f $current)
    exit 0
}

if (($current -eq $previous) -and (-not $Force)) {
    exit 0   # Sin novedad: no se escribe al log, para que solo tenga eventos.
}

if ($Force -and ($current -eq $previous)) {
    Write-Log ("-Force: reinicio con la IP sin cambios ({0})" -f $current) 'WARN'
} else {
    Write-Log ("la IP de {0} cambio: {1} -> {2}" -f $TargetHost, $previous, $current)
}

if ($DryRun) {
    Write-Log '-DryRun: aca reiniciaria el login server; no se toca nada'
    exit 0
}

# Si el login no esta corriendo no hay nada que reiniciar, pero si hay que
# anotar la IP nueva: si no, el proximo arranque a mano quedaria con el valor
# viejo guardado y este script no volveria a dispararse.
if (-not (Get-LoginPid)) {
    Set-Content -Path $StateFile -Value $current -Encoding ascii
    Write-Log 'el login server no esta corriendo; anoto la IP nueva y salgo' 'WARN'
    exit 0
}

if (Restart-LoginServer) {
    Set-Content -Path $StateFile -Value $current -Encoding ascii
    Write-Log 'listo: el game server se re-registra solo en los proximos 5 segundos'
    exit 0
}

# No se guarda el estado a proposito: si el reinicio fallo, queremos que la
# proxima corrida lo vuelva a intentar en vez de darlo por hecho.
Write-Log 'el reinicio fallo; no guardo el estado para reintentar en la proxima corrida' 'ERROR'
exit 1
