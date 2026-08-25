# Auditoría sistemática del código

Registro de la lectura área por área de todo el repo: 2342 archivos, ~433.000
líneas. Esto **no** es lo que hacen las rutinas diarias — ellas eligen una cosa
por corrida. Acá se lee un área completa, se entiende, y se arregla todo lo que
aparezca.

Las rutinas mantienen el piso entre pasadas; esta auditoría baja el techo.

**Cómo usar este archivo:** antes de auditar un área, leé si ya está empezada y
qué se encontró. Al terminar una sesión de auditoría, actualizá el estado y los
hallazgos aunque no hayas terminado el área.

---

## Mapa y estado

| Área | Archivos | Líneas | Estado |
|---|---:|---:|---|
| **`model/clan`** | **5** | **3.110** | **en curso — 2 bugs arreglados** |
| ~~`model/itemcontainer`~~ | 10 | 3.727 | **TERMINADA** |
| `model/skill` | 19 | 3.869 | pendiente |
| `model/olympiad` | 7 | 4.554 | pendiente |
| `loginserver` | 45 | 5.649 | pendiente |
| `gameserver/ai` | 15 | 7.907 | pendiente |
| `commons` | 47 | 11.240 | pendiente |
| datapack `ai/` | 67 | 14.462 | pendiente |
| `managers` | 44 | 14.636 | pendiente |
| `data` | 71 | 14.922 | pendiente |
| `network/serverpackets` | 259 | 19.900 | pendiente |
| `network/clientpackets` | 201 | 21.727 | pendiente |
| datapack `handlers/` | 375 | 51.203 | pendiente |
| `model/actor` | 166 | 53.236 | pendiente |
| datapack `quests/` | 298 | 79.342 | pendiente |

Orden elegido: de menor a mayor, salvo que la criticidad mande. Se empezó por
`itemcontainer` porque es chico y es donde vive la duplicación de ítems, que es
la única clase de bug que puede arruinar una economía de forma irreversible.

---

## `model/itemcontainer` — TERMINADA

**Leído:** `ItemContainer.java` completo e `Inventory.java` hasta ~1330:
integridad de ítems (`dropItem`, `addItem`, `removeItem`), el núcleo del
paperdoll (`setPaperdollItem`), `equipItem` entero, `BowCrossRodListener` y
`reloadEquippedItems`.

**Área completa.** Los diez archivos leídos enteros, 3.727 líneas.

### Evaluación de la clase base

**No se encontró ninguna vía de duplicación.** El código es más cuidadoso de lo
esperado:

- `transferItem` revalida que el ítem siga en el contenedor **dentro** del
  bloque `synchronized`, no antes.
- `destroyItem` verifica el retorno de `removeItem` antes de destruir.
- `_items` es `ConcurrentHashMap.newKeySet()`, así que iterar mientras se
  remueve es seguro.
- El conteo pedido se acota al disponible antes de mover.

### Hallazgos

