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
| ~~`taskmanagers`~~ | 18 | 3.000 | **TERMINADA** |
| ~~`model/clan`~~ | 5 | 3.110 | **TERMINADA** |
| ~~`model/itemcontainer`~~ | 10 | 3.727 | **TERMINADA** |
| ~~`model/skill`~~ | 19 | 3.869 | **TERMINADA** |
| ~~`model/olympiad`~~ | 7 | 4.554 | **TERMINADA** |
| ~~`gameserver/config`~~ | 55 | 5.554 | **TERMINADA** |
| ~~`loginserver`~~ | 45 | 5.649 | **TERMINADA** |
| ~~`gameserver/ai`~~ | 15 | 7.907 | **TERMINADA** |
| **`commons`** | **47** | **11.240** | **en curso** |
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

**6 — Un default doble se registra como victoria.**
`OlympiadGameTask.checkDefaulted()` recorre a los dos participantes y puede
marcar a ambos: alcanza con que ninguno de los dos esté bajo el límite de peso,
o que los dos estén muertos, o que los dos choquen con el límite de conexiones
por IP. Cuando eso pasa corren los dos bloques de `validateWinner`, y los dos
escriben el ELO a partir de los valores capturados **antes** de que corriera
cualquiera de ellos, así que el segundo reemplaza en silencio lo que hizo el
primero.

Resultado: un combate al que no se presentó ninguno de los dos quedaba anotado
como victoria del jugador uno. Y como `nextOpponents()` elige como jugador uno al
de **ELO más alto** del par, el sesgo siempre favorecía al mismo lado: el mejor
rankeado subía rating por partidas que nadie jugó.

Los puntos sí estaban bien: cada uno pierde los suyos sobre su propio `StatSet`.
El arreglo deja el ELO quieto cuando fallan los dos, que es lo que ya hacía la
rama de doble crash unas líneas más abajo.

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

## `model/skill` — TERMINADA

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

### Hallazgo transversal: doble chequeo de bloqueo roto en 12 sitios

Salió de auditar `Skill.hasEffectType()`, que cachea `_effectTypes` con
double-checked locking. El campo **sí** es `volatile`, o sea que ahí está bien —
pero el patrón aparece en **15 lugares** del código y solo **3** lo declaran
`volatile`: `Creature._ai`, `Skill._effectTypes` y `EffectZone._task`.

Los otros doce no. Sin `volatile`, el hilo que lee el campo **fuera** del
candado no tiene relación happens-before con el constructor que corrió
**adentro**, así que puede ver una referencia publicada apuntando a un objeto
cuyos campos todavía no son visibles. `WorldObject.addScript` hasta nombra el
patrón en un comentario.

Arreglados agregando `volatile` a: `Attackable._firstCommandChannelAttacked`,
`Creature._seenCreatures`, `ControlTower._guards`, `Cubic._actionTask`,
`Monster._minionList`, `Npc._summonedNpcs`, `Player._manufactureItems`,
`Player._teleportWatchdog`, `ListenersContainer._listeners`,
`Quest._timerExecutor`, `WorldObject._scripts` y `EffectZone._skills`.

**Dos de los doce eran peores**: no tenían el segundo chequeo adentro del
candado, así que no es un problema de visibilidad sino una pérdida de escritura
lisa y llana.

- `getManufactureItems()` asignaba un mapa nuevo sin condición. Dos llamadores
  que pasen el chequeo externo construyen uno cada uno y la segunda asignación
  **descarta la primera**, junto con lo que ya se le hubiera puesto adentro.
- El watchdog de teleport agendaba la tarea sin condición y el campo se queda
  solo con la última, así que la anterior **nunca se cancela**.
  `TeleportWatchdogTask` sale enseguida si el jugador no está teleportándose,
  así que disparar de inmediato es inofensivo — pero sobrevive hasta un teleport
  **posterior** y le llama `onTeleported()`, terminándolo antes de tiempo.

**Anotado sin tocar:** la rama `else` que cancela el watchdog corre **fuera** del
candado, así que un cancel concurrente con un schedule sigue siendo una carrera
aparte. Y `Creature.java:3232` se parece pero **no** es este patrón: todo el
cuerpo ya está adentro del candado.

### Segunda ronda sobre `Skill.java`: descartadas con prueba

- **`applyEffects`, rama `self`: la condición extra `hasEffectType(EffectType.BUFF)`.**
  La rama principal agrega el `BuffInfo` a la lista de efectos con solo
  `addContinuousEffects`; la rama `self` exige además que la skill tenga algún
  efecto de tipo BUFF, con el comentario `// Skill target validation
  (simplified).` — que sonaba a que alguien había recortado una validación.
  Parecía que un efecto self continuo que no fuera BUFF nunca se agregaría a la
  lista y por lo tanto **nunca se inicializaría**.

  **No bloquea nada.** En todo el datapack hay solo **6** bloques
  `<selfEffects>`, con tres handlers: `Buff` (3) e `ImmobileBuff` (1), que son
  `EffectType.BUFF` y pasan el guard; y `FocusEnergy` (2, skills 345 y 346), que
  es `EffectType.NONE` pero **`isInstant()` devuelve true**, así que corre por la
  rama de instantáneos de `applyEffectScope` y no necesita la lista de efectos.
  Encima esas dos skills son `operateType = A1`, que no es continuo (solo A2, A4
  y DA2), ni auto-continuo (A3), ni toggle, así que `addContinuousEffects` ya era
  false para ellas.

- **`activateSkill` hace `obj.asCreature()` sin chequear `isCreature()`**, a
  diferencia de `SkillChannelizer.run()` que sí lo chequea, y
  `WorldObject.asCreature()` devuelve **null** en la clase base. **No es
  alcanzable**: los 14 target handlers devuelven Creatures —`Unlockable` filtra a
  puertas y cofres, y `Door extends Creature`, `Chest extends Monster`—, y los 11
  llamadores de `activateSkill` pasan jugadores, summons u objetivos ya
  resueltos. El único que pasa algo potencialmente nulo,
  `SellBuffBypassHandler:535` con `player.getSummon()`, tiene el guard
  `if ((player.getSummon() == null) || player.getSummon().isDead())` justo antes.
  Queda como trampa latente: es la asimetría con `SkillChannelizer`, no un bug.

- **`Skill.getEffects(EffectScope)` puede devolver null** (`EnumMap.get` de una
  clave ausente) y es público. Su único llamador, `applyEffectScope`, lo protege
  con `hasEffects(...)`, que sí chequea null. Trampa latente para un futuro
  llamador.

### Anotado sin tocar (segunda ronda)

- **Mensaje engañoso cuando un target handler tira excepción.**
  `getTargetList(Creature, boolean, Creature)` loguea la excepción y **después
  cae** en `creature.sendMessage("Target type of skill is not currently
  handled.")`. El tipo sí está manejado; el handler falló. El jugador recibe un
  diagnóstico falso.

- **Doble bloque de auto-play en `checkForAreaOffensiveSkills`.** El primero
  devuelve false si el modo es 1 (Monster) o 3 (NPC). El segundo devuelve false
  si el modo **no** es 0 (Any) ni 2 (Characters) — o sea, nunca, para los modos
  documentados, porque el primero ya se llevó el 1 y el 3. Y para cualquier otro
  valor **contradice** al intérprete autoritativo: `isTargetModeValid` trata su
  `default` como "Any Target", donde los jugadores sí son objetivos válidos. El
  modo se toma sin validar de `settings.get(3)` de un comando por voz. Redundante
  y confuso, sin daño demostrable; cambiarlo es decisión de mecánica.

### `BuffInfo` completo

**Arreglado: `_isInUse` no era `volatile`.** `EffectList` lo apaga cuando un buff
queda tapado por otro o cuando se reemplaza un pasivo, y lo vuelve a prender
cuando termina una hierba y hay que restaurar un buff escondido — todo desde el
hilo que aplica o quita el efecto. `BuffInfo.onTick()` lo lee desde el hilo del
scheduler que corre los ticks, y tres paquetes de servidor lo leen desde hilos de
red. Sin `volatile`, un buff tapado podía seguir corriendo su efecto por tick, y
uno restaurado podía quedarse mudo. `_finishType`, declarado cuatro líneas más
arriba en el mismo bloque, ya era `volatile`.

Revisado el resto de la clase: `_effector`, `_effected`, `_skill` y
`_periodStartTicks` son `final`; `_tasks` es un `ConcurrentHashMap`; y `_effects`
y `_abnormalTime` solo se mutan antes de publicar el objeto. El único que muta un
`BuffInfo` que no acaba de crear, `StealAbnormal`, le pone el tiempo y le aplica
los efectos **antes** de agregarlo a la lista del objetivo.

### El caso que ilustra por qué las rutinas eran peligrosas

**`addAbnormalVisualEffects()` llama a `updateAbnormalEffect()` sin condición**,
aunque ninguna de sus tres ramas haya hecho nada — mientras que su espejo
`removeAbnormalVisualEffects(broadcast)` solo lo llama si `broadcast`. Y
`updateAbnormalEffect()` no es barato: para un jugador es un `broadcastUserInfo()`
completo, y para un NPC recorre **todos** los jugadores que lo ven y le manda un
`NpcInfo` a cada uno. Se llama en cada aplicación de buff en la que se procesó al
menos un efecto continuo. Solo **237 de 1970** skills declaran
`abnormalVisualEffect`, o sea que el ~88% de las veces no hay nada visual que
mostrar.

