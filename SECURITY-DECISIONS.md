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

## Ya arreglado

_(vacío por ahora)_

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
