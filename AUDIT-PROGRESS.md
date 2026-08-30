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

## Resumen ejecutivo

**Estado: las 21 áreas del mapa están cerradas.** 2.342 archivos, ~434.000 líneas.
El núcleo y el datapack compilan; la suite de tests pasa (47, de los 31 que había).

### Los diez hallazgos que más cuestan si no se arreglan

Ordenados por lo que le pasa al jugador, no por dificultad técnica.

| # | dónde | qué ocurría |
|---|---|---|
| 1 | `ItemContainer.addItem` | dos depósitos simultáneos al mismo apilable **perdían la mitad**; medido con un test contra la versión rota: 8.519 de 17.000 |
| 2 | `Spawn` + `RespawnTaskManager` | un decremento perdido dejaba el contador clavado y **ese spawn no volvía a aparecer** en lo que durara el servidor |
| 3 | `ClanHallAuction.endAuction` | borraba la subasta y **después** desreferenciaba: sala sin dueño con la adena del ganador ya cobrada |
| 4 | `PlayerVariables` y dos más | doble desbloqueo: el fallo de base se sepultaba a sí mismo y **las variables sin guardar se perdían** |
| 5 | `DropProtection.protect` | validaba tras escribir: el ítem quedaba protegido sin dueño, **imposible de recoger para siempre** |
| 6 | `QuestState._vars` | mapa liso construido con pereza: dos hilos podían **perder un juego entero de variables** |
| 7 | `DocumentBase.parseLogicNot` | envolvía un nulo que el propio parser fabrica, y lo desreferenciaba **en cada prueba de esa habilidad** |
| 8 | `WorldObject.spawnMe` | no comprobaba la región que `getRegion` devuelve nula a propósito |
| 9 | `OlympiadGame` | el daño que decide el combate, acumulado sin candado |
| 10 | `World.addFactionPlayerToWorld` | un jugador podía quedar en **las dos facciones** y anclar su `Player` al salir |

**El patrón que los une:** casi ninguno es descuido genérico. Son **intención a
medio aplicar** — clases que sí tienen candado, atómico o colecciones concurrentes
y dejaron uno o dos campos fuera. Por eso se pueden señalar sin discutir cuál era
la intención: la propia clase la declara.

### Una sola raíz explica ~80 hallazgos de contenido

Las 53 páginas de los maestros de aldea 32092–32098, los 22 ítems del bingo y del
almacén de castillo, las 5 skills inertes y los 10 multisells que no abren **no son
ochenta errores**. Son un hecho: **el código es de una crónica posterior al dato
distribuido**.

Y se comprobó lo contrario: **82.950 referencias del dato contra sí mismo**
—botín, skills de npc, multisells, buylists— **y ninguna rota**. Revisados después
los otros 610 ficheros de dato, aparecen **133 rotas** en tres —pesca, skills de
mascota y semillas del manor—, y también apuntan al mismo hueco: el grado de pesca
difícil trajo sus 81 peces y ninguno de sus ítems. Las **skills dan 0 de 14.290**,
que es la señal de que lo que falta es contenido nuevo, no mecánica.

**La decisión no es "arreglar ochenta cosas", es "qué crónica lleva este
servidor"**, y de ahí sale si hay que añadir contenido o quitar ramas. No es una
decisión de quien audita.

### Lo que está verificado y lo que no

- **Verificado por test contra la versión rota**: el candado del contenedor de
  ítems, los contadores de spawn, el `<not>` del parser, el nulo de `State`, el
  desempaquetado de `EffectZone`. Regla: *un test que también pasa con el defecto
  puesto vale lo mismo que un barrido ciego*.
- **Verificado solo por lectura**: `QuestState` y el doble desbloqueo de las tres
  clases de variables. Sus constructores exigen `Player` real o base de datos, y en
  `test/libs` solo hay JUnit y hamcrest.
- **Nunca ejecutado**: el servidor. Todo esto es compilación más 47 tests.

### Decisiones abiertas, que son tuyas

1. **`buffDebuffMod`** arrancaba en 1 y no en 0; corregido. **Mueve un punto cada
   probabilidad de prender** de todo buff y debuff. Se puede revertir en un commit.
1. **La skill 4522 "Eye of Assassin"** declaraba dos condiciones hermanas donde el
   parser solo lee la primera, así que sus dos bonos de crítico se aplicaban **desde
   cualquier ángulo**. Corregido con `<and>`: ahora exigen estar por la espalda, que
   es lo que el dato pedía. También mueve números.
2. **La crónica**: añadir el contenido que falta o quitar las ramas muertas.
3. **`IdManager`** sigue arrancando tras un fallo de inicialización, con el espacio
   de ids a medio construir — riesgo de colisiones. Abortar el arranque sería
   inventar un mecanismo que ningún gestor tiene.
4. **Un guardado fallido de variables descarta los cambios** en vez de reintentar.
   Reintentar exige un `INSERT ... ON DUPLICATE KEY UPDATE` que hoy no está.
5. **`Q00627` línea 142** pide una página inexistente en la rama "no tienes la
   gema". Apuntarla a la de recompensa sería peor; la que se quiso no está.

### Método, en una línea

**Enumerar la población y arreglar solo a quien le falta la guarda que llevan los
demás** — y cuando la proporción se da vuelta (37 a 6, 1.313 a 6, 46 a 0), no hay
clase: es el patrón de la casa y el barrido se descarta entero. Cada barrido se
valida **contra el fichero anterior al arreglo**; tres dieron ceros falsos y se
corrigieron antes de creerles.

---

## Índice

- **Mapa y estado** — la tabla de las 21 áreas
- **Áreas**, en el orden en que se cerraron: `model/itemcontainer`, `model/clan`,
  `model/skill`, `model/olympiad`, `loginserver`, `gameserver/ai`, `taskmanagers`,
  `network/serverpackets`, `data`, `managers`, datapack `ai/`, `commons`,
  `gameserver/config`, `network/clientpackets`, datapack `handlers/`,
  `model/actor`, `model/stats`, `model/conditions`, `model/script`, `model` (raíz),
  el resto fuera del mapa
- **Auditar la auditoría** — si los barridos tardíos cubrieron las áreas viejas, y
  la suite de tests que existía y no se había corrido
- **Tests para los invariantes que esta auditoría cambió** — los cinco, con sus
  números contra la versión rota
- **`quests/` — las clases decidibles** — páginas, ids, skills, coordenadas,
  tiendas, ítems que se quedan pegados, y el dato contra sí mismo

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
| ~~`commons`~~ | 47 | 11.240 | **TERMINADA** |
| ~~datapack `ai/`~~ | 67 | 14.462 | **TERMINADA** |
| ~~`managers`~~ | 44 | 14.636 | **TERMINADA** |
| ~~`data`~~ | 71 | 14.922 | **TERMINADA** |
| ~~`network/serverpackets`~~ | 259 | 19.900 | **TERMINADA** |
| ~~`network/clientpackets`~~ | 201 | 21.727 | **TERMINADA** |
| ~~datapack `handlers/`~~ | 375 | 51.203 | **TERMINADA** |
| ~~`model/actor`~~ | 166 | 53.236 | **TERMINADA** |
| ~~`model/stats`~~ | 7 | 2.663 | **TERMINADA** |
| ~~`model/conditions`~~ | 80 | 4.989 | **TERMINADA** |
| ~~`model/script`~~ | 9 | 7.622 | **TERMINADA** |
| ~~`model` (raiz)~~ | 58 | 14.546 | **TERMINADA** |
| ~~resto fuera de mapa~~ | 408 | 68.882 | **TERMINADA** |
| `quests/` | 296 | 77.828 | **clases decidibles cerradas** |

Orden elegido: de menor a mayor, salvo que la criticidad mande. Se empezó por
`itemcontainer` porque es chico y es donde vive la duplicación de ítems, que es
la única clase de bug que puede arruinar una economía de forma irreversible.

### El mapa estaba incompleto

Medido con los quince primeros cerrados: el repositorio tiene **2.342** archivos
y **434.459** líneas, y las áreas que este mapa listaba cubrían **1.703**. Los
otros **639 archivos y 114.775 líneas nunca estuvieron en el plan** — entre ellos
`model/stats/Formulas` (la matemática de combate entera), los **80** archivos de
`model/conditions` que deciden si una habilidad puede usarse, el motor de quests
de `model/script`, las zonas, el asedio y `Item.java`.

No están vírgenes: **once** barridos corrieron sobre todo el repositorio y no solo
sobre el área en curso —comparadores rotos, `split` indexado, índices de parámetro
de sentencias preparadas, productos sin ensanchar, `ClanTable.getClan` sin
comprobar, retiros de ítem con retorno descartado, aritmética de redondeo, mapas
concurrentes indexados con otra búsqueda, comparaciones de identidad, y las dos de
búsqueda repetida—, y varios arreglos ya cayeron ahí dentro. Pero como áreas
leídas, faltan.

Van antes de `quests/`: son código de núcleo que corre en cada combate y cada
habilidad, mientras que los quests son en su mayoría guiones repetidos que rinden
más por barrido que por lectura.

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


## `network/serverpackets` — TERMINADA

Son **259** archivos y **19.900** líneas: los escritores de paquetes salientes.


### Hallazgos

| # | Archivo | Defecto | Severidad |
|---|---|---|---|
| 1 | `ClanTable` / `AllianceInfo` | destruir el clan líder de una alianza deja a sus aliados con un id que no resuelve | alta |

**1 — Una alianza que sobrevive a su líder.** `Clan.dissolveAlly` recorre cada clan
miembro y le limpia el id de alianza, el nombre y la penalidad. `destroyClan`
**nunca lo llama**, y sin embargo maneja todo lo demás: asedios, clan halls, una
puja de subasta pendiente, el almacén, los miembros, los dos mapas de búsqueda y
seis tablas de la base.

Así que disolver el clan que lidera una alianza deja a cada aliado con un id de
alianza que no resuelve a ningún clan. Y no solo en memoria: a esos aliados
tampoco se les llama `updateClanInDB`, así que `clan_data` conserva el id viejo y
vuelve después de reiniciar.

Donde se manifiesta es `AllianceInfo`, cuyo constructor son dos líneas:

```java
final Clan leader = ClanTable.getInstance().getClan(allianceId);
_name = leader.getAllyName();
```

`RequestAllyInfo` solo comprueba que el id sea positivo antes de construir eso,
así que un aliado de un líder disuelto recibe un NPE en vez de su ventana de
alianza. El ejecutor de paquetes lo atrapa, o sea que cuesta una traza en el log
y ninguna respuesta, pero el estado de abajo sigue mal. Se arregló el origen —
`destroyClan` desengancha la alianza con los mismos tres campos que usa
`dissolveAlly`— y el síntoma, porque los `clan_data` de un servidor que ya está
corriendo pueden traerlos.

### Tercera vuelta: un comparador que rompe su contrato

| # | Archivo | Defecto | Severidad |
|---|---|---|---|
| 5 | `SortedWareHouseWithdrawalList` | el comparador de recetas dice que `a > b` y `b > a` a la vez | alta |

**5 — Un test unilateral sobre dos argumentos.** `WarehouseItemRecipeComparator`
resuelve los dos ítems a un `RecipeList` y después los prueba de a uno:

```java
if (rp1 == null) { return (order == A2Z ? A2Z : Z2A); }
if (rp2 == null) { return (order == A2Z ? Z2A : A2Z); }
```

Cuando **ninguno** de los dos tiene receta, el primer test dispara en las dos
direcciones, así que `compare(a, b)` y `compare(b, a)` vuelven con el mismo signo.

Medido de tres maneras en vez de afirmado.

*La asimetría*: con dos ítems sin receta la lógica vieja contesta `1` en las dos
direcciones; la nueva contesta `0` en las dos.

*Que es alcanzable*: de los **849** ítems de tipo receta en los datos
distribuidos, **68** no tienen entrada en `Recipes.xml`. Dos de ésos en un mismo
almacén es toda la precondición.

*Que cuesta algo*: `Collections.sort` es TimSort, que detecta comparadores rotos y
lanza `IllegalArgumentException`. Sobre **43.314** ordenamientos generados,
**17.371 lanzaron** — el 40 %. El caso más chico que lanza es **32 ítems con 2 sin
receta**, que es exactamente el umbral de TimSort: por debajo de 32 usa inserción
binaria y no lo nota. Un almacén de 32 ítems es corriente, y la excepción sale de
`writeImpl`, así que el jugador no recibe ninguna lista.

Devolver "iguales" es además la respuesta correcta por sí sola: el llamador ordena
por nombre primero y este ordenamiento es estable, así que dos ítems sin receta
conservan su orden por nombre.

### Sospechas evaluadas y descartadas (tercera vuelta)

- **El resto de los comparadores del archivo** (nombre, grado, tipo, parte del
  cuerpo). Ninguno desreferencia nada opcional; sus reglas para el dinero son
  antisimétricas porque `A2Z` y `Z2A` son `1` y `-1`.

- **Comparadores rotos en otra parte.** Barrido todo el core y el datapack
  buscando cuerpos de `compare()` con un test unilateral que devuelve una
  constante: **un solo acierto**, el que se arregló acá.

### Segunda vuelta: el barrido de conteo declarado

El bug característico de un paquete saliente es prometer N elementos y escribir
otra cantidad: el cliente lee un paquete malformado, no un campo faltante.
Barrido sobre los 259: **una cantidad escrita desde el tamaño de una colección,
seguida de un bucle sobre esa colección que contiene un `continue`**. Siete
candidatos, **tres defectos**.

| # | Archivo | Defecto | Severidad |
|---|---|---|---|
| 2 | `SystemMessage` | promete los parámetros declarados y escribe solo los que existen | alta |
| 3 | `ConfirmDlg` | la misma forma | media |
| 4 | `NewCharacterSuccess` | la misma forma | media |

**2 — Un array construido para quedar incompleto.**

```java
_params = _smId.getParamCount() > 0 ? new SMParam[_smId.getParamCount()] : EMPTY_PARAM_ARRAY;
```

Se dimensiona con la cantidad de parámetros que **declara** el id de mensaje, y
se va llenando a medida que se llaman `addString`, `addInt` y compañía. Un
llamador que aporte menos de los declarados deja nulos en la cola. Y el bucle **ya
lo sabe**: loguea *"Found null parameter for SystemMessageId"* antes de saltearlo,
o sea que el caso se observó — y el conteo de arriba se dejó prometiéndolos igual.

La clase además **ya tiene el número correcto**: quince líneas más arriba, la
rama de localización recorre `_paramIndex`, la cantidad realmente agregada, en
vez del largo del array. Dos lecturas del mismo array en el mismo método, una
correcta.

Importa porque `SystemMessage` está entre los paquetes más enviados del servidor.

### Sospechas evaluadas y descartadas (segunda vuelta)

- **Los otros cuatro candidatos del barrido**, cada uno por su propia razón.
  `QuestList` y `GmViewQuestInfo` hacen `continue` **después** de escribir un
  relleno equivalente, así que todo elemento cuesta los mismos bytes.
  `PledgeShowMemberListAll` y `PledgeReceiveMemberInfo` escriben `size() - 1` y
  saltean al jugador que mira, lo que se sostiene porque los seis sitios que los
  construyen pasan un jugador que **es** miembro del clan listado — incluido el
  camino de aceptar invitación, que agrega al miembro antes de enviar el paquete.

- **El caché de difusión.** `WritablePacket` escribe una vez y reusa el buffer
  para todos los destinatarios, lo que sería incorrecto para cualquier paquete
  cuyo contenido dependa de quién lo recibe. `sendInBroadcast()`, el único método
  que enciende esa bandera, **no tiene ningún llamador**: el mecanismo existe
  entero y está muerto.

### Cómo se cubrió el área

De los 259 archivos se leyeron línea por línea los que los barridos señalaron más
`SystemMessage`, `SortedWareHouseWithdrawalList`, `CharSelectionInfo`,
`AllianceInfo`, `CharInfo` y `AbstractNpcInfo`. El resto son escritores de buffer
mecánicos, y sobre los **259** corrieron siete barridos:

1. **operación compuesta sobre colección concurrente** — 0 candidatos;
2. **`containsKey` seguido de `get`** — 5, todos sobre mapas locales del paquete;
3. **división por variable** — 32, todos el mismo `_moveMultiplier`, descartado
   midiendo los datos;
4. **encadenamientos sin guarda** — casi todos `getInventory()` sobre un `Player`;
   el único real estaba en el constructor de `AllianceInfo`, no en la escritura;
5. **conteo declarado contra elementos escritos** — 7 candidatos, **3 defectos**;
6. **comparadores que rompen su contrato** — 1 acierto en todo el repo;
7. **índices de array sin acotar** — 0.

Dos mecanismos quedaron descartados enteros: el **caché de difusión** de
`WritablePacket`, que sería incorrecto para cualquier paquete cuyo contenido
dependa del destinatario y cuyo único activador —`sendInBroadcast()`— no tiene
llamadores; y **`writeString(null)`**, que escribe solo el terminador en vez de
lanzar, junto con `writeSizedString`, que chequea el null.

### Sospechas evaluadas y descartadas (`serverpackets`)

- **Ocho paquetes dividen por `_moveMultiplier`.** `getMovementSpeedMultiplier()`
  es `getMoveSpeed() * (1. / baseSpeed)`, así que un NPC con velocidad base cero
  da `Infinity` o `NaN`. Medidos los 5.739 bloques `<speed>` de los datos: **4
  NPCs** tienen andar y correr en cero, y para ellos el multiplicador es `NaN` —
  que en `Math.round(velocidad / NaN)` da **0**, la velocidad correcta para un NPC
  estático. Degrada al resultado justo. Los únicos consumidores del multiplicador
  son esos paquetes y `Pet`, que nunca es estático.

- **Encadenamientos sin chequear en los paquetes de información.** Barridos los
  `.getClan()`, `.getPet()`, `.getParty()`, `.getInventory()` y compañía sin
  guarda: la enorme mayoría son `getInventory()` sobre un `Player`, que nunca es
  nulo. Los de `AllianceInfo` en `writeImpl` se construyen desde
  `getClanAllies()`, así que tampoco pueden serlo — el problema estaba en el
  constructor, no en la escritura.

- **Los cinco `containsKey` seguidos de `get`** (`ExShowCropSetting`,
  `ExShowSeedSetting`, `ExShowSellCropList`). Los mapas son locales del paquete,
  armados en su propio constructor y no compartidos.

## `data` — TERMINADA

Son **71** archivos y **14.922** líneas: los cargadores XML (41), las tablas SQL
(10), los holders (12) y algunos sueltos.

**Leído:** `SchemeBufferTable` entero, y de los cargadores todo lo que el comando
`//reload` toca. 
### Hallazgos

| # | Archivo | Defecto | Severidad |
|---|---|---|---|
| 1 | `WalkingManager` | cuatro colecciones planas mutadas desde varios hilos, con un solo método `synchronized` | **crítica** |
| 2 | 5 cargadores | `//reload` vacía y rellena `HashMap` planos que los hilos de paquetes están leyendo | alta |
| 3 | `SchemeBufferTable` | el guardado del apagado itera colecciones que los jugadores modifican | alta |
| 4 | `SevenSignsFestival` | siete colecciones planas con `put`, iteración y `clear` simultáneos | alta |
| 5 | `AdminData` | `hasAccess` hace un `put` estructural desde hilos de paquetes | alta |
| 6 | `SchemeBuffer` | se hace `add` sobre la lista inmutable que devuelve `getScheme` | media |
| 7 | `DoorData` | `addDoorGroup` lee, testea y escribe para armar sus conjuntos | media |
| 8 | `SchemeBuffer` | el id de skill del bypass va a `isDance()` sin chequear null | media |

**1 — Un `synchronized` solitario que delata a toda la clase.** `_activeRoutes`
es un `HashMap` plano con una entrada por NPC que camina. `cancelMoving` le hace
`remove` **y lleva `synchronized`**; `startMoving` le hace `put` y **no**, y
tampoco lo llevan `isOnWalk`, `isRegistered`, `getRouteName`, `resumeMoving` ni
`stopMoving`, que lo leen. Un escritor tomando el monitor y otro sin tomarlo no
es exclusión mutua: es un `HashMap` plano reestructurado concurrentemente —desde
las tareas de caminata, desde `ArrivedTask` y desde hilos de paquetes— de forma
continua, para cada NPC que camina en el servidor. Un `put` que redimensiona
mientras otro hilo recorre un bucket es la forma clásica de dejar un `get`
girando.

Ese `synchronized` único es el delator: alguien vio la concurrencia y candó uno
de los seis métodos que tocan el mapa.

**2 — Datos que se reescriben mientras se leen.** `//reload` deja recargar la
mayoría de las tablas en caliente, y cada `load()` es un `clear()` seguido de un
bucle que repuebla, mientras los hilos de paquetes leen sin parar.

La división entre esas clases es el delator otra vez: las que más se recargan
—`NpcData`, `SkillData`, `MultisellData`, `BuyListData`, `TeleporterData`— ya son
`ConcurrentHashMap`. `ItemData`, `DoorData`, `AdminData`, `EnchantItemData` y
`EnchantItemGroupsData` eran planas, y están en el mismo menú de `//reload`. Los
tres mapas de `ItemData` cargan la lectura más caliente del servidor, porque cada
operación con ítems pasa por `getTemplate`.

**5 — `AdminData` lo dice solo.** `_gmList`, el campo justo debajo de los dos que
se cambiaron, ya era `ConcurrentHashMap`. Y su `_adminCommandAccessRights` es
peor que una ventana de recarga: `hasAccess` —llamado desde
`AdminCommandHandler` para **cada comando de GM**, en hilo de paquetes— le hace
`put` cuando encuentra un comando sin derechos definidos. Ese mapa se modifica
estructuralmente en caliente por los mismos hilos que lo leen.

**3 — Un jugador editando, los esquemas de todos perdidos.** `saveSchemes` corre
desde `Shutdown`, y la cuenta regresiva del apagado deja a los jugadores
conectados y clickeando. Recorre el mapa de esquemas de cada jugador y la lista
de skills de cada esquema para armar un solo batch. Solo el mapa exterior era
concurrente: los esquemas de cada jugador eran un `TreeMap` plano y las skills de
cada esquema un `ArrayList` plano, y el NPC del buffer muta los dos desde hilos
de paquetes. Un solo jugador editando durante la cuenta regresiva puede tirar una
modificación concurrente fuera del bucle, y el `catch` que lo envuelve **abandona
el `executeBatch` entero**: no los esquemas de ese jugador, los de todos.

### Quinta vuelta: el barrido de parseo paralelo

`IXmlReader.parseDirectory` reparte los archivos de un directorio sobre un pool de
hilos cuando `ThreadsForLoading` está prendido — y `Threads.ini` lo distribuye
**prendido**. Eso convierte cualquier lectura-seguida-de-escritura dentro de un
`parseDocument` en una carrera. Barrido sobre todos los cargadores que parsean un
directorio: **cuatro archivos con la forma, cinco defectos**.

| # | Archivo | Defecto | Severidad |
|---|---|---|---|
| 13 | `NpcData` | el id de clan de NPC se deriva del tamaño del mapa | alta |
| 14 | `NpcData` | dos hilos con el mismo id de NPC pierden una definición entera | alta |
| 15 | `SpawnData` | el id de plantilla de spawn se deriva del tamaño del mapa | media |
| 16 | `SkillTreeData` | dos archivos que definen la misma clase pierden un árbol | media |
| 17 | `SkillTreeData` | `_parentClassMap` es un `LinkedHashMap` plano escrito en paralelo | media |

**13 y 15 — Un id sacado del tamaño del mapa.** Los dos escriben la misma forma:

```java
final int newId = _mapa.size();
_mapa.put(newId, valor);
```

Dos hilos con **nombres distintos** leen el mismo tamaño y escriben el mismo id.
En `NpcData` eso da dos clanes de NPC sin relación compartiendo un id, y
`AttackableAI` usa esos ids para decidir a qué vecinos llama un NPC cuando lo
atacan: un choque hace que NPCs de un clan respondan a los llamados de otro. Los
datos distribuidos definen **105 nombres de clan repartidos en 77 archivos**. En
`SpawnData` da dos archivos de spawn compartiendo una plantilla y un nombre de
archivo perdido. Los dos ids salen ahora de un `AtomicInteger`.

**14 y 16 — La rama de fusión que nunca corre.** Los dos escriben
`get` → `if (null) crear y poner` → `else fusionar`. Dos hilos que se encuentran
con la misma clave **los dos ven null**, los dos construyen, y un `put` pisa al
otro: la definición perdedora se descarta entera en vez de fusionarse por la rama
que existe justamente para eso.

Que importe se puede medir: **29 de los ids de NPC distribuidos están definidos en
más de un archivo**, que es por qué la rama `else` está escrita.

### Cómo se cubrió el área

De los 71 archivos se leyeron línea por línea `SchemeBufferTable`, los cinco
cargadores que `//reload` reescribe, los cuatro de `sql/`, y las partes de
`NpcData`, `SpawnData` y `SkillTreeData` donde se construye y se consulta el
estado compartido — más `MultiSellChoose`, `RequestAcquireSkill`, `SkillList`,
`TradeList` y `SchemeBuffer`, que están fuera del paquete pero son el otro extremo
de éstos.

Sobre los **71** se corrieron seis barridos mecánicos, cada uno derivado de un
defecto ya encontrado:

1. **operación compuesta sobre colección concurrente** — 5 candidatos;
2. **`containsKey` seguido de `get`** — 12 candidatos;
3. **división o módulo por variable** — 4 candidatos, los dos de geometría con su
   denominador cero ya chequeado;
4. **colección plana mutada en caliente** — 23 archivos, cruzados contra la lista
   de `//reload`;
5. **producto ensanchado tarde** — 2 aciertos en todo el core y el datapack;
6. **parseo paralelo con lectura-luego-escritura** — 4 archivos, 5 defectos.

El sexto es el que más rindió, y salió de preguntar por qué `ZoneManager.addZone`
podía perder una zona: la respuesta —que los XML se parsean en paralelo por
defecto— aplicaba a todos los cargadores, no solo a ése.

### Cuarta vuelta: el aprendizaje de skills

| # | Archivo | Defecto | Severidad |
|---|---|---|---|
| 12 | `SkillList` (bypass) | el id de clase del bypass llega a `canTeach` sin validar | baja |

**12 — Un id que no nombra ninguna clase.** El camino de aprendizaje alternativo
toma el id de clase directo del bypass:

```java
Folk.showSkillList(player, target.asNpc(), PlayerClass.getPlayerClass(Integer.parseInt(id)));
```

`getPlayerClass` es un lookup de mapa y devuelve **null** para un id que no nombra
ninguna clase. Ese null va a `showSkillList`, que llama a `canTeach` sobre él, y
lo primero que hace `canTeach` es `playerClass.level()`. Un id no numérico revienta
un paso antes, en el `parseInt`. Los dos quedan atrapados por el `catch` del
manejador de bypass, así que cuestan un stack trace en el log y nada peor, pero
ninguno debería ser alcanzable desde texto arbitrario del cliente.

### La cadena que parecía un exploit de progresión y no lo es

Vale la pena dejarla escrita entera, porque cada eslabón se lee mal y la
conclusión igual es que no hay defecto.

1. `SkillList` deja que el **cliente elija la clase** cuyo árbol de skills se va a
   mostrar, con un id que manda el bypass.
