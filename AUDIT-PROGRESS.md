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
| ~~`model/clan`~~ | 5 | 3.110 | **TERMINADA** |
| ~~`model/itemcontainer`~~ | 10 | 3.727 | **TERMINADA** |
| **`model/skill`** | **19** | **3.869** | **en curso — 5 bugs arreglados** |
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

## `model/clan` — TERMINADA

Son **5** archivos y **3.110** líneas: `Clan.java` (2095), `ClanMember.java`
(727), `ClanPrivileges.java` (182), `ClanAccess.java` (57), `ClanInfo.java` (49).
**Leídos los cinco de punta a punta.**

### Hallazgos

Nueve defectos arreglados, en cuatro commits. Ocho de los nueve salieron de
comparar caminos que deberían ser espejo; ninguno de leer un método aislado.

| # | Archivo | Defecto | Severidad |
|---|---|---|---|
| 1 | `Clan.restoreRankPrivs` | `_privs.get(rank)` sin null-check borra los privilegios del clan | alta |
| 2 | `Clan.setNewLeader` | `_leader.getPlayer()` sin guard aborta el traspaso semanal de liderazgo | alta |
| 3 | `RequestPledgeReorganizeMember` | tipo de subunidad tomado del cliente sin validar | media |
| 4 | `Clan.changeLevel` + `ClanMember.setPlayer` | tres umbrales distintos para la misma regla de skills de asedio | media |
| 5 | `Clan.dissolveAlly` | crest de alianza borrado con la alianza ya desarmada | media |
| 6 | `Clan.changeLevel` | `_leader.isOnline()` sin guard corta los broadcasts | media |
| 7 | `Clan` y 12 archivos más | `int * 86400000` y `int * 3600000` desbordan | media |
| 8 | `Clan.storeNotice` | corta en `MAX - 1` con un guard que admite `MAX` | cosmética |
| 9 | `Clan.updateBloodOathCountInDB` | el log de error nombra al otro método | cosmética |

**1 — NPE al restaurar privilegios de rango.** `initializePrivs()` crea
exactamente los rangos 1..9 y corre **antes** de `restore()`.
`restoreRankPrivs()` hacía `_privs.get(rank).setPrivs(privileges)` sin chequear
null: cualquier fila de `clan_privs` con un rango fuera de ese conjunto tiraba
NPE, y como el `catch` está en la propia función, **abortaba el `while` entero**.
Todos los rangos posteriores a la fila mala se quedaban con la `ClanPrivileges()`
vacía. Los miembros de ese clan perdían todos sus privilegios al reiniciar, en
silencio. Que ocurre está probado por el propio código: el `if (rank == -1)
continue;` existe porque alguien se topó con esas filas y parcheó el valor
puntual, no la clase. El rango 0 —el `DEFAULT` de la columna— no estaba cubierto.

**2 — NPE con clanes sin líder.** `ClanTable` instancia un `Clan` por cada fila
de `clan_data` sin verificar que `leader_id` resuelva a un personaje, y
`restore()` solo asigna `_leader` si encuentra la fila. Un clan cuyo líder fue
borrado queda con `_leader` en null — estado que el propio código conoce, porque
`getLeaderName()` lo loguea como "Clan X without clan leader!".
`DailyResetManager.clanLeaderApply()` corre cada miércoles sobre **todos** los
clanes con `new_leader_id` pendiente y no verifica que haya líder actual. Un solo
clan sin líder abortaba el loop: ningún clan posterior recibía su cambio, y el
resto de `onReset()` no corría ese día. Se repite semana a semana porque nada
repara el dato. Verificado que el `RunnableWrapper` de `ThreadPool` captura
`Throwable` sin relanzar, así que la tarea recurrente sí sobrevive.

