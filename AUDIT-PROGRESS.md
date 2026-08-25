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
| ~~`model/skill`~~ | 19 | 3.869 | **TERMINADA** |
| **`model/olympiad`** | **7** | **4.554** | **en curso — 13 bugs arreglados** |
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

## `model/olympiad` — en curso

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