Parece una optimización obvia. **Casi la hago, y me hubiera equivocado por poco.**
El primer razonamiento fue: si la hago condicional, se rompe la propagación de
stats, porque para el 88% de las skills esa sería la única emisión de
`UserInfo`/`NpcInfo` al aplicar un buff. Eso resultó **falso**: `addStatFuncs`
—que corre unas líneas antes, en el mismo `initializeEffects`— ya llama a
`broadcastModifiedStats`, que emite un broadcast completo si cambió `MOVE_SPEED`
y un `StatusUpdate` si no. Y los iconos los cubre `updateEffectIcons()` desde
`EffectList.add`. Así que la llamada probablemente sea **redundante, y encima
duplicada**.

**Igual no la toqué.** Es una mejora de performance, no una corrección, y quedó
un riesgo sin cerrar: un `onStart` de algún efecto podría cambiar algo que viaja
en `UserInfo` sin emitir su propio broadcast, y hoy se estaría apoyando en esta
llamada sin saberlo. Verificarlo exige auditar los ~200 effect handlers.

Vale la pena dejarlo escrito porque es exactamente el hallazgo que una rutina de
performance habría "arreglado": compila, el diff es de tres líneas, la
justificación suena impecable, y el modo de fallo —desincronización visual o de
velocidad en el 88% de los buffs— no lo detecta ningún build ni ningún test.

### Anotado sin tocar (`BuffInfo`)

- **`removeAbnormalVisualEffects` chequea `_effected == null || _skill == null` y
  `addAbnormalVisualEffects` no.** Ambos vienen del constructor y `applyEffects`
  corta antes si `effected == null`, así que el chequeo del lado de la baja es
  defensivo nada más. Asimetría sin consecuencia.

### Hallazgo transversal: índices de enum tomados del cliente

Salió de revisar los enums del área. El patrón `values()[id]` con un `id` que
llega del cliente aparece en varios lados. `AdminFence` es el ejemplar correcto:
deriva la cota del propio enum con `>= FenceState.values().length`.

**Arreglados (3):**

- **`RequestShortcutReg` acotaba con `typeId > 6`.** Ese literal está copiado de
  `RequestMakeMacro`, cuyo `MacroType` tiene **siete** constantes; `ShortcutType`
  tiene **seis**. Así que un `typeId` de exactamente 6 pasaba el chequeo e
  indexaba una posición más allá del final. Y como la búsqueda está en
  `readImpl()`, fallaba durante el parseo del paquete, antes de llegar a ninguna
  validación del handler.
- **`RequestMakeMacro`** tenía el literal correcto para su enum, pero es el mismo
  número copiado que produjo el defecto de al lado, y ninguna de las dos líneas
  se enteraría si al enum se le agrega o quita una constante. Ahora las dos
  derivan la cota del enum.
- **`AcquireSkillType.getAcquireSkillType()` no tenía cota alguna.** La llaman
  `RequestAcquireSkill` y `RequestAcquireSkillInfo` con un `readInt()` crudo.
  Ahora un valor fuera de rango resuelve a `CLASS`, que los dos llamadores
  rechazan por el camino que ya tienen: `getSkillLearn()` devuelve null para
  cualquier cosa que el jugador no pueda aprender legítimamente, y cada uno tiene
  un `default` que loguea el tipo inesperado.

**Verificado y descartado:** `RequestPetition` acota con
`_type <= 0 || _type >= 10` contra un `PetitionType` de **nueve** constantes
indexado en `_type - 1`. Es exacto. (Casi lo cuento como off-by-one: mi primer
conteo dio 8 porque la última constante no lleva coma y no la matcheó el grep.
Listarlas lo corrigió.)

**Anotado sin tocar:** `MacroList` y `Shortcuts` hacen `values()[]` sin cota
sobre una columna leída de la base, dentro de un bucle cuyo único `catch` está
afuera — así que una fila inesperada le costaría al jugador **todas** sus macros
o atajos restantes. Es la misma forma que el defecto de `clan_privs` ya
arreglado, pero acá no hay evidencia de que esa fila exista, así que queda
registrado y no endurecido por especulación.

### Cierre de `model/skill`

**Los 19 archivos leídos.** Los enums (`AbnormalType` con 326 constantes,
`TargetType`, `AffectScope`, `AffectObject`, `Element`, `SkillFinishType`,
`EffectCalculationType`, `SkillOperateType`) no persisten ordinales ni los usan
como índice, y `AbnormalVisualEffect` usa una máscara explícita en vez de
derivarla del ordinal, que es el patrón seguro.

Descartados en el cierre:

- **`SkillOperateType`**: los 10 valores están cubiertos por alguna de las seis
  clasificaciones. El datapack solo usa A1, A2, A3, P y T.
- **`TargetType` tiene 38 constantes y solo hay 14 handlers.** Un tipo sin
  handler devuelve lista vacía y le manda al jugador "Target type of skill is not
  currently handled". Usados en datos pero sin handler: `NONE` (25 skills) y
  `CORPSE` (1). **Ninguno es alcanzable**: de las 25 con `NONE`, 13 son pasivas
  (nunca se castean) y 11 de las 12 activas son disparadas por item handlers
  —anzuelos de pesca y pergaminos de encantamiento— que llaman a `activateSkill`
  directo sin pasar por `getTargetList`. Las dos que quedan, la skill 3049 y la
  4653 (`CORPSE`), son **datos muertos**: ningún arma las declara en
  `skills_on_crit` / `skills_on_magic` / `enchant4_skill`, y ningún NPC tiene la
  4653.
- **`AbnormalType.getAbnormalType(String)` devuelve `NONE` en silencio** ante un
  nombre desconocido, y `Enum.valueOf` es sensible a mayúsculas. Validados los 5
  nombres de las tablas `#dispelAbnormals` y los 45 inline de
  `DispelBySlotProbability`: **todos existen** en el enum. Los `slot="buff"` y
  `slot="debuff"` que parecían inválidos van a `DispelByCategory`, que guarda el
  slot como `String` y no toca `AbnormalType`. Y `BlockAbnormalSlot`, el único
  que sí lo convertiría, no lo usa ninguna skill.
- **`SkillHolder.getSkill()` hace lazy init sin candado.** Benigno:
  `SkillData.getSkill()` devuelve siempre la misma instancia compartida, cargada
  al arrancar, así que una carrera solo repite la búsqueda.
- **`SkillUseHolder` hereda `equals`/`hashCode` sin incluir sus propios campos**
  (`ctrlPressed`, `shiftPressed`). No importa: sus tres usos son campos sueltos
  de `Player` (`_currentSkill`, `_currentPetSkill`, `_queuedSkill`), nunca claves
  de un `Set` o `Map`.
- **`Skill.hasEffectType` castea `ordinal()` a `byte`.** `EffectType` tiene 40
  constantes, así que entra — mismo margen que `ClanAccess`: correcto hoy, sin
  lugar para llegar a 128.
- **`AffectObject` tiene una constante `NOE`**, que parece un typo de `NONE`. No
  la toco: es exactamente el caso `ClanAccess.NONE`.

## `model/olympiad` — TERMINADA

Son **7** archivos y **4.554** líneas: `Olympiad.java` (1695), `OlympiadGame.java`
(1012), `Hero.java` (935), `OlympiadGameTask.java` (414), `OlympiadManager.java`
(376), `OlympiadStadium.java` (81), `CompetitionType.java` (41).

**Leído:** de `Olympiad.java` todo el ciclo de los puntos (carga, techo, puntos
semanales, fin de período, `loadNoblesRank`, `getNoblessePasses`, registro); de
`OlympiadGame.java` el resultado completo del combate; `OlympiadManager.java`
entero; de `Hero.java` el ciclo de héroe (`resetData`, `computeNewHeroes`,
`claimHero`, `isHero`, `isUnclaimedHero`). Además, siguiendo el hilo, el script
`MonumentOfHeroes` y el handler de bypass `ScriptLink`.

**Pendiente:** el resto de `Olympiad.java` y de `Hero.java` (diario, peleas,
mensajes, persistencia), `OlympiadGameTask.java`, `OlympiadStadium.java`.

### Hallazgos

| # | Archivo | Defecto | Severidad |
|---|---|---|---|
| 1 | `MonumentOfHeroes` | las ramas que **otorgan** no repiten los chequeos de las que muestran | alta |
| 2 | `Olympiad.java` | el techo de puntos deja afuera los puntos iniciales | alta |
| 3 | `Q00631` | índice de recompensa sin acotar, y los ítems se consumen **antes** | alta |
| 4 | `Q00374` | mismo índice sin acotar (sin pérdida de ítems) | media |
| 5 | `OlympiadGame` | dos `catch` que descartaban la falla al entregar el premio | media |
| 6 | `OlympiadGame` | con los dos jugadores en default, el ELO queda como victoria del primero | media |

**1 — Los chequeos estaban en las ramas equivocadas.** `MonumentOfHeroes` tiene
eventos que deciden qué página HTML mostrar y llevan las condiciones
(`HeroClaim` chequea héroe y héroe-sin-reclamar; `HeroWeapon` chequea héroe y si
ya se llevó un arma), y eventos que **entregan**, que no llevaban ninguna:
`HeroReceive`, que otorga el estado de héroe, y el `default`, que parsea el
evento como id de arma y da el ítem.

