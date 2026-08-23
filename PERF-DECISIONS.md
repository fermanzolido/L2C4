# Registro de decisiones de performance

Este archivo es la memoria de las revisiones automáticas de performance.
**Leelo antes de proponer cualquier optimización.** Si algo ya está acá como
aplicado o como rechazado, no lo vuelvas a proponer.

Regla importante: **este archivo nunca debe modificarse dentro de la rama de
una PR de optimización.** Se actualiza solo con un commit aparte sobre `main`.
Commitearlo en cada rama fue exactamente lo que puso en conflicto entre sí a
las 143 PRs del bot anterior.

---

## Ya aplicado en main (no re-proponer)

| Área | Qué se hizo |
|---|---|
| `CharInfoTable.getIdByName` | Índice `_namesLower` en minúsculas para lookup O(1), mantenido en add/remove y en los fallbacks a BD. Usa `Locale.ENGLISH`. |
| `ClanTable.getClanByName` | Índice `_clansByName`, mantenido en alta, rename y baja. Usa `Locale.ENGLISH`. |
| `Quest.setQuestToOfflineMembers` | JDBC batching + try-with-resources + guarda de lista vacía. |
| `Quest.deleteQuestToOfflineMembers` | try-with-resources (fix de leak de `PreparedStatement`). |
| `OfflineTraderTable.storeOffliners` | Batches ejecutados fuera del bucle. |
| `GrandBossManager.storeMe` | `executeUpdate` en bucle reemplazado por batch. |
| `RaidBossPointsManager` | Cache `_totalPoints`: `getPointsByOwnerId` O(1), ranking O(n). Se limpia en `cleanUp()`. |
| `FriendListExtended` | N+1 reemplazado por una query `IN (...)`, preservando el orden de la lista. |
| `SchedulingPattern`, `BuyListData`, `MultisellData` | Regex precompilados en constantes estáticas. |
| `ServerConfig` + `PetNameTable` + `VillageMaster` | `PET_NAME_TEMPLATE` y `CLAN_NAME_TEMPLATE` compilados una vez a `Pattern` en vez de por llamada. |
| `StringUtil.append/concat` | `LinkedList` intermedia reemplazada por `String[]` dimensionado. |
| `World`, `InstanceWorld`, `SkillCoolTime`, `CharSelectionInfo` | Ya usan `ArrayList` en los hot paths de visibilidad y packets. |
| Target handlers (20 archivos) + `Creature.getTargetList` | `LinkedList` → `ArrayList` en el hot path de targeting. |
| `EffectList`, `Inventory`, `ItemContainer`, `PlayerInventory` | Nueve acumuladores locales `LinkedList` → `ArrayList`. |
| `ServerConfig` + `Say2.checkText` | Filtro de chat: `replaceAll("(?i)" + palabra, ...)` compilaba el regex de cada palabra en **cada mensaje de chat**. Ahora se precompilan a `List<Pattern>` con `CASE_INSENSITIVE` al cargar la config. Regex inválido se loguea y se saltea en la carga en vez de tirar excepción durante el chat. (PR #199, mergeada 2026-08-23) |

## Rechazado con motivo (no re-proponer sin argumento nuevo)

| Propuesta | Por qué se rechazó |
|---|---|
| Pre-dimensionar las listas de visibilidad de `World` contando primero los objetos de las regiones vecinas | Cuesta una pasada completa extra solo para evitar el crecimiento amortizado de `ArrayList`. Probablemente más lento que no hacer nada. |
| Dimensionar listas en target handlers con `skill.getAffectLimit()` | Ese método llama a `Rnd.get()` internamente, y los handlers ya lo llaman para el tope real de objetivos. Dimensionar así produce una segunda tirada de RNG distinta en cada casteo. |
| Batchear raid points por party (nueva API `addPoints(Map, int)`) | Requiere cambiar la API de `RaidBossPointsManager` y solo ayuda al matar un raid boss. Ganancia marginal, riesgo de reemplazar una implementación ya verificada. |
| Sacar `synchronized` de `CharInfoTable.doesCharNameExist` | El atajo O(1) ya está dentro del método; mantener el lock preserva la serialización de la que dependía el código original. |
| Hoistear `_instance.getNpcs()` a una variable local en `InstanceWorld` | `getNpcs()` solo devuelve un campo. La JIT lo inlinea; el cambio es cosmético. |
| Reescribir comentarios de optimizaciones existentes | Sin cambio funcional. Es ruido en el diff. |

## Resuelto

- **Filtro anti-buff de monstruos en `Skill.java`** (resuelto 2026-08-23). El
  guard `(result instanceof ArrayList || result instanceof LinkedList)` nunca
  corría para las skills de objetivo único, porque esos handlers devuelven
  `Collections.singletonList` o `emptyList` — y tampoco podía funcionar sobre
  ellas, al ser inmutables (`removeIf` habría tirado excepción). Se eliminó el
  guard y se arregló el caso real en el origen: Riba Iren (`Orfen.java`)
  casteaba heal 4516 sobre su atacante en vez de sobre sí mismo. También se
  quitó el override muerto `doCast` de `Monster.java`.

  **Pendiente relacionado, no es performance:** la protección efectiva quedó
  solo en `AttackableAI`, que los scripts de AI con `setTarget(player)` a mano
  esquivan. Hay 13 scripts con el patrón `setTarget` + `doCast` (Baium,
  Valakas, Zaken, KetraOrcSupport, VarkaSilenosSupport, CabaleBuffer, entre
  otros); varios son buffers legítimos, pero ninguno está auditado. No lo
  toques desde una PR de performance.

  **Auditado el 2026-08-23: no quedaron casos.** Se revisaron las 74 skills
  distintas que castean los scripts de `ai/` (no solo los 13 con el patrón
  `setTarget` + `doCast` — el barrido amplio agregó `QueenAnt`, que usa
  `useMagic`). Solo 4 son beneficiosas con target distinto de SELF: Angel Heal
  (4133), Orfen Heal (4516) y los dos heals de Queen Ant (4020/4024). Las
  cuatro apuntan siempre a NPCs — a sí mismo, al `caller` de `onFactionCall`,
  a la larva o a la reina — nunca a un jugador. Todo lo que un monstruo lanza
  sobre un jugador tiene `effectPoint` -100 o -1, o sea hostil.

  Los buffers que sí apuntan a jugadores a propósito (Ketra, Varka, Cabale,
  ArenaManager, CastleChamberlain) son `Folk`, `Warehouse` o `Merchant`, no
  `Monster`, así que el guard nunca les aplicó: exigía `isMonster()`.

## Quedan `LinkedList` a propósito

No todo `LinkedList` es un error. Antes de convertir uno, verificá que sea un
acumulador local y no se use con semántica de cola (`addFirst`, `removeFirst`,
`poll`) ni con borrado desde el medio.