**3 — Tipo de subunidad sin validar.** `RequestPledgeReorganizeMember`
intercambia dos miembros entre subunidades, pero `_newPledgeType` es un
`readInt()` crudo que nunca se contrastaba con la subunidad real de `member2`.
Un miembro en una subunidad inexistente deja de contar en
`getSubPledgeMembersCount(0)`, que es lo que `checkClanJoinCondition` compara
contra el límite: **el clan puede superar su máximo de miembros**. Y mandando
`-1` el miembro quedaba en el tipo "academia", la rama que `removeClanMember`
exime del penalty, así que un líder podía mover a alguien y echarlo sin los 5
días de penalización.

**4 — Tres umbrales para la misma regla.** Las skills de asedio se otorgan según
`SiegeClanMinLevel`, que es lo que usan `Player` al loguear y `Siege` para gatear
el registro. `Clan.changeLevel` tenía 5 hardcodeado y `ClanMember.setPlayer`
tenía 4. Con el `SiegeClanMinLevel = 4` que trae el repo, un clan que llegaba a
nivel 4 **perdía** las skills que acababa de ganar hasta que el líder relogueara.
`setNewLeader` ya usaba el config correctamente, lo que confirma cuál era la
intención. `changeLevel` además mezclaba el chequeo de asedio con el mensaje de
reputación de nivel 5; son umbrales distintos y quedaron separados.

**5 — Crest de alianza borrado a destiempo.** `dissolveAlly` llamaba a
`changeAllyCrest(0, false)` **después** de `setAllyId(0)`. Ese método resuelve a
qué clanes afectar desde `_allyId`, así que con la alianza ya desarmada el
`UPDATE` no matcheaba nada y `getClanAllies(0)` devolvía lista vacía (`ClanTable`
tiene un guard `allianceId != 0`). El crest igual se borraba del `CrestTable`,
dejando a todos los clanes de la alianza apuntando a un crest inexistente. Para
el clan líder el id viejo además sobrevivía al reinicio, porque `updateClanInDB`
no escribe `ally_crest_id` y `changeAllyCrest` es su único escritor.

**7 — Desbordamiento de enteros.** `int * 86400000` desborda pasados 24 días;
`int * 3600000`, pasadas 596 horas. En los 22 sitios la multiplicación se hacía
en `int` y recién después se ensanchaba a `long`. `GlobalAuctionManager` ya tenía
la forma correcta con `L`. Los defaults del repo no desbordan, pero cualquier
admin que suba una penalización arriba de 24 días o ponga un boss a un mes de
respawn obtenía un valor **negativo**. `Auctioneer` es el único alcanzable sin
tocar config: parsea los días de un bypass sin validar rango, y con 25 la subasta
nacía vencida.

### Anotado sin tocar

- **`calculatePledgeClass`: el valor baja cuando el clan sube de nivel 3 a 4.**
  El `case 4` tiene `if (player.isClanLeader())` **sin `else`**, así que un
  miembro común de un clan nivel 4 se queda en `pledgeClass = 0` — que es el
  valor de "sin clan". Los niveles 0 a 3 caen en el `default` y dan 1. **No lo
  toqué**: verifiqué que hoy nada distingue el 0 del 1. El único consumidor real
  es `ConditionPlayerPledgeClass`, y el datapack solo usa los valores `2` y `-1`;
  `RequestExAskJoinMPCC` pide `>= 5`; y `Skill.getMinPledgeClass()` no tiene
  ningún consumidor. Sin fuente autoritativa del valor retail para ese caso,
  cambiarlo sería adivinar.

- **Asimetría del penalty al echar a un miembro.** Online, `removeClanMember`
  respeta la exención `getPledgeType() != -1`; offline, `removeMemberInDatabase`
  escribe `clan_join_expiry_time` siempre. Misma acción, resultado distinto según
  si la víctima está conectada. **No lo toqué** porque no existe la academia en
  este fork: `RequestJoinPledge` tiene el `readInt()` del pledgeType comentado y
  el campo fijo en 0. Con el hallazgo 3 arreglado el pledgeType ya no puede
  volverse -1, así que la rama es inalcanzable por los dos lados. Sin ese
  arreglo, esta asimetría **era un exploit vivo**.