| Severidad | Qué |
|---|---|
| Bajo | `MULTIPLE_ITEM_DROP` gobierna dos cosas distintas: tirar ítems al piso (`Npc.java:1649`) y **agregar al inventario** (`ItemContainer.java:300`). Si un admin lo pone en `False` pensando en los drops, agregar N ítems no apilables agrega 1 y los otros se pierden en silencio. Hoy no afecta: viene en `True` por defecto y en el `General.ini` que se distribuye. |
| Medio | `Inventory.dropItem(process, objectId, count, ...)` no validaba `count`. Con un valor negativo, la rama de drop parcial hacía `changeCount(-count)` sobre el ítem de origen, o sea **le sumaba**: duplicación. **No era explotable** — `RequestDropItem` rechaza `count` negativo, cero y mayor al disponible, y el otro llamador con count (`Pet`) pasa `item.getCount()`. El problema real era la inconsistencia: `transferItem` acota internamente y `destroyItem` rechaza internamente, pero este delegaba en el llamador. **Arreglado:** ahora rechaza `count <= 0` y `count > getCount()`. Inocuo, porque todos los llamadores actuales ya cumplían. |
| Medio | `setPaperdollItem` notificaba `ON_PLAYER_ITEM_UNEQUIP` cuando `old == item`, o sea cuando el slot no cambió y el bloque de trabajo se salteó entero. **Alcanzable desde el cliente:** `UseItem.java:253` mete el cebo de pesca en `PAPERDOLL_LHAND` sin chequear si ya está ahí, así que usar el mismo cebo dos veces seguidas con la caña equipada dispara un unequip que nunca ocurrió. Sin impacto observable hoy porque ningún script escucha ese evento. **Arreglado:** la condición ahora es `(old != null) && (old != item)`. |
| **Alto** | `ArmorSetListener.notifyUnequiped` quitaba la skill del escudo al sacar **cualquier** pieza del set, aunque el escudo siguiera equipado. La skill del escudo se gana por llevar el escudo del set, no por completarlo: `notifyEquiped` la otorga desde su propia rama y `containAll()` ni siquiera mira el slot del escudo. Con el set completo se autocorrige al reequipar la pieza, pero **con el set incompleto la skill no vuelve** hasta reequipar el escudo. **Arreglado:** solo se quita si el escudo ya no está puesto. |
| **Alto** | `PlayerInventory.transferItem` desreferenciaba el resultado de `super.transferItem` sin chequear null, para notificar `ON_PLAYER_ITEM_TRANSFER`. `ItemContainer.transferItem` tiene tres caminos que devuelven null, incluido el re-chequeo dentro del lock que detecta que el ítem dejó de estar. **Alcanzable:** `SendWareHouseDepositList` valida con `checkItemManipulation` y después llama a `transferItem`; entre esas dos llamadas otro hilo puede sacar el ítem. El propio handler loguea "Error depositing a warehouse object (newitem == null)", o sea que el caso estaba previsto — pero el NPE ocurre antes del `return`, así que ese log es inalcanzable. El hermano `destroyItem`, 46 líneas abajo, sí chequea null. **Arreglado.** |
| **Alto** | `getNonQuestSize()` devolvía `_items.size()`, o sea el total, sin filtrar ítems de quest. Se usa en cinco chequeos de capacidad de inventario. La prueba de que estaba sin terminar está en `Player.java:11526`: `includeQuestInv ? getSize() : getNonQuestSize()` — un ternario que elige entre dos conteos que eran idénticos, así que el parámetro no hacía nada. Los ítems de quest ocupaban slots aunque el llamador pidiera lo contrario. **Arreglado:** ahora filtra por `isQuestItem()`. **Cambia comportamiento:** los límites de inventario se vuelven menos restrictivos para quien lleva ítems de quest, que es lo que los cinco llamadores piden por su nombre. |
| **Alto** | `ClanWarehouse` repetía el mismo patrón dos veces más. En `transferItem` es peor que en `PlayerInventory`: no llama a `super` primero, hace `getItemByObjectId(objectId)` y desreferencia de una. El almacén de clan es **compartido**, así que dos miembros retirando el mismo ítem a la vez dejan al segundo con la búsqueda vacía y un NPE. En `addItem(process, itemId, ...)` la base devuelve null si el id de ítem no existe. **Arreglados los dos.** |
| Bajo | `ItemSkillsListener.notifyUnequiped`: al desequipar una armadura, el bucle que restaura las skills que otro ítem equipado también otorgaba tomaba el cooldown de `item` —la pieza que se está sacando— en vez de `itm`, el ítem del que sale la skill restaurada. `notifyEquiped` lo hace bien porque ahí la skill y el delay salen del mismo ítem. **Arreglado.** |

### Sospechas evaluadas y descartadas

- **NPE en `ItemContainer.java:309`** — `actor.sendInventoryUpdate(iu)` sin
  chequear null, en un método que 27 líneas antes hace `actor != null ? ...`.
  Se buscaron llamadores: los que pasan `null` son a `Player.addItem`, donde el
  `null` es la *referencia*, no el actor; y `CastleManorManager` apunta a un
  warehouse, no a `INVENTORY`. **No se encontró un camino alcanzable.** Queda
  como riesgo latente, no como bug. Un chequeo defensivo sería inofensivo.
- **`destroyAllItems` iterando mientras remueve** — seguro, `_items` es un set
  concurrente.

### Asimetrías anotadas, sin consecuencia demostrada

**`equipItem` está bien.** Se revisaron todos los casos de body part: arma a
dos manos (`LR_HAND` limpia el LHAND), mano izquierda (desequipa el arma a dos
manos del RHAND salvo las combinaciones arco+flecha y caña+cebo), aro, anillo,
armadura completa (limpia piernas) y piernas (limpia la armadura completa). Las
exclusiones mutuas son correctas.

El comentario `// Don't care about arrows, listener will unequip them
(hopefully).` en el caso `R_HAND` es injustificado: `BowCrossRodListener`
efectivamente limpia el LHAND cuando se desequipa un arco o una caña del RHAND.

**`reloadEquippedItems` es frágil, pero converge.** Se usa al cambiar de clase
y llama `notifyUnequiped` seguido de `notifyEquiped` por cada listener,
asumiendo que son refrescos puros de stats y skills.
`BowCrossRodListener.notifyUnequiped` no lo es: hace
`setPaperdollItem(PAPERDOLL_LHAND, null)`, o sea desequipa la flecha de verdad
y muta `_paperdoll` mientras el bucle de arriba lo recorre.

