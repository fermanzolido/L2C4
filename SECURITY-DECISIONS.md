# Registro de decisiones de seguridad

Memoria de la revisión automática de seguridad. **Leelo antes de proponer nada.**

Este archivo se actualiza **solo con un commit aparte sobre `main`**, nunca
dentro de la rama de una PR.

---

## Regla de divulgación

Este repositorio es **público**. Una PR que describa con detalle cómo explotar
una vulnerabilidad es una divulgación pública antes de que el arreglo esté
desplegado.

Al abrir una PR de seguridad:

- Describí **qué se endurece**, no cómo explotarlo.
- Nada de payloads, pasos de reproducción ni pruebas de concepto.
- "Se agrega validación de rango al índice recibido del cliente" está bien.
  "Un cliente puede mandar índice -1 y leer memoria arbitraria" no.
- Si el hallazgo es grave y no se puede arreglar sin describirlo, **no abras
  PR**: informalo en la salida de la corrida para que lo maneje una persona.

## Pendiente para esta rutina (tomar de acá primero)

Lo que esté en esta sección es **trabajo para hacer**, no una exclusión. Si hay algo acá, tiene prioridad sobre buscar candidatos nuevos. Todas las demás secciones del archivo son listas de NO proponer.

_(vacío)_

## Ya arreglado

- **PR #202** (2026-08-24): en el handler de multisell (`MultiSellChoose`), se
  corrigieron dos chequeos de desbordamiento que comparaban una
  multiplicación/suma de cantidad ya calculada en `int` contra
  `Integer.MAX_VALUE` — la comparación nunca podía dispararse porque el
  cálculo ya se había desbordado antes de llegar a ella. Ahora la comparación
  se hace en `long`, así una cantidad que desbordaría se detecta y la
  operación se cancela en vez de continuar con un valor incorrecto.

## Auditado, sin hallazgos (no repetir el barrido)

- **Guardas de overflow en `clientpackets/`** (2026-08-24). Once archivos
  comparan un `int` contra `Integer.MAX_VALUE`, lo cual nunca puede ser cierto:
  son condiciones muertas. Se revisó si esa guarda era la única protección
  contra un overflow real de cantidad por precio, como sí pasaba en
  `MultiSellChoose` (PR #202, donde las guardas tienen cast a `long` y una era
  inefectiva). No lo es: el cálculo de precio usa una comprobación por división
  (`MAX_ADENA / count < price`) que nunca multiplica primero, y además la
  cantidad ya está acotada por otra vía — tope de 10000 en compras a NPC, stock
  real del vendedor en tienda privada, stock del manor en semillas.

  Conclusión: son código muerto, no un agujero. **Limpiarlas le corresponde a la
  rutina de limpieza, no a esta.** Anotado en `DEBT-DECISIONS.md`.

- **Revisados sin hallazgos en la misma corrida:** handlers de ítems
  (destroy, drop, crystallize, henna), trade (`AddTradeItem`, `TradeList`),
  comandos de clan (`RequestPledgeSetMemberPowerGrade`, `RequestOustPledgeMember`)
  y el despacho de comandos de admin (`AdminCommandHandler`,
  `RequestBypassToServer`). Validaciones de autorización y rango consistentes.

## Rechazado con motivo

_(vacío por ahora)_

---

## Qué buscar

El gameserver recibe paquetes de clientes **no confiables**. Esa es la
superficie principal.

- Concatenación de SQL con datos del cliente en vez de `PreparedStatement`.
- Falta de validación de entrada en `network/clientpackets/`: índices,
  cantidades, ids de objeto, longitudes de string.
- Cantidades que pueden desbordar o ser negativas (adena, stacks de ítems).
- Chequeos de autorización faltantes en comandos de admin y GM.
- Credenciales, tokens o rutas absolutas hardcodeadas.
- Confiar en valores que el cliente puede falsificar en vez de recalcularlos
  del lado del servidor.
