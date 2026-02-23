# Análisis del Sistema ELO de la Olimpiada

## Resumen Ejecutivo

Este documento detalla el análisis del sistema de ELO implementado en el servidor L2JMobius para las Olimpiadas. Se ha revisado la lógica de cálculo de puntuación, emparejamiento, recompensas y concurrencia.

Se identificó y corrigió un **bug crítico de concurrencia** en el proceso de registro que podría causar condiciones de carrera y corrupción de datos.

## 1. Funcionamiento del Sistema ELO

El sistema utiliza una variante del sistema de clasificación ELO estándar adaptado para un MMORPG.

### Fórmula de Actualización
La puntuación ELO se actualiza tras cada partida según la fórmula:
\[ ELO_{nuevo} = ELO_{actual} + K \times (ResultadoReal - ResultadoEsperado) \]

Donde:
*   **K (Factor K)**: Determina la volatilidad del ELO. Configurable mediante `OLYMPIAD_ELO_K_FACTOR` (Por defecto: 32).
*   **ResultadoReal**: 1.0 para victoria, 0.0 para derrota, 0.5 para empate.
*   **ResultadoEsperado**: Probabilidad de victoria estimada basada en la diferencia de ELO:
    \[ E_{A} = \frac{1}{1 + 10^{(ELO_{B} - ELO_{A})/400}} \]

### Mecánicas Adicionales

1.  **Anti-Feeding**: Si la diferencia de ELO entre los participantes supera `OLYMPIAD_ELO_MAX_GAP` (Por defecto: 400), la partida no otorga ni resta puntos ELO. Esto previene que jugadores de alto nivel abusen de cuentas secundarias de bajo nivel para inflar su puntuación.
2.  **Recompensas Dinámicas**: La cantidad de `Gate Passes` (u otro item de recompensa) escala proporcionalmente al promedio de ELO de los participantes, incentivando la participación de jugadores competitivos.
    \[ Recompensa = Base \times \frac{ELO_A + ELO_B}{2000} \times Multiplicador \]
3.  **Soft Reset (Reinicio de Temporada)**: Al finalizar la temporada, el ELO no se reinicia a 0 o 1000, sino que se comprime hacia el valor base.
    \[ ELO_{nuevo} = 1000 + (ELO_{viejo} - 1000) \times Multiplicador_{SoftReset} \]
    Esto mantiene la jerarquía relativa pero reduce las brechas extremas al inicio de una nueva temporada.

## 2. Correcciones Técnicas Implementadas

Durante el análisis del código fuente (`Olympiad.java`), se detectaron problemas de seguridad de hilos (Thread Safety) en la gestión de las listas de registro.

### Problema: Condición de Carrera en `registerNoble`

El método `registerNoble` presentaba dos vulnerabilidades relacionadas con el acceso concurrente a las estructuras de datos:

1.  **Operación Check-Then-Act no atómica**:
    El código verificaba si existía una lista de registro para una clase (`containsKey`) y, si no existía, creaba una nueva y la insertaba (`put`).
    ```java
    if (_classBasedRegisters.containsKey(id)) {
        list = _classBasedRegisters.get(id);
    } else {
        list = new ...;
        _classBasedRegisters.put(id, list); // Posible condición de carrera
    }
    ```
    Si dos jugadores de la misma clase se registraban simultáneamente (y era la primera vez para esa clase), ambos hilos podían entrar en la rama `else`, sobrescribiendo uno la lista del otro y perdiendo el registro del primer jugador.

2.  **Riesgo de `ConcurrentModificationException`**:
    Se iteraba manualmente sobre la lista de participantes (`ArrayList` envuelto en `SynchronizedList`) para verificar duplicados sin bloquear explícitamente la lista.
    ```java
    for (Player participant : classed) { ... } // No sincronizado
    ```
    Si `OlympiadManager` modificaba la lista (ej. emparejando jugadores y removiéndolos) durante esta iteración, se produciría una excepción, potencialmente interrumpiendo el hilo de ejecución.

### Solución Aplicada

Se realizaron las siguientes modificaciones en `java/org/l2jmobius/gameserver/model/olympiad/Olympiad.java`:

1.  **Inserción Atómica**: Se reemplazó la lógica `if-else` con `computeIfAbsent`, que garantiza la atomicidad de la operación de creación e inserción en el mapa concurrente.
    ```java
    _classBasedRegisters.computeIfAbsent(id, k -> Collections.synchronizedList(new ArrayList<>())).add(noble);
    ```

2.  **Iteración Sincronizada**: Se envolvió el bucle de verificación de duplicados en un bloque `synchronized` sobre la lista, asegurando acceso exclusivo durante la lectura.
    ```java
    synchronized (classed) {
        for (Player participant : classed) {
            // Verificación segura
        }
    }
    ```

Estas correcciones aseguran la estabilidad del sistema de registro bajo carga y previenen la pérdida de datos de los participantes.