Se trazó el flujo completo y **no produce un bug**: el `notifyEquiped` que sigue
reequipa la flecha, y como RHAND es el índice 7 y LHAND el 8, el orden del
recorrido hace que el balance de stats cierre. Cuesta escrituras de base y
eventos de más, nada más. **No se tocó**, pero el acoplamiento es frágil: si
alguien agrega otro listener con efectos colaterales, o cambia el orden de los
índices del paperdoll, deja de cerrar.

**El bucle de restauración solo corre para armaduras.** En
`ItemSkillsListener.notifyUnequiped`, la compensación que devuelve una skill
que otro ítem equipado también otorga está detrás de `if (item.isArmor())`.
Desequipar un arma que comparta skill con una armadura puesta quita la skill y
no la restaura. **No se tocó:** quitar esa condición cambiaría el
comportamiento en cada cambio de arma, y puede ser deliberado por costo. Hace
falta decidirlo, no deducirlo.

**`equipItem` ignora el slot guardado, pero `restore` lo compensa.**
`equipItem` elige el primer slot libre en vez de leer `item.getLocationSlot()`,
así que en principio un aro guardado en REAR podría volver en LEAR.
**No pasa:** la consulta de `restore` termina en `ORDER BY loc_data`, o sea que
los ítems se procesan en orden de slot y cada uno cae en el suyo.

_(Corrección: en una pasada anterior esto se anotó como un efecto cosmético
real. Era falso, y se detectó al leer `restore`.)_


**Cobertura despareja de los eventos de equipar.** `ON_PLAYER_ITEM_UNEQUIP` se
dispara desde `Inventory.setPaperdollItem`, o sea desde cualquier camino,
incluidos los internos como restaurar el inventario al loguear.
`ON_PLAYER_ITEM_EQUIP` se dispara desde `Player.useEquippableItem`, o sea solo
en el camino iniciado por el jugador. Un ítem equipado por un camino interno no
genera evento de equip, pero al removerlo sí genera el de unequip. Hoy no rompe
nada: ningún script del datapack escucha ninguno de los dos.


`transferItem` revalida la pertenencia del ítem al contenedor dentro del lock;
la rama de destrucción parcial de `destroyItem` (`item.getCount() > count`) no
lo hace. Llamar `contenedorA.destroyItem(ítemDeB, ...)` descontaría de B y
refrescaría el peso de A. Los dos accesores por id (`destroyItem(objectId...)` y
`destroyItemByItemId`) buscan primero en el propio contenedor, así que no
exponen el problema. No se encontró un llamador que pase un ítem ajeno.

### Archivos chicos — anotado sin tocar

**`PlayerRefund.addItem` desaloja al azar.** Cuando el refund pasa de 12
ítems saca uno con `_items.stream().findFirst().get()`, pero `_items` es un
`ConcurrentHashMap.newKeySet()`, que **no tiene orden**. En vez de sacar el más
viejo saca uno arbitrario, así que podés perder la posibilidad de recomprar algo
que acabás de vender mientras sobrevive una venta anterior. Arreglarlo bien pide
una estructura ordenada: es un cambio de diseño, no una corrección.

**`Mail.returnToWh` es código muerto.** Cero llamadores. Su rama para
`wh == null` solo cambia la ubicación en memoria, sin actualizar la base ni
sacar el ítem del contenedor, a diferencia de la rama que usa `transferItem`.
Latente porque nada la alcanza. **Candidato para la rutina de limpieza.**

**`PetInventory.getOwnerId` usa `catch (NullPointerException)` como control de
flujo** para el caso de mascota sin dueño. Funciona, pero un chequeo explícito
diría lo mismo sin atrapar NPEs que vengan de más adentro.

**`PetInventory.transferItemsToOwner` no se defiende** del dueño nulo, mientras
que `getOwnerId` en el mismo archivo sí. Inconsistente, pero solo se llama desde
`Pet.java:653`, donde el dueño existe.

---

## `model/clan` — en curso

Son **5** archivos, no 6, y **3.110** líneas: `Clan.java` (2095),
`ClanMember.java` (727), `ClanPrivileges.java` (182), `ClanAccess.java` (57),
`ClanInfo.java` (49).