2. `Folk.showSkillList` hace `player.setLearningClass(playerClass)` con esa clase.
3. `RequestAcquireSkill` resuelve qué skill se puede aprender con
   `getClassSkill(id, lvl, player.getLearningClass())` — la clase **de
   aprendizaje**, no la real.
4. `getAvailableSkills` filtra **solo** por nivel del jugador y por progresión de
   la skill. **No verifica en ningún momento que la clase del jugador sea esa
   clase ni descendiente de ella.**
5. La única puerta es `npc.getTemplate().canTeach(playerClass)`, que comprueba
   que **el entrenador** enseñe esa clase — no que el jugador tenga algo que ver
   con ella.
6. Y los entrenadores enseñan líneas enteras: medido sobre los 201 del
   `SkillLearn.xml` distribuido, van de 3 a 22 clases cada uno, y el primero
   —Auron— cubre Human Fighter, Warrior, Gladiator, Warlord, Knight, Paladin,
   Dark Avenger, Rogue y Treasure Hunter.

Leído así, un Human Fighter de nivel suficiente podría aprender skills de
Gladiator sin haber cambiado de clase nunca.

**Pero el paso 1 está entero adentro de `if (PlayerConfig.ALT_GAME_SKILL_LEARN)`**,
que es la función "alternative skill learn" de L2J: existe justamente para eso. El
código la define en `false` y el `Player.ini` distribuido trae
`AltGameSkillLearn = False`. Apagada, el `else` de ese mismo bloque pasa
`player.getPlayerClass()` y la pregunta no llega a plantearse. Encendida, toda la
cadena es la función funcionando como se anunció.

Lo único mal en ese camino era el id sin validar del hallazgo 12.

### Sospechas evaluadas y descartadas (cuarta vuelta)

- **`RequestAcquireSkill` cobrando antes de otorgar.** El orden es correcto:
  chequea nivel, SP e ítems requeridos —los ítems en dos pasadas, verificando
  todos antes de consumir ninguno—, después descuenta SP, y recién ahí
  `addSkill`. El retorno de `destroyItemByItemId` se chequea.

- **Un `destroyItemByItemId` fallido en el bucle de consumo otorgando la skill
  igual.** Ocurre, y está deliberado: llama a `handleIllegalPlayerAction` con
  `IllegalActionPunishmentType.NONE`, o sea loguear sin castigar, después de que
  una pre-verificación completa ya pasó. Un fallo ahí es genuinamente anómalo.

### Tercera vuelta: multisell y el barrido de ensanchado tardío

| # | Archivo | Defecto | Severidad |
|---|---|---|---|
| 11 | `MultiSellChoose` | el peso de lo que se va a recibir se suma en `int` y se valida contra un parámetro `long` | media |

**11 — El rango se tiraba antes de la llamada.** `MultiSellChoose` totaliza lo que
el jugador está por recibir en `int slots` e `int weight`, y se lo pasa a
`inv.validateWeight` y `inv.validateCapacity`, **que las dos toman `long`**. O sea
que el rango se perdía acá, en aritmética de `int`, antes de llegar a la
validación que sí podía sostenerlo.

Sesenta líneas más abajo, el bucle de **ingredientes** guarda exactamente este
peligro y lo hace bien:
`((long) e.getItemCount() * _amount) > Integer.MAX_VALUE`, y rechaza el
intercambio. El de **productos**, arriba, no. Esa asimetría es lo que lo vuelve
hallazgo.

Medido antes de cambiarlo: `MultisellAmountLimit` viene en 10.000, y entre los 74
archivos de multisell distribuidos y sus 7.119 producciones el peor caso es
**109.800.000** contra un techo de 2.147.483.647 — diecinueve veces libre, y nada
de lo distribuido desborda. Pero el umbral es `count × weight > 214.748`, y el
ítem más pesado del juego pesa 10.980: **una producción custom de veinte de esos
lo cruza**. Los archivos de multisell están entre los más editados en un servidor
privado, y un peso que envuelve a negativo **pasa** el chequeo que debería
fallar.

### Anotado sin tocar (tercera vuelta)

- **El conteo de producto en el bucle que entrega** (`e.getItemCount() * _amount`)
  no tiene el guard simétrico. Desbordarlo pide un `count` de producción por
  encima de 214.748 y el mayor de los datos distribuidos es **700** — tres órdenes
  de magnitud. Agregar la guarda ahí sería código muerto.

- **`Spawn.setRespawnDelay` hace `Math.max(1, minDelay) * 1000`**, o sea `int` por
  mil. Un delay configurado por encima de 24,9 días desborda a negativo, y
  `_doRespawn = _respawnMinDelay > 0` daría `false`: el NPC no vuelve nunca. El
  mayor de los 20.727 atributos `respawnDelay` distribuidos es **604.800 segundos**
  (siete días).

- **`Spawn.setRespawnDelay` con un delay negativo solo loguea y sigue.** El
  `Math.max(1, ...)` que viene después lo clampea a un segundo, así que el NPC
  reaparece cada segundo en vez de romperse.

### Sospechas evaluadas y descartadas (tercera vuelta)

- **El barrido de ensanchado tardío sobre todo el core y el datapack.** Buscando
  acumuladores `int` cuyo lado derecho multiplica dos o más términos no
  constantes, sin `(long)` a la vista: **dos aciertos**, los dos cálculos de
  recompensa de quest (`Q00325`, `Q00360`) que necesitarían millones de ítems de
  quest acumulados para desbordar. El barrido saliendo casi vacío es lo que
  confirma que el tesoro del manor y el peso del multisell eran los casos reales y
  no una clase difusa.

### Segunda vuelta: `data/sql`

| # | Archivo | Defecto | Severidad |
|---|---|---|---|
| 9 | `ClanHallTable` | `setOwner` mueve el hall entre mapas y **después** desreferencia un clan que puede no existir | alta |
| 10 | `ClanHallTable` | `setFree` mete en un `ConcurrentHashMap` el null que devuelve un `get` | media |

**9 — Tirar con el hall ya movido.** `setOwner` recibe el `Clan` como parámetro y
sin embargo escribe:

```java
ClanTable.getInstance().getClan(clan.getId()).setHideoutId(chId);
```

Volver a buscarlo por id y desreferenciar la respuesta significa que un clan
disuelto entre ganar la subasta y el cierre de ésta **tira acá** — y para ese
momento el hall ya salió de `_freeClanHall` y entró en `_clanHall`. El hall queda
registrado como poseído sin dueño puesto. Ahora se resuelve el hall antes de
mover nada y se usa el clan que ya está en la mano.

**10 — Un null que el mapa rechaza.** `setFree` empezaba metiendo
`_clanHall.get(chId)` en `_freeClanHall`. Un hall que no está poseído no está en
`_clanHall`, así que ese `get` da null, y un `ConcurrentHashMap` **rechaza
valores nulos**. Alcanzable con `//siege` sobre un hall que ya está libre.

### Sospechas evaluadas y descartadas (`data/sql`)

- **La tienda privada listando ítems que no son del vendedor.**
  `SetPrivateStoreListSell` va del id de objeto que manda el cliente directo a
  `TradeList.addItem(objectId, ...)` **sin** `validateItemManipulation` — mientras
  que `AddTradeItem`, el camino hermano de la ventana de intercambio, **sí** lo
  llama. Y `addItem` busca con `World.findObject(objectId)`, que encuentra
  cualquier ítem del mundo, y su chequeo de manipulación es por id de plantilla,
  no de instancia.

  Parecía duplicación. **No lo es:** `privateStoreBuy` llama a `validate()` antes
  de mover nada, y `validate()` corre `_owner.checkItemManipulation` sobre cada
  ítem listado. Un listado con un objeto ajeno pasa el armado y falla en la
  compra, que además cierra la tienda con `lock()`. La puerta de pertenencia está
  río abajo.

- **`OfflineTraderTable` guardando la instancia en un caso y la plantilla en
  otro.** En `SELL` persiste `i.getObjectId()` y en `BUY` `i.getItem().getId()`.
  Es deliberado y simétrico: la restauración usa `addItem(objectId, ...)` para uno
  y `addItemByItemId(itemId, ...)` para el otro.

- **Filas huérfanas en `character_offline_trade_items`.** El `catch` por jugador
  está dentro del bucle, así que una excepción después de batchear ítems pero
  antes de batchear la fila de dueño deja ítems sin dueño. La restauración lee
  desde la tabla de dueños y filtra por `charId`, así que nunca los ve, y el
  siguiente guardado limpia las dos tablas.

- **El `continue` dentro de los `case` de `storeOffliners`**, que saltea el
  `addBatch` de la fila de dueño. Está colocado **antes** del bucle que batchea
  ítems en los tres casos, así que no deja ítems sin dueño.

- **`ClanTable` y las otras tablas SQL.** `_clans` y `_clansByName` ya son
  `ConcurrentHashMap`; `_allAuctionableClanHalls` es plano pero solo se escribe en
  la carga.

### Método

El hallazgo 1 salió de convertir el hallazgo 1 del área anterior en un barrido:
**una clase con uno o dos métodos `synchronized`, campos de colección planos, y
métodos sin sincronizar que mutan esos mismos campos**. El `synchronized`
solitario es lo que marca la clase como una donde alguien notó la concurrencia y
cubrió un método. Diez clases en todo el core; `SevenSignsFestival` tiene siete
campos así, el máximo. `Olympiad.NOBLES_RANK` también aparece y **ya estaba
verificado y descartado** en la pasada de olimpiada: su único lector es privado y
corre dentro del mismo método que reconstruye el mapa.

## `managers` — TERMINADA

Son **44** archivos y **14.636** líneas. Se empezó por los que mueven dinero, que
es donde vienen apareciendo los defectos.

**Leído:** `GlobalAuctionManager` entero, `MonsterRaceManager` en apuestas, cuotas
y ciclo de carrera, `RecipeManager` en el cálculo de pasadas, `LotteryManager` en
el reparto de premios, y `RaceManager` (que es `model/actor/instance` pero es el
NPC de las carreras) en su bypass.


### Hallazgos

| # | Archivo | Defecto | Severidad |
|---|---|---|---|
| 1 | `GlobalAuctionManager` | el saldo se pone en cero **antes** de pagarlo, y se trunca a `int` | alta |
| 2 | `RaceManager` | el número del bypass se parsea y se indexa sin validar | alta |
| 3 | `MonsterRaceManager` | `_odds` se vacía y se rellena mientras otro hilo lo indexa | alta |
| 4 | `MonsterRaceManager` | la apuesta por calle es una lectura-modificación-escritura no atómica | media |
| 5 | `RecipeManager` | se divide y se modula por un valor que los datos pueden dejar en cero | baja |

**1 — Cobrar el propio pago.** `collectFunds` ponía el saldo en cero, en memoria y
en base, y **después** intentaba acreditarlo. Lo que el tope de adena rechace se
pierde; el comentario que estaba ahí lo admitía: *"Rollback logic would be complex
here, assume DB works"*.

Y el saldo es un `long` que se acumula a lo largo de varias ventas, mientras
`addAdena` toma un `int`. Un vendedor con más de dos mil millones acumulados
recibía ese `long` casteado a un `int` **negativo** —que por ser negativo tampoco
entra en el clampeo del tope— y el método devolvía la cifra completa igual, o sea
que reportaba un número que el jugador nunca recibió.

Ahora calcula cuánto lugar tiene realmente el jugador, entrega eso, y descuenta
solo eso del saldo; el resto queda para cobrar después.

**2 — Un número del cable indexando dos arrays de ocho.**
`int val = Integer.parseInt(command.substring(10));` sin nada en el medio. `val`
elige calle vía `getMonsters()[val - 1]` y `val - 10` elige tarifa vía
`TICKET_PRICES[val - 11]`, así que **9 y 19 indexan uno más allá del final** de
arrays de ocho, y un negativo indexa por debajo de cero. Además un comando sin
nada después de `"BuyTicket "` revienta en el `substring` y uno no numérico en el
`parseInt`.

**3 y 4 — Concurrencia a medias, otra vez.** `_betsPerLane` es un
`ConcurrentHashMap` **y su declaración lleva un comentario explicando que las
apuestas llegan concurrentes** — que es justo el delator, porque una lectura
seguida de una escritura no es atómica por más que cada mitad lo sea. Dos apuestas
a la misma calle podían leer el mismo total y la segunda escritura descartaba la
primera del pozo con el que se calculan las cuotas.

Y `_odds` era un `ArrayList` que la tarea de carrera vaciaba y rellenaba en el
lugar mientras `RaceManager` lo indexa desde un hilo de paquetes, calle por calle.
Un lector que llegue entre el `clear()` y los `add` ve una lista más corta que el
índice que va a usar. Además arranca **vacía** hasta la primera carrera. Ahora se
arma aparte y se publica en una sola asignación a un campo `volatile`, y los dos
lectores chequean el tamaño.

### Segunda vuelta: el barrido de operaciones compuestas

Después de que el patrón rindiera tres veces —una colección hecha concurrente a
propósito, con una operación compuesta no atómica escrita al lado— se barrieron
los 44 archivos buscando exactamente eso: una lectura sobre un `ConcurrentHashMap`
seguida de un `put` sobre el mismo mapa dentro de cinco líneas. Salieron **siete
candidatos**; cinco resultaron defectos.

| # | Archivo | Defecto | Severidad |
|---|---|---|---|
| 6 | `WalkingManager` | `cancelMoving` no saca nada de los tres mapas de tareas | alta |
| 7 | `WalkingManager` | tres `get`-chequeo-`put` sobre futuros, uno de tasa fija | alta |
| 8 | `CaptchaManager` | `containsKey` y después `get` sobre un mapa del que borra un timeout | alta |
| 9 | `CastleManorManager` | tres multiplicaciones que ensanchan a `long` **después** de multiplicar | media |
| 10 | `CaptchaManager` | el contador de intentos anti-bot se incrementa sin atomicidad | media |
| 11 | `CaptchaManager` | se cancela una tarea que puede no estar, sin chequear | media |
| 12 | `ZoneManager` | registro de zona con `get`, null, `put`, `get` otra vez | media |

**6 y 7 — Tareas que corren para siempre sin nadie que las pueda parar.**
`WalkingManager` guarda tres mapas de futuros indexados por `Npc`, y los tres
tenían la misma forma `get` → chequeo → `put`. `startMoving` se alcanza desde
`ArrivedTask` **y** desde la tarea repetitiva que se instala en esa misma línea,
en hilos distintos del pool, así que dos llamadores pueden encontrar la tarea
terminada y agendar cada uno un reemplazo. **Esos mapas son el único asidero de
esos futuros**, así que aquel cuyo `put` perdió quedó corriendo a tasa fija cada
diez segundos, sin nada capaz de cancelarlo.

Y `cancelMoving` no sacaba nada de esos mapas. Saca la ruta de `_activeRoutes` y
cancela la tarea de chequeo de `WalkInfo` —que da la casualidad de ser el mismo
futuro que la entrada repetitiva, así que **esa** sí paraba—, pero las entradas
quedaban y las tareas de arranque y de llegada no se cancelaban en absoluto. Dos
consecuencias: todo NPC que hubiera caminado una vez quedaba anclado en tres
mapas por una clave fuerte, y una llegada pendiente seguía disparándose para un
NPC recién muerto.

**8, 10 y 11 — Tres carreras en el control que debería atrapar bots.**
`analyseBypass` abría con `containsKey` y después hacía dos `get` más sobre la
misma clave. El captcha tiene una tarea de timeout que borra esa entrada, así que
una respuesta que llega mientras el timer dispara pasa el `containsKey` y después
desreferencia el null que devuelven los `get`.

El contador de intentos era `getOrDefault` + 1 + `put`. Dos respuestas juntas leen
el mismo número y la segunda escritura repone el mismo valor, o sea que **una
respuesta incorrecta no cuesta nada**. En un control anti-bot esa es la dirección
equivocada para fallar.

Y los dos sitios que paran el timer escribían
`BEGIN_VALIDATION.get(...).cancel(true)` seguido de un `remove`. La entrada no
está garantizada —el timer se borra solo al vencer— así que el `get` daba null y
el `cancel` tiraba; en `banPunishment` eso cae **entre** borrar la validación y
aplicar el castigo.

**9 — Ensanchar después de multiplicar.** Tres sitios multiplican precio por
cantidad y entregan el resultado a algo que toma un `long`. `SeedProduction`
declara los dos como `int`, así que el producto se calculaba en aritmética de
`int` y recién el resultado ya desbordado se ensanchaba. El que cuesta dinero es
`castle.addToTreasuryNoTax(crop.getAmount() * crop.getPrice())`, porque
`addToTreasuryNoTax` **lee un monto negativo como un retiro**: un desborde ahí no
solo pierde el depósito, saca la misma suma del tesoro.

Se calculó cuán lejos está el dato distribuido del borde en vez de suponerlo.
`RequestSetCrop` valida el precio contra `getCropMaxPrice()` (precio de referencia
del cultivo × 10) y la cantidad contra `getCropLimit()` (`limit_crops` ×
`RateDropManor`). En las 198 filas de `Seeds.xml` el `limit_crops` mayor es 9.000
y entre los 54 ítems de cultivo el precio de referencia mayor es 2.750, así que
con el `RateDropManor = 1` distribuido el peor caso es **247.500.000**, cómodo
dentro de `int`. Pero ese rate escala el límite, y el umbral es
`2147483647 / (9000 × 27500)` = **8,67**: un servidor con `RateDropManor` en 9 o
más desborda, y las tasas altas están muy por encima de eso.

### Tercera vuelta: el barrido de `containsKey` + `get`, y los controles de seguridad

El `containsKey` seguido de `get` del captcha también era un patrón. Barridos los
44 archivos: **13 candidatos**, de los cuales importan aquellos de cuya colección
**algo borra concurrentemente** y cuyo resultado se desreferencia sin chequear.

| # | Archivo | Defecto | Severidad |
|---|---|---|---|
| 13 | `AntiFeedManager` | el contador de conexiones por IP se decrementa dos veces y compara con `== 0` | alta |
| 14 | `AntiFeedManager` | el límite de dualbox se chequea y se incrementa en pasos separados | alta |
| 15 | `SellBuffBypassHandler` | se cobra al comprador sin verificar que el vendedor pueda recibir | alta |
| 16 | `RaidBossSpawnManager` | dos lecturas de `_bosses` desreferencian sin chequear null | media |
| 17 | `AntiFeedManager` | la rama olímpica decrementa a mano, sin limpiar ni acotar | media |
| 18 | `SellBuffBypassHandler` | el mensaje de "precio muy alto" nombra el mínimo | baja |

**13, 14 y 17 — Un límite que se desactiva solo.** `tryAddClient` leía la cuenta,
la comparaba y después incrementaba: tres pasos. Dos clientes de la misma
dirección que llegan juntos leen el mismo valor, los dos encuentran lugar y los
dos incrementan, así que el límite admite uno más de los que permite.

Pero lo que lo vuelve permanente es lo otro. `removeClient` borraba la entrada
cuando `decrementAndGet() == 0`, con igualdad exacta — y `onDisconnect` **recorre
todos los eventos registrados y saca al cliente de cada uno sin preguntar si
alguna vez estuvo en ese**. Un cliente que salió bien de un evento por
`removePlayer` y después se desconecta se decrementa dos veces por un solo
incremento. Pasado el cero, la igualdad exacta no vuelve a coincidir: la entrada
queda con cuenta negativa, y una cuenta negativa hace pasar la comparación de
`tryAddClient` sin importar nada. **El límite de dualbox para esa dirección queda
apagado por el resto del tiempo que el servidor esté arriba.**

Es la misma forma que el `FloodProtectorListener` del loginserver: un contador
compartido que se incrementa por un camino, se decrementa por varios, y se compara
con igualdad exacta.

**15 — Cobrar por algo que nunca llegó.** Los dos caminos de compra movían el pago
con `Quest.takeItems(comprador)` seguido de `Quest.giveItems(vendedor)`.
`giveItems` devuelve `void`, y debajo `Inventory.addItem` responde `null` cuando
el destinatario no puede sostener lo que se le da —inventario lleno, pasado del
límite de peso, o en el tope de adena— y `giveItems` simplemente vuelve. Así que
el comprador pagaba, el vendedor no recibía nada, y el pago dejaba de existir. El
buff se casteaba igual.

**16 — Preguntar dos veces.** `getRaidBossStatus` y `getRaidBossStatusId` abren con
`containsKey` sobre `_bosses` y después hacen `get` de la misma clave,
desreferenciando sin test de null. El mapa recibe `remove` al desspawnear un boss
y `clear` al recargar, desde hilos distintos de los que piden el estado. La clase
**ya lo lee bien** treinta líneas más arriba, en el bucle de actualización: un solo
`get` en una local, con el chequeo de null que a estos dos les faltaba.

### Sospechas evaluadas y descartadas (tercera vuelta)

- **`WalkingManager` con `containsKey` sobre `_activeRoutes`** seguido de `get`, y
  `cancelMoving` borra de ese mapa. Sí chequea el null que devuelve el `get`.

- **Los otros diez candidatos del barrido** (`DimensionalRiftManager`,
  `InstanceManager`, `ScriptManager`, `ZoneManager`, `MercTicketManager`): o la
  colección solo se escribe en la carga, o el resultado se usa de una forma que
  tolera el null.

- **`AntiFeedManager` en el resto de su estructura.** Es un `ConcurrentHashMap` de
  `ConcurrentHashMap` de `AtomicInteger`, con `computeIfAbsent` y
  `computeIfPresent` donde corresponde. El defecto no era la estructura sino las
  dos operaciones compuestas escritas encima.

### Anotado sin tocar (segunda vuelta de `managers`)

- **`RequestBuySeed` acumula en un `totalPrice` de tipo `int`** y lo compara con
  `MAX_ADENA` después de cada suma. Cada término está acotado por un
  pre-chequeo de desborde explícito arriba, pero dos términos cerca del tope de
  2.000.000.000 suman por encima de `Integer.MAX_VALUE` y envuelven a negativo, lo
  que **pasa** el chequeo en vez de fallarlo. No cuesta nada: el `reduceAdena` por
  ítem del bucle de compra cobra el precio positivo correcto y saltea lo que el
  jugador no puede pagar, así que solo se evade el castigo anti-trampa, y los
  precios de manor necesarios para llegar ahí están muy por encima de lo que
  permiten los datos.

- **`RETRIES` de `CaptchaManager` nunca se limpia.** Solo se pone en cero. Está
  indexado por object id, así que no ancla objetos `Player`, pero crece con cada
  jugador que alguna vez vio un captcha.

- **`ZoneBuildManager.PLAYER_LOCATIONS` está indexado por `Player`** y solo se
  limpia al terminar de construir la zona. Un GM que se desconecta a mitad deja su
  `Player` anclado. Herramienta de GM, impacto bajo.

### Sospechas evaluadas y descartadas (segunda vuelta)

- **`ZoneManager.addZone` perdiendo zonas en el arranque.** El defecto es real y
  se arregló, pero **no es alcanzable con los datos distribuidos**, y la razón vale
  la pena: `load()` pre-puebla `_classZones` con un mapa para 28 clases de zona
  antes de parsear, así que la rama del null solo corre para una clase que esa
  lista se haya salteado. Comparada contra las subclases de `ZoneType` queda
  exactamente una, `NoPvPZone`, y los 37 archivos de zonas distribuidos no
  contienen ni una zona `NoPvP`.

- **`GlobalAuctionManager.addFunds` con `getOrDefault` + `put`.** Todos los métodos
  que tocan `_funds` son `synchronized` sobre el manager.

- **El barrido de multiplicaciones de dinero ensanchadas tarde** sobre todo el core
  dejó un solo sitio más, `RequestBuySeed:180`, y está cubierto por el
  pre-chequeo de desborde que tiene arriba.

### Cuarta vuelta: consumir antes de otorgar

| # | Archivo | Defecto | Severidad |
|---|---|---|---|
| 19 | `MercTicket` | se contrata el mercenario y **después** se saca el ítem, con el booleano descartado | alta |
| 20 | `Recipes` | se registra la receta y después se destruye el libro | media |
| 21 | `FishShots` | se carga el shot y después se destruye | baja |

**19 — Un ticket, dos mercenarios.** El handler contrata al mercenario, lo
spawnea, crea el ticket en el mundo y lo registra, y recién ahí saca el ítem del
inventario del jugador — descartando lo que `destroyItem` devuelve. Dos usos del
mismo ticket que llegan juntos pasan los dos los límites de castillo, tipo y
distancia, colocan cada uno un mercenario, y solo un `destroyItem` tiene éxito.

**20 — Quedarse con el libro y con la receta.** `Recipes` registra la receta y
después destruye el libro. Un `destroyItem` fallido deja al jugador con las dos
cosas, y el libro es comerciable.

Los tres **destruyen primero** ahora y vuelven sin otorgar si el consumo no
ocurrió.

Encontrados contando en vez de leyendo: en los handlers de ítems hay **diez**
llamadas a `destroyItem` que testean lo que devuelve y, antes de esto, **tres**
que lo ignoraban. Esas tres eran exactamente éstas.

### Cómo se cubrió el área

De los 44 archivos se leyeron línea por línea **los que mueven valor o hacen de
control**: `GlobalAuctionManager`, `MonsterRaceManager`, `LotteryManager`,
`RecipeManager`, `CastleManorManager`, `SellBuffsManager`, `MercTicketManager`,
`ClanHallAuctionManager`, `CaptchaManager`, `AntiFeedManager`, `ZoneManager`,
`WalkingManager` y `RaidBossSpawnManager` — más `RaceManager`, `Auctioneer`,
`SellBuffBypassHandler`, `MercTicket`, `Recipes` y `FishShots`, que están fuera
del paquete pero son el otro extremo de estos.

Sobre los **44** se corrieron cuatro barridos mecánicos, cada uno derivado de un
defecto ya encontrado:

1. **operación compuesta sobre colección concurrente** — 7 candidatos, 5 defectos;
2. **`containsKey` seguido de `get`** — 13 candidatos, 2 defectos;
3. **división o módulo por variable** — 7 candidatos, 1 defecto;
4. **multiplicación de dinero ensanchada tarde** — 1 candidato más en todo el core,
   ya cubierto por su propio pre-chequeo.

Los cuatro están anotados arriba con lo que encontraron **y con lo que
descartaron**, que es la mitad que importa para no volver a correrlos.

### Anotado sin tocar (`managers`)

- **57 sitios hacen `Integer.parseInt` sobre un trozo del comando de bypass**, casi
  ninguno con guarda. Medido adónde va la excepción: `RequestBypassToServer`
  envuelve todo el camino en un `catch (Exception)` que loguea el mensaje y el
  stack. O sea que están **contenidos** — un cliente puede ensuciar el log, no
  tirar el servidor. Por eso no se barrieron los 57; se arregló el de
  `RaceManager`, donde el valor fuera de rango además deja el ticket guardado en un
  estado del que el jugador no sale.

### Sospechas evaluadas y descartadas (`managers`)

- **`purchaseItem` truncando el precio a `int`.** El casteo está, pero el test de
  saldo que tiene arriba compara contra `getAdena()`, que devuelve `int`, así que
  un precio por encima del tope se rechaza antes de llegar al casteo.

- **`addFunds` con `getOrDefault` + `put` sobre un `ConcurrentHashMap`**, que sería
  una actualización perdida. Todos los métodos que tocan `_funds` son
  `synchronized` sobre el manager.

