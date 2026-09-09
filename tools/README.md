# tools

Herramientas de operacion del servidor. No forman parte del datapack ni del zip
de distribucion.

## watch-ddns.ps1

Reinicia el login server cuando cambia la IP publica.

**Por que hace falta.** El cliente de L2 recibe la direccion del game server como
cuatro bytes crudos dentro del packet `ServerList` — el protocolo no tiene campo
para un nombre, asi que el DDNS del `l2.ini` no interviene en ese salto. Esos
bytes salen de `GameServerThread._gameExternalIP`, que se resuelve en un solo
lugar: cuando el game server se registra contra el login. No-IP puede tener el
DNS al dia y el login seguir repartiendo la IP vieja.

Reiniciar el login fuerza al game server a reconectar (reintenta cada 5s) y
re-registrarse, y en ese re-registro el login resuelve el hostname de nuevo. El
game server no se toca: el mundo, los spawns y los timers siguen vivos. Medido
de punta a punta: 5 segundos.

El disparador es el cambio del **registro DNS**, no el de la IP publica. Si
reaccionara a la IP publica podria reiniciar antes de que No-IP haya propagado,
y el login volveria a congelar la IP vieja.

**Uso manual**

```powershell
.\watch-ddns.ps1 -DryRun -Force   # muestra que haria, sin tocar nada
.\watch-ddns.ps1 -Force           # reinicia aunque la IP no haya cambiado
```

Estado y log quedan en `build\login\log\` (`ddns-state.txt`, `ddns-watch.log`).
El log solo registra eventos: si no pasa nada, no escribe.

## L2C4-DDNS-Watch.task.xml

La tarea programada que corre el script cada 2 minutos. Para reinstalarla:

```powershell
Register-ScheduledTask -TaskName 'L2C4 DDNS Watch' -Xml (Get-Content tools\L2C4-DDNS-Watch.task.xml -Raw) -User 'Man_z'
```

Corre con `InteractiveToken` a proposito, no con S4U como la tarea del agente
web: el login relanzado tiene que quedar en la sesion de escritorio o su ventana
no seria visible. El costo es que la tarea solo corre con la sesion iniciada,
que es la misma condicion que ya tienen los servidores, porque se arrancan a
mano y tienen GUI.

Para pararla sin desinstalar nada: `Disable-ScheduledTask -TaskName 'L2C4 DDNS Watch'`

## start-all.ps1

Levanta el stack completo en orden: MariaDB, Apache, login server, game server.

**Por que hace falta.** Nada de esto arrancaba solo: XAMPP no esta instalado como
servicio, no hay claves Run ni entradas en la carpeta de Inicio, y los servidores
se abrian a mano desde los `.vbs`. Despues de un reinicio no volvia ni la base.

El orden importa. Login y game abren su pool de HikariCP al arrancar, asi que sin
MariaDB escuchando fallan; y el game se registra contra el login. Cada paso espera
al anterior y corta la cadena si no contesta, en vez de arrancar cosas condenadas
a fallar. Apache es la excepcion: si no levanta se sigue igual, porque solo sirve
para phpMyAdmin.

Es idempotente — lo que ya escuche su puerto no se toca — asi que se puede correr
a mano cuando sea para levantar lo que falte.

```powershell
.\start-all.ps1 -DryRun        # muestra que levantaria
.\start-all.ps1 -SkipApache    # sin Apache
.\start-all.ps1                # levanta lo que falte
```

Log en `build\autostart.log`. Medido en frio (todo abajo, cache de disco frio):
84 segundos, de los cuales 75 son el game server cargando el datapack.

## L2C4-Autostart.task.xml

La tarea que corre `start-all.ps1` al iniciar sesion, con un minuto de retraso
para que la red y el cliente de No-IP esten arriba primero. Sin repeticion: corre
una vez por login.

```powershell
Register-ScheduledTask -TaskName 'L2C4 Autostart' -Xml (Get-Content tools\L2C4-Autostart.task.xml -Raw) -User 'Man_z'
```

## Ojo con el Panel de Control de XAMPP

El panel maneja su propio `mysqld` y su propio `httpd`. Si esta abierto y se le da
Start a MySQL con la base ya corriendo, mata la que hay y levanta otra: eso deja a
InnoDB haciendo crash recovery, y login y game se quedan sin base, salen con
codigo 2 y sus supervisores `.vbs` los relanzan. Se recupera solo, pero son un par
de minutos de caida y una recuperacion de InnoDB que no hacia falta.

Con estas dos tareas el panel ya no es necesario para arrancar nada. Conviene
usarlo solo para mirar, o directamente cerrarlo.
