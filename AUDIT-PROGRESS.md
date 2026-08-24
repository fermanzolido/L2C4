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
| `model/clan` | 6 | 3.163 | pendiente |
| **`model/itemcontainer`** | **10** | **3.727** | **en curso** |
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

## `model/itemcontainer` — en curso

**Leído:** `ItemContainer.java` completo (661 líneas, la clase base) e
`Inventory.java` hasta la línea ~930: vías de integridad de ítems (`dropItem`,
`addItem`, `removeItem`) y el núcleo del paperdoll (`setPaperdollItem`).

**Pendiente:** `equipItem` (183 líneas de lógica por body part), los cinco
listeners de paperdoll, `restore`, `PlayerInventory.java` (999) y siete
archivos chicos.

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
