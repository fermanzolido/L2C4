# Registro de decisiones de deuda técnica

Memoria de la revisión automática de limpieza. **Leelo antes de proponer nada.**

Este archivo se actualiza **solo con un commit aparte sobre `main`**, nunca
dentro de la rama de una PR.

---

## Ya arreglado

| Área | Qué se hizo |
|---|---|
| `Monster.java` | Override muerto `doCast` y sus imports sin uso. |
| `CharInfoTable.java` | Import `Map.Entry` sin uso. |
| `RaidBossPointsManager.java` | Bloque javadoc duplicado. |
| `LoginServer.java` | Import `java.net.ServerSocket` sin uso (PR #203). |
| `VillageMaster.java`, `Teleporter.java` | Imports sin uso: `Pattern`, `PatternSyntaxException`, `StringUtil` (PR #205). |

## Conocido, pendiente de decisión humana

- **Los tests no son ejecutables.** `test/` usa JUnit 4 (`org.junit.Test`), pero
  no hay jar de JUnit en `dist/libs/` ni target `test` en `build.xml`. O sea,
  las 8 clases de test no corren desde un checkout limpio. Arreglarlo implica
  decidir cómo se vendoriza la dependencia; no lo hagas por tu cuenta.
- **`Quest.java` tiene formato inconsistente** con el resto del código: llaves
  en la misma línea, mientras el proyecto usa estilo Allman. Reformatearlo
  entero generaría un diff enorme que taparía el historial. Requiere decisión.

## Pendiente para esta rutina (tomar de acá primero)

Lo que esté en esta sección es **trabajo para hacer**, no una exclusión. Si hay algo acá, tiene prioridad sobre buscar candidatos nuevos. Todas las demás secciones del archivo son listas de NO proponer.

### Derivado desde la rutina de seguridad

- **Condiciones muertas `int > Integer.MAX_VALUE` en `clientpackets/`** (anotado
  el 2026-08-24 por la rutina de seguridad). Once archivos comparan una variable
  `int` contra `Integer.MAX_VALUE`, comparación que nunca puede ser cierta:
  `RequestBuyItem`, `RequestBuySeed`, `RequestPrivateStoreBuy`,
  `RequestPrivateStoreSell`, `RequestProcureCropList`, `RequestSellItem`,
  `SendWareHouseDepositList`, `SendWareHouseWithDrawList`, entre otros.

  Ya se verificó que NO son un problema de seguridad: la protección real contra
  overflow es `MAX_ADENA / count < price`, que no multiplica primero. Son código
  muerto y nada más.

  **Cuidado al tocarlas:** en `RequestPrivateStoreBuy` y `RequestProcureCropList`
  la condición no rechaza sino que acota (`cnt = Integer.MAX_VALUE`), así que
  sacarla cambia la forma del bloque. Y NO toques `MultiSellChoose`: ahí las
  guardas tienen cast a `long` y sí están vivas.

## Rechazado con motivo

_(vacío por ahora)_

---

## Qué buscar

Bajo riesgo, con beneficio real de mantenimiento:

- Código muerto: métodos privados sin llamadores, campos sin leer, ramas
  inalcanzables.
- Imports sin usar.
- Duplicación copiada y pegada que puede unificarse sin cambiar comportamiento.
- TODOs y FIXMEs que ya están resueltos o que describen algo verificable.
- Javadoc que contradice lo que hace el código.

**No** cuentan: reformateo masivo, renombres por gusto, ni refactors grandes
que toquen muchos archivos. Si el diff supera unos 150 renglones, es demasiado
grande para esta rutina.