- **`RequestPledgeSetMemberPowerGrade` acepta cualquier power grade.**
  `readInt()` sin rango, escrito a `characters.power_grade`. **No es escalada**:
  `getRankPrivs` sí tiene null-check y devuelve privilegios vacíos para un rango
  desconocido, o sea que degrada a *menos* permisos. Queda basura en la base y un
  miembro en un rango que la UI no muestra, auto-infligido por el líder y
  reversible. Validarlo bien pide API nueva en `Clan` para preguntar si un rango
  existe.

- **`RequestPledgePower` caso 3 no hace nada con miembros offline.** Resuelve el
  miembro con `getClanMember(id).getPlayer()`, null si no está conectado, y sale
  sin avisar ni escribir. El líder cree que asignó privilegios y no pasó nada.
  Arreglarlo pide un `setClanPrivileges` en `ClanMember` con su escritura a base.

- **`setRankPrivs` no tiene ningún llamador.** Su rama `else` es justamente la
  que crearía las filas de rango arbitrario del hallazgo 1. **No la toco**: mismo
  caso que `ClanAccess.NONE`.

- **`Skill.getMinPledgeClass()` y los configs `FRINTEZZA_SPAWN_INTERVAL` y
  `FRINTEZZA_SPAWN_RANDOM` no tienen consumidores.** Mismo criterio: no se tocan.

- **`getNotice()` usa `toLowerCase()` sin `Locale`.** El filtro anti-bypass
  compara contra "action" y "bypass"; en un servidor con locale turco la I
  minúscula sale sin punto y el filtro no matchea. `ClanTable` sí usa
  `Locale.ENGLISH` para su índice por nombre, así que la convención existe en el
  repo. Riesgo real pero exótico.

- **`getNotice()` compila una regex por llamada** (`replaceAll` con `<.*?>`), y
  `incrementHiredGuards()` hace `_hiredGuards++` sin atomicidad sobre un `int`
  plano, a diferencia de los `AtomicInteger` de kills y deaths.

- **`checkAllyJoinCondition` es un método de instancia que ignora `this`**: usa
  `player.getClan()` como clan líder. Debería ser estático. Sin efecto.

- **`ClanPrivileges`: `1 << ordinal()` sobre un `int` se rompe a partir de 31
  valores.** `ClanAccess` tiene 24. Entra, pero queda poco margen.

### Sospechas evaluadas y descartadas

Nueve sospechas verificadas que **no** resultaron bug:

- **`changeAllyCrest` con `true` no borra el crest viejo del `CrestTable`**, a
  diferencia de `changeClanCrest` y `changeLargeCrest`. Es **correcto**: los tres
  llamadores con `true` son clanes miembro adoptando o limpiando el crest *de la
  alianza*, que no les pertenece — borrarlo se lo rompería a los demás.

- **`broadcastToOnlineAllyMembers` parecía una copia de
  `broadcastToOnlineMembers` que se olvidó de iterar la alianza.** Sí la itera,
  vía `getClanAllies(getAllyId())`, y en `dissolveAlly` se llama mientras
  `_allyId` todavía vale.

- **`updateClanMember` no llama a `setPlayer`**, a diferencia de
  `addClanMember(Player)`. El constructor `ClanMember(Clan, Player)` ya asigna
  `_player` (línea 90), así que el campo queda bien; solo se saltean los efectos
  colaterales.

- **`ClanInfo` captura `_total` y `_online` al construirse.** No se cachea: se
  construye fresco dentro del constructor de `AllianceInfo` y se envía.

- **`levelUpClan`**: los cinco casos son consistentes entre sí — SP más adena
  para los niveles 0-1, SP más ítem para 2-4, mismo orden de validación y
  consumo, y el consumo va dentro del `&&`, así que corta antes si falta SP.

- **`store()`**: los 13 parámetros mapean 1:1 con las 13 columnas de
  `INSERT_CLAN_DATA`.

- **`calculatePledgeClass` tiene el bloque `getLeaderSubPledge` comentado** en
  los niveles 6 a 11. Es un port incompleto **uniforme**, idéntico en todos los
  casos, no una asimetría entre ellos.