`Player.processQuestEvent` valida que el script exista, que el evento no esté
vacío y que el jugador esté a distancia de interacción del NPC. **No** valida que
el evento sea uno que el script haya ofrecido, así que llegar a una rama que
otorga nunca requirió pasar por la que muestra. Que las ramas hermanas sí tengan
el chequeo es lo que delata que faltaba: alguien lo puso donde se dibuja la
pantalla, no donde se entrega.

El `default` además llamaba `Integer.parseInt` sobre el evento sin manejo, así
que cualquier evento que este script no define tiraba excepción.

**2 — El techo de puntos deja afuera lo que se regala al empezar.** Los puntos se
recortan en dos lugares —al cargar de base y al aplicar los puntos semanales— a
`(OLYMPIAD_MAX_POINTS * competitions_done) + (OLYMPIAD_WEEKLY_POINTS * 4)`. Falta
el término de los puntos iniciales, y con los defaults del repo ese es **el más
grande de los tres**: start 18, max 10, weekly 3.

Un noble sin competencias hechas tiene techo `0 + 12 = 12` pero se crea con
**18**. Pierde seis puntos en el siguiente reinicio, y a partir de ahí
`addWeeklyPoints()` no hace **nada** para él, porque `12 + 3` se recorta de vuelta
a 12. Los puntos semanales existen justamente para los nobles que no están
compitiendo, y el techo los anulaba para exactamente ese grupo. Los puntos además
gatean el registro (menos de 3 por clase, menos de 5 libre).

La expresión estaba duplicada a setecientas líneas de distancia, que es cómo las
dos copias pudieron divergir; quedó en un único helper privado que dice qué
significa el techo.

**3 y 4 — El índice de recompensa sale del evento.** Las dos quests dejan que el
nombre del evento elija una fila de su tabla de premios y ninguna verificaba que
la fila exista.

`Q00631` se protegía solo con `StringUtil.isNumeric`, que únicamente comprueba
que todos los caracteres sean dígitos: acepta un índice más allá de la última de
las seis filas. Y **el `takeItems` venía antes del lookup**, así que un índice
fuera de rango consumía los 120 ítems de quest del jugador y después tiraba la
excepción, antes de entregar nada. `Q00374` lee el dígito que sigue a un prefijo
fijo con `substring(9, 10)`: un evento que terminaba en el prefijo tiraba en el
propio `substring`. Ahí el lookup ya venía antes del `takeItems`, así que no
había pérdida.

### Anotado sin tocar

- **CORRECCIÓN.** Escribí antes que `_playerOneDefaulted` y `_playerTwoDefaulted`
  nunca se asignaban y que el bloque de "un jugador no se presentó" era código
  muerto. **Era falso**: grepeé los campos solo dentro de `OlympiadGame.java`, y
  se asignan en `OlympiadGameTask.checkDefaulted()`, líneas 148 y 152. El bloque
  está vivo, y el defecto del ELO que había anotado como hipotético era real
  (ver hallazgo 6).

- **`Hero.claimHero()` construye un `StatSet` vacío** si el jugador no tiene
  registro de héroe, y lo persiste: una fila sin nombre, sin clase y con conteo
  cero. Con el hallazgo 1 arreglado, sus dos llamadores ya no pueden llegar ahí.
  Endurecerlo también sería defender un camino que ya no existe.

- **Dos caminos de empate con comportamiento distinto.** Si los dos jugadores
  desaparecieron, se incrementa `COMP_DRAWN` y se avisa, pero no se tocan puntos
  ni ELO ni se llama a `saveResults`. El empate real sí hace las tres cosas.

- **La rama de doble crash no actualiza ELO ni emite mensaje**, a diferencia de
  las otras dos ramas de crash. Defendible (ninguno demostró nada), pero es una
  asimetría dentro de un trío.

- **El tercer `catch` vacío del archivo envuelve `Thread.sleep`** en el bucle de
  cuenta regresiva. Es manejo de interrupción, no una falla descartada, y pide un
  tratamiento distinto (restaurar el flag de interrupción).

### Sospechas evaluadas y descartadas

- **Carrera sobre `NOBLES_RANK`, que es un `HashMap` plano** reconstruido con
  `clear()` + `put` mientras —eso creía— hilos de paquetes lo leían al reclamar
  recompensas. **Falso, y lo había armado sobre un supuesto sin verificar**:
  `getNoblessePasses` es `private` y su único llamador está dentro del mismo
  `loadNoblesRank()` que reconstruye el mapa. Mismo hilo, sin lectura concurrente.

- **La recompensa de fin de temporada ignora los puntos del noble** y sale de
  `(esHéroe ? HERO_POINTS : 0) * GP_PER_POINT`, así que un no-héroe recibe cero.
  **Es el diseño de este fork**, no un olvido: el comentario del llamador dice
  "Store remaining hero reward points to player variables". Casi lo cuento como
  bug.

- **`clearRegistered()` no tiene ningún llamador**, lo que parecía dejar
  registros viejos vivos al cambiar de período. No: `init()` reasigna las dos
  listas con instancias nuevas, que es por qué el método sobra.

- **`nextOpponents` indexa con `Rnd.get(searchScope)`.** Verificados los tamaños
  1, 10 y 100: `searchScope` nunca supera el tamaño de la lista.

- **`getRandomClassList` puede devolver null.** Sus dos llamadores lo manejan: uno
  se lo pasa a `existNextOpponents`, que chequea null, y el otro lo compara con
  null antes de usarlo.

- **`_div` podría ser cero** y dividir por cero en `pointDiff`. El `switch` que lo
  asigna solo produce 5 o 3.

- **`COMP_DONE` no se incrementa en las ramas de crash.** Sí se incrementa, una
  sola vez, después de las tres ramas.

### Segunda ronda de `model/olympiad`: el ciclo de vida del combate

**7 — Un default doble se registra como victoria.** Ver arriba (hallazgo 6).

**8, 9 y 10 — El manager desarmaba todo con partidas en curso.** Al terminar el
período, `OlympiadManager` espera a que las partidas cierren antes de limpiar la
cola, las instancias, los registros y el estado del anti-feed. La espera plegaba
los resultados con **`||`** sobre una variable llamada `allGamesTerminated`, bajo
un comentario que dice "wait for all games terminated": salía apenas **una**
partida terminaba, y todo lo de abajo corría con el resto todavía peleándose.

Ese arreglo depende de que cada tarea reporte que terminó, así que hubo que
tocar dos cosas más primero:

- `OlympiadGameTask.run()` pon\u00eda `_game` en null **antes** de `_terminated`. En
  esa ventana `isTerminated()` le\u00eda el flag en false y despu\u00e9s dereferenciaba el
  juego nulo. Lo llama el hilo del manager, en una condici\u00f3n **fuera** del `try`
  que la rodea, y el manager es un `Thread` crudo: eso habr\u00eda terminado la
  olimpiada para el resto del per\u00edodo.
- `run()` tambi\u00e9n ten\u00eda un retorno temprano que no liberaba el estadio ni
  marcaba el flag. Hoy parece inalcanzable, pero con la espera convertida en
  conjunci\u00f3n una tarea que no reporta colgar\u00eda el hilo del manager en vez de
  solo filtrar un estadio, as\u00ed que se maneja igual.

`_game`, `_terminated` y `_started` los escribe el hilo de la tarea y los lee el
del manager: ahora son `volatile`.

**11 — Los buffs previos al combate nunca se aplicaban.** La cuenta regresiva es
`for (byte i = 60; i > 0; i -= step)`, y adentro hab\u00eda una rama comentada
"Apply buffs at 0 seconds remaining" guardada por `if (i == 0)`. La condici\u00f3n
del bucle es `i > 0`, as\u00ed que el cuerpo **nunca** corre con `i` en cero: pasa por
60, 50, 40, 30, 20, 10, 5, 4, 3, 2, 1 y sale. La rama era inalcanzable.

`applyBuffs()` y `healplayer()` no tienen otro llamador, as\u00ed que los
participantes entraban a **cada** combate sin Wind Walk ni Haste/Acumen, y sin
que les restauraran HP, CP y MP. El que llegaba de otra pelea, o simplemente no
lleno, com\u00eda con lo que ten\u00eda puesto.

**12 y 13 — Paginaci\u00f3n del diario y el historial de h\u00e9roe.** Los dos m\u00e9todos
arrancan el bucle en `(page - 1) * perpage` e indexan la lista directo desde
ah\u00ed. El `page` llega de un bypass del cliente: `RequestBypassToServer` parsea
`_diary` y `_match` con `Integer.parseInt` y lo pasa sin chequear. Debajo de uno
el \u00edndice arranca negativo, y lo bastante grande desborda la multiplicaci\u00f3n a
negativo. El `catch` exterior del handler loguea la excepci\u00f3n **con stack trace
completo**, as\u00ed que era una forma repetible de llenar el log.

### Barrido transversal: \u00edndices de p\u00e1gina del cliente

Cinco lugares del repo convierten un n\u00famero de p\u00e1gina en \u00edndice de lista. **Dos
ya lo hac\u00edan bien**: `GlobalAuctioneer` con `Math.max(1, Math.min(page, maxPage))`
—el modelo— y `AutoPlay`, que pisa sus tres sitios con `Math.max(0, ...)`, lo que
adem\u00e1s absorbe un negativo por desbordamiento. Los otros tres no:

- **`SchemeBuffer`** acotaba solo el l\u00edmite superior, as\u00ed que una p\u00e1gina menor a
  uno daba un `subList` con inicio negativo. Su `page` sale de un token de bypass.
