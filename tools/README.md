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