- **`RecipeManager` dividiendo por `_creationPasses`.** Está clampeado a ≥ 1 en la
  línea siguiente a donde se calcula.

- **Las tres divisiones del reparto de la lotería** (`/ count1`, `/ count2`,
  `/ count3`). Las tres están dentro de su `if (countN > 0)`.

- **`_odds` modificado durante la carrera desde dos hilos.** `calculateOdds()` y el
  `_odds.get(getFirstPlace())` están en dos `case` del **mismo** switch de la tarea
  de carrera, o sea secuenciales. El cruce de hilos real es con `RaceManager`, que
  es el que se arregló.

## datapack `ai/` — TERMINADA

Son **67** archivos y **14.462** líneas, repartidas en `others` (45), `areas`
(13) y `bosses` (9).

**Leído:** los nueve `bosses/` en su máquina de estados, muerte y persistencia
del respawn; `ClassMaster` (1.112) y `CastleChamberlain` (1.109) enteros en sus
caminos de bypass; `KetraOrcSupport` y `VarkaSilenosSupport`.


### Hallazgos

| # | Archivo | Defecto | Severidad |
|---|---|---|---|
| 1 | `CastleChamberlain` | `withdraw` sin la cota inferior que sí tiene `deposit` | alta |
| 2 | `CastleChamberlain` | las tablas de precio devuelven 0 para un nivel que no conocen | alta |
| 3 | `CastleChamberlain` | el bucle de puertas compara un índice creciente con un contador decreciente | media |
| 4 | `Castle` | el índice de torre de llama se usa sin acotar sobre una lista que puede ser nula | media |
| 5 | `KetraOrcSupport` / `VarkaSilenosSupport` | la clave del buff se parsea dos veces detrás de un `isNumeric` | baja |

**1 — La rama hermana tenía el chequeo.** `withdraw` solo comprobaba que el monto
no superara el tesoro; `deposit`, diez líneas más arriba, lo acota de los dos
lados. Un valor negativo pasaba ese test y llegaba a `addToTreasuryNoTax` como su
propia negación, que ese método lee como un depósito, así que el tesoro **crecía**;
el `giveAdena` correspondiente retornaba temprano con el conteo no positivo y no
le daba nada al jugador.

**2 — Cero como respuesta a "no sé".** Las dos tablas de precio son `switch`
sobre los niveles que conocen, inicializados en `0` y devolviendo ese valor
inicial para cualquier otro. **Cero pasa cualquier test de "¿le alcanza?"**, así
que un nivel de puerta o de torre que la tabla nunca oyó nombrar se aplicaba
gratis, se escribía en `castle_doorupgrade` o `castle_trapupgrade` y quedaba ahí.
Ni el ratio de HP de la puerta ni el nivel de la trampa tienen techo propio. Las
dos tablas contestan ahora `-1` y las dos ramas de confirmación lo rechazan, lo
que deja funcionando un precio configurado en cero de verdad.

**3 — Una cota móvil.** `for (int i = 0; i <= st.countTokens(); i++)` sobre
`final int[] doors = new int[2]`: `countTokens()` **decrece** a medida que
`nextToken()` consume, así que el índice crecía contra un tope que bajaba. Con
cuatro o más tokens sobrantes se salía del array; con ninguno, `nextToken()`
llegaba sin nada que dar.

### Segunda vuelta: `FourSepulchers`, `WyvernManager` y `takeItems`

| # | Archivo | Defecto | Severidad |
|---|---|---|---|
| 6 | `FourSepulchers` | el id de tumba 0 se usa como clave de mapa en cinco sitios | alta |
| 7 | `FourSepulchers` | `STORED_PROGRESS` es un `HashMap` plano con lectura-modificación-escritura | media |
| 8 | `WyvernManager` | se cobra antes de montar y se descarta el booleano de `mount()` | media |
| 9 | `Quest.takeItems` | devuelve `true` incondicionalmente | baja |

**6 — La guarda estaba escrita, en el lugar equivocado.** `getSepulcherId`
recorre las cuatro zonas y devuelve **0** para un jugador que no está en ninguna.
`STORED_PROGRESS` y `STORED_MONSTER_SPAWNS` están indexados de 1 a 4, así que con
id 0 el `get` devuelve `null` y los cinco sitios que usan el id como clave lo
desempaquetan o lo iteran.

El archivo **ya sabe** que el id puede ser cero: dos ramas testean
`sepulcherId > 0`, y en el caso de la muerte del boss ese test está **tres
sentencias después** del `get` que debía proteger. Ese es el delator: la guarda
se escribió por la razón correcta y se puso en el lugar equivocado.

Los dos que importan:

- `WAVE_DEFEATED_CHECK` se reagenda **cada cinco segundos** mientras la oleada
  vive, así que un jugador que sale de la zona —murió y volvió a pueblo, se
  teleportó, terminó el evento— deja un disparo pendiente, y ese disparo indexaba
  los dos mapas con cero antes de llegar al chequeo veinte líneas más abajo.
- `OPEN_GATE` **consumía la llave de capilla y marcaba el portero como usado**
  antes de resolver la tumba, así que la excepción caía con la llave ya gastada.

**8 — Descartar la respuesta de quien sabe si funcionó.** `player.mount(...)`
devuelve `false` cuando `disarmWeapons` no puede desequipar el arma en mano, que
es lo que significa `isForceEquip`: un arma maldita. El script cobraba los
cristales, desmontaba el strider, llamaba a `mount` **ignorando el resultado** y
devolvía la página de éxito. Un líder de clan con un arma maldita pagaba, perdía
el strider y no recibía nada.

**9 — Un booleano que nunca significó nada.** `takeItems` termina en un
`return true` incondicional: ignora lo que le devuelve `takeItem` —que es la
respuesta de `destroyItemByItemId`, y puede ser `false`— y además reporta éxito
cuando el jugador simplemente tenía menos de lo pedido, porque el bucle se lleva
lo que haya y sale.

Arreglarlo no rompe nada porque **nadie lo lee**: 1806 llamadores en el datapack
y ni uno usa el retorno. Por eso es higiene de API y no un arreglo de
comportamiento vivo — todos los llamadores chequean la cantidad por su cuenta
antes. Ahora reporta lo que pasó de verdad.

### Anotado sin tocar (segunda vuelta)

- **El par `getQuestItemsCount(...) >= N` seguido de `takeItems(..., N)` es la
  forma dominante en el datapack**, y entre el chequeo y el cobro hay una ventana:
  si los ítems se van en el medio, `takeItems` se lleva lo que quede y —hasta este
  cambio— reportaba éxito igual. Cerrarlo de verdad exige que los 1806 llamadores
  consulten el retorno, que no es un cambio que se pueda hacer a ciegas.

- **`ROOM_SPAWN_DATA` es `private static` no final** y un `ArrayList` plano. Solo
  se escribe en la carga (`clear()` y `add`), así que no hay escritura
  concurrente, pero nada lo impide.

### Tercera vuelta: `OracleTeleport` y dos barridos del compilador

| # | Archivo | Defecto | Severidad |
|---|---|---|---|
| 10 | `OracleTeleport` | la tarifa del rift se cobra sin mirar el bolsillo | alta |
| 11 | `OracleTeleport` | dos búsquedas a mano dejan el contador en el largo del array | media |

**10 — Cobrar sin saldo.** La escalera de tarifas por nivel llamaba a `takeItems`
directo, sin que nada comprobara la adena antes. `takeItems` **se lleva lo que
haya** en vez de negarse, así que quien no llegaba al precio pagaba lo que tenía y
se teletransportaba igual — y quien no tenía nada, viajaba gratis. Ahora la
escalera es un helper que devuelve la tarifa, el saldo se compara contra ella y la
rama se niega con el mensaje estándar.

**11 — Un contador que sale del bucle sin haber encontrado nada.** Dos de los
cuatro sitios que convierten un id de npc en índice de destino recorren
`TELEPORTERS` a mano y, si no encuentran el npc, salen con el contador en el largo
del array. `TELEPORTERS` tiene 62 entradas y `RETURN_LOCS` 64, o sea que 62 **no
está fuera de rango**: es simplemente el destino equivocado, guardado en el estado
del quest y usado más tarde por el evento `Return`.

Los otros dos sitios están bien y se dejaron intactos. Están guardados por
`ArrayUtil.contains` sobre `TOWN_DAWN` y `TOWN_DUSK` y después recorren
`TELEPORTERS`, lo que se lee como un desajuste hasta que uno mira los arrays: los
diez npcs del alba ocupan `TELEPORTERS[0..9]` y los diez del ocaso `[10..19]`, así
que la guarda **sí** garantiza que el recorrido encuentre. `TEMPLE_PRIEST` y
`RIFT_POSTERS` no comparten ni un miembro con `TELEPORTERS`, y eso es justo lo que
vuelve alcanzables a las dos ramas sin guarda con un id que el recorrido no puede
encontrar.

### Barridos del compilador: dos resultados nulos, medidos

Dos categorías de `javac -Xlint` se agotaron sin encontrar un solo defecto. Vale
la pena dejarlo escrito para que nadie las vuelva a correr esperando algo.

- **`fallthrough`: 9 sitios, 0 bugs.** Tres en el datapack y seis en el core. Dos
  son deliberados y están documentados con un comentario (`// break; fallthrough`
  en `Q00610`/`Q00616`, `// Fallthrough.` en `Creature`). Los otros siete caen en
  un `default: { break; }`, o sea que el "olvido" aterriza sobre un `break` y no
  cambia nada.

- **`lossy-conversions`: 41 sitios, 0 bugs.** Casi todos son
  `campoEntero *= multiplicadorFlotante` en `NpcTemplate`, donde la truncación de
  la fracción sobre estadísticas de NPC no significa nada. El único que parecía
  serio era `Quest.addExpAndSp`: `addExp` es `long` y los rates son `float`, así
  que `addExp *= rate` calcula **en float**, cuya mantisa de 24 bits no representa
  exactamente valores por encima de 16,7 millones.

  **Medido en vez de argumentado.** Con rate 1.0 el error es cero hasta bien
  entrados los cientos de millones; recién a 1.546.580.000 aparece, y vale **32
  puntos**. La mayor recompensa de experiencia de todo el datapack es **2.299.404**,
  donde el error es exactamente cero. Es imprecisión real y no vale un cambio.

### Cómo se cubrió el área

De los 67 archivos se leyeron línea por línea los **17 que concentran el riesgo**:
los nueve `bosses/`, `ClassMaster` y `CastleChamberlain` (los dos de más de mil
líneas), `FourSepulchers`, `OracleTeleport`, `WyvernManager`, `KetraOrcSupport`,
`VarkaSilenosSupport` y `SiegeGuards` — unas 7.500 de las 14.462 líneas. Los 50
restantes promedian noventa líneas de diálogo y spawn.

Sobre los **67** se corrieron barridos: índices de array con variable, parseo de
tokens del bypass, cobros de adena, y las ocho categorías de `-Xlint`. Los cuatro
están anotados arriba con lo que encontraron y con lo que descartaron.

### Anotado sin tocar (datapack `ai/`)

- **`ClassMaster` parsea dos tokens del bypass sin guarda** y desreferencia
  `ClassListData.getClass(classId)` sin chequear. El despachador de eventos
  atrapa y loguea, así que queda contenido, pero es una vía fácil de ensuciar el
  log. Su `canChange` **sí** valida categoría y nivel en la rama que otorga.

- **`onPlayerBypass` hace `substring(18)` sobre un prefijo de 19 caracteres**, así
  que el evento que pasa empieza con un espacio. Funciona porque
  `StringTokenizer` se lo saltea; `substring(19)` sería lo correcto.

- **178 sitios en 57 scripts parsean un token del bypass sin guarda.** Barridos
  buscando la forma que sí hizo daño en `Q00631` —el valor parseado usado como
  índice— quedan dos, y los dos estaban bien protegidos con `containsKey`.

- **`QuestGuard` despacha `new OnAttackableKill(null, this, false)`**, o sea con
  atacante nulo, mientras `Attackable.doDie` solo despacha cuando hay un `Player`.
  Cualquier `onKill` registrado para un id de `QuestGuard` recibe `killer == null`.

- **El respawn de grand boss sale de `intervalo + getRandom(-rango, +rango)`.** Un
  rango configurado mayor que el intervalo daría un delay negativo, que
  `validateDelay` aplasta a cero: el boss reviviría al instante y con un
  `respawn_time` en el pasado. Con los valores distribuidos (intervalo ≥ 36 h,
  rango 8) no se alcanza.

### Sospechas evaluadas y descartadas (datapack `ai/`)

- **`ClassMaster` permitiendo tercera clase a nivel 40.** Los nombres de las
  categorías van corridos uno respecto de la terminología de "primer/segundo/tercer
  cambio de clase": `THIRD_CLASS_GROUP` es lo que se obtiene en el **segundo**
  cambio, a nivel 40. El propio `case "thirdclass"` del archivo, que exige
  `THIRD_CLASS_GROUP` y nivel > 75, confirma la semántica desde adentro.

- **`ClassMaster` cargando su XML de una ruta equivocada.** Hace
  `parseDatapackFile("config/ClassMaster.xml")` y el archivo está en
  `dist/game/config/`, no bajo `data/`. `parseDatapackFile` resuelve con
  `new File(".", path)`, o sea contra el directorio de trabajo del proceso, que
  para el gameserver es `dist/game/`. Ruta correcta, y los otros cinco llamadores
  del mismo patrón cargan archivos de ese mismo directorio.

- **Bosses que no vuelven de la muerte tras un reinicio.** Los cuatro que usan el
  modelo ALIVE/DEAD (`Core`, `Orfen`, `QueenAnt`, `Zaken`) comparan el
  `respawn_time` guardado contra el reloj y, si ya venció, spawnean en el acto y
  ponen el estado en ALIVE. Los cuatro.

- **`Valakas` sin ninguna transición a ALIVE.** Usa otro vocabulario —DORMANT,
  WAITING, FIGHTING, DEAD— igual que `Antharas` y `Baium`. El grep medía la
  palabra equivocada.

- **`onKill` de los bosses con `killer` nulo**, que reventaría en
  `ZONE.isCharacterInZone(killer)` porque ese método hace `getObjectId()` sin
  chequear null, dejando al boss en IN_FIGHT sin respawn agendado.
  `Attackable.doDie` solo despacha el evento cuando el matador existe **y** es un
  `Player`.

## `commons` — TERMINADA

Son **47** archivos y **11.240** líneas. Es infraestructura compartida: cada
defecto acá se multiplica por todo el servidor.

**Leído:** `network/` entero salvo los buffers de escritura —`ResourcePool`,
`BufferPool`, `Connection`, `ReadHandler`, `PacketExecutor`, `ConnectionConfig`,
`ReadablePacket`, `ReadableBuffer`—, `threads/ThreadPool`,
`database/DatabaseFactory`, la familia de parseo de `util/IXmlReader` y los
ayudantes de `util/StringUtil`.

**No leido linea por linea:** `ui/` (3 archivos Swing, sin logica de juego) y el
detalle interno de `DynamicPacketBuffer`, cuyo control de limites se comprobo
desde afuera.

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


### Segunda vuelta de `commons`: config, tiempo, deadlock y checksum

| # | Archivo | Defecto | Severidad |
|---|---|---|---|
| 8 | `DeadlockWatcher` | el `sleep` adentro del `try`, así que una excepción deja el hilo girando sin pausa | **crítica** |
| 9 | `DeadlockWatcher` | un NPE en el reporte se lleva puesto el callback que reacciona al deadlock | alta |
| 10 | `NewCrypt` | `verifyChecksum` y `appendChecksum` ignoran el `offset` al acotar el bucle | alta |
| 11 | `ConfigReader` | `getBoolean` devuelve `false` en silencio para cualquier valor inválido | alta |
| 12 | 5 scripts de evento | el centinela "sin match futuro" se suma a 5000 y arranca el evento | alta |
| 13 | `ConfigReader` | ningún getter recorta, y `Properties.load` conserva el espacio final | media |
| 14 | 4 archivos | `toLowerCase()` sin locale alimentando comparaciones | media |
| 15 | `DeadlockWatcher` | reporte y callback repetidos cada intervalo para el mismo deadlock | media |

**8 y 9 — El vigilante de deadlocks fallando durante el deadlock.** `Server.ini`
trae `DeadlockWatcher = True` con intervalo de 20 s, así que este hilo corre en
todo servidor estándar. Tenía tres problemas que se componen exactamente cuando
hay un deadlock, que es el único momento en que importa.

El `sleep` era la última sentencia **dentro** del `try` que hace la detección y
el reporte. Cualquier excepción en ese camino saltaba al `catch`, que loguea y
deja que el bucle vuelva a girar **sin ninguna pausa**.

Y había una excepción disponible: el reporte llama
`monitor.getLockedStackFrame().getLineNumber()`, y `getLockedStackFrame` está
documentado para devolver `null` cuando el marco no está disponible. Ese NPE
abortaba el reporte y, como el callback está después en el mismo bloque, se
saltaba el callback — la máquina que avisa a los jugadores y arranca el reinicio
cuando `RestartOnDeadlock` está prendido. O sea: hay deadlock, el reporte tira,
el reinicio no ocurre, y el vigilante gira a toda velocidad relanzando la misma
excepción.

Tercero: los hilos en deadlock siguen en deadlock, así que
`findDeadlockedThreads` devuelve el mismo conjunto en cada pasada y un solo
incidente producía un reporte completo y otro callback cada 20 segundos.

**10 — Un `offset` que no se respeta.** Los dos métodos reciben `offset` y
`size`, arrancan el bucle en `offset` y lo acotan con `size - 4`. La región es
`[offset, offset + size)`, así que la última palabra empieza en
`offset + size - 4`. `crypt` y `decrypt`, más abajo **en el mismo archivo**,
escriben `i < (offset + size)`: ese contraste es el delator.

Medido en vez de argumentado: comparando la implementación contra una de
referencia sobre offsets 0..8 y tamaños 8..96, **115 de 207 combinaciones dan
distinto**. Todo offset 4 está mal en todo tamaño.

El único llamador con offset no nulo es `LoginServerThread.sendPacket`, con
offset 2 sobre un payload alineado a 8 bytes. Esa forma **no** está entre las
115: con el bucle avanzando de a 4 desde el índice 2 y el `count` alineado a 4,
el primer índice que el bucle rechaza es el mismo bajo cualquiera de las dos
cotas. El defecto es real en el método e inerte en el único sitio que podía
exponerlo — y eso es justo lo que vuelve seguro arreglarlo: antes del cambio la
forma viva ya coincidía con la referencia, y después coinciden las 207, así que
los bytes del enlace login↔gameserver no cambian.

**11 y 13 — El único getter cuyo fallo era mudo.** `getBoolean` llamaba a
`Boolean.parseBoolean`, que **nunca lanza** y responde `false` para cualquier
cosa que no sea `"true"`. Su `catch` no podía dispararse y el warning
"Invalid boolean" que protege no se imprimió jamás. Un `"True "` —el valor
correcto con un espacio invisible detrás— se leía como `false`, y lo mismo
`"yes"`, `"1"` y cualquier typo.

`Properties.load` conserva el espacio final de un valor y un `.ini` lo lleva
invisible: **dos de los archivos distribuidos lo tenían**. `getIntArray` era el
único getter que recortaba algo, y recorta cada elemento en vez del valor — el
delator de que alguien chocó con esto una vez y arregló la llamada que tenía
adelante. Los nueve getters tipados pasan ahora por un helper que recorta.

Verificado antes de cambiarlo: las 440 claves leídas con `getBoolean` tienen 351
asignaciones en los `.ini` distribuidos y **las 351 son `true` o `false`
válidos**, así que el cambio no agrega ni un warning a una instalación estándar.

**12 — Un centinela sumado a un delay.** `SchedulingPattern.next` devuelve `-1`
cuando no encuentra match dentro de su ventana de cuatro años, y
`getDelayToNextFromNow` lo pasa tal cual. Los cinco scripts de evento lo tratan
como duración: `addTimer(..., delay + 5000, ...)`, o sea **4999 ms**. El evento
arranca cinco segundos después de cargar en vez de nunca, y el mismo bloque en
`onTimerEvent` lo reprograma igual, así que **se reinicia cada cinco segundos**
mientras el servidor esté arriba. Corrido de verdad con el build actual:

```
[0 20 * * *] delay=8982704   addTimer recibiria 8987704 ms
[0 0 30 2 *] delay=-1        addTimer recibiria 4999 ms
```

**14 — Plegado de mayúsculas sin locale.** En turco y azerí una `I` mayúscula se
vuelve `ı` sin punto, así que cualquier palabra clave con `i` deja de calzar
después del plegado. El repo ya era inconsistente: 19 llamadas pasan locale, 123
no, y 60 de esas alimentan una comparación, un `switch` o un `valueOf`.

El que falla en una máquina real es `DatabaseBackup`: decide si prefijar la ruta
de MySQL con
`System.getProperty("os.name").toLowerCase().contains("win")`. En un Windows con
locale turco `"Windows"` se pliega a `"wındows"`, el test da falso y el backup no
corre. `SchedulingPattern` resuelve los alias `jan`-`dec` y `sun`-`sat` que su
propio javadoc anuncia con un lookup en minúsculas: `"fri"` lleva `i`.

### Anotado sin tocar (segunda vuelta de `commons`)

- **`Client.write` traga toda excepción con un `catch` vacío**, y `Client` no
  tiene `LOGGER`. Parte es deliberado: `writeDataToBuffer` usa
  `throw new Exception()` sin mensaje como control de flujo para "este paquete
  decidió no mandarse". Pero eso vuelve indistinguible un fallo real de
  serialización de una negativa normal, y los dos son invisibles. Separarlos pide
  un tipo de excepción propio en el camino más caliente del servidor.

- **`ArrayPacketBuffer.ensureSize` desborda**: `(_data.length + size) * 1.2`
  suma como `int` antes de promover a `double`, así que una suma por encima de
  `Integer.MAX_VALUE` da negativo y `Arrays.copyOf` tira
  `NegativeArraySizeException`. Haría falta un paquete de ~2 GB, y los salientes
  los genera el servidor.

- **`appendChecksum` no tiene la guarda de tamaño que sí tiene
  `verifyChecksum`** (`size` múltiplo de 4 y mayor que 4), así que un buffer
  corto llega como `ArrayIndexOutOfBoundsException`. Ningún llamador puede
  producirlo hoy.

- **`ConfigReader.getEnum` y `getDuration` tienen cero llamadores.** El primero
  hace exactamente lo que necesitaban los seis `Enum.valueOf` a mano del paquete
  de config.

- **~89 claves booleanas se leen sin estar en ningún `.ini` distribuido**, y cada
  una loguea "not found, using default" en cada arranque. Sumado a los otros
  tipos, son cientos de warnings por boot.

- **`getOffsettedDelayToNextFromNow` convierte el centinela -1 en 0** con su
  `Math.max(0, delay - offset)`. Sin llamadores.

- **119 sitios más de `toLowerCase()`/`toUpperCase()` sin locale** fuera de
  `commons`, 60 de ellos alimentando control de flujo. Se atienden área por área
  en vez de en un barrido a ciegas.

### Sospechas evaluadas y descartadas (segunda vuelta de `commons`)

- **`toByteBuffer` mandando la capacidad en vez de lo escrito.** `_limit` se pone
  igual a `_data.length` al crecer el buffer, lo que parecía enviar ceros de cola
  en cada paquete e inflar el caché `MAXIMUM_PACKET_SIZE`. **Falso:**
  `writeDataToBuffer` llama a `buffer.mark()` antes de devolver, y `mark()` hace
  `_limit = _index`.

- **`MAXIMUM_PACKET_SIZE` como mapa estático mutable** escrito desde los hilos de
  red. Las tres copias son `ConcurrentHashMap`.

- **`handleNotWritten` reintentando el mismo paquete para siempre** tras un fallo
  silencioso. Llama a `writeFairPacket()`, que toma el **siguiente** de la cola.

- **Doble liberación del buffer de escritura.** Cuando `writeDataToBuffer` libera
  y lanza, la asignación `buffer = packet.writeData(this)` nunca se completa, así
  que el `finally` recibe `null` y no vuelve a liberar.

- **`Boolean.parseBoolean` aceptando dígitos no ASCII vía `Character.isDigit`.**
  `Integer.parseInt` usa `Character.digit`, así que `isNumeric` y el parseo
  coinciden; la brecha real de `isNumeric` es el desborde, no el alfabeto.


### `crypt/` y `BCrypt` — verificados corriéndolos, no leyéndolos

Son 2.219 líneas entre `BlowfishEngine` (1.468) y `BCrypt` (751), casi todo
tablas S-box y constantes. Leerlas línea por línea rinde poco; lo que rinde es
comprobar que **produzcan la salida correcta**.

**`BlowfishEngine` es Blowfish estándar.** Contra los nueve vectores publicados
por Schneier, la primera corrida dio **0 de 9**. Pero el patrón delataba la
causa: `4EF997456198DD78` esperado contra `4597F94E78DD9861` obtenido es la misma
salida con **cada palabra de 32 bits invertida en bytes**. Las filas que no
calzaban ese patrón eran justo aquellas cuyo texto plano no es invariante bajo
esa inversión.

Reformulada la hipótesis —Blowfish estándar con E/S little-endian por palabra,
que es la convención del cliente L2— y vuelta a correr alimentando el texto plano
invertido y desinvirtiendo el cifrado: **9 de 9 exactos**. El round-trip
cifrar→descifrar da 9/9 también. No es un defecto, es la convención del
protocolo, y quedó demostrado sin leer las tablas.

**`BCrypt` es jBCrypt canónico y funciona.** Los dos puntos donde las copias
viejas de jBCrypt fallan están bien acá: usa `getBytes("UTF-8")` —no el charset
de la plataforma, así que el hash no depende del host— y `gensalt` usa
`SecureRandom`. Comprobado corriéndolo:

- round-trip 6/6 sobre contraseñas vacías, ASCII, con espacios, con símbolos y
  **con acentos y ñ**, que es lo que prueba el UTF-8;
- rechazo de contraseña incorrecta 6/6;
- dos hashes de la misma contraseña difieren y los dos verifican;
- el coste queda embebido y se respeta (`$2a$04$`, `$2a$06$`, `$2a$08$`);
- el truncado a 72 bytes es el comportamiento estándar de bcrypt, no un defecto;
- un hash malformado lanza, y el `catch (Exception)` que envuelve
  `isLoginValid` lo convierte en login rechazado más warning.

### Anotado sin tocar (`crypt`)

- **`BCrypt.checkpw` compara con `String.compareTo`**, que corta en el primer
  carácter distinto. `isLoginValid` usa `MessageDigest.isEqual` —de tiempo
  constante— para el camino legacy SHA en el **mismo método**. La asimetría es
  real, pero el beneficio práctico es despreciable y no toco una implementación
  vendorizada canónica por una ganancia teórica; misma regla que con
  `Arrays.equals` sobre el hex id en `loginserver`.

- **Solo se aceptan `$2$` y `$2a$`.** Un hash `$2y$` o `$2b$` migrado desde otro
  sistema lanza. Como `gensalt` solo produce `$2a$`, es internamente consistente,
  y el `catch` lo degrada a login rechazado.

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

---

## `network/clientpackets` — TERMINADA

201 archivos, 21.727 líneas. Todo lo del área entra por la red desde un cliente
que puede mandar cualquier cosa, así que el eje es: **qué se lee del paquete y
qué se comprueba antes de usarlo**.