- **`DropSearchBoard`** no acotaba ninguno. Su bucle va de `(page - 1) * 4` y hace
  `list.get(index)` directo. **Ya calculaba la cantidad de p\u00e1ginas y no la usaba.**
- **`AdminSearch`** tambi\u00e9n calculaba un `max` que nunca aplicaba, y sin l\u00edmite
  inferior. Solo admin, pero es la misma l\u00ednea.

### M\u00e1s descartadas

- **`teleportCountdown` escalona con un `switch` sobre valores exactos** (60, 30,
  15, 5), lo que parec\u00eda romperse con cualquier otro `OlympiadWaitTime`. Est\u00e1
  **validado al cargar el config**: si no es 120, 60, 30, 15 o 5, se fuerza a 120.

- **`_div` de `pointDiff`**, `getRandomClassList`, los l\u00edmites de `nextOpponents`
  y `COMP_DONE` en las ramas de crash: ver la ronda anterior.

### Lecci\u00f3n de m\u00e9todo de esta \u00e1rea

**Dos veces grepe\u00e9 dentro de un solo archivo y saqu\u00e9 la conclusi\u00f3n contraria.**
Con `_playerOneDefaulted` conclu\u00ed "nunca se asigna, es c\u00f3digo muerto" cuando se
asigna en `OlympiadGameTask`; con `clearRegistered()` conclu\u00ed "no tiene
llamadores" cuando lo llama `OlympiadManager`. Las dos veces la respuesta real
estaba en el archivo de al lado. **Para decidir si algo es alcanzable, el grep va
sobre `java/` y `dist/game/data/scripts/` completos, nunca sobre el archivo que
estoy leyendo.**

### Tercera ronda: persistencia, h\u00e9roes y coordinaci\u00f3n entre hilos

**14 y 15 — Referencias a clanes borrados.** `Hero.updateHeroes()` lee el clan y
la alianza del h\u00e9roe para guardarlos con el registro, con **cuatro** llamadas a
`ClanTable.getClan()` sin chequear. Una fila de `characters` puede nombrar un clan
que ya no existe en `clan_data`: **`DatabaseIdManager` tiene una consulta de
limpieza para exactamente ese estado**, que es lo que prueba que ocurre.

El `catch` que rodea el bloque solo cubre `SQLException`, y
`OlympiadEndTask.run()` no tiene `catch` ninguno, as\u00ed que el fallo escapaba a los
dos: abortaba la transici\u00f3n de temporada a la mitad, despu\u00e9s de poner `_period`
en 1 y recomputar los h\u00e9roes, pero **antes** de agendar `ValidationEndTask`. La
olimpiada quedaba en per\u00edodo de validaci\u00f3n hasta reiniciar.

Al barrer el patr\u00f3n apareci\u00f3 que **`Hero.java` ten\u00eda una segunda copia** del mismo
bloque en `processHeros()`, que corre al arrancar — arregl\u00e9 una y me falt\u00f3 la
hermana. M\u00e1s `ClanHallTable.setFree()` y la consulta de alianza del due\u00f1o del
castillo en `Siege`. Todos los dem\u00e1s sitios ya asignaban a variable y chequeaban.

**16 — Un chequeo de null puesto despu\u00e9s de lo que deb\u00eda proteger.** Las dos
ramas de `Hero.loadFights()` hacen
`ClassListData.getClass(id).getClassName()` y **despu\u00e9s** preguntan
`if ((name != null) && (cls != null))`. `getClass(int)` documenta que devuelve
null para un id desconocido, as\u00ed que la dereferencia ocurr\u00eda antes de que el
chequeo pudiera correr: nunca pudo hacer nada.

**17 — \u00cdndice de arena de espectador.** Tres m\u00e9todos hermanos indexan `STADIUMS`:
`removeSpectator` atrapa `ArrayIndexOutOfBoundsException`, `getSpectators` la
atrapa y devuelve null, y `addSpectator` indexaba directo. Sus dos llamadores le
pasan un valor crudo de un bypass. `bypassChangeArena` adem\u00e1s **saca al jugador
de su arena actual antes** de llamarlo, y nada lo deshace si falla.

**18 — Recompensa de temporada borrada sin entregarse.** `claimSeasonReward`
agregaba el \u00edtem y despu\u00e9s borraba la variable que lo registra, sin condici\u00f3n.
`addItem` devuelve null cuando el id ya no resuelve a una plantilla, as\u00ed que una
recompensa cuyo \u00edtem hab\u00eda sido removido o renombrado se borraba de las variables
del jugador sin haberse entregado nunca.

**19 — Un noble que no se guarda nunca m\u00e1s.** `saveNobleData` elige entre INSERT y
UPDATE seg\u00fan el flag `to_save`, y pon\u00eda el flag en false **mientras cargaba los
par\u00e1metros del INSERT**, antes de `execute()`. Un INSERT fallido se loguea por
noble, pero el flag ya hab\u00eda cambiado: el siguiente guardado tomaba el camino del
UPDATE sobre una fila que nunca se cre\u00f3, y el UPDATE no matcheaba nada. Nunca
m\u00e1s. El noble exist\u00eda solo en memoria y desaparec\u00eda al reiniciar, con sus puntos,
combates y ELO.

**20 — Banderas de coordinaci\u00f3n sin publicar.** Tres `static boolean` coordinan la
olimpiada entre hilos y ninguna era `volatile`. `_inCompPeriod` es **la condici\u00f3n
del `while` del hilo del `OlympiadManager`**, que corre en un `Thread` crudo
mientras las tareas que la escriben corren en el pool. `_battleStarted` es la
condici\u00f3n de un **bucle de espera** de la tarea que cierra el per\u00edodo.

### La sospecha que revert\u00ed

**`cleanEffects()` sale temprano si cualquiera de los dos se desconect\u00f3**, as\u00ed
que no limpia los efectos de **ninguno**. Al lado, `PlayersStatusBack()` —que
corre una l\u00ednea antes en la misma tarea— trata a cada jugador por separado. Lo
cambi\u00e9 para que hiciera lo mismo, compil\u00e9, y despu\u00e9s **lo revert\u00ed**.

El espejo correcto de `cleanEffects()` no es `PlayersStatusBack()` sino
**`removals()`**, que arma lo que el otro desarma — y `removals()` tiene los
**mismos tres retornos tempranos**. Son un par: si el armado se saltea, el
desarme tambi\u00e9n. Con mi cambio, un jugador cuyo rival se desconecta antes de
entrar al estadio hubiera perdido los buffs que tra\u00eda de afuera, en un combate
que nunca ocurri\u00f3.

**Queda anotado el residuo real**: si la desconexi\u00f3n pasa *durante* el combate,
`removals()` ya corri\u00f3 pero `cleanEffects()` sale temprano igual, y el que
sobrevive se lleva puestos los efectos del combate — incluidos los buffs previos,
ahora que el hallazgo 11 los hace aplicarse de verdad. Arreglar eso bien exige
distinguir "el armado corri\u00f3" de "no corri\u00f3", o sea estado nuevo, y no lo hago a
ojo en un camino de desconexi\u00f3n.

### M\u00e1s descartadas

- **`RequestWriteHeroWords` sin l\u00edmite de longitud.** S\u00ed valida: `isHero()` y
  `length() > 300`, que coincide exacto con el `varchar(300)` de la columna.
- **`character_variables` con inserciones duplicadas.** La tabla no tiene clave
  \u00fanica, solo dos \u00edndices no \u00fanicos, as\u00ed que el INSERT pelado no falla.
- **`Hero.shutdown()` sin cablear**, lo que dejar\u00eda los mensajes de h\u00e9roe sin
  persistir. Est\u00e1 cableado en `Shutdown.java:488`.
- **`OlympiadStadium._freeToUse` sin `volatile`.** Sus cuatro accesos est\u00e1n todos
  dentro de `OlympiadManager`, y `run()` es `synchronized`. `removeGame`, que s\u00ed
  llega del hilo de la tarea, no toca esa bandera.
- **`_olympiadInstances`** es `ConcurrentHashMap`, y lo tocan los dos hilos.
- **`sortHerosToBe`, rama Soulhound**: reemplaza su `StatSet` por
  `NOBLES.get(charId)` y lo dereferencia enseguida. `NOBLES` se carga de la misma
  tabla que lee la consulta y nunca se hace `remove`, solo `clear` y repoblado, y
  la consulta hace join con `characters`. Sin camino demostrable a null.
- **`_compStarted`** aparece solo dentro de c\u00f3digo comentado en tres archivos.
  No se toca.
- **`getWaitingList()` devuelve null** fuera del per\u00edodo de competencia. Su \u00fanico
  llamador chequea null.

### Cierre de `model/olympiad`

Los 7 archivos le\u00eddos. **Veinte defectos arreglados**, en trece commits, m\u00e1s un
barrido transversal de \u00edndices de p\u00e1gina y otro de `getClan()` encadenado que
salieron del \u00e1rea y tocaron `SchemeBuffer`, `DropSearchBoard`, `AdminSearch`,
`ClanHallTable`, `Siege` y dos quests.

## `loginserver` — TERMINADA

Son **45** archivos y **5.649** líneas. Superficie de red pura: autenticación de
jugadores, registro de game servers y la lista de servidores.