- **`Player` duplica los clamps de noble y héroe** que `calculatePledgeClass` ya
  hace al final. Los valores coinciden (5 y 8) y el camino de `Player` es el de
  jugador sin clan, que `calculatePledgeClass` cubre igual.

- **Doble lookup en `getRankPrivs`**: chequear-y-usar, el mismo patrón que sí era
  carrera en `ClanHallAuction`. Acá no: no hay ni un `remove` ni un `clear` sobre
  `_privs` en todo el archivo.

Más las cinco de la sesión anterior: desbordamiento de la máscara de privilegios,
skills de asedio sin quitar al desloguear, `removeSiegeSkills` con el clan en
null, y privilegios del líder saliente sin persistir.

### Lo que enseñó esta área

El hilo más productivo no fue leer archivo por archivo, sino **seguir un dato**:
de dónde sale y a dónde va el `pledgeType` de un miembro. Ese solo hilo destapó
los hallazgos 1, 3 y 4, y de paso la asimetría del penalty. Los datos que cruzan
la frontera cliente-servidor y terminan en la base son el mejor punto de entrada.

Segundo patrón: **el guard puntual delata la clase**. El `if (rank == -1)` y los
null-checks de `getLeaderId()` y `getLeaderName()` son cicatrices de bugs reales
que alguien parcheó en el valor concreto que vio. Cada guard así es una pista de
que la misma dereferencia está sin proteger en otro lado — y en los dos casos lo
estaba.

## `model/skill` — en curso

Son **19** archivos y **3.869** líneas. Los grandes: `Skill.java` (1758),
`BuffInfo.java` (482), `AbnormalType.java` (381), `SkillChannelizer.java` (245),
`SkillOperateType.java` (180).

**Leído:** `SkillChannelizer` y `SkillChannelized` completos, `BuffFinishTask`
completo, de `BuffInfo` el ciclo de vida (`initializeEffects`, `finishEffects`,
`onTick`, `stopAllEffects`), y de `Skill.java` la familia `getPower` y
`getAffectLimit`. Además, siguiendo el hilo, los 14 target handlers del datapack.

**Pendiente:** el grueso de `Skill.java` (constructor y parseo del `StatSet`,
`getTargetList`, `applyEffects`, `activateSkill`), el resto de `BuffInfo`, y los
enums (`AbnormalType`, `SkillOperateType`, `AbnormalVisualEffect`).

### Hallazgos

Cinco defectos arreglados en dos commits.

| # | Archivo | Defecto | Severidad |
|---|---|---|---|
| 1 | `Creature.isChannelized` | negación invertida: la canalización nunca se aborta | alta |
| 2 | `SkillChannelizer.run` | registros de canalizador que nunca se dan de baja | alta |
| 3 | `CommandChannel` (targethandler) | falta el guard `> 0`: la skill llega a un solo miembro | alta |
| 4 | `CorpseClan` (targethandler) | rama NPC sin guard ni hoist: la skill no llega a nadie | alta |
| 5 | `Clan` (targethandler) | `getAffectLimit()` dentro del bucle: tope distinto por iteración | media |

**1 y 2 — había que arreglarlos juntos.** `Creature.isChannelized()` devolvía
`!_channelized.isChannelized()`, la negación de lo que promete su propio javadoc
y de lo que reporta el delegado. Su hermano 15 líneas más arriba,
`isChanneling()`, tiene el javadoc gemelo y no tiene la negación. Los seis
llamadores usan el mismo guard:

    if (isChannelized()) { getSkillChannelized().abortChannelization(); }

así que el aborto corría solo cuando no había nada que abortar, y nunca cuando sí
lo había. El efecto visible es sobre el caster: `run()` descuenta
`getMpPerChanneling()` en cada tick **antes** de mirar la lista de objetivos, así
que si el objetivo muere el jugador sigue perdiendo MP hasta quedarse en cero, en
vez de que la canalización se corte. Viene del commit inicial, no de un cambio
reciente.