**Leído:** `ClanPrivileges` y `ClanAccess` completos; de `ClanMember` el vínculo
con el jugador (`setPlayer`, `isOnline`, `getPowerGrade`), constructores,
`setPledgeType`/`setPowerGrade` y sus escrituras a base; de `Clan.java` el cambio
de líder, los campos, el sistema de rangos completo (`initializePrivs`,
`restoreRankPrivs`, `getRankPrivs`, `setRankPrivs`), alta y baja de miembros
(`addClanMember`, `updateClanMember`, `removeClanMember`,
`removeMemberInDatabase`), `getMaxNrOfMembers`, `getSubPledgeMembersCount` y
`checkClanJoinCondition`. Además, por seguir el hilo del pledgeType, los 32
paquetes de cliente de clan/alianza/pledge.

**Pendiente:** el resto de `Clan.java` (alianzas, crests, level up, guerras), el
resto de `ClanMember.java`, y `ClanInfo.java`.

### Hallazgos

Dos bugs arreglados. Los dos salieron del mismo hilo: seguir de dónde viene el
`pledgeType` de un miembro.

| # | Archivo | Defecto | Severidad |
|---|---|---|---|
| 1 | `Clan.java:1338` `restoreRankPrivs` | `_privs.get(rank).setPrivs(...)` sin chequeo de null | alta |
| 2 | `RequestPledgeReorganizeMember.java:85` | `_newPledgeType` del cliente se escribe sin validar | media |

**1 — NPE al restaurar privilegios de rango que borra los privilegios del clan.**
`initializePrivs()` puebla exactamente los rangos 1..9 y corre **antes** de
`restore()`. `restoreRankPrivs()` después recorre las filas de `clan_privs` y
hace `_privs.get(rank).setPrivs(privileges)` sin chequear null. Cualquier fila
con un rango fuera de 1..9 tira `NullPointerException`, que la cortan el `catch`
de la propia función — y con eso **se aborta el `while` entero**: todos los
rangos que venían después de la fila mala se quedan con la `ClanPrivileges()`
vacía que dejó `initializePrivs()`.

Consecuencia: los miembros de ese clan pierden todos los privilegios de rango al
reiniciar el servidor, en silencio, con una sola línea de log. Se recupera
recién si alguien limpia la fila a mano.

Que esto pasa está probado por el propio código: la línea `if (rank == -1)
continue;` existe porque alguien se topó con filas de rango -1. Parchearon el
valor puntual que vieron, no la clase de problema. -1 quedaba cubierto; 0 —que es
el `DEFAULT` de la columna— y 10 en adelante, no.

Arreglo: chequeo de null con log de warning. Para `rank == -1` el comportamiento
queda **idéntico** (se sigue saltando en silencio, sin ensuciar el log al
arrancar); para el resto, se saltea la fila mala y los rangos siguientes sí se
cargan.

**2 — El tipo de subunidad se toma del cliente sin validar.**
`RequestPledgeReorganizeMember` intercambia dos miembros entre subunidades:
`member1` va a `_newPledgeType` y `member2` va al viejo de `member1`. Pero
`_newPledgeType` es un `readInt()` crudo del cliente, y nunca se contrasta con la
subunidad en la que `member2` realmente está. Único control previo:
`hasAccess(MODIFY_RANKS)`.

El intercambio solo es coherente si `_newPledgeType == member2.getPledgeType()`.
Con cualquier otro valor no es un swap: la subunidad de `member2` pierde un
miembro que nadie reemplaza, y `member1` cae en una subunidad que puede no
existir. Los tipos válidos son 0, -1, 100, 200, 1001, 1002, 2001 y 2002
(`getMaxNrOfMembers`); cualquier otro cae en el `default` y devuelve límite 0.

Dos consecuencias concretas:

- Un miembro en una subunidad inexistente deja de contar en
  `getSubPledgeMembersCount(0)`, que es lo que `checkClanJoinCondition` compara
  contra `getMaxNrOfMembers(0)`. **El clan puede superar su límite de miembros**,
  uno por cada miembro escondido así.
- Mandando `_newPledgeType = -1` el miembro queda en el tipo "academia", que es
  justo la rama que `removeClanMember` exime del penalty: un líder puede mover a
  alguien a -1 y después echarlo **sin los 5 días de penalización** para entrar a
  otro clan.

Arreglo: rechazar el paquete si `_newPledgeType != member2.getPledgeType()`. El
cliente legítimo siempre manda la subunidad donde está el miembro seleccionado,
así que el flujo normal no cambia. De yapa, esto también corta el caso
`_memberName == _selectedMember`, que hoy hace dos escrituras a base para
terminar en el mismo estado.

**Interacción entre los dos.** El arreglo 2 es lo que vuelve inalcanzable el
tercer hallazgo de abajo (la asimetría del penalty). Sin él, esa asimetría es un
exploit vivo.