**Leído:** `LoginController`, `GameServerThread`, `GameServerTable`,
`FloodProtectorListener`, `LoginPacketHandler`, `LoginClient` (bucle de lectura y
desconexión), las dos clases base de paquetes, y los paquetes de cliente y de
game server. La UI Swing (`Gui`, `frmAbout`, 436 líneas) se revisó **solo por
acceso a estado del servidor**, no línea por línea: no tiene lógica de juego, y
lo que sí toca —iterar la lista de game servers y limpiar los baneos— quedó
cubierto por el hallazgo 5 y por el `ConcurrentHashMap` que ya usaban los baneos.

### Hallazgos

| # | Archivo | Defecto | Severidad |
|---|---|---|---|
| 1 | `AbstractGameServerPacket` / `AbstractClientPacket` | `readBytes` aloja antes de validar el largo | alta |
| 2 | `GameServerTable` | la lista de game servers es un `ArrayList` compartido por tres grupos de hilos | alta |
| 3 | `LoginController` | comparación de contraseñas por referencia: banea al que se equivoca | media |
| 4 | `FloodProtectorListener` | contador de conexiones no atómico | media |
| 5 | `GameServerThread` | largo de paquete sin validar sobre la conexión del game server | media |
| 6 | `GameServerTable` | `findFreeID` devuelve 0 en vez del centinela negativo que su llamador espera | media |
| 7 | `GameServerThread` | clave blowfish nula pasada a `NewCrypt` | baja |

**1 — Alojar antes de validar.** `readBytes(length)` creaba el array y recién
después leía, así que el largo solo se validaba fallando la lectura. Dos
llamadores le pasan un `readInt()` crudo del paquete: `GameServerAuth` para el
hex id y `BlowFishKey` para la clave. **Los dos se parsean antes de que la
conexión esté autenticada**, y hasta entonces el cifrado usa una clave blowfish
por defecto que está hardcodeada en el archivo.

Un largo negativo daba `NegativeArraySizeException`; uno cercano a
`Integer.MAX_VALUE` le pedía a la VM un array de dos gigabytes antes de que nada
lo mirara. Eso último es un `OutOfMemoryError`, que es `Error` y no `Exception`,
así que ni el `catch (IOException)` del bucle de lectura ni nada del camino lo
contiene — y no queda acotado a la conexión que lo provocó. Ahora el largo se
compara contra lo que realmente queda en el buffer y se rechaza antes de alojar.

**2 — Una lista compartida sin protección.** `_gameServerList` era un `ArrayList`
sin sincronizar en ningún lado. Lo escribe **el hilo propio de cada game server**
al registrarse (`addServer`, que además borra la entrada previa del mismo id y
después ordena), y lo leen los **hilos de paquetes de los jugadores**
(`createServerList` lo ordena y lo recorre; `isARegisteredServer`,
`getServerIDforHex`, `hasRegisteredGameServerOnId` y `getGameServerStatus` lo
caminan) y el **hilo de eventos de Swing** desde la UI. Un game server
reconectando mientras hay jugadores pidiendo la lista es una modificación
estructural concurrente con iteraciones y un sort.

**3 — Comparar contraseñas por referencia.** El contador anti-fuerza-bruta se
saltea el incremento cuando se reintenta **la misma** contraseña equivocada, para
no banear a quien se equivoca al tipear. El test decía `password != lastPassword`,
que compara referencias: una llega del socket como `String` nueva y la otra sale
de un mapa, nunca son la misma instancia, y el test siempre daba true. La
exención jamás aplicó. Con el `LoginTryBeforeBan = 5` que trae el repo, tipear la
misma contraseña mal cinco veces baneaba la dirección quince minutos — el caso
exacto que el código dice que existe para evitar.

**4 — Contador de flood no atómico.** El hilo de accept lo incrementa y **cada
hilo de cliente** lo decrementa al desconectar. Era un `int` plano, así que esas
lecturas-modificaciones-escrituras se pisaban. Si el contador deriva para arriba,
se empiezan a rechazar conexiones honestas como flood; si deriva para abajo, la
entrada nunca llega al valor que la saca del mapa. Además el camino de accept
releía el campo para cada uno de los tres tests de flood, y `removeFloodProtection`
comparaba con `== 0` exacto, que una entrada ya pasada de largo nunca cumple.

**6 — Un centinela que nunca se produce.** `findFreeID` recorre los ids desde
cero y devuelve el primero libre; si estaban todos ocupados devolvía **cero**, que
el mismo recorrido trata como un id perfectamente válido. Su único llamador
pregunta `if (availableId < 0)` antes de reportar que no quedan ids: esa rama no
podía ejecutarse nunca, y un servidor que llegara con todos los ids tomados se
registraba sobre el id cero, encima del que ya lo tenía.

### Anotado sin tocar

- **El hex id del game server se compara con `Arrays.equals`**, que corta en el
  primer byte distinto, mientras que `isLoginValid` usa `MessageDigest.isEqual`
  —de tiempo constante— para las contraseñas de jugador. Asimetría real, pero el
  beneficio práctico es despreciable frente al jitter de red y no toco
  comparaciones de autenticación por una ganancia teórica.

- **`_lastPassword` guarda la contraseña intentada en texto plano**, indexada por
  dirección, hasta que esa dirección logra entrar. Un digest serviría igual para
  la comparación.

- **`readString()` sin terminador.** Si no encuentra el `0x00`, `indexOf` devuelve
  -1 y el `substring(0, -1)` tira; el `catch` lo traga, la función devuelve el
  buffer restante entero y **`_off` no avanza**, así que el resto del parseo se
  desincroniza. Con el hallazgo 1 arreglado la consecuencia queda contenida.

- **`e.printStackTrace()` en vez del logger** en `isInternalIP`, `ServerList` y
  `readString` — en un camino no autenticado, eso escribe a stdout sin pasar por
  la configuración de logging.

- **`ServerList` hace `InetAddress.getByName()` al armar el paquete.** Si el
  valor configurado es un nombre y no una IP, eso es una resolución DNS en el
  hilo que le responde al jugador.

- **Código muerto que no se toca**: `LoginServer.ForeignConnection` (copia
  duplicada de la clase de flood, `public` y nunca instanciada);
  `LoginServer.unblockIp` (sin llamadores, y el `ipBlocked` al que delega **borra**
  el contador de intentos en vez de levantar el baneo, pese a los dos nombres);
  `LoginController.loginPossible` y `setMaxAllowedOnlinePlayers` con su campo;
  `GameServerTable.status()`, que es el único sitio que indexa
  `STATUS_STRING[status]` con un valor que llega del game server sin validar.

- **`addServer` borra y agrega como dos operaciones separadas**, así que un lector
  en el medio ve la lista sin ese servidor por un instante. Ventana mucho menor
  que la que cerró el hallazgo 2, y unificarlo pide otra estructura.

### Sospechas evaluadas y descartadas

- **`RequestAuthLogin` descifra con offsets fijos** (`doFinal(buffer, 0x01, 0x80)`)
  sobre un buffer del cliente, y sus `catch` son `GeneralSecurityException` e
  `IllegalArgumentException`. Parecía que un paquete corto escapaba por
  `ArrayIndexOutOfBoundsException`. **Verificado corriéndolo** con Java 25:
  `Cipher.doFinal` con offset y largo fuera de rango tira `IllegalArgumentException`,
  que **sí** está atrapada. El mensaje que loguea ("corrupted system") es
  engañoso, pero el manejo es correcto.

- **`readByte()` podría devolver un byte con signo** y dar un id de servidor
  negativo. Enmascara con `& 0xff`.

- **`getGameServerStatus`, `getOnlinePlayerCount` y `getMaxAllowedOnlinePlayers`
  con un id arbitrario del cliente.** Los tres recorren listas y devuelven un
  valor por defecto; ninguno indexa un array.

- **Paquetes fuera de orden.** `LoginPacketHandler` gatea estrictamente por estado
  del cliente: cada opcode solo se acepta en el estado que corresponde.

- **`SessionKey.checkLoginPair` usa `==`.** Son `int`, es correcto.

- **`ServerStatus.STATUS_STRING[status]` del lado del gameserver.**
  `setServerStatus` valida con un `switch` sobre las constantes válidas y tira
  `IllegalArgumentException` para el resto.

- **`_serverNames` es un `HashMap` plano y público.** Solo se escribe al cargar,
  antes de que existan hilos concurrentes.

- **El par de claves de sesión solo se verifica `if (SHOW_LICENCE)`.** Es coherente
  con el protocolo: sin ese paso el par nunca se le emitió al cliente.

- **`RequestAuthLogin` llama a `setAccount` antes de chequear la sesión duplicada**,
  y el `finally` del cliente hace `removeLoginClient(_account)`, lo que podría
  borrar la entrada del cliente legítimo. Es el diseño: `REASON_ACCOUNT_IN_USE` se
  le manda a **los dos**, así que los dos se van.

## `gameserver/ai` — TERMINADA

Son **15** archivos y **7.907** líneas, dominadas por `AttackableAI` (2628),
`CreatureAI` (1605) y `AbstractAI` (850).

**Leído:** `AbstractAI` (estado compartido, candado e intenciones, y el
despachador de acciones), de `AttackableAI` la selección de objetivo
(`thinkAttack`, `checkTarget`, los dos `targetReconsider`, `movementDisable`) y
los handlers de agresión, de `SiegeGuardAI` la elegibilidad de objetivo y la
agresión, y los handlers de `CreatureAI` para aturdimiento, parálisis, sueño,
raíz, silencio y confusión.

### Hallazgos