En paralelo, `SkillChannelizer.run()` registra al canalizador en los objetivos de
cada tick, pero `_channelized` se **sobrescribe** con la lista nueva y
`stopChanneling()` solo da de baja lo que encuentra ahí. Un objetivo que salía de
rango entre ticks quedaba con el canalizador registrado para siempre. Ese conteo
es el que devuelve `getChannerlizersSize()`, que es **lo que elige el nivel** del
efecto de canalización aplicado a ese objetivo.

La trampa: arreglar solo la negación habría hecho que `abortChannelization()`
empezara a llamar `abortCast()` sobre esos registros viejos, cancelando el casteo
de jugadores que ya no estaban canalizando sobre ese objetivo. Un arreglo que
resolvía una cosa y rompía otra.

**3, 4 y 5 — `getAffectLimit()` parece un getter y no lo es.** Devuelve
`_affectLimit[0] + Rnd.get(_affectLimit[1])`: **cada llamada tira un dado nuevo**.
Y devuelve **cero** para toda skill que no declare `affectLimit`, que son todas
menos trece. Doce de los catorce target handlers contemplan las dos cosas; tres
sitios no.

- `CommandChannel` comparaba contra el tope sin chequear que fuera mayor que
  cero. Con el cero habitual, el primer miembro agregado cumple `size >= 0` y
  corta el bucle: la skill llegaba al caster, su summon y **exactamente un**
  miembro más, en vez de a todo el command channel.
- `CorpseClan` tiene el mismo código dos veces: su rama de jugador lee el tope
  una vez antes del bucle y usa el guard; su rama de NPC no hacía ninguna de las
  dos. Sin guard, la primera pasada cortaba de una, así que una skill de clan de
  NPC solo afectaba al propio caster.
- `Clan` tenía el guard pero leía el tope **dentro** del bucle, con un valor
  distinto en cada pasada.

Los dos sitios dentro de bucles se encontraron revisando el anidamiento de todas
las llamadas a `getAffectLimit()` del directorio de handlers; eran los únicos dos.

### Anotado sin tocar

- **`onExit` sin su `onStart`.** `initializeEffects` saltea `effect.onStart(...)`
  cuando `_effected.isDead() && !_skill.isPassive()` —caso real y comentado en el
  código: el efecto instantáneo mata al objetivo y los continuos se omiten—, pero
  `finishEffects` llama a `effect.onExit(...)` sin esa condición. O sea que un
  efecto puede recibir `onExit` sin haber recibido nunca `onStart`.

  **No lo toqué**: probar que hace daño exige auditar los ~200 effect handlers, y
  eso es del área `handlers/`. Muestreo hecho: `AttackTrait.onExit` tiene un
  guard explícito `if (count == 0) continue;` —otra cicatriz que confirma que el
  caso ocurre—, y `CrystalGradeModify.onExit` usa asignación absoluta, así que
  ambos son inmunes. **Búsqueda concreta a correr al llegar a
  `handlers/effecthandlers/`:** un `onExit` que mute estado compartido de forma
  relativa (decrementar, dividir, quitar de una colección) sin verificar que
  `onStart` haya corrido.

- **`getPower(Creature, Creature, boolean, boolean)` chequea `creature == null`
  pero no `target`**, y sí dereferencia `target.getCurrentHp()` en la rama
  `PHYSICAL_ATTACK_HP_LINK`. Los tres llamadores son `calcPhysDam`,
  `calcMagicDam` y `calcManaDam`, donde `target` es parámetro obligatorio del
  cálculo de daño. Asimetría real, sin camino nulo demostrable: agregar el
  chequeo sería especulativo.

- **`SkillChannelized._channelizers` acumula mapas internos vacíos.**
  `addChannelizer` crea la entrada por skillId con `computeIfAbsent`, y
  `removeChannelizer` vacía el mapa interno pero nunca borra la entrada. Está
  acotado por la cantidad de skills canalizables del juego, o sea que es chico.

- **`finishEffects` cancela las tareas de tick pero no limpia `_tasks`.** El
  `BuffInfo` se descarta después, así que no sobrevive nada.

### Sospechas evaluadas y descartadas