### Primera vuelta: valores del paquete usados sin acotar

| # | Archivo | Defecto | Severidad |
|---|---|---|---|
| 1 | `RequestMakeMacro` | conteo con signo recortado solo por arriba | baja |
| 2 | `RequestSaveInventoryOrder` | ídem | baja |

**1 y 2 — Recortar un solo extremo.** Los dos leen un conteo y lo recortan contra
un techo, y después se lo pasan al constructor de un `ArrayList`. `readByte()`
devuelve un `byte` **con signo** —`readUnsignedByte()` existe justo por eso y no
tiene ni un llamador— y `readInt()` un `int` con signo, así que un conteo
negativo pasa el recorte y llega al constructor como capacidad ilegal.

Lo que los delata no es leerlos: es la tabla. De los **17** sitios del área que
dimensionan una colección con un valor del paquete, **14** llevan la guarda
completa de tres partes —piso, techo, y que el conteo declarado calce con los
bytes que quedan—, la misma que se lee palabra por palabra en `RequestSetCrop`.
Los tres restantes eran éstos dos más `RequestPreviewItem`, que recorta el
negativo a cero y rechaza por arriba, y está bien.

`ClientPacket.read()` atrapa y loguea, y `ReadHandler` solo ejecuta el paquete si
`read()` volvió true, así que el costo es una traza en el log y un paquete
descartado, no una caída.

### Segunda vuelta: captadores anulables desreferenciados

| # | Archivo | Defecto | Severidad |
|---|---|---|---|
| 3 | `RequestExOustFromMPCC`, `RequestPledgePowerGradeList` | el jugador sin comprobar | baja |
| 4 | `RequestSetCrop`, `RequestSetSeed` | el último NPC sin comprobar | media |
| 5 | `RequestExAcceptJoinMPCC` | los dos grupos sin comprobar | media |

**3 — Dos de doscientos.** `getPlayer()` es `getClient().getPlayer()`, que es null
hasta que el jugador entra al mundo. De los ~200 paquetes del área, todos menos
estos dos comprueban el resultado.

**4 — `getLastFolkNPC()` arranca en null** y solo se fija al hablar con un folk.
Estos dos llaman `.canInteract()` de una; **todos** los demás lectores del
repositorio lo meten en una variable y lo pasan por un `instanceof`, que absorbe
el null. Alcanzable por un miembro de un clan dueño de castillo que manda el
paquete recién entrado.

**5 — La invitación comprueba, la respuesta no.** `RequestExAskJoinMPCC` prueba
`isInParty()` antes de cada `getParty()`, en las seis ramas. Su contraparte usa
los dos grupos sin probar ninguno, y entre la invitación y la respuesta cualquiera
de los dos puede dejar el grupo. La guarda tenía que dejar limpio el pedido
pendiente, así que cae al cierre de abajo en vez de retornar.

### Tercera vuelta: lo que el área destapó afuera

| # | Archivo | Defecto | Severidad |
|---|---|---|---|
| 6 | `ClanTable`, `AuctionableHall` | el castillo y el salón nunca se sueltan | **alta** |

**6 — La otra mitad que faltaba.** `destroyClan` probaba `getCastleId()` y
`getHideoutId()` contra cero y, cuando eran cero, desinscribía al clan de lo que
**no** era suyo. Cuando no eran cero —cuando el clan sí era dueño— no pasaba
nada: el castillo se quedaba con un id de dueño apuntando a un clan inexistente,
y el salón con su id de dueño y `isFree` todavía en false.

Ése es exactamente el estado desde el cual las tareas periódicas de tasa buscan
al clan dueño, y las tres se guardan distinto: la de `Castle` prueba el id de
dueño, la de `ClanHall` prueba solo `isFree`, y la de `AuctionableHall` no prueba
nada. Los dos llamadores —el `//pledge dismiss` de administración y el
temporizador de disolución— no tienen precondición sobre ninguno de los dos.

Las ramas de cero están bien como están y quedaron intactas; lo que faltaba era
la otra mitad. El salón subastable va por `setFree`, que lo devuelve al mapa de
libres y limpia al dueño; el asediable no está en ese mapa, así que se libera
directo.

La tarea de tasa de `AuctionableHall` buscaba al dueño tres veces, guardaba la
primera en una variable que no usaba, y no probaba ninguna de las tres. Ahora usa
la que ya tiene.

### Cuarta vuelta: lectura dirigida de los archivos más grandes

| # | Archivo | Defecto | Severidad |
|---|---|---|---|
| 7 | `EnterWorld` | mapa estático al que solo se le pone | media |
| 8 | `EnterWorld` | la instancia buscada sin comprobar | media |
| 9 | `RequestEnchantItem` | el nivel se loguea después de ponerlo en cero | media |
| 10 | `TradeList` | la compra de tienda privada divide por un conteo que puede ser cero | **alta** |

**7 — Un caché sin desalojo.** `TRACE_HWINFO` es un `ConcurrentHashMap` estático
con clave la IP del cliente, y **nada** lo saca: crecía por toda la vida del
servidor, una entrada por cada IP distinta vista alguna vez. Es un atajo de
búsqueda cuyo fallo ya está resuelto —la variable de cuenta unas líneas abajo es
la copia durable—, así que quedó acotado. La función está detrás de
`EnableHardwareInfo`, que no está en ningún `.ini` distribuido y por código
arranca en `false`.

**8 — Dos llamadas, una carrera.** `getPlayer(objId)` recorre las instancias
vivas buscando la que contiene al jugador, así que el id que devuelve existía una
sentencia antes; `getInstance` sobre una destruida en el medio devolvía null.
Importa más acá que en cualquier otro paquete porque `ClientPacket.run()` trata a
`EnterWorld` distinto: una excepción **echa al jugador** en vez de quedar
logueada y tragada.

**9 — Leer después de escribir.** En el fallo bendito, `item.setEnchantLevel(0)`
y **después** `if (item.getEnchantLevel() > 0)` para decidir si el log lleva el
nivel. La rama con nivel estaba muerta: todo fallo bendito quedó registrado como
si el ítem hubiera sido +0. Sus ramas hermanas —el fallo seguro y el fallo
normal— leen el nivel antes de mutar. El registro de encantamientos es
justamente la herramienta forense contra las duplicaciones.

**10 — El hermano de quinientas líneas más abajo.** `privateStoreBuy` recorta el
conteo pedido a lo que la tienda todavía tiene y después pone `found = true` sin
condición. `privateStoreSell`, el método casi idéntico del mismo archivo, escribe
`found = item.getCount() > 0`: pliega el caso vacío dentro de la bandera. La vía
de compra después divide `MAX_ADENA` por ese conteo.

La vía de compra no puede copiar la forma de la de venta: su rama de "no
encontrado" **castiga** al jugador por trampa de paquete, que una entrada agotada
no es. Así que la entrada vacía se saltea sola.

El otro lector del mismo valor está bien: `RequestPrivateStoreBuy` rechaza
cualquier conteo por ítem menor a uno al leer el paquete, así que el cero solo
puede salir del recorte contra la tienda.

### Quinta vuelta: texto del cliente que se convierte en número

| # | Archivo | Defecto | Severidad |
|---|---|---|---|
| 11 | `PlayerTemplateData` | `ConcurrentHashMap.get(null)` lanza en vez de contestar null | **alta** |
| 12 | `Say2` | el analizador de ítems del chat se sale de la cadena | media |
| 13 | `RequestBypassToServer` | cinco ramas convierten sin validar | media |
| 14 | `HomeBoard` | los buffs se cobran antes de resolverse | **alta** |
| 15 | `PlayerConfig`, `RatesConfig`, `ChampionMonstersConfig` | una entrada mal escrita mata el arranque sin decir cuál | media |
| 16 | `PlayerConfig` | dos listas emparejadas por índice sin comprobar largos | media |

**11 — Un null que no llega a comprobarse.** `getTemplate(int)` es
`_playerTemplates.get(PlayerClass.getPlayerClass(classId))`, y `_playerTemplates`
es un `ConcurrentHashMap`, que **lanza** con clave null en vez de contestar null.
El id de clase entra derecho del paquete de creación de personaje sin validar, y
el enum cubre 89 valores en el rango 0..118 —del 58 al 77 hay un hueco, y afuera
del rango también—. `CharacterCreate` ya prueba la plantilla contra null y
contesta `CREATION_FAILED`; el throw pasaba **antes** de que esa prueba corriera,
así que el cliente no recibía nada en vez de un rechazo.

La misma vía de carga indexa el mismo mapa igual, pero está bien como se
distribuye: los **89** archivos de plantilla declaran 89 ids distintos y todos
están en el enum. Se deja como está.

Es la misma forma que ya había arreglado en `ClanHallTable.setFree`. Barrida
entera —mapa concurrente indexado con el resultado de otra búsqueda—: afinando a
claves que de verdad pueden ser null quedan cinco sitios, los dos de la carga de
arriba y tres de `SkillTreeData` cuya clave es un hash primitivo.

**12 — Tres formas de salirse de una cadena.** `parseAndPublishItem` busca `"ID="`
en el mensaje y camina los dígitos que siguen. El mensaje es lo que el jugador
tipeó: `"ID="` al final del texto hacía que `charAt` caminara más allá del final,
`"ID="` sin dígito atrás dejaba la cadena vacía para `parseInt`, y una corrida de
dígitos suficientemente larga quedaba fuera del rango de un int.

**13 — La rama que sí tenía la guarda.** De las cinco ramas del despacho de
bypass que convierten números, `item_` es **la única** que traía `try/catch`, y
eso es lo que delata al resto: `npc_` se apoya en `isNumeric`, que acepta una
corrida de dígitos de cualquier largo mientras `parseInt` no los toma todos;
`_match` y `_diary` llaman `nextToken` y `split("=")[1]` y `parseInt` sin
comprobar nada; `manor_menu_select` indexa tres pares posicionales igual.

`_match`, `_diary`, `manor_menu_select` y `report` están en la lista que
**saltea** la validación de acción html, así que el texto que llega a esas
conversiones es el que mandó el cliente.

**14 — Cobrar antes de resolver.** El tablero de buffs toma el precio y después
recorre las opciones. Cada una se partía por coma y las dos mitades iban a
`parseInt`, y la habilidad que nombran se buscaba y se le leía el id **antes** de
que nada comprobara que la búsqueda encontró algo. Una opción sin coma, una que
no es número, y una que nombra una habilidad inexistente lanzaban las tres — con
el pago ya hecho, así que el jugador quedaba cobrado y sin nada.

Ahora se resuelven primero y el precio sale de lo que sobrevive, así que un buff
no permitido ya no se saltea y se cobra igual, y se aplican solo si el cobro
efectivamente salió. El precio se calcula en long porque la cantidad viene del
bypass.

**15 y 16 — La misma clase, en la configuración.** Tres parsers parten una
propiedad e indexan las piezas de una: `PartyXpCutoffGaps` y
`PartyXpCutoffGapPercent`, `BossDropList`, `ChampionRewardItems`. Una entrada a
la que le falta un campo, o con algo que no es número, lanzaba fuera de la carga
y se llevaba el arranque entero puesto, sin nombrar ni la propiedad ni la
entrada. Es exactamente la forma que ya había arreglado una vez en
`ClassBalanceConfig`, y éstas son el resto. `StringUtil` gana el `parseDouble`
del que `parseInt` no tenía par.

Y `Party` recorre `PARTY_XP_CUTOFF_GAPS` indexando `PARTY_XP_CUTOFF_GAP_PERCENTS`
con el mismo contador: dos propiedades separadas que tienen que tener el mismo
largo, sin nada que lo garantice. Rellenar con cero hace que los tramos sin par
no den nada, y el warning nombra los dos largos.

### Sexta vuelta: productos en int hacia acumuladores que no lo son

| # | Archivo | Defecto | Severidad |
|---|---|---|---|
| 17 | `RequestBuyItem` | el chequeo antidesborde corre sobre el precio sin impuesto | **alta** |
| 18 | `RequestSellItem` | suma al total antes de probarlo, y en int | **alta** |
| 19 | `TradeList.validate` | el producto no se ensancha, así que el recorte no puede dispararse | media |
| 20 | `TradeList.privateStoreBuy` | acumula el peso en int, sin recorte | media |
| 21 | `Inventory.refreshWeight` | ídem 19 | media |
| 22 | `PetInventory.validateWeight` | una asignación compuesta estrecha un long en silencio | media |
| 23 | `SendWareHouseWithDrawList` | los dos totales en int hacia llamadas que toman long | media |

**17 — El precio crece después del chequeo.** `RequestBuyItem` acota
`cuenta * precio` por ítem, pero sobre el precio **antes** del impuesto, y en la
línea siguiente multiplica el precio por `(1 + castleTax + baseTax)`. El total
corría en `int`, así que el total con impuesto podía dar la vuelta, y un total
dado vuelta pasa el test de `MAX_ADENA` que está justo para eso.

Lo que lo delata está veinte líneas más abajo, en el mismo método: el test del
peso dice `weight > Integer.MAX_VALUE`, una condición que **solo** puede ser
cierta si el producto que alimenta ese long se ensancha primero — y no se
ensanchaba.

Medido en vez de supuesto: `MaxAdena` se distribuye en 2.000.000.000 y el
producto más caro de las **6357** entradas con precio de las listas de compra
vale 10.000.000, así que el chequeo por ítem admite cuentas cuyo total con el
`baseTax` de 10–15 % que traen los datos de mercaderes no entra en un int.

**18 — Sumar y después preguntar.** `RequestSellItem` agrega al total antes de
probarlo, y tanto el producto como el total eran `int`. Su test por ítem atrapa
un ítem que solo valga más que el límite, pero no la suma de varios. Ahora el
test corre primero, lo que además evita que la mercadería se mueva para el ítem
que rompe el límite.

**19, 20 y 21 — Un recorte que no podía dispararse.** Tres lugares acumulan el
peso cargado como `cuenta * peso` por ítem. Dos —`TradeList.validate` e
`Inventory.refreshWeight`— toman el total en `long` y cierran con
`Math.min(weight, Integer.MAX_VALUE)`. **Ese recorte es el delator**: solo puede
hacer algo si el producto que llega al long se calcula en long, y se calculaba en
int, así que daba la vuelta antes de que el recorte lo viera y `Math.min`
devolvía el valor dado vuelta tan campante.

El tercero, `privateStoreBuy`, tomaba el total en `int` sin recorte ninguno,
mientras su hermano cincuenta líneas más arriba lo toma en `long`.

Medido: la adena no pesa, y el apilable más pesado de los datos distribuidos pesa
**1000**, así que el producto int da la vuelta a las **2.147.484** unidades. Pasada
esa línea el peso informado se vuelve negativo y el límite de carga deja de
significar nada para ese personaje — que es exactamente el estado que el recorte
estaba escrito para evitar.

**22 — Un estrechamiento sin una palabra del compilador.**
`PetInventory.validateWeight` recibe una cuenta `long`, la multiplica por el peso
—producto `long`— y lo acumula en un `int` local. Una asignación compuesta
estrecha en silencio, y el resultado va después a una sobrecarga que toma `long`.
Su contraparte `ItemContainer.validateWeightByItemId` pasa el producto long
derecho.

### Sospechas de la sexta vuelta, con lo que las descarta

- **El total de la compra de tienda privada** (`TradeList:656`). No puede caer en
  ningún lado más que en negativo cuando da la vuelta: el total corriente y el
  producto por ítem están los dos acotados por `MAX_ADENA`, y el doble de eso
  todavía no completa una vuelta entera. Y el negativo se prueba.

- **`RequestBuySeed`.** Tiene el mismo `(totalPrice < 0)` treinta líneas debajo de
  donde está su chequeo de desborde. Y el peor producto que los datos permiten
  —`limit_seed * seedMaxPrice` sobre las 198 semillas— vale **3.936.000**.

- **`RequestPreviewItem`.** Acumula `WearPrice`, que se distribuye en **10**, sobre
  a lo sumo 100 ítems.

- **`RateSiegeGuardsPrice`** se distribuye en **1**, así que el escalado de precio
  de los guardias es un no-op.

- **`Party`** eleva al cuadrado el nivel de cada miembro: a lo sumo 65.025 para un
  grupo lleno.

- **Los dos índices del multisell no acotados.** `getAllItemsByItemId` devuelve un
  `ArrayList` nuevo —una foto, no una vista viva— y el conteo ya está validado
  contra el mismo filtro, así que los dos son seguros por construcción.

- **`RequestAcquireSkill`, `UseItem`, `MultiSellChoose`, `CharacterCreate`.**
  Leídos línea por línea sin hallazgos propios más allá del 11.

### Barrido: resultado de un split indexado directo

Sobre el núcleo y el datapack: **41** sitios. Repartidos así — 20 en comandos de
administración, 7 en `DocumentBase` (datos de skills), 4 en configuración, 3 en el
tablero comunitario, y el resto sueltos.

Los alcanzables desde el cliente son el 14 y dos de `ClanBoard`, y esos dos ya
están adentro de un `try/catch` con fallback. Los de configuración son el 15. Los
de comandos de administración quedan para el área `handlers/`.

### Séptima vuelta: la misma búsqueda escrita dos veces

| # | Archivo | Defecto | Severidad |
|---|---|---|---|
| 24 | `ClanHallManager`, `RequestRestartPoint` | la función se prueba y después se vuelve a buscar | media |
| 25 | `ClanHallTable`, `Auctioneer` | el salón se desreferencia sin comprobar, y el clan tampoco | media |

**24 — Lo que contestó el test no es lo que se desreferencia.** Trece lugares
prueban `getFunction(...)` contra null y después llaman `getFunction(...)` **otra
vez** para usar la respuesta. `ClanHall.removeFunction` corre desde la tarea de
tasa del salón, que está programada en el pool, así que lo que contestó el test
no tiene por qué seguir ahí para la desreferencia. Once están en
`ClanHallManager`, donde el throw cae en el catch-all del bypass y muere la
interacción del jugador con el administrador; los otros dos en
`RequestRestartPoint`, donde deja al jugador **muerto** en vez de reaparecerlo.

De los **39** sitios que devolvió el barrido de "la misma búsqueda escrita dos
veces", estos trece son los únicos cuyo valor otro hilo puede sacar del medio: el
resto son lecturas de atributos XML durante el parseo, padres de nodos del
buscador de caminos, y trabajo de un solo hilo por el estilo.

**25 — Un hermano que comprueba y otro que no.** `getClanHallByOwner` lee el id
del clan de una, mientras `getCastleByOwner`, su contraparte, contesta null para
un clan null. Cinco lugares de `Auctioneer` desreferencian su respuesta en el
acto, y dos la buscan dos veces. La respuesta es null para un clan que no posee
salón, y tener `ClanAccess.HALL_AUCTION` no implica poseer uno, así que un líder
de clan común llega a todos. Ahora rechazan igual que la rama de al lado ya
rechaza a un jugador sin clan.

`Auctioneer` vive en `model/actor`, que todavía no se abrió; esto es solo lo que
el barrido destapó, y el archivo se lee entero cuando llegue esa área.

### Cómo se cubrió el área

De los **201** archivos se leyeron línea por línea los que los barridos señalaron
más los ocho más grandes: `RequestActionUse` (758), `EnterWorld` (644),
`MultiSellChoose` (434), `RequestEnchantItem` (392), `CharacterCreate` (333),
`RequestBypassToServer` (312), `UseItem` (304), `Say2` (293), `RequestBuyItem`
(291), `RequestAcquireSkill` (280), `RequestRestartPoint` (276) y
`RequestJoinParty` (261). Sobre los 201 corrieron **quince** barridos:

1. valor del paquete como tamaño de alojamiento o cota de bucle — 17, **2 defectos**;
2. `getPlayer()` desreferenciado sin comprobar — **2 defectos**;
3. operación compuesta sobre mapa concurrente — 0;
4. `containsKey` seguido de `get` — 0;
5. división o módulo por variable — 3, descartados midiendo;
6. producto sin ensanchar hacia un long o double — **7 defectos** en la segunda
   pasada, después de corregir el barrido para ver argumentos y retornos;
7. indexado con un valor del paquete — 1, ya guardado;
8. retorno de retiro de ítem descartado — 2 en el área, descartados;
9. encadenamientos sobre captador anulable — 12 candidatos, **3 defectos**;
10. campos sin asignar por un `readImpl` que retorna temprano — 2, guardados;
11. `ClanTable.getClan` sin comprobar — 106 sitios, que llevaron al hallazgo 6;
12. mapa concurrente indexado con otra búsqueda — 5, **1 defecto**;
13. resultado de un `split` indexado directo — 41, **2 defectos** más los de config;
14. misma búsqueda probada y vuelta a buscar — 39, **13 defectos**;
15. estado estático mutable en paquetes y `synchronized` solitario — 0 defectos.

Dos mecanismos quedaron descartados enteros: los **acumuladores de dinero**, donde
la aritmética misma prueba que un total acotado por `MAX_ADENA` solo puede dar la
vuelta hacia el negativo y el negativo se prueba; y los **índices del multisell**,
seguros porque `getAllItemsByItemId` devuelve una foto y no una vista viva.

Queda anotado sin tocar: los dos `LOGGER_CHAT` de `Say2` y `RequestSendFriendMsg`
son `static` sin `final`, se asignan una vez y solo se leen — estilo, no defecto.

### Sospechas de la cuarta vuelta, evaluadas y descartadas

- **`RequestActionUse`, el archivo más grande del área (758 líneas).** Los casos
  de mascota que no llaman `validateSummon` no lo necesitan: `useSkill` valida
  adentro, en sus dos sobrecargas. `mountPlayer` arranca con `pet != null`.

- **Los datos de mascota faltantes.** `getPetData` **detecta** que faltan, lo
  loguea, y devuelve el null igual a llamadores que lo desreferencian —incluido
  uno dentro de la tarea periódica de comida—. Pero está fuera de alcance: las
  **12** mascotas están definidas en `data/stats/pets`, los dos ids de montura
  fijos del datapack (12526 y 12621) están entre ellas, y el efecto `Ride`, que
  tomaría un id arbitrario con default 0, no se usa en ningún dato de skill
  distribuido.

- **Borrar mientras se itera el inventario.** La protección contra sobre-encantado
  de `EnterWorld` destruye ítems dentro de un `for` sobre `getItems()`, y
  `updateItems` de `TradeList` quita elementos dentro de un `for` sobre `_items`.
  Los dos contenedores son `ConcurrentHashMap.newKeySet()`.

- **SQL armado por concatenación.** Ninguno en los 201 archivos.

- **Barrido de lectura rancia**: un setter a valor neutro seguido de una lectura
  de lo mismo, en todo el núcleo y el datapack. **12** candidatos, **1 defecto**
  (el 9). Los otros once los salva un `return`, un `continue`, o son ramas
  excluyentes, o leen el valor nuevo a propósito.

### Sospechas evaluadas y descartadas, con lo que las descarta

- **El producto de la finca.** `getPrice()` es `count * price` en int. El peor
  caso con los datos distribuidos da **10.012.500**, y haría falta
  `RateDropManor ≥ 214,5` para desbordar; se distribuye en **1**. El
  `cropLimit = limit_crops * RateDropManor` necesitaría **238.610**. El `weight`
  int necesitaría **1.073.741.824** unidades de una recompensa cuando el tope de
  cantidad es **9.000**. Las **34** recompensas de cultivo tienen entrada de ítem,
  así que el `template` no puede venir null. Y `Seed.getReward(type)` es un
  ternario, no un índice: cualquier tipo distinto de 1 cae en la segunda
  recompensa.

- **La división de las tiendas privadas.** `(MAX_ADENA / _count) < _price`
  dividiría por cero, pero `cnt < 1` se rechaza al leer. Y el `getPrice()` de al
  lado multiplica en int: no puede desbordar porque **`MAX_ADENA` es un `int`**,
  así que el producto que la guarda deja pasar cabe en int por construcción.

- **El impuesto del multisell.** `getTaxAmount() * _amount` es int por int hacia
  un `addToTreasury(long)`, y el archivo ya ensancha a long en cinco lugares. No
  hace falta un sexto: el importe gravado se suma **dentro** del ingrediente de
  adena, y el chequeo de veinte líneas más arriba ya acota ese producto a
  `Integer.MAX_VALUE`.

- **`Npc.getCastle()` devolviendo null.** Puede, explícitamente. Pero el respaldo
  es `findNearestCastleIndex` con cota `Long.MAX_VALUE`, que recorre todos los
  castillos: solo da negativo si no hay ninguno cargado.

- **Retornos de retiro de ítem descartados.** 60 descartados contra 92 usados en
  todo el repositorio: población difusa, no una clase. Los dos del área
  —`RequestExEnchantSkill` y `RequestHennaEquip`— son ventanas de carrera y su
  orden ya es el correcto o neutro; cambiarlo cambia una carrera por otra.

- **Cobertura del protector de inundación.** 19 de 33 paquetes que mueven ítems o
  dinero lo consultan. También difuso: creación de personaje o entrada al mundo
  están limitados por su naturaleza. Es política, no defecto.

- **Campos que un `readImpl` deja sin asignar.** Dos aciertos,
  `RequestSetAllyCrest` y `RequestSetPledgeCrest`, y los dos guardan su `byte[]`
  de forma indirecta: `runImpl` vuelve a probar el largo, que es exactamente la
  condición que lo deja null.

- **`ClanTable.getClan` sin comprobar.** 36 sitios contra 70 comprobados; 21 sin
  ni siquiera un test del id. Los alcanzables desde esta área resultaron todos
  guardados —en el sitio, como `ExShowCastleInfo`, o en el único llamador, como
  `AllianceInfo`— y eso es lo que terminó apuntando a `destroyClan`. Los de
  `Siege` quedan para cuando se abra esa área.

### Barridos corridos sobre los 201 archivos

1. valor del paquete como tamaño de alojamiento o cota de bucle — 17, **2 defectos**;
2. `getPlayer()` desreferenciado sin comprobar — **2 defectos**;
3. operación compuesta sobre mapa concurrente — 0;
4. `containsKey` seguido de `get` — 0;
5. división o módulo por variable — 3, todos descartados midiendo;
6. producto asignado a long sin ensanchar — 0 (el barrido no veía argumentos ni
   retornos, y por eso se revisó a mano el del multisell);
7. indexado con un valor del paquete — 1, ya guardado;
8. retorno de retiro de ítem descartado — 2 en el área, descartados;
9. encadenamientos sobre captador anulable, con las guardas que cada uno acepta —
   12 candidatos, **3 defectos**;
10. campos sin asignar por un `readImpl` que retorna temprano — 2, guardados;
11. `ClanTable.getClan` sin comprobar — 106 sitios, que llevaron al hallazgo 6.

### Método

Los dos barridos que rindieron son el mismo movimiento: **listar toda la
población y ver quién no lleva la guarda que llevan los demás**. Catorce de
diecisiete con la guarda de tres partes; ciento noventa y ocho de doscientos con
el chequeo de null del jugador; todos menos dos pasando el último NPC por un
`instanceof`. Cuando la proporción se da vuelta —60 contra 92, 19 contra 14— no
hay clase que encontrar y el barrido se descarta entero, que es igual de útil
porque es lo que impide volver a correrlo.

El hallazgo 6 no salió de leer `ClanTable`: salió de que un barrido del área
llegó hasta `RequestSetAllyCrest`, que resultó guardado, y al mirar por qué
apareció la pregunta de qué pasa con un clan destruido que era dueño de algo.

