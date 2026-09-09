<#
.SYNOPSIS
    Abre en el Firewall de Windows los puertos que el juego necesita de afuera:
    TCP 2106 (login server) y TCP 7777 (game server).

.DESCRIPTION
    El reenvio del router estaba bien, pero no alcanzaba: no habia ninguna regla
    de entrada para esos puertos, ni por puerto ni por programa, asi que Windows
    descartaba las conexiones en silencio -- sin RST y sin nada en el log del
    servidor. Desde afuera se ve igual que un servidor apagado, que es lo que
    hacia dificil encontrarlo.

    Las reglas se crean en los tres perfiles a proposito. La placa Ethernet esta
    catalogada como Publica, que es el perfil mas restrictivo, asi que una regla
    solo para Privada no serviria de nada. Y ponerlas en los tres evita que
    dejen de aplicar si Windows recategoriza la red despues de un reinicio o de
    un cambio de router.

    No se abre el 9014 (el canal login <-> game): escucha solo en 127.0.0.1, asi
    que nunca sale de la maquina y una regla ahi solo agrandaria la superficie.

    Es idempotente: borra la regla del mismo nombre antes de crearla, asi que
    correrlo dos veces no deja duplicados.

    Necesita permisos de administrador.

.PARAMETER Remove
    Borra las reglas en vez de crearlas.

.EXAMPLE
    powershell -ExecutionPolicy Bypass -File tools\firewall-rules.ps1
#>
param(
    [switch]$Remove
)

$ErrorActionPreference = 'Stop'

# --- Configuracion --------------------------------------------------------
$Rules = @(
    @{ Name = 'L2C4 Login Server (TCP 2106)'; Port = 2106 }
    @{ Name = 'L2C4 Game Server (TCP 7777)';  Port = 7777 }
)

# --- Requiere elevacion ---------------------------------------------------
$isAdmin = ([Security.Principal.WindowsPrincipal] `
    [Security.Principal.WindowsIdentity]::GetCurrent()
).IsInRole([Security.Principal.WindowsBuiltInRole]::Administrator)

if (-not $isAdmin) {
    Write-Host 'Hace falta ejecutarlo como administrador.' -ForegroundColor Red
    Write-Host 'Abri PowerShell con "Ejecutar como administrador" y volve a correrlo.'
    exit 1
}

# --- Aplicar --------------------------------------------------------------
foreach ($rule in $Rules) {
    # Sin -ErrorAction Ignore esto corta el script la primera vez, cuando
    # todavia no existe nada que borrar.
    Remove-NetFirewallRule -DisplayName $rule.Name -ErrorAction Ignore

    if ($Remove) {
        Write-Host ('borrada: {0}' -f $rule.Name) -ForegroundColor Yellow
        continue
    }

    New-NetFirewallRule `
        -DisplayName $rule.Name `
        -Description 'Servidor L2C4 Sieghardt. Creada por tools/firewall-rules.ps1' `
        -Direction   Inbound `
        -Action      Allow `
        -Protocol    TCP `
        -LocalPort   $rule.Port `
        -Profile     Domain,Private,Public `
        -Enabled     True | Out-Null

    Write-Host ('creada: {0}' -f $rule.Name) -ForegroundColor Green
}

# --- Verificacion ---------------------------------------------------------
# Lo que importa no es que la regla exista sino que quede habilitada y en el
# perfil Publico, que es donde esta la placa.
Write-Host ''
Write-Host 'Estado actual:'
foreach ($rule in $Rules) {
    $r = Get-NetFirewallRule -DisplayName $rule.Name -ErrorAction Ignore
    if (-not $r) {
        Write-Host ('  {0}  -> NO EXISTE' -f $rule.Port)
        continue
    }
    $f = $r | Get-NetFirewallPortFilter
    Write-Host ('  TCP {0}  {1}  {2}  perfiles: {3}' -f `
        $f.LocalPort, $r.Action, $r.Enabled, $r.Profile)
}