**1 — `targetReconsider()` no podía reconsiderar nada.** `movementDisable`
termina con `// If cannot cast nor attack, find a new target.` seguido de
`targetReconsider()`. Los dos filtros del método descartaban a todo candidato que
**no** fuera ya el más odiado, así que la única criatura a la que podía llegar era
el objetivo que acababa de fallar. No buscaba uno nuevo: reelegía el viejo y le
sumaba su propio odio encima.

Tres cosas de adentro dicen que la comparación estaba invertida:

- El odio que se le entrega al elegido es `actor.getHating(mostHate)`, que es el
  modismo para volver **a otro** el más odiado. Dárselo al que ya lo tiene no
  hace nada.
- El segundo filtro lleva `obj == getAttackTarget()` **al lado**, que descarta el
  objetivo actual. Eso es lo que el par debía hacer junto, y contradice a una
  cláusula que exige el objetivo actual.
- La misma clase ya tiene un `targetReconsider(boolean)` que recorre bien la
  lista de odio y los alrededores.

Efecto visible: un monstruo que no puede alcanzar ni castear sobre su objetivo
principal ahora se da vuelta contra alguien que sí puede alcanzar, en vez de
quedarse trabado mientras el resto le pega gratis.

**2 — Seis acciones de IA que tiraban excepción en cada aplicación.**
`notifyAction(Action, Object...)` lee `args[0]` en los `case` de las acciones que
llevan una criatura. **Seis** llamadores las notifican sin ningún argumento, así
que el `switch` indexaba un array vacío:

| Origen | Acción |
|---|---|
| `Creature.startStunning()` | `STUNNED` |
| `Creature.startParalyze()` | `PARALYZED` |
| `Confuse` | `CONFUSED` |
| `Sleep` | `SLEEPING` |
| `Root` | `ROOTED` |
| `Mute` y `PhysicalMute` | `MUTED` |

O sea: aturdir, paralizar, dormir, enraizar, silenciar y confundir. Combate
corriente. Y en `startStunning` la notificación **no** es la última sentencia: la
excepción salía antes de `setIntention(IDLE)` y `updateAbnormalEffect()`, así que
la criatura aturdida conservaba su intención anterior y nunca mostraba el efecto.

Toda la cadena tolera un atacante ausente —`addDamageHate` e `isInAggroList`
cortan con null, `onIntentionAttack` rechaza un objetivo nulo, y
`CreatureAI.onActionAttacked` ni mira el parámetro—, así que el arreglo es leer
la criatura solo cuando la hay.

### Error de método propio, y cómo lo encontré

**Arreglé tres de las seis y di el trabajo por terminado.** El grep que las
encontró lo pasé por `head`, se truncó en diez resultados, y leí salida truncada
como si fuera la respuesta completa.

Lo que destapó las otras tres fue **no volver a grepear**: construí la aridad que
el `switch` espera para cada acción y la comparé contra todos los llamadores. Esa
tabla no se puede truncar sin que se note, porque cada fila tiene que aparearse.

**Regla:** cuando la pregunta es "¿están todos?", el resultado no pasa por `head`,
y si se puede, se responde con un conteo apareado en vez de una lista.

**3 — El bucle principal de IA se tragaba todo.** `AttackableAI.onActionThink`
envolvía su `switch` entero en `catch (Exception e)` con la línea de log
**comentada**, así que cualquier falla dentro de `thinkActive`, `thinkAttack` o
`thinkCast` desaparecía sin dejar rastro. Ese es el bucle que corre una vez por
tick por cada atacable del servidor.

Sus dos hermanos no hacen eso: `SiegeGuardAI` y `ControllableMobAI` usan
`try/finally` **sin catch**, así que una falla ahí llega al manejador del pool y
queda registrada. Solo este se la tragaba.

La línea comentada además no habría compilado: ninguna de `AttackableAI`,
`CreatureAI` ni `AbstractAI` declara un `LOGGER`. Es un resto de una versión que
sí lo tenía. Ahora la clase tiene uno y el `catch` loguea con la excepción
adjunta, sin cambiar el flujo.

**A mirar cuando esto corra:** si el warning nuevo aparece en cada tick en vez de
raramente, eso es el hallazgo, no el logueo.

### Anotado sin tocar (`gameserver/ai`)

- **`DoorAI` y `BoatAI` anulan tres de las seis acciones de control de masas.**
  Los dos definen `onActionStunned`, `onActionSleeping` y `onActionRooted` como
  métodos vacíos —puertas y barcos ignoran eso— pero **no** `onActionParalyzed`,
  `onActionConfused` ni `onActionMuted`, que caen a la implementación de
  `CreatureAI` y terminan en `clientStartAutoAttack()` sobre una puerta. Que sea
  alcanzable depende del `targetType` de las skills del datapack y no pude
  demostrarlo, así que seis métodos vacíos por un caso no probado quedan sin
  escribir.

- **`ControllableMobAI.checkAutoAttackCondition` chequea `target.isNpc()` dos
  veces**, la segunda bajo el comentario "Summon hierarchy check: assumes summons
  are not in Npc hierarchy". Es un duplicado literal de la línea de arriba, o sea
  no-op. El estilo del comentario es el mismo marcador de refactor automático que
  aparece en `Clan.java` y `OlympiadGame`.

### Sospechas evaluadas y descartadas (`gameserver/ai`)

- **Huecos en los `switch` de intenciones y acciones.** Comparados los 9 valores
  de `Intention` y los 21 de `Action` contra los `case` del despachador: no falta
  ninguno.

- **Casts sin chequear.** Barridos los 21 sitios del paquete que encadenan
  `.asPlayer()`, `.asMonster()`, `.asAttackable()` y compañía: todos tienen su
  `isX()` inmediatamente antes, o el tipo está garantizado por la jerarquía
  (`ControllableMob` es `Monster`, `RaidBoss` es `Monster`, el `_actor` de
  `AttackableAI` es `Npc`).

- **`_intentionArg0` y `_intentionArg1` son no-`volatile`** y se escriben bajo el
  candado. Se leen solo en `PlayerAI.changeIntention`, que toma el mismo candado
  antes. Consistente.

- **`AttackableAI` no maneja `onActionAggression` con objetivo nulo** mientras que
  `SiegeGuardAI` sí. Los seis llamadores pasan un objetivo no nulo, y donde puede
  ser nulo el aggro positivo vuelve no-op a la rama de `SiegeGuardAI`.

- **División por `dist` en `CreatureAI`** cuando el actor ya está en el destino.
  Es división de `double`: da `NaN`, y `(int) NaN` es 0, así que degrada a "moverse
  a donde ya estás".

- **La convergencia de `_globalAggro`** clampea a cero desde los dos lados y solo
  avanza su timestamp cuando pasó al menos un segundo, así que no pierde tiempo en
  ticks sub-segundo.

### Segunda vuelta: `CreatureAI` y `PlayerAI`

**9 — `onIntentionActive()` soltaba la intención pero no los objetivos.** El
método pone la intención en ACTIVE, y ACTIVE significa por definición que el
actor no tiene objetivo. Después hay una salida temprana cuando la región del
actor y sus vecinas están inactivas —una optimización para no pensar donde no
hay nadie mirando— y esa salida quedaba **antes** de `setCastTarget(null)` y
`setAttackTarget(null)`. Un actor cuyos alrededores se vaciaron se quedaba con
las dos referencias vivas mientras su intención decía lo contrario.

Los `null` ahora van antes de la puerta; los paquetes y el `onActionThink()`
quedan atrás, que es para lo que la puerta existe realmente.

**10 — El casteo diferido no revalidaba nada.** `onIntentionCast` tiene un
preámbulo que rechaza el casteo si el actor está descansando. Si el actor está en
el retroceso de un disparo de arco, en vez de castear agenda un `CastTask` para
cuando ese temporizador expire — y ese task llamaba `changeIntentionToCast`
directo, salteándose el preámbulo entero. Forzaba la intención aunque en el
ínterin el actor se hubiera sentado o **muerto**.

Ahora descarta el casteo si la criatura murió y si no reentra por
`onIntentionCast`, de modo que el camino diferido pasa por la misma puerta que el
inmediato. El temporizador de arco que el task esperó ya expiró, así que no puede
rebotar a agendarse otra vez.

**11 — Tres de los cuatro `_thinking` sin `volatile`.** `AttackableAI`,
`PlayerAI`, `SiegeGuardAI` y `SummonAI` llevan una bandera con semántica
idéntica: se chequea al entrar a `onActionThink`, se prende, y se apaga en un
`finally`. **`SummonAI` la declara `volatile`; los otros tres no.** Esa
diferencia solitaria es la evidencia: el mismo razonamiento que produjo el
`volatile` en los summons vale igual para los hermanos.

`onActionThink` se alcanza desde hilos de paquetes y desde el task manager de IA,
así que la bandera es compartida de verdad. Sin `volatile`, una lectura vieja o
deja entrar dos disparos a la vez al bucle de pensamiento —justo lo que la
bandera existe para impedir— o deja a una criatura viendo un `true` permanente y
sin volver a pensar nunca.

**12 — `PlayerAI._nextIntention` leído sin el candado que su propia clase toma.**
`changeIntention` toma `_aiLock` para tocar ese campo, lo que ya establece que la
clase lo considera disputado. Los otros cinco accesos lo ignoran, y dos de ellos
chequean null y después lo desreferencian tres veces más.
`onActionFinishCasting` se notifica desde el task de fin de casteo mientras
`onActionCancel` corre en el hilo de paquetes del jugador: un null puesto entre
el chequeo y una lectura es un NPE. Los dos sitios ahora sacan una foto a una
local, y el campo es `volatile` para que la foto sea una lectura coherente.