- **`BuffFinishTask.stop()` cancela la tarea pero no pone `_task = null`**, a
  diferencia de `removeBuffInfo()` que sí lo hace. Como `start()` y
  `addBuffInfo()` solo reprograman `if (_task == null)`, parecía que después de
  un `stop()` los buffs no volvían a expirar nunca — y el ciclo existe: `deleteMe()`
  → `onDecay()` → `decreaseCount()` → `RespawnTaskManager` → `respawnNpc()` →
  `initializeNpc()` sobre **la misma instancia** → `onSpawn()` → `start()`.

  **No es bug**: `Creature.deleteMe()` hace
  `_effectList.stopAllEffectsWithoutExclusions(false, false)` **antes** de
  `_buffFinishTask.stop()`, y eso llega a `removeBuffInfo` por cada buff
  (`stopAndRemove` → `info.stopAllEffects` → `removeBuffInfoTime`). Al vaciarse
  el mapa, `removeBuffInfo` ya cancela y anula `_task`, así que para cuando corre
  `stop()` el campo ya está en null. La asimetría queda como **trampa latente**:
  cualquier futuro llamador de `stop()` que no vacíe `_buffInfos` primero dejaría
  la expiración de buffs muerta para siempre en esa criatura.

- **`removeChannelizer` sobre un skillId desconocido.** `getChannelizers` devuelve
  `Collections.emptyMap()`, y llamarle `remove()` a un mapa inmutable parecía
  `UnsupportedOperationException`. **Verificado corriéndolo** con Java 25:
  `Collections.emptyMap().remove(k)` devuelve null sin tirar nada, porque hereda
  `AbstractMap.remove`, que sobre un mapa vacío nunca llega a `iterator.remove()`.

- **`finishEffects` no espeja a `initializeEffects`.** Sí lo hace en las tres
  operaciones que importan: cancela las tareas de tick, `removeStats()` espeja a
  `addStatFuncs`, y `removeAbnormalVisualEffects` espeja a
  `addAbnormalVisualEffects`. La única diferencia es que el alta es condicional
  (`if (update)`) y la baja no, o sea que se quita de más, que es la dirección
  segura.

### Descartadas en el barrido de `Skill.java` (constructor y parseo)

- **Hueco en el override de duración de skills.** El bloque de
  `ENABLE_MODIFY_SKILL_DURATION` reemplaza la duración si
  `(_level < 100) || (_level > 140)` y la **suma** si
  `(_level >= 100) && (_level < 140)`. El nivel exactamente 140 no cae en
  ninguna de las dos y el override se ignora en silencio. **Inalcanzable**: los
  niveles que existen son 101-130 y 141-170 (las dos rutas de encantamiento);
  no hay ningún 131-140.

  Pero el mismo bloque deja algo real anotado: la ruta 1 (101-130) **suma** la
  duración configurada y la ruta 2 (141-170) la **reemplaza**, siendo las dos
  rutas de encantamiento. Afecta a las 34 skills de `SkillDurationList`, que
  vienen activas en `Player.ini`. **No lo toqué**: cambiarlo altera la duración
  de canciones y danzas encantadas y no tengo fuente autoritativa de cuál es el
  comportamiento correcto.

- **`parseExtractableSkill` avisa y sigue igual, tres veces.** Si el producto
  tiene menos de 3 campos loguea "wrong seperator!" y **no** hace `continue`; si
  el id o la cantidad son `<= 0` loguea y **agrega igual** el `ItemHolder`; y el
  `catch` se traga la excepción y agrega el producto con `chance` en 0. El
  constructor hace lo mismo un nivel más arriba: avisa si `capsuled_items` está
  vacío y después lo parsea igual. **Inalcanzable con los datos del repo**:
  validadas las 220 tablas `#extractableItems` y sus 720 productos, todos tienen
  exactamente 3 campos con id y cantidad positivos.

- **`_channelingTickInitialDelay` usa `_channelingTickInterval` como default.**
  Lo divide por 1000 y lo vuelve a multiplicar, lo cual da vueltas pero es
  correcto, y el orden de asignación es el correcto: el intervalo se asigna
  antes de usarse como default.