---

## datapack `handlers/` — TERMINADA

375 archivos, 51.203 líneas, repartidos en doce carpetas. Las dos grandes son
`admincommandhandlers` (75 archivos, 19.522 líneas) y `effecthandlers` (145,
12.156).

### La medición que decidió treinta candidatos de una

Antes de tocar nada había que contestar: **¿cuánto texto tecleado por el jugador
llega de verdad a estos manejadores?** Porque los bypasses pasan por
`validateHtmlAction`, y de eso depende si un `Integer.parseInt` sin guarda es un
defecto o está parseando texto que escribió el propio servidor.

`validateHtmlAction` es **coincidencia exacta**, salvo cuando la acción cacheada
termina en el carácter de parámetro variable (`$`), donde pasa a ser coincidencia
de **prefijo** — o sea, texto arbitrario detrás.

Sobre el html distribuido: de **7.746** bypasses, **297** terminan en parámetro
variable. **286 de ésos son de administración**, y `AdminCommandHandler` envuelve
cada manejador en un `catch (RuntimeException)` que le informa al GM el comando
que falló. Quedan **once** sitios donde texto de un jugador común llega a un
manejador, y los once **ya guardan**: `SevenSigns` envuelve sus dos montos
tecleados con página de error, `Auctioneer` tiene la puja en `try/catch`
anidados.

Eso descarta de una los **20** sitios de `split` indexado de los comandos de
administración y los **27** parses de los manejadores de bypass.

### Pero la misma medición destapó lo contrario

**`_bbs` está en la lista de prefijos que saltean la validación entera.** Todo el
tablero comunitario recibe lo que el cliente mandó, campo por campo.

| # | Archivo | Defecto | Severidad |
|---|---|---|---|
| 1 | `HomeBoard` | cuatro servicios pagos que cobran con la respuesta descartada | **alta** |
| 2 | `Loto` | paga el premio antes de tomar el boleto | **alta** |
| 3 | `DropSearchBoard` | id, página y caché sin comprobar | media |
| 4 | `FriendsBoard`, `MailBoard` | el token de acción de un tokenizador que puede no tenerlo | media |
| 5 | `HomeBoard`, `ClanBoard`, `FavoriteBoard` | tres conversiones más sin acotar | media |
| 6 | `ClanBoard` | una rama llega a leer el id del clan sin comprobarlo | media |
| 7 | `RebalanceHP` | cero sobre cero hacia `setCurrentHp` | media |
| 8 | `Escape` | la rama del jugador asume que el afectado lo es | media |
| 9 | `NpcActionShift` | el archivo de spawn de una plantilla desconocida | baja |
| 10 | `AdminDebug` | dos búsquedas que la tarea puede sobrevivir | baja |
| 11 | `AdminSkill` | el guardado de un GM en una única ranura estática | media |
| 12 | `AutoPlay`, `ClanHandler`, `ChatAdmin` | ocho parámetros tecleados sin comprobar | media |

**1 y 2 — El orden del cobro.** Cinco ramas del tablero (buffs, teleport, heal,
delevel, premium) tomaban la tarifa con la respuesta **descartada** y entregaban
igual. La de teleport además bajaba las habilidades del jugador y mandaba el
paquete del tablero *antes* de intentar el cobro. Y `Loto` paga el premio antes
de tomar el boleto, descartando si lo tomó.

Arreglada primero la de buffs, dejar las otras cuatro era la inconsistencia; cada
una otorga ahora dentro del resultado del propio cobro.

**11 — Una ranura para todos.** `AdminSkill` guarda las habilidades propias de un
GM mientras lleva puestas las de otro jugador. El campo es estático: dos GMs
usándolo a la vez se intercambian los conjuntos, y el que restaura primero vacía
la ranura y deja al otro con el aviso de que nunca tomó nada.

**12 — Uno de siete lo hacía bien.** `AutoPlay` lee siete números de lo que el
jugador tecleó; el del porcentaje de poción prueba con `isNumeric` y los otros
seis van derecho a `parseInt`. Ése es el delator. Y `isNumeric` sola tampoco
alcanza en ningún lado: prueba que los caracteres son dígitos, no que el número
entre en un int. `ClanHandler` prueba `startsWith("privileges")` —diez
caracteres— y corta con `substring(11)`.

### Segunda vuelta: lo que persiste y lo que no lanza

El catch de `RuntimeException` del despachador cubre todo lo que tira en un
comando de administración, así que lo que queda ahí es lo que **no** tira:
escrituras que persisten y acciones sobre el objetivo equivocado.

| # | Archivo | Defecto | Severidad |
|---|---|---|---|
| 13 | `AdminShowQuests` | cuatro consultas armadas pegando valores en el texto | media |
| 14 | `AdminEditChar` | conexión filtrada y un `UPDATE` sin valor que asignar | **alta** |
| 15 | `AdminEffects`, `AdminMobGroup` | el objetivo leído sin comprobar | baja |

**14 — Dos problemas en tres líneas.** El comando que quita la penalidad de clan
a un personaje **desconectado** abre la conexión fuera de un try-with-resources y
no la cierra nunca, así que cada uso filtra una del pool. Y la sentencia dice
`UPDATE characters SET clan_join_expiry_time WHERE char_name=?` — **sin valor que
asignar**, que no es algo que la base pueda ejecutar. La rama nunca funcionó.
Ahora pone la columna en cero, que es lo que hace la rama del personaje conectado.

**13 — Valores pegados en el texto de la consulta.** Tres de las cuatro pegan el
nombre de quest que el GM tecleó detrás del comando; la cuarta pega solo el id de
objeto del personaje. Las cuatro vinculan sus valores ahora, así que el texto de
la sentencia queda fijo y el nombre viaja como parámetro.

**15 — Cuarenta y dos de cuarenta y seis.** De los sitios que leen algo de
`getTarget()` en el acto, todos menos cuatro lo prueban antes o lo guardan en una
variable. Dos de los cuatro son estos.

### Cómo se cubrió el área

De los **375** archivos se leyeron a fondo los nueve del tablero comunitario y
todos los que los barridos señalaron. Sobre los 375 corrieron **dieciocho**
barridos:

1. operación compuesta sobre mapa concurrente — 2, descartados;
2. `containsKey` seguido de `get` — 4, el modismo de conteo sobre mapas locales;
3. división o módulo por variable — 6, **1 defecto** (`RebalanceHP`);
4. producto sin ensanchar — 18, todos `double` por `double`;
5. `onExit` que muta de forma relativa — 3 con el patrón corregido, los tres
   guardan (el primer patrón no leía mutaciones a través de un índice);
6. campo escrito en `onStart` y leído en `onExit` — **0**, que cierra el pendiente;
7. `split` indexado directo — 20 en administración, descartados por el catch;
8. lectura de texto tecleado por el jugador — 44, **9 defectos** en el tablero;
9. encadenamientos sobre captador anulable — 13, **3 defectos**;
10. misma búsqueda probada y repetida — 29, descartados;
11. retiro de ítem con retorno descartado — 5, **5 defectos**;
12. orden entre tomar y dar en `itemhandlers` — 1 archivo, correcto;
13. estado estático mutable — 11, **1 defecto** (`AdminSkill`);
14. indexado por un valor calculado — 3, los arreglos miden 119 contra id 118;
15. guardas comparadas entre `targethandlers` — 34, todos delegan en el ayudante;
16. SQL armado por concatenación — 5, **2 defectos**;
17. conexión fuera de try-with-resources — 19, **1 defecto**;
18. `synchronized` solitario, bloqueo dentro de un handler, y bandera booleana sin
    `try/finally` — **0** en los tres.

### Tres mecanismos descartados enteros

- **El catch del despachador de administración.** `AdminCommandHandler` envuelve
  cada manejador en `catch (RuntimeException)` y le informa al GM el comando que
  falló. Eso cubre los 20 sitios de `split` indexado y las conversiones sin
  acotar de esa carpeta.

- **El chequeo de cercanía vive un nivel más arriba.** Seis manejadores de bypass
  mueven ítems o teletransportan sin comprobar la distancia al NPC, y no hace
  falta: `RequestBypassToServer` rechaza el bypass si el jugador se alejó del NPC
  de origen, antes de que el manejador corra.

- **La división por defensa.** No es una guarda faltante sino cómo este código
  calcula daño en todos lados: `Formulas` sola divide por `defence` en ocho
  lugares.

### El pendiente arrastrado desde `serverpackets`, cerrado

`BuffInfo.initializeEffects` saltea `onStart` cuando el objetivo está muerto y la
habilidad no es pasiva; `finishEffects` llama `onExit` **sin condición**. La
premisa era exacta.

Pero no cuesta nada: barridos los **145** manejadores de efecto buscando un campo
escrito en `onStart` y leído en `onExit` —**cero**— y `onExit` que mute de forma
relativa. Ese segundo barrido hubo que correrlo **dos veces**: el primero devolvió
`Disarm` y `Distrust`, y su patrón no sabía leer una mutación a través de un
índice, así que se perdía justamente los dos casos que importaban. Corregido
devuelve **tres**: `Disarm`, `AttackTrait` y `DefenceTrait`.

Los tres guardan, y los dos de rasgos guardan **con la respuesta exacta al
problema**: llevan un contador por rasgo que hace de libro mayor de si `onStart`
llegó a correr, y `onExit` no divide si el contador está en cero. El hilo no
vuelve, ahora por la razón correcta.

### Sospechas evaluadas y descartadas, con lo que las descarta

- **La división por defensa** (`EnergyDamage`). No es una guarda faltante sino
  cómo este código calcula daño en todos lados: `Formulas` sola divide por
  `defence` en ocho lugares. Se descarta como clase, no se parchea en un
  manejador suelto.

- **Los arreglos de `ClassBalanceConfig` indexados por id de clase.** Los **37**
  miden **119**, y el id más alto del enum `PlayerClass` es **118**. El tamaño
  está elegido a propósito; el índice entra por construcción.

- **Duplicar una cosecha.** `Attackable.takeHarvest()` es `getAndSet(null)`, y
  `takeSweep()` igual: la segunda llamada recibe null y la guarda de arriba hace
  el resto.

- **Los 18 productos que el barrido de ensanchamiento marcó en los efectos.** Son
  todos `double` por `double`: `_power` está declarado `double` en
  `AbstractEffect`.

- **El orden en `itemhandlers`.** De los 25, uno solo toma y da, y consume
  primero con el retorno comprobado.

- **El `SimpleDateFormat` estático de `AdminPunishment`** —la versión clásica de
  este problema— ya se usa dentro de un `synchronized` sobre sí mismo.

- **La aritmética de `substring` de `ChatGeneral`.** Parece frágil y es segura por
  construcción: el primer token arranca en el índice cero porque la línea empieza
  con el punto, y la rama solo corre cuando hay un segundo token, así que el corte
  siempre cae dentro.

- **Los cuatro `containsKey` seguidos de `get`** son el modismo de conteo sobre
  mapas locales de un método.

---

## `model/actor` — TERMINADA

166 archivos, 53.236 líneas. `Player.java` sola tiene **12.131**, `Creature.java`
6.483, y después nada pasa de 1.900.

### Primera vuelta: la misma búsqueda escrita dos veces

| # | Archivo | Defecto | Severidad |
|---|---|---|---|
| 1 | `ClanHallManager`, `PetStat`, `CastleDoorman`, `SiegeFlag`, `Player` | trece búsquedas vivas probadas y vueltas a escribir | media |
| 2 | `SignsPriest` | el intercambio de las tres piedras paga sin mirar si tomó | **alta** |
| 3 | `Auctioneer` | seis ramas leen el clan sin preguntar, y una encadena tres búsquedas | media |
| 4 | `ClanHallManager` | el salón desreferenciado en la primera línea del bypass | **alta** |
| 5 | `ClanHallDoorman` | ídem en las dos puertas | media |

**1 — Lo que contestó el test no es lo que se usa.** Ocho de los trece son la
forma de **una sola línea** del caso de `ClanHallManager` ya arreglado; la pasada
anterior solo había matcheado la de varias líneas. `PetStat` lee el ítem de
control de la mascota, que es una consulta al inventario por id en cada llamada.
`CastleDoorman` y `SiegeFlag` leen `getConquerableHall()`, que es una **búsqueda
espacial** cada vez — el portero corría dos por puerta, dentro del bucle. Y
`Player` prueba `_client` y lo lee dos veces más en la misma expresión, en
`isJailed` y en `isChatBanned`; el campo se anula al desconectar y los dos métodos
se llaman desde el camino del chat.

**2 — El hermano de treinta líneas más abajo.** Intercambiar los tres tipos de
piedra de sello a la vez hacía tres retiros con la respuesta descartada y pagaba
la adena antigua por los tres conteos pase lo que pase. Intercambiar **un solo**
tipo sí comprueba que el retiro salió antes de pagar. La recompensa ahora se
calcula sobre lo que efectivamente se tomó.

**3 — Seis de diez.** De las ramas del subastador que leen el clan del jugador,
`bid1` y `selectedItems` lo comprueban, y dos más quedan cubiertas por
`hasAccess`. Eso último se verificó en vez de suponerse: `setClan(null)` reemplaza
los privilegios por un conjunto vacío y los privilegios solo se otorgan desde el
rango de un clan, así que **`hasAccess` no puede ser cierto sin clan**. Las seis
restantes lo leían sin preguntar. `cancelBid` además encadena tres búsquedas sin
probar ninguna: la subasta es null cuando el clan no tiene puja en pie, y el
pujador es null cuando la puja no es de ese clan.

**4 — Una comprobación que cubre veinticinco lecturas.** `getClanHall()` busca un
salón a 500 unidades del NPC, cae al registro de salones asediables, y devuelve
**null** si ninguno lo encuentra. `onBypassFeedback` lo desreferencia en su
**primera línea**, así que un administrador parado en otro lado tiraba abajo
cualquier bypass que le llegara. Una sola guarda en la entrada alcanza porque
todas las lecturas de abajo llegan al salón por el id que esa primera llamada
cachea.

### Segunda vuelta: dos ids que no son el mismo id

| # | Archivo | Defecto | Severidad |
|---|---|---|---|
| 6 | `Player` | el ítem de control de la mascota comparado por id de plantilla | **alta** |
| 7 | `PvpConfig` | dos listas de ítems convertidas sin acotar | media |
| 8 | `TopicBBSManager` | el paginador de temas cuenta clanes | media |
| 9 | `GameServerThread` | el hilo arranca sin flujos que usar | media |

**6 — Uno de cinco.** Cuando un jugador muere y dropea ítems hay una guarda que
debería saltear el ítem que invoca a su mascota. Compara
`_summon.getControlObjectId()` contra `itemDrop.getId()` — pero el primero es un
**id de objeto** y el segundo el **id de plantilla**. Los ids de objeto arrancan
en 0x10000000 y los de plantilla llegan a unos diez mil: los dos números no
pueden ser iguales nunca, así que la guarda no se disparó jamás.

Lo delata el propio archivo: otros **cuatro** usos leen ese mismo valor y todos lo
comparan contra un id de objeto —`validateItemManipulation` dos veces, el id del
objeto de montura, y el ítem de control guardado—. Éste era el único que no.

**8 — Contar lo que no se pagina.** `TopicBBSManager` divide la cantidad de temas
por ocho y redondea hacia arriba comparando el resultado contra **la cantidad de
clanes del servidor**. La última página aparecía o desaparecía según un número sin
relación con ese foro. La forma viene del paginador de `ClanBoard`, que sí cuenta
lo que pagina.

**9 — Arrancar a medias.** `GameServerThread` abre los flujos del socket en su
constructor, loguea la `IOException` si falla, y arranca el hilo igual. `run()`
escribe el paquete inicial en su primera línea, y el bloqueo que `sendPacket` toma
sobre el flujo de salida revienta contra el null. `run()` solo atrapa
`IOException`, así que el hilo moría con un throw sin manejar y el socket abierto.

### Cómo se cubrió el área

De los **166** archivos, los de `instance/` —donde vive la mayoría de los
hallazgos— se leyeron a fondo, junto con los caminos de más riesgo de los dos
grandes: en `Player.java` (12.131 líneas) la persistencia, el drop por muerte, el
karma y el cambio de subclase; en `Creature.java` (6.483) el movimiento, la
aplicación de golpe y el sumidero de daño. Sobre los 166 corrieron **doce**
barridos, más los once que corren sobre todo el repositorio.

### Sospechas evaluadas y descartadas, con lo que las descarta

- **Los índices de parámetro de las sentencias preparadas.** **125** revisadas en
  núcleo y datapack, contando los `?` de cada consulta contra los `setXxx(n, …)`.
  13 marcadas, las 13 explicadas: vinculaciones por lote dentro de un bucle, y dos
  sentencias en el mismo try-with-resources. **Cero defectos.**

- **Columnas guardadas y nunca releídas.** Diez sobre la fila del personaje, cada
  una con su razón; ninguna es un valor que se pierda.

- **El camino de subclase.** `childOf(null)` recorre padres y devuelve false sin
  lanzar. Y el `getParent().getId()` detrás de `subClassId >= 88` es seguro por
  construcción: de las **89** constantes del enum, las **9** raíz tienen id ≤ 53 y
  de las **30** con id ≥ 88 ninguna lo es.

- **`synchronized` sobre un campo reasignable** (22) y **doble comprobación sin
  `volatile`** (12). Todos aguantan salvo el hallazgo 9: las tres colecciones que
  parecían candados reasignables son `final`, y los doce campos de doble
  comprobación son `volatile` —incluido el de `Cubic`, cuya declaración el barrido
  no supo leer por el comodín de `Future<?>`—.

- **Las absorciones de vida y maná en `onHitTimer`.** Recortan contra un margen que
  se vuelve negativo si la criatura está sobrecurada, y el recorte cae debajo del
  test de cero que sigue.

- **Las divisiones de movimiento de `Creature`.** Cada una está guardada por la
  rama en la que vive: `700/distance` solo corre con `distance > 700`, y el `else`
  de los senos solo con `distance >= 1.79`.

- **Los 32 captadores que entregan una colección interna no concurrente.** Son
  todos datos de solo lectura llenados en la carga.

- **El reparto de experiencia de `Attackable`.** Divide por un total entero, pero el
  total y el mapa de recompensas se incrementan bajo la misma condición.

- **`getClient().getFloodProtectors()`**: 36 sitios, ninguno comprueba null. Patrón
  de la casa.

---

## `model/stats` — TERMINADA

36 archivos contando el subpaquete `functions/`; `Formulas.java` sola tiene 1.830
líneas y es cada cálculo de daño, regeneración y probabilidad del juego.

| # | Archivo | Defecto | Severidad |
|---|---|---|---|
| 1 | `Calculator` | el arreglo de funciones publicado sin `volatile` | **alta** |
| 2 | `Creature` | seis eventos de combate compartidos en vez de construidos | **alta** |
| 3 | `Formulas` | el modificador de vulnerabilidad arranca en 1 y no en 0 | media |
| 4 | `Formulas` | cuatro búsquedas de función probadas y vueltas a escribir | media |
| 5 | `BaseStat` | el índice de las tablas de bonificación sin acotar al cargar | media |

**1 — La mitad de un copy-on-write.** `Calculator` publica su arreglo de funciones
desde tres mutadores `synchronized`, y lo leen `calc()` y `getFunctions()`, que
**no toman el candado**. Sin `volatile` el lector no tiene arista de precedencia
con el escritor y puede ver la referencia nueva con los elementos todavía en null
— y `calc()` corre en cada estadística que el servidor calcula. El patrón lo
delata: los tres mutadores están bloqueados, o sea que la intención estaba
entera; faltaba la palabra que hace correcta la publicación.

**2 — Un objeto por criatura para un evento por golpe.** `Creature` guardaba seis
objetos de evento como campos, repoblándolos y despachándolos. El objeto es de la
criatura, así que dos atacantes golpeando al mismo objetivo repueblan el mismo, y
un oyente puede recibir el atacante, el daño o la habilidad de otro golpe. El de
daño recibido se despacha **asincrónico**: lo lee en otro hilo después de que el
golpe siguiente ya lo sobrescribió.

Contado antes de tocar nada: **86** sitios de despacho construyen su evento donde
lo mandan y **7** reusan un campo. Seis de los siete eran éstos; el séptimo es
inmutable y queda.

**3 — Un punto de sesgo en cada debuff.** El inicial que se le pasa a `calcStat`
es lo que contesta cuando nada modifica la estadística, y entra derecho al
porcentaje: el caso neutro daba 1,01 en vez de 1. Su hermana `calcCancelEffects`
lee el mismo tipo de estadística desde cero, y de los **22** sitios del
repositorio donde un `calcStat` alimenta una división por 100, **los 22** usan
base 0. Éste era el único fuera de línea. **Mueve números**: cada probabilidad de
prender se corre un punto de su modificador.

### Sospechas evaluadas y descartadas, con lo que las descarta

- **El índice de las tablas de bonificación, del lado de la lectura.**
  `STRbonus[actor.getSTR()]` no acota nada y corre en cada cálculo. El techo real
  es **CON en 59** —base 47 en las plantillas, más tres hennas de 4— contra
  arreglos de 100, y los conjuntos de armadura distribuidos **no declaran ninguna
  estadística base**. Cuarenta y uno de margen, y ningún comando de administración
  fija esas estadísticas.

- **La resistencia a cancelación** se invierte si la resistencia combinada llega a
  100, pero el dato la declara con `sub` dieciséis veces, lo que hace el valor
  negativo y el divisor mayor que uno. La única declaración aditiva es un debuff de
  vulnerabilidad de +30 que no apila consigo mismo, y `cancelProf` no aparece.

- **`calcFallDam`** multiplica altura por vida máxima en `int`: el HP base más alto
  de las plantillas es **3.834**, contra los **134.218** que haría falta para
  desbordar con una caída de dieciséis mil.

- **La división por defensa** de las seis fórmulas de daño: no es una guarda
  faltante sino cómo este código calcula daño en todos lados.

- **`calcHitMiss`** recorta después del bono de condición y devuelve "falla" para
  una probabilidad baja; **`calcBlowSuccess`**, **`calcStealEffects`** y el bono de
  atributo recortan todos sus resultados.

- **`FuncEnchant`**: el sobre-encantado está acotado por la protección de
  sobre-encantado y por el límite de olimpiada.

- **Los rasgos de arma y defensa** se indexan por ordinal sobre arreglos
  dimensionados con `TraitType.values().length`, así que entran por construcción.

---

## `model/conditions` — TERMINADA

80 archivos, 4.989 líneas: la capa que decide si una habilidad o un ítem puede
usarse. Casi toda condición es un `testImpl` de tres líneas, así que el método
aquí no fue leer cada una sino **alinear a las hermanas**: cuando dieciséis
condiciones hacen lo mismo y cinco no, las cinco son el defecto.

| # | Archivo | Defecto | Severidad |
|---|---------|---------|-----------|
| 1 | `DocumentBase.parseLogicNot` | envuelve el null que `parseCondition` sabe devolver | **alta** |
| 2 | cinco condiciones de objetivo | leen `effected` sin comprobarlo, diez hermanas sí | **alta** |
| 3 | `ConditionUsingItemType` | piernas sin peto cae a la máscara vestida | media |
| 4 | `ConditionTargetAggro` | la rama de jugador ignora la bandera | media |
| 5 | `ConditionPlayerCallPc` | vuelca el objeto en un mensaje al jugador | media |
| 6 | `ConditionPlayerCanCreateBase` | prueba el clan atacante y lo vuelve a pedir | media |
| 7 | `ConditionPlayerCanResurrect` | igual, con `checkIsAttacker` de por medio | media |
| 8 | `ConditionSiegeZone` | lee las coordenadas de un objetivo que puede faltar | baja |
| 9 | `ConditionPlayerCanSummonSiegeGolem` | dos `getSiege()` y ninguno comprobado | baja |

**1 — El null que el propio parser fabrica.** `parseCondition` contesta null por
tres caminos: nodo desconocido, `<not>` vacío y atributo no reconocido. Las dos
combinadoras hermanas lo saben y lo descartan —`ConditionLogicAnd.add` y
`ConditionLogicOr.add` abren las dos con `if (condition == null) return;`— pero
`parseLogicNot` lo envolvía. El null quedaba guardado en el campo y se
desreferenciaba **en cada prueba de esa habilidad**. Un solo sitio de
construcción y veinte `<not>` en el dato distribuido, así que la guarda en el
parser es el arreglo entero.

**3 — La media armadura que pasaba.** El bloque de armadura devuelve en todos los
casos menos uno: piernas puestas y sin peto. Ése caía a `getWearedMask()` de
abajo, que hace un OR de **todas** las ranuras del muñeco, así que la media
armadura contestaba que sí donde el peto solo contesta que no. Alcanzable:
**33** declaraciones del dato entran por esa rama (LIGHT 19, HEAVY 8, MAGIC 6).

**4 — Una bandera honrada a medias.** El mismo método compara su rama de monstruo
contra `_isAggro` y su rama de jugador contra nada, así que `aggro="false"` daba
verdadero para un jugador con karma. Nada declara `aggro` hoy; el arreglo no
mueve el juego actual y deja de morder a quien lo escriba mañana.

**5 — El volcado del objeto.** `player.sendMessage(effected.asPlayer() + " is
dead...")` y `Player` no define `toString()`, así que hereda el de `WorldObject`
y al jugador le llegaba `Player:Nombre[268437456] is dead at the moment`. Un
barrido de **todos** los mensajes visibles del repositorio encontró éste y
ninguno más — pero solo después de corregirlo: el primer intento usaba
`[\w.()]+` para el operando, que es voraz e incluye el paréntesis, así que se
tragaba `sendMessage(` dentro del operando y **no encontraba ni el caso que ya
tenía en la mano**. Un barrido ciego no cuenta como barrido.

### Sospechas evaluadas y descartadas, con lo que las descarta

- **La división por un máximo cero.** `ConditionPlayerCp` divide por `getMaxCp()`,
  y `baseCpMax` es `set.getFloat("baseCpMax", 0)`: cero por defecto, y **ningún**
  fichero de npc declara cp. Parecía una excepción aritmética garantizada. No lo
  es: `getCurrentCp()` devuelve `double` y `getMaxCp()` `int`, así que la
  división es en coma flotante y no lanza — el NaN que da un máximo cero hace
  falsa toda comparación, que es la respuesta que se quiere. Las dos condiciones
  de peso **sí** comprueban su divisor porque las suyas son `int / int`: ahí la
  guarda evita una excepción real. La asimetría tiene motivo.

- **El lanzador sin comprobar**: 12 de 53 condiciones lo comprueban. Es el patrón
  de la casa, no una guarda faltante.

- **`skill` sin comprobar en `ConditionPlayerCanResurrect`**: `canResurrect` se
  declara **solo** en ficheros de skills —seis veces— y nunca en items, así que
  el `skill` nunca llega nulo.

- **`isBehind` / `isInFrontOf` / `isInSideOf`** con objetivo nulo: las tres
  guardan `target == null` en `ILocational` y contestan falso.

- **`checkIsAttacker(clan)`** es literalmente `getAttackerClan(clan) != null`, así
  que la rama que lo probaba y volvía a pedirlo era una sola pregunta hecha dos
  veces. Se sostiene la respuesta; no cambia la semántica.

### Dos asimetrías registradas y **no** tocadas