**13 — `onActionReadyToAct` limpiaba `_nextIntention` después y no antes.** Para
un casteo *toggle*, `changeIntention` guarda la intención actual **en ese mismo
campo** para restaurarla cuando el casteo termine. Limpiarlo después de llamar a
`setIntention` borraba lo que `changeIntention` acababa de guardar. Limpiarlo
antes de consumirlo lo marca usado igual y deja la restauración intacta.

### Anotado sin tocar (segunda vuelta)

- **Cinco de los seis `onIntention*` de `CreatureAI` no chequean `_actor.isDead()`
  y `onIntentionFollow` sí.** Es el patrón de "guard angosto delata la clase" que
  ya rindió dos veces, pero acá no pude construir el camino concreto por el que
  llega una intención a un cadáver, y agregar el chequeo a cinco handlers sin
  ese camino es exactamente el tipo de cambio que compila, se lee obviamente
  correcto y no lo agarra ningún test. Queda anotado, no aplicado.

- **`onIntentionPickUp` y `onIntentionInteract` llaman a `clientStopAutoAttack()`
  antes de sus salidas tempranas**, así que interactuar con algo con lo que ya
  estás interactuando —o levantar un ítem que ya no está en el piso— te corta el
  auto-ataque sin hacer nada más.

- **`CreatureFollowTaskManager.follow()` traga toda excepción con un `catch`
  vacío.** Corre cada 500 ms por criatura, así que loguear ahí es un riesgo real
  de spam; distinto del caso de `AttackableAI.onActionThink`, que ya se logueó.

- **`RespawnTaskManager` hace `spawn._scheduledCount--`** sobre un campo público
  no atómico, y solo cuando `spawn != null`.

### Sospechas evaluadas y descartadas (segunda vuelta)

- **Fuga en los mapas de seguimiento.** `follow()` decide "dejar de seguir" en dos
  ramas y en las dos solo llama `ai.setIntention(Intention.IDLE)`, sin sacar la
  criatura del mapa; `onIntentionIdle` tampoco llama `stopFollow()`. Parecía una
  fuga permanente que además retiene la `Creature` viva. **Falso:**
  `AbstractAI.setIntention` llama `stopFollow()` para toda intención que no sea
  `FOLLOW` ni `ATTACK`, o sea que la baja está en la puerta de entrada, no en el
  handler.

- **`onActionForgetObject` dejando referencias colgadas.** La IA guarda cuatro
  (`_target`, `_attackTarget`, `_castTarget`, `_followTarget`) y las limpia a las
  cuatro, más el caso de que el objeto olvidado sea el propio actor.

- **`maybeMoveToPosition` dividiendo por cero.** `dist` no puede ser 0 dentro de
  esa rama: el guard `isInsideRadius2D` la excluye. Por el mismo guard,
  `dist - (offset - 5)` siempre queda positivo.

- **`checkTargetLostOrDead` contradiciendo su javadoc**, que promete "false si es
  fakedeath" sin que se vea el chequeo. Usa `isDead()` y no `isAlikeDead()`, y el
  fakedeath está vivo, así que el comportamiento es el que el javadoc describe.

- **`thinkInteract` pasando un no-`Creature` a `doInteract`.** El guard excluye
  `StaticObject` pero no otros tipos; `WorldObject.asCreature()` devuelve `null`
  en la clase base en vez de tirar, y `doInteract` chequea null. No-op silencioso.

- **`_nextIntention` retenido después de un `return` temprano en los cuatro
  bucles de `onActionThink`.** Los cuatro apagan `_thinking` en un `finally`.

## `taskmanagers` — TERMINADA

Son **18** archivos y **3.000** líneas. No estaba en el mapa original; se llegó
tirando del hilo del seguimiento desde `AbstractAI.startFollow`.

**Leído:** los 12 task managers que llevan bandera de reentrada, entero cada uno.

### Hallazgos

| # | Archivo | Defecto | Severidad |
|---|---|---|---|
| 1 | 12 archivos | la bandera de reentrada no se resetea en `finally` | **crítica** |
| 2 | 12 archivos | esa misma bandera no es `volatile` | alta |

**1 — Una excepción mata el subsistema hasta reiniciar.** Cada task manager del
paquete protege su `run()` recurrente con un `boolean static` que se prende al
entrar y se apaga al salir. Son **14 banderas en 12 archivos**, y ninguna se
apagaba en un `finally`.

**10 de los 12 no tienen `try/catch` en ninguna parte.** Una excepción lanzada
dentro del bucle sale de `run()` y la absorbe `ThreadPool.RunnableWrapper`, que
atrapa `Throwable` sin relanzar para que la agenda sobreviva — pero la bandera
nunca se limpia. Cada disparo posterior vuelve en seco en el guard y ese
subsistema queda muerto para todo el servidor, en silencio, hasta reiniciar.

`RespawnTaskManager` es el caso más filoso: hace `iterator.remove()` **antes** de
`spawn.respawnNpc(npc)`, así que un solo respawn que falle pierde ese NPC *y*
detiene todos los respawns futuros del servidor. `DecayTaskManager` (los cadáveres
no vuelven a desaparecer), `ItemLifeTimeTaskManager` (los ítems temporales no
vuelven a vencer), `PvpFlagTaskManager` (las banderas de PvP no vuelven a
bajarse) y `PlayerAutoSaveTaskManager` (ningún jugador vuelve a autoguardarse)
fallan del mismo modo.

Verificado que ningún cuerpo tenía un `return` temprano —solo dos `continue`—,
así que el bloqueo requería una excepción y no era determinista.

**2 — La misma bandera, sin `volatile`.** Se escribe y se lee desde hilos
distintos del pool, así que una lectura vieja o deja solaparse dos disparos
—exactamente lo que el guard existe para impedir— o se saltea un tick.

Las 14 quedaron `volatile` y limpiadas en un `finally`. Se verificó con un diff
insensible al whitespace que no cambió ninguna sentencia: los únicos cambios son
la palabra `volatile` y los envoltorios `try/finally`.

### Método

Esta fue la tercera vez que la tabla sistemática rindió más que el grep. El
defecto apareció leyendo **un** archivo (`CreatureFollowTaskManager`), y la
pregunta que lo convirtió en 14 no fue "¿hay más?" sino la tabla explícita de
*declaración × `volatile` × `finally`* sobre los 12 archivos del paquete: 14
declaraciones, 0 `volatile`, 0 `finally`. Uniforme. Un grep por `_working` habría
mostrado los sitios sin mostrar que **ninguno** estaba protegido.

## `commons` — en curso

Son **47** archivos y **11.240** líneas. Es infraestructura compartida: cada
defecto acá se multiplica por todo el servidor.

**Leído:** `network/` entero salvo los buffers de escritura —`ResourcePool`,
`BufferPool`, `Connection`, `ReadHandler`, `PacketExecutor`, `ConnectionConfig`,
`ReadablePacket`, `ReadableBuffer`—, `threads/ThreadPool`,
`database/DatabaseFactory`, la familia de parseo de `util/IXmlReader` y los
ayudantes de `util/StringUtil`.

**Pendiente:** `ConfigReader`, `time/TimeUtil`, `time/SchedulingPattern`,
`util/DeadlockWatcher`, `crypt/` (`BlowfishEngine`, `NewCrypt`), `util/BCrypt`,
`ui/`, y los buffers de escritura (`DynamicPacketBuffer`, `ArrayPacketBuffer`,
`WritablePacket`, `BaseWritablePacket`).

### Hallazgos

| # | Archivo | Defecto | Severidad |
|---|---|---|---|
| 1 | `ResourcePool` | el largo declarado por el par remoto hace crecer el índice de pools sin techo | **crítica** |
| 2 | `ResourcePool` | `TreeMap` plano mutado en caliente por un pool de hilos sin cota | alta |
| 3 | `Connection` | los buffers se reciclan **antes** de soltar el campo que los apunta | alta |
| 4 | `ResourcePool` | dos hilos creando el pool del mismo tamaño se pisaban | media |
| 5 | `BufferPool` | `isFull()` recorre la cola entera, una vez por buffer pedido | media |
| 6 | `ThreadPool` | el pool de alta prioridad no recibe ninguna de las tres configuraciones de su hermano | media |
| 7 | `DatabaseFactory` | `getConnection` tira NPE en vez de la excepción que documenta | baja |

**1 — Un largo de la red haciendo crecer una estructura permanente.**
`ReadHandler.handleHeader` toma el largo del paquete del cable con
`Short.toUnsignedInt`, le resta los dos bytes de cabecera y se lo pasa a
`readPayload`, que le pide a `ResourcePool` un buffer de ese tamaño. Los dos
caminos de búsqueda respondían a un tamaño que ningún pool configurado cubría
**construyendo un pool para él** y metiéndolo en el mapa compartido.

El mapa es uno por servidor y permanente. El `Network.ini` que se distribuye
define pools hasta 32768 y el campo de cabecera llega a 65533, así que cada largo
distinto por encima del pool más grande agregaba una entrada que no se borra
nunca. Ese mismo archivo pone `BufferPool.InitFactor = 2`, que hace que cada pool
nuevo aloje **diez buffers directos de su tamaño** al crearse, y después siga
aceptando más a medida que se reciclan. Es memoria fuera del heap cuyo
crecimiento lo dirige un valor que elige el par.

