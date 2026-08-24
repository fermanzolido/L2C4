# Registro de decisiones de correctitud

Memoria de la revisión automática de bugs. **Leelo antes de proponer nada.**
Si algo figura acá como arreglado o como rechazado, no lo vuelvas a proponer.

Este archivo se actualiza **solo con un commit aparte sobre `main`**, nunca
dentro de la rama de una PR. Commitear el archivo de memoria en cada rama es
lo que puso en conflicto entre sí a las 143 PRs del bot anterior.

---

## Ya arreglado (no re-proponer)

| Área | Qué se arregló |
|---|---|
| `Skill.getTargetList` | Se eliminó el guard `instanceof ArrayList \|\| instanceof LinkedList` que pretendía impedir que los monstruos buffearan jugadores. Nunca corría para skills de objetivo único, porque esos handlers devuelven `Collections.singletonList`, y tampoco podía correr sobre ellas por ser inmutables. |
| `Orfen.java` (Riba Iren) | Casteaba heal 4516 sobre su atacante en vez de sobre sí mismo. Era el único caso real que el guard pretendía tapar. |
| `Monster.java` | Se quitó el override muerto `doCast(Skill, Creature, List<WorldObject>)`. |
| `Quest.deleteQuestToOfflineMembers` | `PreparedStatement` fuera de try-with-resources: se filtraba si `executeUpdate()` tiraba excepción. |
| `FriendListExtended.java` | Decidía "amigo online" con `World.getInstance().getPlayer(objId) != null` en vez de `Player.isOnline()`. Durante la ventana de logout (entre que `deleteMe()` marca `_isOnline=false`/persiste `online=0` y que `decayMe()` saca al jugador de `World`), un amigo desconectándose aparecía como online con su `objectId` real en vez de con los datos offline de la DB. |

## Auditado, sin hallazgos (no repetir el barrido)

- **Monstruos casteando skills beneficiosas sobre jugadores** (2026-08-23). Se
  revisaron las 74 skills distintas que castean los scripts de `ai/`. Solo 4 son
  beneficiosas con target distinto de SELF: Angel Heal (4133), Orfen Heal (4516)
  y los dos heals de Queen Ant (4020/4024). Las cuatro apuntan siempre a NPCs.
  Todo lo que un monstruo lanza sobre un jugador tiene `effectPoint` negativo.

## Rechazado con motivo

_(vacío por ahora)_

---

## Qué buscar

Bugs reales, no hipotéticos. Priorizá por consecuencia visible para el jugador:

- Condiciones de carrera sobre estado compartido (colecciones no concurrentes
  accedidas desde varios hilos, chequeo-y-acción no atómico).
- `NullPointerException` en caminos alcanzables.
- Off-by-one en índices, rangos y niveles.
- Recursos sin cerrar: `PreparedStatement`, `ResultSet`, `Connection`, streams.
- Lógica invertida o condiciones que nunca se cumplen.
- Código muerto que encubre un bug (un `if` que jamás entra).
- Excepciones tragadas que ocultan fallas reales.

**No** cuentan: preferencias de estilo, refactors sin cambio de comportamiento,
ni "esto podría fallar si alguien algún día hace X" sin un camino real que lo
alcance.