Las dos son preguntas de diseño, no de código: las dos lecturas son
internamente consistentes y nada en el repositorio decide cuál se quiso.

- **`ConditionPlayerCp` compara con `>=` donde `Hp` y `Mp` usan `<=`.** Lo que
  zanja el caso de `Hp` no sirve aquí: `hp="5"` bajo `>=` sería casi un no-op, y
  eso obliga a `<=`; `cp="50"` no es degenerado en ninguna de las dos lecturas.
  El parser trata las tres igual, el `msgId="113"` es el genérico "cannot be used
  due to unsuitable terms", y los dos únicos usos son las skills 1326 y 1327
  (Harmony y Symphony of Noblesse) con `<player cp="50" />` junto a
  `<player SiegeZone="126" />`. **Cambiarlo movería el juego sobre una conjetura.**

- **`ConditionPlayerCheckAbnormal` compara con `>=`** donde `ActiveEffectId` y
  `ActiveSkillId` usan `<=`. Además `checkabnormal` **no se declara ni una vez**
  en el dato distribuido, así que la pregunta es hoy teórica.

- **`ConditionPlayerCanTakeCastle` anuncia desde dentro de un predicado**
  (`announceToPlayer` de "el clan enemigo empezó a grabar"). Si la condición se
  prueba más de una vez por lanzamiento, el anuncio sale más de una vez. Es
  comportamiento existente del juego, no una guarda faltante; cambiarlo es una
  decisión de diseño.

---

## `model/script` — TERMINADA

9 archivos, 7.622 líneas: el motor de quests. `Quest.java` sola son 5.706. Es la
capa que más hilos cruza del repositorio —el temporizador dispara en el pool
mientras el hilo del paquete del dueño contesta un bypass— y no toma un candado
en ninguna parte.

| # | Archivo | Defecto | Severidad |
|---|---------|---------|-----------|
| 1 | `QuestState` | el mapa de variables, liso y construido con pereza | **alta** |
| 2 | `Quest` | el mapa de temporizadores, escrito bajo candado y leído sin él | **alta** |
| 3 | `QuestTimer` | el planificador publicado sin `volatile` | **alta** |
| 4 | `NpcTemplate` | las tres listas de botín probadas y vueltas a leer | **alta** |
| 5 | `SevenSignsFestival` | sigue tras atrapar el fallo del spawn | media |
| 6 | `SevenSignsFestival` | `getHighestScoreData` incumple su contrato | media |
| 7 | `LongTimeEvent` | valida la contención antes que el periodo | media |
| 8 | `State` | `switch` sobre una cadena de columna anulable | media |
| 9 | `IdManager` | lee fuera del try lo que el try construyó | baja |
| 10 | `Quest` | `showError` con la condición invertida | baja |
| 11 | `Quest` | `getRandomPartyMember` pide la variable dos veces | baja |
| 12 | dato `HeavyMedal` | el periodo de botín termina antes de empezar | baja |

**1 y 2 — La misma mitad que faltaba en `Calculator`.** En `Quest`, las listas
internas de temporizadores **sí** son `CopyOnWriteArrayList`: la intención
concurrente estaba entera. El mapa exterior era un `HashMap` que
`startQuestTimer` escribía bajo `synchronized` y que `getQuestTimer`,
`cancelQuestTimer`, `removeQuestTimer` y `unload` leían sin tomarlo — y
`removeQuestTimer` lo llama **el hilo del temporizador**, mientras tres scripts
de evento y un comando de administración recorren el mapa que reparte
`getQuestTimers()`.

En `QuestState` el mapa de variables se construía con pereza sin candado: dos
hilos podían construir uno cada uno y perderse un juego de variables entero, y
`exitQuest` anula el campo mientras los lectores están entre su comprobación de
nulo y su lectura. Ahora es concurrente, se construye una sola vez con el mismo
doble chequeo que `Quest` usa para su ejecutor de temporizadores —**el idioma ya
estaba en la casa**— y cada lector sostiene el mapa que comprobó.

**4 — Una recarga de npcs en mitad de una muerte.** `NpcTemplate` probaba sus
tres listas de botín y las dos ayudantes volvían a leer los campos, que recorren
**sin comprobar**. `removeDrops` y `removeDropGroups` los anulan, y la recarga de
datos de npc los llama con el mundo corriendo. Las ayudantes reciben ahora la
lista que se probó.

**5 — El `catch` que sabía y siguió.** El bloque atrapa un fallo al invocar a la
Bruja del Festival, lo registra, y sigue derecho a `new MagicSkillUse(_witchInst,
_witchInst, ...)`. `unspawnMobs` es el **único** sitio del archivo que comprueba
ese campo. De ahí salió un barrido nuevo, y hubo que corregirlo dos veces (ver
abajo).

**6 — Un contrato a medias.** `getHighestScoreData` promete un conjunto en blanco
cuando no encuentra puntuación, pero solo un ciclo ausente **lanza** y cae en el
`catch`; un id de festival ausente dentro de un ciclo presente simplemente
contesta nulo. Los **seis** llamadores desreferencian lo devuelto.

### El barrido nuevo, y su segunda ceguera

De (5) salió: *asignado dentro de un `try`, desreferenciado después del `catch`
sin comprobar*. Primer intento: **cero** en todo el repositorio. Pero el recorrido
de profundidad contaba llaves, y la línea `} catch (...) {` tiene una de cada
signo, así que pasaba de largo el `catch` y descartaba todo. La prueba que lo
delató fue correr el barrido **contra la versión del archivo anterior al arreglo**:
si no encuentra el defecto que lo originó, el barrido no vale. Corregido,
encuentra ese y otro más en el mismo archivo, y **5** en el repositorio: dos de
ellos son correctos (`type` y `quest` se inicializan a un valor no nulo antes del
try, con el comentario "managed below"), y los otros tres son los defectos 5, 6 y 9.

### Sospechas evaluadas y descartadas, con lo que las descarta

- **`isInParty()` y luego `getParty()` sin sostenerlo**: **37** sitios lo
  re-piden contra **6** que lo guardan en un local. La proporción se da vuelta:
  es el patrón de la casa, como `getFloodProtectors` (36 a 0). Se descartó el
  barrido entero — y con él una "corrección" a `getRandomPartyMemberState` que
  iba a hacer antes de medir.

- **Los desplazamientos de `QuestState.setCond`**: `1 << cond` está acotado por
  el `if ((cond < 3) || (cond > 31))` de arriba, y `1 << old` por la condición
  `cond > (old + 1)` de su propia rama, que obliga a `old <= 29`.

- **`QuestSound.SOUND_PACKETS`** ya es un `ConcurrentHashMap`, y sus valores
  nunca son nulos, así que el `containsKey` seguido de `get` es una búsqueda de
  más, no una carrera.

- **Los diez métodos `notify*` de `Quest`** tienen los diez su `try`/`catch`. Sin
  excepción que delate a nadie.

- **`playerEnter` concatena el jugador en un registro**, no en un mensaje: ahí el
  `toString` de `WorldObject` es lo apropiado. El barrido de mensajes visibles lo
  excluye por eso.

- **`_startDate.after(today)`** de `LongTimeEvent` sí está cubierto: lo protege
  `isValidPeriod()`, que comprueba los dos extremos. Lo que estaba mal era el
  orden respecto de la comprobación de contención, no la comprobación.

### Señalado y **no** cambiado

- **`IdManager` sigue arrancando tras un fallo de inicialización**, registrando
  SEVERE y continuando con un espacio de ids a medio construir — lo que puede dar
  colisiones de id de objeto, la clase de daño irreversible. El idioma de la casa
  ante un fallo de carga es exactamente ése (registrar y seguir) y **no existe un
  mecanismo de abortar arranque** en ningún gestor, así que introducir uno es una
  decisión de diseño del dueño del servidor, no del arreglo. Lo acotado sí se
  arregló: la línea que leía el conjunto fuera del `try` reportaba un nulo en vez
  del fallo real.

---

## `model` (raiz) — TERMINADA

58 archivos, 14.546 líneas. Los barridos corrieron sobre los 58; a mano se leyeron
los doce mayores (`EffectList`, `WorldObject`, `TradeList`, `StatSet`, `World`,
`AutoSpawnHandler`, `Spawn`, `WorldRegion`, `BlockList`, `Request`,
`DropProtection`, `WalkInfo`), que son unas 7.500 de esas líneas.

**Casi todo lo encontrado es la misma familia**: una clase que *sí* pensó en
concurrencia —tiene candado, atómico o colecciones concurrentes— y dejó uno o dos
campos fuera. No es descuido genérico: es una intención a medio aplicar, y por eso
se puede señalar sin discutir cuál era la intención.

| # | Archivo | Defecto | Severidad |
|---|---------|---------|-----------|
| 1 | `Spawn` + `RespawnTaskManager` | dos contadores, tres hilos, ningún candado | **alta** |
| 2 | `DropProtection` | valida el argumento tras las escrituras que impide | **alta** |
| 3 | `WorldObject.spawnMe` | no comprueba la región que `setXYZ` sí comprueba | **alta** |
| 4 | `OlympiadGame` | el daño que decide el combate, acumulado sin candado | **alta** |
| 5 | `World` | un jugador podía quedar en las dos facciones | media |
| 6 | `Request` | limpia cuatro campos desde el pool sin el monitor | media |
| 7 | `EffectList` | `EnumSet` entre seis colecciones concurrentes | media |
| 8 | `Siege` | el contador de torres decrementado sin atomicidad | media |
| 9 | `GrandBossManager` | `HashMap` entre dos mapas concurrentes | media |
| 10 | `TradeList`, `Clan`, `PlayerStat`, `WorldRegion` | escrito bajo candado, leído sin él | media |
| 11 | `AutoSpawnHandler` | índice cíclico probado con `==` | baja |

**1 — El respawn que no vuelve.** El hilo de decaimiento baja `_currentCount` y
sube `_scheduledCount`; el gestor de respawn baja `_scheduledCount` **metiendo
mano en un campo público desde su propio hilo**; el hilo de spawn sube
`_currentCount`. Y los dos se leen juntos para decidir si cabe un respawn. Un solo
decremento perdido deja esa suma clavada en el máximo y **ese spawn no vuelve a
aparecer en lo que dure el servidor**. Se pierde en silencio, y se acumula.

**2 — El ítem que nadie puede recoger.** `protect()` marcaba protegido, asignaba
el dueño, y **entonces** lanzaba si el dueño era nulo. El objeto quedaba protegido,
sin dueño y sin tarea que lo limpiara, que es el único estado en el que cada
`tryPickUp` posterior lanza — para todos, para siempre.

**3 — La región que se sabe nula.** `World.getRegion` devuelve null **a propósito**
para un objeto fuera de la rejilla: lo desecha primero y lo dice en el registro.
`setXYZ` comprueba exactamente eso antes de usar la respuesta; `spawnMe` seguía
adelante, volvía a meter en `_allObjects` un objeto recién desechado, y
desreferenciaba el nulo.

**4 — El combate decidido por daño no contado.** Los dos totales se acumulan desde
los hilos de combate —un golpe y un tic de daño pueden caer juntos— y la tarea de
cuenta atrás los pone a cero desde otro. Deciden el ganador cuando ninguno muere.

**5 — El jugador de dos facciones.** El gestor de facción **no comprueba** si el
jugador ya tiene una, así que puede volver a elegir: entra en el mapa nuevo y se
queda en el viejo. Cuenta doble en el limitador de balance, y al salir solo se le
quita de uno — el `Player` entero queda anclado en el otro.

### Barridos nuevos, los dos validados contra el fichero previo al arreglo

- **Índice cíclico probado con `==` contra una cota**: encuentra el de
  `AutoSpawnHandler` en la versión previa y **cero** en el repositorio entero. Era
  el único.
- **Campo público mutable que otra clase muta por referencia**: encuentra el de
  `Spawn` en la versión previa y **19** en el repositorio. Siete son campos de
  `MoveData`, que es una estructura y se usa así en todo el código; el resto son
  colisiones de nombre (`id`, `name`, `b`) que el barrido no puede distinguir. Los
  únicos reales fueron los de Olimpiada.
- **Una o dos colecciones lisas en una clase con mayoría concurrente**: **8**
  candidatos, de los cuales **6** se construyen en la carga y solo se leen después.
  Los dos vivos eran `GrandBossManager` y `EffectList`.

### Sospechas evaluadas y descartadas, con lo que las descarta

- **`StatSet`**: 35 captadores con un contrato **uniforme y deliberado** —ausente
  da el defecto, presente pero mal formado lanza— y los que devuelven objetos usan
  `instanceof`, que cubre el nulo. Sin defecto. Su familia `increase*` (12 métodos,
  uno con retorno `short` que guarda un `byte` truncado) **no se usa en ninguna
  parte** fuera de la propia clase: código muerto, no se toca.

- **`BlockList.isInBlockList`**: el `containsKey` + `put` + `get` son tres
  búsquedas para una pregunta, pero `loadList` nunca devuelve nulo y **nada quita
  del mapa**, así que el `get` no puede fallar.

- **`WorldRegion._surroundingRegions`**: se asigna una sola vez en el constructor
  de `World`, antes de que exista ningún jugador.

- **`EffectList._stackedEffects`**: el `containsKey`+`get` está dentro de
  `synchronized (this)` y el código **ya comprueba nulo tras cada `get`**. Bien
  escrito; el autor sabía.

- **`TradeList`**: `(MAX_ADENA / count) < price` divide por una variable, pero
  `count <= 0` se comprueba dos guardas antes, en los dos sitios.

- **`WalkInfo.getCurrentNode`** ya acota su índice con
  `Math.min(Math.max(0, …), size - 1)`.

---

## El resto fuera del mapa — TERMINADA

Contado de verdad al abrirlo: **408** archivos y **68.882** líneas en **77**
carpetas, no los 485 y 84.955 que el mapa corregido estimaba. La diferencia es que
`model/actor/instance`, `model/actor/tasks` y `model/actor/templates` ya habían
entrado con `model/actor`.

Los barridos mecánicos —publicación insegura, uso tras el `catch`, colecciones
sueltas en clases concurrentes, colecciones perezosas, índices cíclicos, campos
públicos mutados desde fuera, desempaquetado de nulos, trazas perdidas, doble
desbloqueo, reclamación no atómica— ya habían corrido **sobre el repositorio
entero**, así que aquí el trabajo fue leer las carpetas de más riesgo.

| # | Archivo | Defecto | Severidad |
|---|---------|---------|-----------|
| 1 | `ItemContainer` | las dos formas de `addItem` cambian la cuenta sin el candado | **muy alta** |
| 2 | `ClanHallAuction` | borra la subasta y luego desreferencia tres búsquedas | **alta** |
| 3 | `PlayerVariables` y dos más | doble desbloqueo; el fallo de base se pierde | **alta** |
| 4 | `SevenSigns` | los tres mapas de sellos quedan vacíos si la carga falla | **alta** |
| 5 | `ThreadPool` y `PacketExecutor` | concatenan un arreglo creyendo que es la traza | media |
| 6 | `Castle` y `ClanHall` | cobran la renta y desreferencian sin comprobar | media |
| 7 | `GameClient` | el estado de conexión y el jugador sin publicar | media |
| 8 | `FakePlayerData` | el javadoc promete un nulo que el retorno no puede llevar | media |
| 9 | `GrandBossManager` | `getStatus` de un jefe sin fila lanza en el script | media |
| 10 | `FloodProtectorAction` | la reclamación del castigo no es atómica | media |
| 11 | `EffectZone` | pide el mapa tres veces y desempaqueta un nulo | media |
| 12 | `CharSummonTable` | lee lo que el llamador ya había probado | baja |
| 13 | `Disconnection` y 85 más | el registro sin la excepción | baja |
| 14 | `PostBBSManager` | dos hilos construyen el mismo `Post` y uno se tira | baja |

**1 — La duplicación que la casa ya sabía evitar.** `ItemContainer.destroyItem` y
`transferItem` sostienen `synchronized (item)` mientras cambian la cuenta de una
pila. **Las dos formas de `addItem` no lo hacían** — y `changeCount` es leer la
cuenta, decidir contra el tope y volver a escribirla. Contado: de los **siete**
sitios del repositorio que llaman a `changeCount`, **seis** están bajo el monitor
del propio ítem y uno no. Dos miembros de clan depositando el mismo apilable en el
almacén compartido bastan, y eso son dos hilos de paquete sobre un contenedor.

**Y el arreglo creó un problema que hubo que cerrar.** Con el candado en
`addItem`, `transferItem` pasó a sostener el monitor del origen y pedir el del
destino: dos transferencias en sentidos opuestos entre el mismo par se bloquearían
mutuamente. Se comprobó en vez de suponerse — `PacketExecutor` manda los paquetes
de un cliente **a un pool, sin serializar por conexión**, así que un depósito y una
retirada pueden estar en vuelo a la vez. Se cierra tomando los dos monitores en
**orden de id de objeto**, sobre el que ningún par de hilos puede discrepar.

**2 — La sala que el ganador paga y no recibe.** `endAuction` borraba la fila de la
base y **después** desreferenciaba el registro del pujador, el clan detrás y la
sala vendida. Un clan disuelto entre la última puja y el cierre dejaba la subasta
borrada y la sala sin dueño, con la adena ya cobrada. Ahora resuelve primero y, si
falta el ganador, deja la subasta en pie.

**3 — El fallo de base que se sepulta a sí mismo.** Las tres clases de variables
persistidas desbloqueaban en el `catch` y otra vez en el `finally` que siempre le
sigue: `IllegalMonitorStateException` lanzada desde el `finally`, que oculta el
fallo que estaba reportando y devuelve una excepción donde el método promete
`false` — con `clearChangeTracking()` ya ejecutado, así que las variables sin
guardar se perdían. Su `deleteMe` tomaba el candado **sin `finally` alguno**.

**5 — Años imprimiendo un hash.** `ThreadPool` (cuatro sitios) y `PacketExecutor`
concatenaban `e.getStackTrace()` en el mensaje. Eso devuelve un **arreglo**, así
que `String.valueOf` daba `[Ljava.lang.StackTraceElement;@1a2b3c` donde iba la
traza. Salió al medir cómo registra el repositorio: **460 con la excepción contra
86 sin ella**; ahora **546 contra 0**.

### Barridos nuevos, todos validados contra el fichero previo al arreglo

Regla adoptada tras las dos cegueras anteriores: **un barrido que no encuentra el
defecto que lo originó no vale.** Cada uno se corrió primero sobre la versión del
archivo anterior al arreglo.

| barrido | validación | repositorio | qué quedó |
|---|---|---|---|
| índice cíclico con `==` | encuentra el conocido | **0** | era el único |
| campo público mutado desde otra clase | encuentra el conocido | 19 | 7 son campos de `MoveData`, resto colisiones de nombre; reales: Olimpiada |
| nulo desempaquetado a primitivo | encuentra el conocido | 17 | los de quests indexan mapas constantes por los npcs que registran |
| doble desbloqueo / lock sin finally | encuentra los seis | **0** | vivían solo en `variables` |
| bandera reclamada en dos pasos | encuentra el conocido | 6 | 1 real; ver abajo |
| `changeCount` sin el monitor del ítem | — | 7 | 6 con candado, 1 sin: el defecto 1 |

El de banderas tiene un punto ciego conocido y anotado: busca `synchronized`
**hacia atrás**, así que una doble comprobación correcta —candado *dentro*, como
`ClanHallDoorman`— aparece como falso positivo.

### Sospechas evaluadas y descartadas, con lo que las descarta

- **El desbordamiento del producto en multisell**: el ingrediente comprueba
  `> Integer.MAX_VALUE` y el producto no. El mayor `count` de producción del dato
  distribuido es **700**, contra los **214.748** que harían falta con el tope de
  10.000. No alcanzable.

- **La rotación de botín de `Party`** ya está defendida: `try/catch` alrededor del
  índice con el comentario "take another member if this just logged off", y usa
  `>=` donde `AutoSpawnHandler` usaba `==`.

- **`Forum.vload`** puede cargar dos veces, pero `_topic` es un mapa por id y
  `_forumPost` se **asigna**, no se acumula: la segunda carga sobrescribe.

- **`PunishmentTask._isStored`**: cada castigo es un objeto nuevo en su sitio de
  construcción, así que no hay dos hilos sobre el mismo.

- **`ClanHallAuction.setBid`** ya es `synchronized`, así que las pujas cruzadas que
  sus tres lecturas del mapa sugerían no ocurren.

- **`Instance._resetData` y `_exceptionList`** son listas lisas, pero solo se
  llenan al parsear la plantilla y se leen después.

- **`WorldRegion._surroundingRegions`** se asigna una vez en el constructor de
  `World`; **`StatSet`** tiene 35 captadores con un contrato uniforme y sus
  captadores de objeto usan `instanceof`, que cubre el nulo; **`BlockList`** nunca
  quita de su mapa, así que su `get` no puede fallar.

### Señalado y **no** cambiado

- **Un guardado fallido de variables sigue descartando los cambios** en vez de
  reintentarlos. Conservarlos exigiría que el `INSERT` fuese idempotente y es un
  `INSERT` liso sin `ON DUPLICATE KEY UPDATE`: reintentar tras un lote a medias
  chocaría por clave. Cambiar la semántica de persistencia sin eso sería cambiar un
  problema por otro.

- **`IdManager` sigue arrancando tras un fallo de inicialización** (ver el registro
  de `model/script`).

---

## Auditar la auditoría

Antes de seguir buscando defecto número doscientos, dos preguntas sobre lo ya
hecho.

### ¿Cubrieron los barridos tardíos las áreas cerradas temprano?

La mitad de los barridos —`unbox`, `catchuse`, `cyclic`, `pubfield`, `unlock`,
`claim`, `odd`, `lazy`, `count`— se escribieron en los últimos paquetes. Las áreas
cerradas antes de que existieran (`itemcontainer`, `clan`, `skill`, `olympiad`,
`loginserver`, `ai`, `commons`, `managers`, `data`, `serverpackets`,
`clientpackets`, `handlers`, `actor`) nunca las vieron nacer.

**Sí las cubrieron**: todos usan globs de repositorio entero. Corridos otra vez
al final, lo que queda está triado y explicado:

| barrido | quedan | qué son |
|---|---|---|
| `catchuse` | 3 | variables inicializadas a un valor no nulo antes del `try`, con su "managed below" |
| `cyclic` | 0 | — |
| `unlock` | 0 | — |
| `trace` | 0 | de 86 a 0 |
| `claim` | 6 | una doble comprobación correcta, dos iteradores locales, un objeto por castigo, una carga idempotente, el cifrado del handshake |
| `pubfield` | 17 | 7 campos de `MoveData`, resto colisiones de nombre |
| `odd` | 7 | se construyen en la carga y solo se leen después |
| `lazy` | 16 | igual |
| `unbox` | 14 | mapas constantes de quests indexados por los npcs que registran |
| `count` | 2 «sin monitor» | **artefacto de mi propio arreglo**: están dentro de `synchronized (outerLock) { synchronized (innerLock)`, y el barrido compara el nombre del candado |

Resultado negativo, pero es el que se buscaba: ningún área marcada TERMINADA
escondía un acierto de los barridos posteriores.

### ¿Se ha ejecutado algo?

Hasta aquí, **no**. Cuarenta commits verificados solo por `javac`. El repositorio
**sí tiene suite de tests** —`test/`, con JUnit 4.13.2 en `test/libs`, y objetivos
`compile-tests` y `test` en `build.xml`— y no se había corrido ni una vez.

Corre así, sin `ant` (que no está en PATH), replicando lo que hace el objetivo:

1. compilar `test/**.java` con el build del core más `dist/libs` más `test/libs`;
2. `java org.junit.runner.JUnitCore <clases>`, excluyendo
   `ItemsOnGroundManagerTest` como hace `build.xml` (usa `main()`, no es JUnit).

**Resultado: OK (31 tests).**

**Y hay que ser exacto sobre qué prueba eso.** La suite cubre `BCrypt`,
`StringUtil`, `Subnet`, `TraceUtil`, el parseo del teletransportador, `MathUtil` y
`LoginController`. **No toca nada de lo que esta auditoría cambió**: ni el candado
del contenedor de ítems, ni el hilado del motor de quests, ni los contadores de
spawn, ni el marcador de Olimpiada. Confirma que la compilación y esas utilidades
siguen en pie. Nada más.

### El límite que queda escrito en el código

La edición de mayor consecuencia de toda la auditoría es el candado de
`ItemContainer`, y es la única donde el arreglo **creó** un peligro que hubo que
cerrar después. Releída entera al final, queda un residuo que se documenta en el
propio método en vez de fingir que está cerrado:

`transferItem` busca la pila destino **antes** de tomar los candados, porque esa
búsqueda es la que decide el orden. Si otro hilo crea una pila de ese id en el
destino entre la búsqueda y el `addItem` de más abajo, ese `addItem` toma un tercer
monitor sobre el que no se acordó orden. Hace falta además que ese hilo quiera el
origen de éste. Se deja así porque la alternativa —sostener un candado a través de
la búsqueda que decide el candado— es circular.

---

## Tests para los invariantes que esta auditoría cambió

La suite que ya existía pasa, pero **no toca nada de lo que se cambió**. Estos tres
ficheros cubren tres de los arreglos, y cada uno se validó igual que un barrido:
**contra la versión rota**. Un test que también pasa con el defecto puesto no vale
nada, exactamente igual que un barrido ciego.

De **31** tests a **41**.

### `ItemContainerConcurrencyTest`

Ocho hilos sumando 2.000 unidades cada uno a una misma pila, y un segundo caso con
mitad sumando y mitad restando la misma cantidad.

**Validación con el candado de `addItem` quitado, tres corridas:**

| esperado | obtenido |
|---|---|
| 17.000 | 8.519 |
| 17.000 | 8.303 |
| 17.000 | 8.846 |

**Se pierde la mitad.** El registro de esta auditoría decía "dos miembros de clan
depositando a la vez bastan para perder un depósito"; era quedarse corto. Bajo
contención se pierde alrededor del cincuenta por ciento de lo depositado. El
segundo caso pierde ~1.500 unidades por corrida sobre un millón, y eso son ítems
de jugador **destruidos**, no descolocados.

Con el candado puesto: exacto, tres de tres.

Nota sobre el propio test: usa el id 1060, no adena. `changeCount` acota la adena
con `Inventory.MAX_ADENA`, que se lee de `PlayerConfig`, y un test corre **sin
configuración cargada** — así que ese tope vale cero y la primera suma recortaría
la pila entera. Costó una tarde entenderlo y queda escrito en el test.

### `StateTest`

Cuatro casos sobre `State.getStateId`, incluido el nulo que la columna
`character_quests.value` permite.

**Validación con la guarda quitada:** falla solo ese caso, con
`NullPointerException: Cannot invoke "String.hashCode()" because "<local1>" is
null` — que es exactamente lo que el cargador de quests atrapaba y convertía en
"este personaje pierde las quests que quedaban por leer".

### `EffectZoneTest`

Tres casos de contrato **más uno de regresión**, y la distinción importa: los tres
primeros **pasan también contra el código viejo**, porque un solo hilo nunca ve
desaparecer una entrada entre el `containsKey` y el `get`. El cuarto vacía el mapa
por debajo de un lector.

**Validación con la versión de tres búsquedas restituida, tres corridas:** falla
las tres con `NullPointerException: Cannot invoke "java.lang.Integer.intValue()"
because the return value of "java.util.Map.get(Object)" is null`.