Ninguno de los dos caminos registra pools ya. Cuando nada cubre el tamaño pedido
el llamador recibe un buffer de un solo uso; `recycleBuffer` busca la capacidad,
no encuentra pool y lo suelta, así que se colecta normalmente. El tráfico dentro
de los tamaños configurados no cambia y se sigue pooleando.

**2 — Un árbol rojo-negro reestructurado bajo lecturas concurrentes.**
`ConnectionConfig` arma un `ResourcePool` por servidor y **todas** las conexiones
lo comparten. `ConnectionManager` le da al grupo de canales asincrónicos un
`ThreadPoolExecutor` con máximo `Integer.MAX_VALUE`, así que los handlers de
lectura y escritura corren sobre un conjunto de hilos sin cota y cada uno llega a
este objeto en cada paquete. El índice era un `TreeMap` plano al que los dos
caminos del hallazgo 1 le hacían `put` en caliente. Un recorrido que cruza una
rotación puede devolver el pool equivocado, o seguir un enlace ya reescrito y
girar: un hilo de red clavado que no vuelve a atender su conexión. Quedó
`ConcurrentSkipListMap`, que es el `NavigableMap` concurrente.

**3 — Reciclar antes de soltar.** `releaseReadingBuffer` devolvía el buffer al
pool y **después** limpiaba el campo. Entre esas dos sentencias el buffer ya está
disponible para otra conexión mientras ésta todavía lo apunta y puede tener una
lectura en vuelo hacia él. Dos clientes escribiendo la misma memoria es
corrupción de paquetes y filtración entre conexiones. `releaseWritingBuffer`
tenía la misma forma a lo largo de todo su bucle. Los dos sueltan primero y
reciclan después.

### Anotado sin tocar (`commons`)

- **`IXmlReader.parseInt` es el impar de una familia de nueve.** Cada tipo tiene
  cuatro sobrecargas que devuelven el tipo envuelto; `parseInt` tiene dos y
  devuelve primitivo, así que es la única que puede tirar NPE al desempaquetar un
  default nulo. Barridos los llamadores: ninguno le pasa un default que pueda ser
  nulo.

- **Los enteros usan `decode` y los flotantes `valueOf`.** `Integer.decode("010")`
  es **8**, no 10, y `decode("08")` tira. Buscados los 1447 XML de datos: no hay
  ni un atributo numérico con cero a la izquierda, ni ninguno hexadecimal que
  justifique `decode`. Peligro latente para quien edite datos, no un bug vivo.

- **`StringUtil.isNumeric` no garantiza que el valor entre en un `int`.** Es la
  misma debilidad que en `Q00631`. Sus 94 llamadores están dominados por
  `ClassBalanceConfig`, que quedó arreglado.

- **`DatabaseFactory.testDatabaseConnections` no prueba lo que dice.** Su javadoc
  dice verificar que el pool llega a su máximo, pero pide y devuelve **una**
  conexión por iteración en vez de sostenerlas, así que re-prueba la misma N veces
  y siempre reporta éxito total. Eso vuelve inalcanzable a `adjustPoolSize`, que a
  su vez tiene su propio problema: su piso de 20 puede **subir** el máximo por
  encima de la cantidad de conexiones que realmente funcionaron, que es lo
  contrario de para lo que existe. Hacerlo bien exige sostener N conexiones
  simultáneas en el arranque, que es una decisión de despliegue y no un arreglo de
  bug; además `TestDatabaseConnections` viene en `false` y ningún `.ini`
  distribuido lo activa.

- **`ReadablePacket.readSizedString` aloja antes de validar**, igual que el
  hallazgo 1 del loginserver, pero acá el largo sale de un `readShort()` y queda
  acotado a 65534, así que no escala. Su `catch (Exception ignored)` sí deja el
  buffer parcialmente avanzado y desincroniza el resto del parseo.

### Sospechas evaluadas y descartadas (`commons`)

- **`handleHeader` con `dataSize <= 0` llamando a `client.read()` sobre un buffer
  ya consumido**, lo que parecía un `_channel.read` de cero bytes en bucle
  infinito. `Client.read()` va a `Connection.readHeader()`, que **libera** el
  buffer y pide uno nuevo.

- **`resumeRead` sin avanzar cuando `bytesRead == 0`.** Para llegar ahí el buffer
  tendría que no tener espacio, y en ese caso `_expectedReadSize` ya es 0 y la
  condición que llama a `resumeRead` no se cumple.

- **Excepción de un paquete malformado escapando.** `parseAndExecutePacket`
  envuelve el parseo y el `read()` en un `try` que desconecta al cliente. La
  contención es por conexión.

- **`DynamicPacketBuffer` sin control de límites.** Tira
  `IndexOutOfBoundsException` desde tres sitios distintos.

- **`RejectedExecutionHandlerImpl` corriendo una tarea diferida de inmediato.**
  `ScheduledThreadPoolExecutor` usa una cola sin cota, así que solo rechaza
  después del apagado, y el handler chequea `isShutdown()` y vuelve.

## `gameserver/config` — TERMINADA

Son **55** archivos y **5.554** líneas. No estaba en el mapa; se llegó desde los
94 llamadores de `StringUtil.isNumeric`.

### Hallazgos

| # | Archivo | Defecto | Severidad |
|---|---|---|---|
| 1 | `ClassBalanceConfig` | tres caminos de excepción sin guarda, ×37 bloques copiados | alta |
| 2 | `GeneralConfig` | un `catch` que no puede atrapar lo único que su bloque tira | alta |
| 3 | 4 archivos | `Enum.valueOf` sin guarda, sensible a mayúsculas | alta |
| 4 | `ClassBalance.ini` | la línea que se distribuye está malformada y se ignora en silencio | media |

**1 — El mismo bloque, treinta y siete veces, sin validar nada.**
`ClassBalanceConfig` era el mismo bloque de catorce líneas repetido una vez por
tabla de multiplicadores, distinto solo en el nombre de la propiedad y el array.
El **83 %** del archivo era ese bloque. Ninguna copia validaba nada:

```java
ARRAY[StringUtil.isNumeric(id) ? Integer.parseInt(id)
    : Enum.valueOf(PlayerClass.class, id).getId()] = Float.parseFloat(...);
```

Un nombre de clase desconocido tira desde `Enum.valueOf`. Un id numérico más allá
del final de la tabla tira desde el store del array — y como `isNumeric` solo
prueba que todos los caracteres sean dígitos, una tira larga de dígitos llega a
`Integer.parseInt` y tira ahí antes. Un multiplicador que no es número tira desde
`parseFloat`.

Nada entre acá y el constructor de `GameServer` atrapa ninguna de las tres.
`ConfigLoader.init()` llama a los cargadores en línea recta, así que **una sola
entrada mal escrita en este archivo opcional impide arrancar el servidor**, con
un mensaje que no nombra ni la propiedad ni la entrada, y los ~30 configs
listados después nunca se cargan.

Los 37 bloques son ahora una llamada a un ayudante que llena la tabla con 1f
neutro y después valida cada entrada por separado, logueando y salteando la mala
con el nombre de la propiedad y el texto que falló. El archivo pasó de 636 a 199
líneas y el comportamiento para toda entrada válida es idéntico.

**2 — Manejo de errores inerte.** La lista de canales de chat baneados ya tenía
un `try/catch` escrito para exactamente esto, y el `catch` es
`NumberFormatException`. `NumberFormatException` es **subclase** de
`IllegalArgumentException`, que es lo que tira `Enum.valueOf`: una subclase no
puede atrapar a su superclase, así que ese `catch` nunca pudo dispararse por lo
único del bloque que tira, y su línea de log jamás se imprimió.

Además, aun con el tipo correcto, la primera entrada mala descartaba lo que el
bucle ya había juntado. Ahora se chequea canal por canal.

**3 — `Enum.valueOf` es sensible a mayúsculas.** Escribir `GlobalChat = on` en vez
de `ON` alcanzaba para no poder arrancar. `PlayerConfig` era el **único** de los
seis sitios que pasaba su entrada a mayúsculas antes — ése es el delator: alguien
chocó con esto una vez y arregló el sitio que tenía adelante. Esa llamada igual
tiraba con un valor que no fuera un método de corte, y su `toUpperCase` no tenía
locale; quedó fijado a `Locale.ROOT`.

Los cinco restantes pasan por `StringUtil.isEnum`, que existe justo para esto y
no se usaba en ninguna parte.

**4 — Una línea distribuida que nunca hizo nada.** El `ClassBalance.ini` que se
distribuye trae `PvpMagicalSkillDamageMultipliers = 0.85`. El formato, documentado
dos líneas más arriba **en ese mismo archivo**, es
`ELVEN_FIGHTER*2;PALUS_KNIGHT*2.5`, así que un número pelado no tiene clase a la
cual aplicarse: parte en un pedazo en vez de dos y siempre se salteó en silencio.
Quedó en blanco —que es exactamente lo que ya hacía— para que el arreglo del
hallazgo 1 no lo convierta en un warning en cada arranque.

### Método

El hallazgo 3 salió de preguntar **dónde más aparece el patrón** después de
arreglar `ClassBalanceConfig`, no de leer los 55 archivos. Un grep por
`Enum.valueOf` en el paquete dio seis sitios; de esos seis, uno tenía defensa
parcial (`toUpperCase`) y otro tenía defensa inerte (el `catch` imposible). Los
dos son la misma señal que ya rindió tres veces: **una guarda angosta delata a
toda la clase**.