### Anotado sin tocar

- **Asimetría del penalty al echar a un miembro.** Con el jugador online,
  `removeClanMember` respeta la exención `if (exMember.getPledgeType() != -1)`;
  con el jugador offline, `removeMemberInDatabase` escribe
  `clan_join_expiry_time` **siempre**, sin esa condición. Misma acción, resultado
  distinto según si la víctima está conectada. **No lo toqué** porque en este
  fork no existe la academia de clan: `RequestJoinPledge` tiene el
  `readInt()` del pledgeType comentado y el campo fijo en 0, y no hay ninguna
  constante de academia en todo el repo. Con el hallazgo 2 arreglado, el
  pledgeType no puede volverse -1, así que la rama es inalcanzable por los dos
  lados. Emparejarla sería agregar código para un caso imposible.

- **`RequestPledgeSetMemberPowerGrade` acepta cualquier power grade.** Es un
  `readInt()` sin rango, escrito a `characters.power_grade`. **No es escalada de
  privilegios**: `getRankPrivs` sí tiene chequeo de null y devuelve
  `ClanPrivileges()` vacío para un rango desconocido, o sea que degrada a *menos*
  permisos. El daño es basura en la base y un miembro en un rango que la UI no
  puede mostrar, auto-infligido por el propio líder y reversible. Validarlo bien
  requiere API nueva en `Clan` para preguntar si un rango existe; no lo hice para
  no ampliar el alcance.

- **`RequestPledgePower` caso 3 no hace nada con miembros offline.** Resuelve el
  miembro con `getClanMember(id).getPlayer()`, que es null si no está conectado,
  y sale sin avisar ni escribir a base. El líder cree que asignó privilegios y no
  pasó nada. Arreglarlo pide un `setClanPrivileges` en `ClanMember` con su
  escritura a base, que hoy no existe.

- **`setRankPrivs` no tiene ningún llamador** en `java/` ni en el datapack. Su
  rama `else` es justamente la que crearía las filas con rango arbitrario que
  rompen el hallazgo 1. **No la toco** — es exactamente el caso `ClanAccess.NONE`
  otra vez: parece muerta y podría no serlo.

- **`updateClanMember` no llama a `setPlayer`.** A diferencia de
  `addClanMember(Player)`, que hace `new ClanMember(...)` y después
  `setPlayer(player)`. El constructor ya asigna `_player`, así que el campo queda
  bien; lo que se saltea son los efectos colaterales de `setPlayer` (skills de
  asedio al líder). Verificado que el constructor asigna `_player` en
  `ClanMember.java:90`. No es bug en sí, pero es la clase de asimetría que
  conviene mirar cuando se lea el resto de `ClanMember`.

### Sospechas evaluadas y descartadas

Cinco sospechas evaluadas, **ninguna resultó bug**:

- **Desbordamiento de la máscara de privilegios.** `1 << ordinal()` sobre un
  `int` se rompe a partir de 31 valores. `ClanAccess` tiene 24, ordinales 0 a
  23. Entra con margen, pero **queda poco margen**: si algún día se agregan más
  de 31 privilegios, la máscara se corrompe en silencio.

- **Skills de asedio sin quitar al desloguear.** Se agregan con
  `addSkill(sk, false)`, o sea sin persistir, así que no hay filtración.

- **`removeSiegeSkills` con el clan ya en null.** Lee
  `character.getClan().getCastleId()` sin chequear. En `Clan.java:415` corre
  antes de `player.setClan(null)` en la 420. Orden correcto.

- **Privilegios del líder saliente que no se persisten.** Al líder offline se le
  escribe `clan_privs` directo en la base; al online solo se le hace
  `disableAll()` en memoria. Pero `UPDATE_CHARACTER` incluye `clan_privs`, así
  que se persiste en el próximo guardado. Cambia el *cuándo*, no el *si*.

- **Doble lookup en `getRankPrivs`.** `_privs.get(rank) != null ?
  _privs.get(rank).getPrivs() : ...` es el mismo patrón chequear-y-usar que sí
  era una carrera en `ClanHallAuction`. Acá **no lo es**: no hay ni un `remove`
  ni un `clear` sobre `_privs` en todo el archivo, solo `put` y `get`. Anotado
  sin tocar, igual que los otros casos del mismo patrón.

### Nota de calidad

Todas las colecciones de `Clan.java` son concurrentes (`ConcurrentHashMap`,
`newKeySet`), y el cambio de líder maneja los cuatro casos —saliente y entrante,
online y offline— persistiendo a mano cuando el jugador no está conectado. Es
código más cuidado que el promedio de lo visto hasta ahora.