### Cómo se corre sin `ant`

`ant` no está en PATH. El objetivo `test` de `build.xml` se replica en dos pasos:

1. `javac` de `test/**.java` con el build del core, `dist/libs` y `test/libs`;
2. `java org.junit.runner.JUnitCore <clases>`, excluyendo `ItemsOnGroundManagerTest`
   como hace `build.xml` — usa `main()`, no es JUnit.

**El classpath tiene que ir en forma de Windows** (`C:/...`), no en la de Git Bash
(`/c/...`): javac es un binario de Windows y con la segunda forma no resuelve las
clases del build, dando errores de "cannot find symbol" que parecen del código.

### Dos más, y la deuda que queda nombrada

De **41** a **47**.

#### `SpawnCounterTest`

`new Spawn(null)` sale del constructor antes de reflexionar sobre la clase de la
plantilla, así que cuesta nada construirlo. El contador es privado, así que el test
lo fija y lo lee por reflexión en vez de ensanchar la clase para un test.

**Validación con la forma antigua repuesta** —decremento directo, sin candado ni
suelo, que es como lo hacía el gestor de reaparición metiendo mano en el campo
público—, tres corridas de 16.000 decrementos:

| esperado | obtenido |
|---|---|
| 0 | 11.706 |
| 0 | 13.139 |
| 0 | 12.120 |

**Y aquí hay que leer con cuidado, no repetir el número.** Eso es ~12.500 de 16.000
decrementos perdidos, pero es **un bucle apretado**: sin `volatile` ni candado el
JIT mantiene el campo en un registro durante toda la iteración y los decrementos ni
siquiera llegan a memoria. En el servidor real están repartidos en el tiempo y la
tasa sería muchísimo menor. El test prueba que **la pérdida es posible y que el
arreglo la impide**; no dice que se pierdan tres de cada cuatro reapariciones en
producción.

El segundo caso pide más decrementos que reapariciones programadas: con el suelo
puesto queda en cero, sin él la cuenta se va por debajo.

#### `DocumentBaseNotConditionTest`

El constructor de `DocumentBase` solo guarda el fichero, así que una subclase de
test llega a `parseLogicNot` con un DOM construido a mano y sin tocar disco.

Cuatro casos: hijo desconocido, `<player>` sin atributo legible, `<not>` vacío, y un
hijo legible que debe seguir funcionando.

**Validación con la versión que envolvía el nulo:** fallan **los dos primeros**,
devolviendo un `ConditionLogicNot` que envuelve null. Los otros dos pasan con las
dos versiones —el `<not>` vacío ya se rechazaba y el camino normal no se tocó—, que
es exactamente lo que debe pasar y por eso están.

#### La deuda que queda

**`QuestState` no es comprobable en aislamiento.** Su constructor exige un `Player`
real, y el de `Quest` reflexiona sobre `ScriptEngine.getCurrentLoadingScript()`, que
fuera del cargador de scripts es nulo. Sin un framework de mocks —solo hay JUnit y
hamcrest en `test/libs`— sacarlo exigiría instanciar sin constructor por reflexión
interna, que es frágil y necesita `--add-opens`. **El mapa de variables de quest
sigue verificado solo por lectura**, y se deja dicho en vez de forzarlo.

Lo mismo para el doble desbloqueo de `PlayerVariables`, `AccountVariables` e
`ItemVariables`: sus rutas de guardado y borrado hablan con la base de datos.

---

## `quests/` — las clases decidibles

296 archivos y 77.828 líneas no se cierran leyéndolos uno a uno, y los barridos
estructurales ya habían pasado por todos. Pero hay una clase **completamente
decidible** que rompe la quest para el jugador: **una página que el script nombra y
el datapack no tiene**.

Cuando eso pasa el síntoma es preciso, y conviene saberlo: `showHtmlFile` solo
envía si `getHtm` encontró algo, así que **no se envía nada**. El jugador hace clic
y la ventana no cambia. No hay error, no hay traza, no hay nada en el registro. La
quest simplemente parece atascada.

### El barrido, y las tres veces que hubo que afinarlo

De **6.267** referencias literales a páginas, empezó marcando 243 inexistentes.
Ninguna cifra intermedia era la buena:

1. **243** — resolvía todo junto al `.java`. Falso: el html de administración vive
   en `data/html/admin/`. Añadidas las raíces reales → **179**.
2. **179** — `AbstractSagaQuest` es una **clase base** y sus páginas viven en las
   carpetas de las 31 sagas concretas. Contando como resuelto lo que existe en
   cualquier carpeta del datapack → **103**.
3. **103** — `htmltext += "mystic.html"` es una concatenación, y el filtro
   comprobaba que la línea terminara en `+`, no en `+=`. Corregido → **86**.

De esas 86, **12 son fragmentos** que se arman con el id del npc en tiempo de
ejecución (`npcId + "-01.htm"`), y **74 son código muerto**: ninguna página del
datapack las nombra, así que la rama no se alcanza.

El grueso de las 74 son los maestros de aldea de los npcs **32092 a 32098**, que
son de una crónica posterior y cuyo contenido nunca se distribuyó — 53 páginas de
ramas que no existen. No se tocan.

### Lo que sí eran defectos

| quest | dice | existe | qué falla |
|---|---|---|---|
| `Q00386_StolenDignity` | `30843-09a.**html**` | `30843-09a.**htm**` | las reglas del bingo |
| `Q00386_StolenDignity` | `30843-09.**htm**` | `30843-09.**html**` | las reglas del bingo |
| `Q00503_PursuitOfClanAmbition` | `307**66**-24t.htm` | `307**60**-24t.htm` | la respuesta con `memberCond == 13` |

Las dos de `StolenDignity` tienen las extensiones **cruzadas** entre sí, y los dos
ficheros existen con el mismo contenido. La de `PursuitOfClanAmbition` es un dígito:
todas las ramas de alrededor hablan por el npc 30760, incluida la de justo encima.

### Señalado y **no** cambiado

**`Q00627_HeartInSearchOfPower` línea 142** dice `"31518-7.htm"` —sin el cero— y la
línea 128 del mismo método dice `"31518-07.htm"`, que sí existe. Parece el mismo
error de un carácter que los tres de arriba, **y no se toca**, por esto: las dos son
las ramas de un mismo `if`. La primera es "tienes la Gema de los Santos" y muestra
la página de recompensa; la segunda es "no la tienes". Apuntarla a `-07` le mostraría
la confirmación de recompensa a quien no recibió ninguna, que es peor que la ventana
en blanco. Cuál era la página que el autor quiso no está en el repositorio, y
adivinarla es escribir contenido, no arreglar un error.

### La segunda clase decidible: ids que el dato no define

Mismo método que las páginas. El dato define **7.852 ítems** y **5.783 npcs**
(contando los subdirectorios `custom/`, que la primera versión del barrido se dejó
fuera y por eso marcaba los npcs de eventos como inexistentes).

**El nombre de la constante no sirve para decidir si un id es de ítem o de npc.** El
primer intento lo intentó y falló en las dos direcciones: `TOMBSTONE` (31523),
`GILMORE` (30754) y `STONE_GOLEM` (20016) son **npcs** que contienen "STONE" o
"GEM"; `GATEKEEPER_TOKEN` (1659), `GUARDIAN_BASILISK_FANG` (3207) y
`MONSTEREYE_CORNEA` (3410) son **ítems** que contienen "KEEPER" o "GUARD". Lo que
decide es **el sitio de llamada**: el segundo argumento de `giveItems` es un ítem y
el primero de `addKillId` es un npc, sin ambigüedad posible.

Resultado con ese método: **0 npcs** y **22 ítems**.

| dónde | ids | qué pasa |
|---|---|---|
| `Q00386_StolenDignity` | 16, entre 8331 y 8722 | el premio del bingo |
| `CastleWarehouse` | 9910 y 9911 | el intercambio entero |
| `Tunatun` | 15473 | el látigo del domador |

`giveItems` con un id que el dato no define llega a
`ItemData.getTemplate(id) == null`, deja `Invalid ItemId` en la consola y vuelve sin
dar nada. Para el operador hay un aviso; **para el jugador no pasa nada**: gana el
bingo y no recibe el premio.

### Señalado y **no** cambiado

Los 22 son **contenido de una crónica posterior al dato distribuido**, la misma
categoría que las 53 páginas de los maestros de aldea 32092–32098. Arreglarlo sería
o **inventar las estadísticas** de dieciséis armas, o **borrar** dieciséis ramas de
premio y un intercambio completo. Ninguna de las dos es una decisión de quien
audita: son de quien decide qué contenido lleva el servidor.

Queda la lista exacta, que es lo accionable: son esas tres quests y esos ids.

### Tercera y cuarta clase decidibles: skills y coordenadas

**Coordenadas: cero.** Ninguna de las `new Location(x, y, z)` literales del
repositorio cae fuera de la rejilla del mundo (X −131.072 a 229.376, Y −262.144 a
262.144). Interesaba comprobarlo porque `World.getRegion` devuelve nulo justo para
eso y `spawnMe` desechaba el objeto. Resultado negativo limpio.

**Skills: diez referencias a ids que el dato no define**, y esta vez en el núcleo,
no en quests. El dato define **1.976** skills contando `skills/custom/`, que la
primera pasada se dejó fuera y por eso marcaba como inexistentes el 100000 de los
eventos y el 49991 de las monturas.

| dónde | skill | qué queda inerte |
|---|---|---|
| `Player` (5 usos) | 5076 | la penalización por muerte entera |
| `Antharas` | 5092, 5093 | su Terror y su Meteoro |
| `Cubic` ATTRACT_CUBIC | 5115, 5116 | el cubo de atracción entero |
| `Cubic` SMART_CUBIC_SHILLIENTEMPLAR | 5115 | la mitad de sus lanzamientos |

**No hay caída en ninguna parte**, y eso se comprobó en vez de suponerse:
`CubicAction` saca una skill al azar de la lista y **comprueba el nulo** antes de
usarla; `CubicHeal` recorre la lista y hace `s.getId()` **sin comprobarlo**, pero
solo se programa para `LIFE_CUBIC`, cuya skill 4051 sí existe. `ATTRACT_CUBIC` sí se
programa, con una lista de dos nulos: cada tick saca uno, ve que es nulo y vuelve.

### Señalado y **no** cambiado, con el motivo

Se podría filtrar los nulos al construir la lista del cubo. **No se hace**, y no por
pereza: `SMART_CUBIC_SHILLIENTEMPLAR` lleva 4049 (existe) y 5115 (no existe), así
que hoy lanza la mitad de las veces. Filtrar el nulo haría que lanzara **siempre** —
duplicar la eficacia de un cubo es un cambio de equilibrio, no un arreglo de un
error. Es la misma línea que con `buffDebuffMod`: cuando la "corrección" mueve
números del juego, la decisión es del dueño del servidor.

Las cinco skills son, otra vez, contenido de una crónica posterior al dato
distribuido — la tercera vez que aparece esa misma raíz, después de las 53 páginas
de los maestros de aldea 32092–32098 y de los 22 ítems.

### Quinta clase decidible: las listas de tienda que el bypass abre

El primer intento buscó estas referencias **en Java** y dio cero. El cero era falso
y lo delató un contador que se añadió a propósito: había examinado **22**
referencias en total —13 de zona, 7 de puerta, 2 de multisell— y **ninguna** de
instancia ni de buylist, con 555 buylists en el dato. Las expresiones no coincidían
con nada.

**Multisells y buylists no se invocan desde Java: viven en bypasses dentro del
html.** Buscando ahí: **190** referencias de multisell y 10 de compra examinadas.

| dónde | id | qué queda muerto |
|---|---|---|
| `Q00336_CoinsOfMagic` | 30078, 30092, 30165, 30183, 30200, 30673, 30688, 30696, 30847 | **el intercambio entero de la quest** |
| `CastleChamberlain` | 350980008 | el único botón de `35507-02.html` |

Los nueve de Coins of Magic son ids de npc, y la carpeta **sí usa esa convención**
(`31522.xml` está ahí), así que se esperaban y faltan. Cada uno de esos nueve npcs
tiene un `-01.htm` cuya única acción es abrir su multisell: el jugador habla, pulsa,
y no pasa nada. El del chambelán es el hueco 8 de una serie donde existen el 1 al 7.

`separateAndSend` con un id que no existe deja un aviso en consola y vuelve sin
abrir la ventana — el mismo síntoma que las páginas ausentes: **para el jugador no
ocurre nada**.

### Dos falsos positivos que valió la pena perseguir

`bypass -h Script CastleMercenaryManager buy 1` **no** referencia el buylist 1. El
script hace `Integer.parseInt(npc.getId() + st.nextToken())`, así que sobre el npc
35102 la lista es la 351021. Los diez aciertos de buylist eran todos esa forma.

### Señalado y **no** cambiado

Otra vez contenido que falta, no código roto. Escribir esos diez multisells es
decidir **qué se cambia por qué** —una tabla de intercambio económica—, y eso no lo
inventa quien audita. Lo accionable es la lista, y está arriba.

### Sexta clase decidible: el ítem que se queda pegado

`QuestState.exitQuest` limpia **solo lo registrado**: llama a
`removeRegisteredQuestItems`, que es `takeItems(player, -1, _questItemIds)`. Un ítem
que la quest entrega y no registra sobrevive al abandono.

Y ahí está lo que lo convierte en defecto y no en molestia: esos ítems están
marcados `is_questitem = true` en el dato, y un ítem de quest **no se puede tirar,
intercambiar ni destruir**. El jugador que abandona se queda con él **para siempre**,
ocupando inventario, sin ninguna forma de deshacerse de él.

La señal para encontrarlos sin falsos positivos: un ítem que la quest **entrega y
además recupera** circula dentro de ella, así que es suyo por definición. Ni un
premio ni un objeto recogido de fuera cumplen las dos cosas.

De **19** quests que salieron, quedaron **11** tras tres filtros, y cada filtro
evitó un daño distinto:

| filtro | qué habría pasado sin él |
|---|---|
| el ítem lo tocan **varias** quests | `BLOODED_FABRIC(4295)` lo usa también el encuentro de **Baium**: registrarlo haría que terminar la quest 348 borrase lo que Baium necesita |
| el ítem **no** está marcado como de quest | ocho ids con precio de venta —Álbum de Monedas 10.000, Polvo de Hada 10.000, Esposas del Penitente 22.000, la propia Poción del Deseo— **son propiedad del jugador**: registrarlos la destruiría al abandonar |
| el registro es **calculado** | `Q00335` los mete en una lista y la registra entera; mi barrido solo lee tokens literales y la marcó por error |

**Arreglado: 11 quests, 25 ítems.** Seis ya tenían `registerQuestItems` y se les
añadió lo que faltaba; cinco no registraban **nada** y ahora lo hacen —
`Q00504_CompetitionForTheBanditStronghold` dejaba hasta veintinueve amuletos
clavados, y `Q00627_HeartInSearchOfPower` registraba uno de tres.

En todas, la ruta de éxito ya consume el ítem antes de terminar, así que el cambio
**solo afecta al abandono**. Eso es lo que lo hace seguro.

### El inverso, que habría sido mucho peor

Se comprobó lo contrario por si acaso: **¿registra alguna quest la adena (57)?**
Registrarla haría que `exitQuest` **borrase el dinero del jugador**. Resultado:
**cero**. `Q00333_HuntOfTheBlackLion` la entrega y la recupera, y correctamente no
la registra.

### Séptima clase: el dato contra sí mismo

Hasta aquí se comprobó **código contra dato**. Falta lo otro: **el dato contra el
dato**, que es la referencia cruzada más grande del repositorio y la que rompería
más juego si estuviera mal.

| comprobación | referencias examinadas | rotas |
|---|---|---|
| ítems en las listas de botín de los npcs | **23.775** | **0** |
| skills declaradas en el `<skillList>` de los npcs | **24.526** | **0** |
| ítems en los multisells (ingredientes y productos) | **18.892** | **0** |
| ítems en las buylists | **15.757** | **0** |
| **total** | **82.950** | **0** |

Ochenta y dos mil novecientas cincuenta referencias y **todas resuelven**. Es el
resultado negativo más grande de la auditoría, y vale exactamente por eso: el dato
distribuido es internamente consistente. Todo lo que se encontró antes —las 53
páginas, los 22 ítems, las 5 skills, los 10 multisells— es **código que pide dato
que no vino**, nunca dato que se contradiga a sí mismo.

### Una comprobación que salió nula, y se dice

Se intentó una quinta —**los secuaces que invoca un jefe**— y examinó **cero
referencias**, y se dio por nula. **Eso era un error de la comprobación, no del
dato**: buscaba `<param name="Privates">` y la forma real es `<minions
name="Privates">`. Rehecha, la cifra es **260 npcs que declaran secuaces, 467
referencias, 0 rotas**. La ruta de invocación de `Spawn.doSpawn` está viva y su dato
es correcto. Se deja escrito el error porque un cero sin denominador sigue sin ser un
aprobado — pero éste sí tenía denominador y había que ir a buscarlo.

### Y una que sí tuvo denominador

Los **conflictos de primer diálogo** —dos scripts reclamando el mismo npc, donde
solo gana uno y cuál gana depende del orden de carga— dan **cero** sobre 55 npcs
declarados en 48 sitios de llamada. Cuatro de esos 48 usan formas que no se pueden
resolver estáticamente (`MANAGERS.keySet()`, una variable, un índice de arreglo),
así que el cero cubre 44 de 48.

### El resto del dato, y una corrección a lo dicho arriba

La tabla de 82.950 referencias limpias cubría cuatro comprobaciones grandes
—botín, skills de npc, multisells, buylists—. Faltaban **610 ficheros de dato** más:
`Seeds.xml`, `Recipes.xml`, `PetSkillData.xml`, `EnchantItemData.xml`, las zonas,
los teletransportadores, la pesca, las instancias y el resto.

Revisados por atributo (`itemId`, `skillId`, `npcId` y sus variantes):

| tipo | examinadas | rotas |
|---|---|---|
| skills | **14.290** | **0** |
| npcs | 2.179 | 36 |
| ítems | 1.308 | 97 |

**Esto corrige lo dicho más arriba.** Decir "el dato es coherente consigo mismo" era
demasiado rotundo: lo es en las cuatro referencias grandes, y **tres ficheros
sueltos tienen 133 referencias rotas**.

| fichero | rotas / examinadas | qué es |
|---|---|---|
| `stats/fishing/fishes.xml` | 89 / 278 | los peces |
| `PetSkillData.xml` | 36 / 1.150 | skills de mascota para ids de npc que no existen |
| `Seeds.xml` | 8 / 198 | semillas del manor |

**La pesca desglosada por grado es la medición que lo explica todo:**

| grado | peces | con ítem inexistente |
|---|---|---|
| `fish_easy` | 81 | **0** |
| `fish_normal` | 116 | 8 |
| `fish_hard` | 81 | **81 — todos** |

El grado difícil **entero** trajo su tabla de peces y no sus ítems. Y los ocho de
`fish_normal`, que parecían anomalías dentro de contenido vivo, resultaron ser **un
solo ítem**: el 8547 "Old Box", en los peces de nivel 20 a 27. Se comprobó si era
una omisión aislada mirando a sus vecinos: **el bloque 8540–8555 falta entero**.

Así que las 133 no son una clase nueva. Son **la misma raíz por cuarta vez**: código
y dato de una crónica posterior a los ítems distribuidos, junto a las 53 páginas,
los 22 ítems de quest, las 5 skills y los 10 multisells.

Que las skills den **0 de 14.290** refuerza la lectura: lo que falta son **ítems y
npcs de contenido nuevo**, no definiciones de mecánica.

### Octava clase: coordenadas y puntos de aparición

Esto sigue directamente del arreglo de `WorldObject.spawnMe`: `World.getRegion`
devuelve nulo a propósito para un objeto fuera de la rejilla y lo desecha. Si algún
punto de aparición del dato cayera fuera, ese npc se borraría al arrancar.

| comprobación | examinadas | fuera de rango o inexistentes |
|---|---|---|
| puntos de aparición (165 ficheros) | **10.167** | **0** fuera de rejilla, **0** npcs desconocidos |
| destinos de teletransporte | 1.190 | **0** |
| puertas: posición y los cuatro nodos | 2.735 | **0** |
| npcs teletransportadores | 138 | **17 no existen** |
| vértices de zona | 11.164 | 300 fuera de rejilla |

**Los 300 vértices de zona no son un defecto**, y se comprobó en vez de suponerse:
`ZoneManager` registra una zona **iterando la rejilla** y preguntando a cada región
`intersectsRectangle`, no indexando por las coordenadas del vértice. Un vértice
fuera solo significa que el polígono se extiende más allá del área jugable, que es
como se dibuja una zona que cubre "todo lo que hay pasada esta línea". Nada lo
indexa, así que nada se sale de rango.

**Los 17 teletransportadores** —35503, 35504, 35505, 35544, 35545, 35548 a 35550,
35566 a 35578 pares, 35605 y 35640— son npcs de castillo y fortaleza que el dato no
define. Sus ficheros de destinos se cargan y nunca se usan, porque el npc no existe.
La misma raíz, por quinta vez.

**Que los 10.167 puntos de aparición estén todos dentro y todos apunten a un npc
definido** es el negativo más útil de esta tanda: el arreglo de `spawnMe` no tenía
que corregir ningún dato, solo dejar de reventar si algún día lo hubiera.

### Novena clase: las consultas contra el esquema

El esquema define **93 tablas**. Revisadas todas las cadenas SQL del repositorio:
**579 referencias a tabla** y **1.213 nombres de columna**.

#### Lo que salió, y era de verdad

**`FriendsBoard` borraba amigos con columnas que no existen.** La consulta de
"borrar todos" decía `WHERE char_id = ? OR friend_id = ?`, y la tabla tiene `charId`
y `friendId`. Las **otras siete** consultas del repositorio contra esa tabla usan los
nombres correctos, **incluida la que está cuarenta líneas más abajo en el mismo
fichero**.

Y el síntoma no era un error visible: la consulta lanza, el `catch` lo registra, y
**el código sigue** — vacía la lista en memoria, manda la lista vacía al cliente y le
dice al jugador *"has vaciado tu lista de amigos"*. Las filas siguen en la base.
**Al reconectar, vuelven todos.**

**Y en el mismo bloque, el lado del amigo estaba al revés.** Las dos ramas hacían
`friend.getFriendList().remove(friend.getObjectId())` — quitar al amigo de su propia
lista, donde nadie está nunca. Lo que hay que quitar es **al jugador**, que es
exactamente lo que hace `RequestFriendDel`, el manejador de paquete:
`target.getFriendList().remove(player.getObjectId())`.

**El esquema de la subasta global no se instalaba.** `GlobalAuctionManager` necesita
`global_auctions` y `global_auction_funds`, y su `CREATE TABLE` vivía en `./sql/`,
que el instalador **no lee**: éste hace `Paths.get("sql", dbType)` desde
`dist/db_installer/`. Los otros cuatro ficheros de esa carpeta son migraciones
—`ALTER`, `INSERT`—; **éste era el único `CREATE TABLE` fuera de la carpeta del
instalador, 92 contra 1**, y nada lo documenta. En una instalación nueva la subasta
global fallaba al cargar con un SEVERE en el registro y quedaba muerta. Movido a
donde el instalador mira.

#### Dos falsos positivos, y qué los causó

- **`Hero.java`** parecía usar `heroes.char_name`. No: la consulta une `heroes` y
  `characters`, mi expresión capturaba solo la primera tabla del `FROM`, y
  `char_name` es de `characters`, donde sí existe.
- **`character_contacts`** parecía no tener esquema. Lo tiene: es el **único** de los
  93 ficheros que escribe sus columnas **sin comillas invertidas**, y mi parser las
  buscaba con ellas.

### Décima clase: las claves de configuración, en los dos sentidos

62 ficheros de configuración declaran **1.208 claves**; el código pide **1.019**
literalmente.

#### La sección anticheat estaba desconectada de sí misma

Cuatro claves que **el código lee y ningún fichero declaraba**:
`AntiCheatMovementEnable`, `AntiCheatAttackSpeedEnable`, `AntiCheatCastSpeedEnable`,
`AntiCheatRangeEnable`. No son constantes muertas — las cuatro gobiernan rutas vivas:

| clave | dónde actúa |
|---|---|
| `AntiCheatMovementEnable` | `ValidatePosition`: rechaza un movimiento que supere 1,5× la velocidad |
| `AntiCheatAttackSpeedEnable` | `AttackRequest`: rechaza un ataque enviado mientras ya se ataca |
| `AntiCheatCastSpeedEnable` | `RequestMagicSkillUse`: rechaza un lanzamiento mientras ya se lanza |
| `AntiCheatRangeEnable` | `Creature`: la comprobación de rango de ataque |

Las cuatro **por defecto activas**, y sin aparecer en ningún `.ini` el dueño del
servidor **no tenía forma de saber que existen**, y por tanto ninguna de apagarlas.

Y en el otro sentido, cuatro claves que **`Player.ini` declara y ningún código lee**:
`AntiCheat`, `AntiCheatCheckInterval`, `AntiCheatCheckDualBox`, `AntiCheatCheckBot`.
Poner `AntiCheat = False` **no hacía absolutamente nada**; el dueño creía haber
desactivado el anticheat y seguía encendido.

Ocho claves, cuatro por lado, **ninguna coincidiendo**. Las cuatro reales están ahora
en `Player.ini` con lo que hace cada una y su valor por defecto. Las cuatro muertas
se **anotan, no se borran**: quitarlas cambiaría en silencio una configuración que el
dueño puede tener editada, y una nota dice la verdad igual de bien.

Tras el arreglo: **0 claves que el código pida y nadie declare.**

#### El otro lado del recuento no vale, y por qué

Quedan 190 claves declaradas que el barrido no ve pedidas, y **no son hallazgos**:
se leen por mecanismos que una búsqueda literal no puede resolver, y son tres
distintos.

| familia | claves | cómo se lee de verdad |
|---|---|---|
| `FloodProtector.ini` | 80 | prefijo: `"FloodProtector" + nombre + "Interval"` |
| `Siege.ini` | 42 | prefijo: `castle.getName() + "ControlTower" + i` |
| `Custom/ClassBalance.ini` | 37 | por clase, fuera de las clases de configuración |
| `Network.ini` | 24 | **patrón** `BufferPool\.\w+?\.Size` sobre el conjunto de claves |
| `Player.ini` | **4** | **nadie: son las anticheat muertas de arriba** |

De las 190, las únicas confirmadas muertas por comprobación directa son esas cuatro.
El resto se declara aquí como **no medido**, no como limpio.

### Undécima clase: validar el dato contra su propio esquema

Los 1.447 XML del datapack declaran cada uno su XSD, y el lector del servidor corre
con `factory.setValidating(false)`. O sea: **si un fichero viola su esquema, nadie
se entera** — el parser lee lo que puede y lo demás no se carga.

Primero lo barato: **los 1.447 parsean** sin error de sintaxis, y **los 1.447**
declaran un XSD que existe en la ruta indicada.

Después lo caro, con un validador escrito para esto (`javax.xml.validation`, porque
`lxml` no está disponible): **1.446 válidos, uno no**.

#### El que no validaba, y lo que costaba

`stats/skills/04500-04599.xml`, skill **4522 "Eye of Assassin"**, dos veces:

```xml
<mul stat="critRatePos" val="2">
    <using kind="DAGGER,DUALDAGGER" />
    <player behind="true" />   <!-- dos condiciones hermanas -->
</mul>
```

**Y el parser real solo lee la primera.** `attachFunc` hace
`parseCondition(n.getFirstChild(), template)`, y `parseCondition` avanza al primer
nodo elemento y parsea **ése y nada más**; los hermanos que siguen no se leen nunca.

Así que `<player behind="true"/>` **se descartaba en silencio** en las dos: el bono
de probabilidad de crítico con daga y el de daño crítico con estoque **se aplicaban
desde cualquier ángulo**, cuando el dato dice claramente que son por la espalda.

#### El arreglo, y el error intermedio que enseñó algo

La forma que el datapack usa para varias condiciones es `<and>` — **119 usos** en el
dato de skills. Envueltas así, `parseLogicAnd` recorre **todos** los hijos y la
condición pasa a contar.

Pero el primer intento **seguía sin validar**, y el motivo es instructivo: el
esquema declara ese `<and>` como `xs:sequence` con `player` **antes** que `using`, y
yo las había puesto al revés. En una secuencia el orden es parte del contrato. Al
parser le da igual —recorre hijos— pero al esquema no. Invertidas:

**1.447 de 1.447 validan.**

#### Esto mueve el juego, y es tuyo decidirlo

Antes, los dos bonos se aplicaban siempre. Ahora se aplican **solo por la espalda**,
que es lo que el dato pedía y nunca hacía. Es la misma clase de cambio que
`buffDebuffMod`: honrar la intención declarada altera números que llevaban años
comportándose de otra manera. Se puede revertir en un commit.

### Duodécima clase: botones que no llaman a nadie

**12.782 páginas** del datapack piden **507 comandos distintos** por bypass. Los
manejadores declaran 532. Cruzarlos marcó **16** sin atender — y **14 eran falsos
positivos**, cada uno por un mecanismo de despacho distinto que hubo que verificar
uno a uno:

| lo marcado | por qué no lo era |
|---|---|
| `TE023`–`TE028` (67 usos) | van por **su propio paquete**, `RequestTutorialLinkHtml`, y la quest del tutorial los toma con `event.startsWith("TE")` |
| `sellbuffadd`, `sellbuffstart`, … | los sirve un manejador en `custom/SellBuff/`, carpeta que mi glob no cubría |
| `_block;del`, `_block;delconfirm` | `FriendsBoard` declara `_block` y casa **por prefijo** |
| `%fav_bypass%` | es un **marcador de plantilla** que `FavoriteBoard` sustituye en tiempo de ejecución |

Que 14 de 16 fueran falsos no resta valor al barrido: obligó a recorrer **cinco
formas distintas** de atender un bypass —lista declarada, prefijo, paquete propio,
sustitución de plantilla y carpeta fuera del árbol de manejadores— y a comprobar
cada una en vez de suponerla.

#### Los dos que sí están muertos

**`talk_select`**, en `html/default/30949.htm` y `32779.htm`. El enlace dice
literalmente `<a action="bypass -h talk_select">Quest</a>`. Nada en el repositorio
declara ese comando; lo único parecido es `manor_menu_select`, que es otro. El
camino termina en `BypassHandler.getHandler` devolviendo nulo: el jugador pulsa
"Quest" y no ocurre nada.

**`menu_select?ask=348&reply=3`** y `&reply=8`, en dos páginas de
`Q00348_AnArrogantSearch`. Las **otras 23** páginas de esa misma quest enlazan con
`bypass Script QN_AnArrogantSearch <pagina>.htm`; estas tres usan una convención que
nada implementa.

#### Señalado y **no** cambiado

Arreglarlos exige saber **a qué página debe llevar cada botón**, y eso no está en el
repositorio: el `onEvent` de la quest solo tiene casos para páginas concretas y
`30864-13.htm` no es una de ellas. Elegir el destino es reconstruir un flujo de
diálogo — contenido, no reparación. Misma línea que con `Q00627`.

### Decimotercera clase: ids definidos dos veces

Un id repetido no da error: el cargador se queda con uno, o mezcla los dos, y nadie
lo dice. Contado **parseando el XML**, no con expresiones:

| tipo | definidos | repetidos |
|---|---|---|
| skills | 1.976 | **0** |
| ítems | 7.852 | 4, todos `base` contra `custom/` |
| npcs | 5.783 | **1** |

Los cuatro de ítems son el mecanismo de sobreescritura: `custom/` pisa la definición
base a propósito.

#### El npc 900103, que dos funciones distintas reclaman

| fichero | qué declara |
|---|---|
| `custom/core_teleporter.xml` | `Teleportation Cubic`, tipo **Teleporter**, nivel 70 |
| `custom/custom.xml` | `Start`, título "Race Manager", tipo **Folk** |

Y las dos tienen dueño en el código:

- `Core.java`: `TELEPORT_CUBE = 900103` — el cubo de salida tras matar a Core, con su
  lista de destinos en `teleporters/others/900103.xml`.
- `Race.java`: `START_NPC = 900103` — el gestor de la carrera, con sus páginas
  `900103-signup.htm`, `900103-onlist.htm`, `900103-quit.htm`.

El cargador **no reemplaza: fusiona** (`template.set(set)` sobre el mismo objeto), y
`type` decide qué clase Java se instancia. Así que el npc final es un híbrido y
**una de las dos funciones queda rota**.

**Y cuál de las dos, no es fijo.** `parseDirectory` reparte los ficheros a un pool
cuando `ThreadsForLoading` está activo, y `Threads.ini` lo trae **activo**. El orden
depende de la planificación de hilos: puede cambiar entre reinicios.

#### Y una corrección a un comentario propio, en código de producción

La fusión ya estaba bajo `synchronized (_npcs)` de una sesión anterior de esta misma
auditoría, y el candado es correcto. Pero su comentario justificaba la necesidad
diciendo **"29 de los ids vienen definidos en más de un fichero"**, y eso es falso:
esa cuenta contaba como definiciones las referencias `<npc id="...">` dentro de los
bloques `<minions>`. **Es uno.** Corregido en el código, nombrando el caso real.

Uno basta para que el candado haga falta, así que la conclusión no cambia — pero el
número que la sostenía estaba mal y ahora dice la verdad.

#### Señalado y **no** cambiado

Resolver la colisión es darle otro id a una de las dos, y eso toca el `.java`, el
`.xml` del npc, sus páginas html, los datos de teletransporte y cualquier spawn o
fila de base que lo mencione. **Cuál de las dos se mueve es una decisión de
contenido**, no de reparación.

### Tres comprobaciones más, las tres limpias

Se registran aunque no encuentren nada: un negativo con denominador es un
resultado, y saber que ya se miró evita volver a mirarlo.

| comprobación | examinadas | rotas |
|---|---|---|
| nombres de estadística en `<add>`, `<mul>`, `<set>`… contra el enum `Stat` | **9.343** | **0** |
| referencias a (skill, nivel) contra los niveles que el dato define | **24.736** | **0** |
| claves `<set name="…">` de ítem que el código lee | 74.254 declaraciones, 48 claves | **4 sin lector** |

El primero importaba porque `Stat.valueOfXml` **lanza** `NoSuchElementException` ante
un nombre desconocido: una errata ahí no falla en silencio, rompe la carga.

#### Las cuatro claves sin lector, y por qué ninguna es un defecto

- **`equip_condition`** (9) y **`use_condition`** (11) llevan valores en **formato del
  cliente retail** —`{{ec_castle;1};{ec_castle_num;{6}}}`— y en los mismos ítems hay,
  justo debajo, un bloque `<conditions msgId="…">` que es el formato que el servidor
  sí interpreta. Metadato heredado junto a una implementación que funciona.

- **`item_type`** (6): el tipo real viaja en el atributo `type` del `<item>`.

- **`recipe_id`** (781) es información **duplicada**: el vínculo que el servidor usa
  vive en `Recipes.xml`. Lo que sí valía la pena era comprobar si las dos copias
  coinciden, y **coinciden exactamente**: 781 recetas, 781 ítems que la declaran,
  **cero** que Recipes.xml no conozca, **cero** en desacuerdo y **cero** recetas
  huérfanas. (La primera pasada dio 781 discrepancias porque leí el formato al revés:
  en `Recipes.xml` el `id` es la lista de receta y `recipeId` es el ítem del libro,
  no al contrario.)

### Decimocuarta clase: comandos de administración sin derechos declarados

`AdminData.hasAccess` con un comando que no conoce **lo deniega**, salvo que quien lo
teclee esté en el nivel de acceso más alto, en cuyo caso se lo auto-concede y lo
registra. O sea: un comando implementado pero no declarado **solo lo puede usar el GM
máximo**; para todos los demás no existe.

Cruzando los 422 comandos declarados con los 431 que implementan los manejadores:

- **17 implementados y sin declarar.**
- **8 declarados sin manejador** — y cuatro de ellos explican a cuatro de los 17.

#### Las cuatro grafías que no encontraban a nadie

`AdminPunishment` registra **`ban_chat`** y **`unban_chat`**. El XML declaraba
`banchat`, `chatban`, `unbanchat` y `chatunban`: **cuatro formas, ninguna la real**.
El resultado es que las entradas concedían nivel 30 a comandos que no existen, y los
que sí existen quedaban sin entrada — así que **ningún GM por debajo del máximo podía
silenciar a nadie**, teniendo el servidor la intención declarada de permitírselo.

Sustituidas por los dos nombres verdaderos, al mismo nivel 30 que llevaban.

#### Los otros trece

También sin declarar: `jail`, `unjail`, `ban_hwid`, `unban_hwid` (los otros cuatro de
la familia de castigos), `set`, `delete_item`, `use_item`, `msgx`, `donate`,
`donation` y los cuatro de puntos de juego.

Los niveles **no se inventaron**: salen de los hermanos que ya estaban declarados en
el mismo fichero. La familia de castigos a **30**, como `ban_acc`, `unban_acc`,
`ban_char` y `punishment` (y con `confirmDlg` los que castigan, como sus hermanos).
Los de ítem y mensaje a **100**, como `create_item` y `msg`.

Tras el arreglo: **0 comandos implementados sin declarar.**

#### Y un error propio que el validador atrapó al momento

La primera versión del arreglo **rompió el esquema**: escribí `--` dentro de un
comentario XML, que no es legal. Lo cazó la comprobación de XSD de la ronda anterior
en la misma sesión, antes de commitear. Los 7 ficheros de configuración validan.

#### Señalado y **no** cambiado

Quedan cuatro declarados sin manejador en ninguna parte: `tracert`,
`add_clan_skill`, `give_clan_skills` y `give_all_clan_skills`. Conceden derechos
sobre comandos que no existen, lo que es inofensivo pero engañoso. Se dejan: no sé si
son restos de algo retirado o el hueco de algo por venir, y borrar entradas de una
configuración que el dueño puede haber editado es su decisión.

### La cola larga de `quests/`: qué se intentó y qué funcionó

Las clases decidibles estaban agotadas y quedaban 296 ficheros de lógica de quest.
En vez de leerlos uno a uno se aplicó el método que había pagado toda la auditoría
—**enumerar la población y mirar a quién le falta la guarda que llevan los demás**—
apostando a que 296 quests casi idénticas hacen que un atípico signifique algo.

**Cuatro barridos. Tres no sirvieron, y por qué no sirvieron importa.**

| barrido | resultado | por qué |
|---|---|---|
| `exitQuest(true)` tras entregar recompensa (repetible = farmeable) | **descartado** | 152 repetibles / 103 de una vez / 8 mixtas es una distribución sana. Las 8 mixtas comprueban bien: `false` en la ruta de premio, `true` en la de abandono. Los 2 marcados eran mi ventana de "diez líneas atrás" cruzando la frontera entre dos `case` |
| npc registrado con `addKillId` y sin rama en `onKill` | **descartado tras 4 revisiones** | ver abajo |
| recompensa en `onKill` sin comprobar el estado de la quest | **descartado** | 198 a 0 tras reconocer `getRandomPlayerFromParty` como guarda |
| umbral con decimales contra una tirada entera | **1 de 398** | ver abajo |

#### El que no se pudo hacer funcionar, y la lección

El de `addKillId` pasó por cuatro versiones y **cada una redujo los falsos positivos
sin llegar a cero**:

1. Primera: **0 aciertos** — pero el denominador, añadido a propósito, reveló que
   había examinado **cero de 211 quests**. La firma real es `public void onKill`, no
   `public String onKill`.
2. Segunda: 41 de 121 — el patrón `== N` capturaba **cualquier** comparación
   numérica, así que `getQuestItemsCount(...) == 30` contaba como "distingue el npc 30".
3. Tercera: 29 de 93, restringido a `switch (npc.getId())` y `npc.getId() == X`.
4. Al leerlas: los `npc.getId() == X` estaban dentro de **ternarios que eligen
   cantidad o probabilidad**, no en ramas excluyentes. En `Q00264_KeenClaws` el
   goblin da el doble de garras que el lobo; **los dos dan**.

**Se descarta el barrido entero.** "Atender a un npc" tiene demasiadas formas para
detectarlo por patrón, y presentar 29 aciertos que no se sostienen sería peor que no
presentar ninguno.

#### El que sí funcionó

`getRandom(N)` devuelve un **entero**. Compararlo con un umbral decimal descarta la
fracción en silencio. De **398** comparaciones de esa forma en el repositorio,
**397 usan un entero y una no**:

`Q00386_StolenDignity:712` — `getRandom(1000) < 20.200001`, que no tira 20,2 en mil
sino **21**. Todas las demás probabilidades de ese mismo fichero son enteras contra
la misma tirada. Alineada a `20`: 2,0% frente al 2,1% que corría, contra el 2,02%
que el número pretendía decir.

#### La conclusión sobre el método

El método funciona cuando **la guarda tiene una forma uniforme** —un candado, una
comprobación de nulo, un tipo de colección, un umbral entero—. Deja de funcionar
cuando lo buscado se puede escribir de muchas maneras igual de válidas. Los cuatro
barridos de esta tanda separan limpiamente los dos casos, y por eso se registran los
tres que fallaron: para no volver a intentarlos.

#### Quinto barrido: el `cond` que se pone y nadie lee

Parecía el candidato ideal para el criterio recién anotado —**una guarda de forma
única**—: el `cond` de una quest se escribe con `setCond(N)` y se lee con `isCond(N)`,
`getCond() == N` o `case N:`. Un `cond` que se escribe y nadie lee sería una quest que
se atasca a mitad.

Hizo falta **arreglar el barrido dos veces antes de creerle**:

1. Primera pasada: **184 de 198**. La proporción dada vuelta significa, por la regla de
   esta auditoría, que no hay clase sino una forma de lectura que no estoy viendo. Era
   cierta: el `cond` casi siempre se copia a una local (`final int cond = st.getCond();`)
   y se compara ahí.
2. Segunda pasada, siguiendo el alias: **184 otra vez**, idéntico. El heredoc de bash se
   había comido el `\b` de `'|\\b%s\\b'`, así que el patrón buscaba **caracteres de
   retroceso** en vez de fronteras de palabra. Es el mismo fallo de heredoc ya anotado en
   este documento; escrito con la herramienta de fichero, funciona.
3. Tercera pasada, ya correcta: **52 de 132** (69 quedan fuera por leer rangos, 6 por
   calcular el valor).

Y a los 52 los mata una sola observación. La familia dominante es
`pone [2], lee [1]`, repetida en decenas de quests de recolección. En `Q00264_KeenClaws`:

```java
if ((st == null) || !st.isCond(1)) { return; }
if (getQuestItemsCount(player, WOLF_CLAW) >= 50) { st.setCond(2, true); }
```

El `cond` 2 se pone al llegar a 50 garras y **la rama que premia lee el contador de
ítems, no el `cond`**. Ese 2 es el marcador que le dice al cliente "ya podés entregar";
la autoridad del servidor es `getQuestItemsCount`. Un `cond` huérfano no es un defecto:
es el diseño.

De paso, los dos únicos `setCond(0)` del repositorio (`Q00334`, `Q00335`) hacen lo mismo
—limpiar el `cond` junto a un `unset` de variable al reiniciar la quest—, así que
tampoco son atípicos.

**Quinto barrido descartado.** Cuatro de cinco sobre lógica de quest no produjeron nada.

### Conclusión sobre `quests/`

La superficie comprobable por máquina de las 296 quests está agotada. Los cinco barridos
lo dicen con precisión: **funcionan cuando el invariante tiene una forma única en todo el
corpus** (el umbral entero contra `getRandom`, 397 de 398) y **fallan cuando lo buscado
se puede escribir de varias maneras igual de válidas** (atender un npc, leer un `cond`,
decidir que una quest terminó). Lo que queda en esos ficheros no se distingue de lo
correcto sin saber qué debía hacer cada quest, y eso es diseño de juego, no verificación.

Los tres barridos que fallaron quedan escritos con su motivo para que nadie —yo incluido—
los vuelva a intentar creyendo que son nuevos.

## Los recursos JDBC y el SQL construido a mano

Elegido a propósito por el criterio que dejó la tanda de quests: **`DatabaseFactory.getConnection()`
tiene una única forma correcta** —`try (...)`—, así que es exactamente el tipo de invariante que
un barrido sí puede comprobar.

### Los recursos

Hizo falta rehacer el detector una vez: la primera versión marcaba como fallo las líneas que son
la **segunda cláusula** de un `try (` de varias líneas, y daba 369 de 469. Emparejando paréntesis
para encontrar de verdad el encabezado del `try`, el reparto real es:

| recurso | fuera de `try-with-resources` | total |
|---|---|---|
| `Connection` | **0** | 354 |
| `PreparedStatement` / `Statement` | 20 | 469 |
| `ResultSet` | 8 | 153 |

Y ninguno de los 28 es una fuga:

- Los **20 statements cierran a mano** (`statement.close()`), medido: **0 de 469 quedan sin cerrar
  en ningún sitio**. El cierre está fuera de un `finally`, así que una excepción a mitad se lo
  saltaría —pero el pool es **HikariCP**, que al devolver la conexión cierra los statements
  abiertos sobre ella, y las 354 conexiones sí están en `try (...)`.
- Los **8 result sets** están dentro de un `try` cuyo statement sí es recurso, y cerrar un
  statement cierra su result set por especificación JDBC.

**No se toca nada.** Convertir 20 sitios por estilo es ruido con riesgo, y lo que se pedía era
que no hubiera fallos, no que todo se escribiera igual. El resultado que queda registrado es el
útil: **el manejo de recursos JDBC de este repositorio es correcto**, con denominadores.

### El SQL construido por concatenación

De **479** llamadas SQL con argumento, **468 están parametrizadas por completo**. Las 11 que
concatenan, una por una:

- **2 pegan constantes de compilación**: un ternario entre dos literales de nombre de columna
  (`AdminEditChar`) y un `private static final String[]` (`ClanHallAuctionManager`).
- **9 están en `DatabaseInstaller`**, la herramienta local de instalación. Siete pegan el nombre
  de la base y dos el de una tabla. Las de tabla vienen del `SHOW TABLES` del propio servidor.
  Y un identificador **no se puede** enlazar con `?` en JDBC: pegarlo es la única forma.

Queda comprobado que no hay una ruta desde entrada remota hacia SQL sin parametrizar.

Lo que sí se reparó es el **entorno** de ese hallazgo. El nombre de la base lo escribe el operador
—campo de texto en la interfaz, `scanner.nextLine()` en consola— y solo se comprobaba que no
estuviera vacío. Un nombre con un espacio o un guion producía un error de sintaxis de MySQL a
mitad de la instalación, con statements ya ejecutados y un mensaje que no dice qué hacer. Ahora
un `isValidDatabaseName` compartido por las dos entradas exige un identificador MySQL simple
(letras, dígitos, `_`, `$`, hasta 64 caracteres) y rechaza antes de conectarse, diciendo qué
está mal.

## Los descriptores de fichero: la misma regla, sin pool que la salve

El barrido de JDBC salió limpio porque HikariCP cierra los statements al devolver la
conexión. Los recursos de sistema operativo —ficheros, directorios, jars, sockets— **no
tienen quien los rescate**: lo que no se cierra, se queda abierto hasta que muere el
proceso. Misma forma única de invariante, consecuencia distinta. Por eso valía repetirlo.

**Antes: 15 de 26 en `try-with-resources`. Después: 25 de 26.** El que queda es el
`_socket` de `LoginServerThread`, un campo que vive lo que vive el proceso y que se
cierra en un `finally`; ese es el diseño, no un descuido.

Los diez tenían todos la misma pinta —`close()` como última línea del `try`, y un
`catch` que solo registra— así que **cualquier excepción a mitad se saltaba el cierre**
y el servidor seguía con el descriptor colgado:

| dónde | qué se escapaba |
|---|---|
| `DatabaseBackup:49` | `Files.list` mantiene abierto el **directorio**; una copia de seguridad por día |
| `ServerConfig:243` | `Files.lines` mantiene abierto el fichero de filtro de chat |
| `SpawnData` ×5 | lector y escritor de los XML de spawn, en un **comando de admin repetible** |
| `ZoneBuildManager:151` | el escritor de la zona dibujada |
| `SystemPanel:110` | el **jar del servidor entero**, si el manifiesto no trae `Build-Date` |
| `Search:760` | ver abajo |

`Search` era el más claro de todos: abría `Files.walk` **en** un `try-with-resources` y
después abría **un segundo `Files.walk` dentro del cuerpo**, solo para contar ficheros,
que nunca se cerraba. Uno por búsqueda.

### El entorno del hallazgo

En `SpawnData` la fuga no era lo único. Las dos rutas terminan igual:

```java
writer.close();
reader.close();
spawnFile.delete();
tempFile.renameTo(spawnFile);
```

Los dos resultados **se descartaban**. Y los descriptores abiertos son precisamente lo
que hace fallar un `renameTo` en Windows, así que la fuga y el descarte se tapaban entre
sí: si algo salía mal, el spawn se perdía en silencio y quedaba un `.tmp` al lado. Ahora
el reemplazo ocurre **fuera** del `try` —donde los descriptores ya están sueltos—, se
comprueban las dos respuestas, y una copia que falló borra su temporal en vez de dejarlo.

En `SystemPanel` el mismo tipo de detalle: el `catch` decía `// Handled above.` y no
había nada arriba que lo tratara; un manifiesto sin `Build-Date` lanzaba antes del
`close`. Ahora el jar se cierra pase lo que pase.

## `equals` sin `hashCode`

Otro invariante de forma única: una clase que define uno de los dos y no el otro rompe
el contrato de `Object`, y con él cualquier `HashSet` o `HashMap` que la contenga.

**12 de 15 clases definen los dos. Tres definían solo `equals`**, las tres con la misma
forma —comparar un campo de identidad y heredar el hash de identidad de `Object`—, de
modo que dos objetos **iguales** caían en cubos **distintos** y una búsqueda con una
instancia equivalente pero no idéntica no encontraba nada:

| clase | compara por | campo |
|---|---|---|
| `WorldObject` | `getObjectId()` | mutable |
| `AbstractResidence` | `getResidenceId()` | `final` |
| `ClientHardwareInfoHolder` | `_macAddress` | `final` |

`WorldObject` es la base de **todo** objeto del juego, y el repositorio declara **100**
colecciones de hash que guardan world objects directamente (`Set<Player>` 30,
`Map<Player,` 19, `Set<Creature>` 16, y así). El contrato se estaba usando de verdad.

### Por qué el campo mutable no impidió el arreglo

Poner `hashCode` sobre un campo que cambia es cambiar el cubo de un objeto ya guardado.
Antes de tocarlo se rastrearon las tres rutas que mueven `_objectId`:

- **`refreshId()`** — sus **dos** llamadas están comentadas. Muerto.
- **`Item.claimNewId()`** — un solo llamador, `ItemManager.createItem`, sobre un ítem
  recién sacado del pool. Y `destroyItem`, que es quien lo devuelve al pool, hace
  `World.removeObject` y `IdManager.releaseId` **antes** de encolarlo.
- **`PlayerInventory:327`** — es un `TradeItem`, que no es un `WorldObject`.

Es decir: ningún world object vivo cambia de id mientras está dentro de una colección de
hash. La disciplina ya existía; solo no estaba escrita. Ahora lo está, en el javadoc de
`refreshId`, para quien alguna vez descomente esas dos llamadas.

### El test

`WorldObjectHashTest`, cinco casos, validado contra la versión sin `hashCode`: **cuatro
fallan** ahí y pasan con el arreglo. El quinto —ids distintos no son iguales— pasa en
ambas: es el control que confirma que el test no está simplemente roto.

## Comparar referencias donde había que comparar valores

Clásico y de forma única: `String` con `==`, y envoltorio contra envoltorio con `==`
(contra un primitivo se desenvuelve y es correcto, así que hay que distinguirlo).

**5.146 comparaciones examinadas, cero defectos**, y esta vez el cero se puede afirmar
sin depender de mi tabla de tipos:

- **0** comparaciones contra un literal de texto, en cualquiera de los dos lados
- frente a **1.244** comparaciones por valor (`.equals(` 670, `.equalsIgnoreCase(` 574)

Los 31 casos que el barrido marcó con un `String` a un lado resultaron ser homónimos:
`order == A2Z` en `SortedWareHouseWithdrawalList` son **`byte`**, `cabal ==
SevenSigns.CABAL_DAWN` es **`int`**. Mi tabla los confundió con variables llamadas igual
declaradas `String` en otro punto del mismo fichero. Ninguno es un defecto.

## Las tareas periódicas: el mecanismo estaba, la visibilidad no

Un `scheduleAtFixedRate` cuya tarea lanza queda **cancelado para siempre** por el
ejecutor, sin decir nada. Con 69 tareas periódicas en el servidor —guardias de asedio,
IA de invocaciones, guardado del manor, reinicio diario, alimentación de monturas— es de
las cosas que matan un servidor despacio.

No pasa aquí. Los cinco puntos de entrada de `ThreadPool` (`schedule`,
`scheduleAtFixedRate`, `schedulePriorityTaskAtFixedRate`, `execute`) envuelven cada
`Runnable` en un `RunnableWrapper` que atrapa `Throwable` y no lo relanza, y el barrido
confirma que **ninguna** tarea periódica se programa fuera de `ThreadPool`. Los tres
ejecutores que sí se crean por fuera (`PacketExecutor`, `IXmlReader`,
`DatabaseIdManager`) no programan nada periódico.

### Lo que sí faltaba

El envoltorio entregaba el `Throwable` a
`Thread.currentThread().getUncaughtExceptionHandler()`. Pero `ThreadProvider.newThread`
fija nombre, prioridad y condición de demonio y **no instala manejador**, y no hay
ninguno global —comprobado: cero `setDefaultUncaughtExceptionHandler` en el repositorio—.
Así que ese manejador era siempre el `ThreadGroup`, que imprime a **stderr**: todo fallo
de tarea del servidor caía fuera del log donde escribe el resto.

Ahora el envoltorio lo registra en el log con el nombre de la tarea, y sigue cediendo a
un manejador real si alguna vez se instala uno. La tarea sobrevivía igual; lo que cambia
es que ahora se ve.

### El test

`ThreadPoolContainmentTest`, validado contra la versión sin el `try` del envoltorio: la
prueba de la tarea periódica **falla** ahí y pasa con él. La de una sola vez pasa en
ambas —el ejecutor de hilos virtuales absorbe la excepción de todos modos—, y se deja
justamente como control.
