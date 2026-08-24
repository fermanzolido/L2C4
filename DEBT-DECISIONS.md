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

## Conocido, pendiente de decisión humana

- **Los tests no son ejecutables.** `test/` usa JUnit 4 (`org.junit.Test`), pero
  no hay jar de JUnit en `dist/libs/` ni target `test` en `build.xml`. O sea,
  las 8 clases de test no corren desde un checkout limpio. Arreglarlo implica
  decidir cómo se vendoriza la dependencia; no lo hagas por tu cuenta.
- **`Quest.java` tiene formato inconsistente** con el resto del código: llaves
  en la misma línea, mientras el proyecto usa estilo Allman. Reformatearlo
  entero generaría un diff enorme que taparía el historial. Requiere decisión.

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
