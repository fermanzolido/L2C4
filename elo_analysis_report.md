# Análisis del Sistema ELO de la Olimpiada

El sistema de ELO implementado en el servidor L2JMobius es una variante del sistema de clasificación ELO estándar, adaptado para las Olimpiadas de Lineage 2. A continuación se detalla su funcionamiento, configuración y hallazgos técnicos.

## 1. Funcionamiento del Sistema

El sistema asigna una puntuación ELO a cada noble, que se utiliza para emparejar oponentes y calcular recompensas.

### Cálculo de ELO
La fórmula utilizada para actualizar el ELO tras una partida es la estándar:
\[ ELO_{nuevo} = ELO_{actual} + K \times (ResultadoReal - ResultadoEsperado) \]

*   **ResultadoReal**: 1.0 (Victoria), 0.0 (Derrota), 0.5 (Empate).
*   **ResultadoEsperado**: Calculado mediante la función logística basada en la diferencia de ELO entre los dos jugadores.
    \[ E_{A} = \frac{1}{1 + 10^{(R_B - R_A)/400}} \]

### Características Especiales
*   **Anti-Feeding**: Si la diferencia de ELO entre dos jugadores es mayor que `OLYMPIAD_ELO_MAX_GAP` (por defecto 400), el ELO **no se actualiza** (la variación es 0). Esto evita que jugadores con alto ELO ganen puntos fáciles contra jugadores con bajo ELO.
*   **Recompensas Dinámicas**: La cantidad de objetos de recompensa (Gate Pass) escala con el promedio de ELO de los participantes.
    \[ Recompensa = Base \times \frac{ELO_A + ELO_B}{2000} \times Multiplicador \]
*   **Títulos de División**: Los jugadores reciben un título y color de título basado en su rango de ELO (Bronce, Plata, Oro, Platino, Diamante).
*   **Reinicio de Temporada (Soft Reset)**: Al finalizar la temporada, el ELO se reinicia parcialmente hacia el valor inicial (1000).
    \[ ELO_{nuevo} = 1000 + (ELO_{viejo} - 1000) \times 0.5 \]
    Los jugadores con menos de 1000 vuelven a 1000.

## 2. Configuración (`OlympiadConfig.java`)

Los parámetros principales se encuentran en `config/Olympiad.ini`:

*   `OLYMPIAD_ELO_INITIAL_VALUE`: Valor inicial (Default: 1000).
*   `OLYMPIAD_ELO_K_FACTOR`: Factor K, determina la volatilidad del ELO (Default: 32).
*   `OLYMPIAD_ELO_MAX_GAP`: Diferencia máxima para ganar puntos ELO (Default: 400).
*   `OLYMPIAD_ELO_ANNOUNCE_THRESHOLD`: Umbral para anunciar partidas destacadas (Default: 1500).
*   `OLYMPIAD_SEASON_SOFT_RESET_MULTIPLIER`: Multiplicador para el reinicio suave (Default: 0.5).

## 3. Hallazgos Técnicos y Bugs

Durante el análisis del código, se identificaron los siguientes problemas potenciales:

### 3.1. Condición de Carrera en el Registro (Bug Crítico)
Las listas de jugadores registrados (`_nonClassBasedRegisters` y `_classBasedRegisters`) en `Olympiad.java` utilizan `ArrayList`, que **no es seguro para hilos (thread-safe)**.
*   **Problema**: `OlympiadManager` modifica estas listas (ordenando y eliminando elementos) en un hilo separado, mientras que los jugadores pueden registrarse/desregistrarse desde el hilo principal del juego.
*   **Consecuencia**: Esto puede provocar `ConcurrentModificationException` y caídas del hilo de la Olimpiada, o corrupción de la lista de espera, dejando jugadores atascados o eliminados incorrectamente.
*   **Solución Propuesta**: Utilizar `Collections.synchronizedList` y sincronizar los bloques de iteración/modificación.

### 3.2. Rendimiento en el Reinicio de Temporada
El método `performSeasonReset` en `Olympiad.java` se ejecuta en el hilo principal de la Olimpiada pero realiza operaciones de base de datos síncronas para **cada noble** individualmente (en `giveSeasonReward`).
*   **Problema**: Si hay muchos nobles, esto podría congelar el servidor o el gestor de olimpiadas durante un tiempo considerable.
*   **Recomendación**: Optimizar las consultas SQL para realizar actualizaciones por lotes (batch updates).

### 3.3. Entrega de Recompensas
El método `giveSeasonReward` entrega recompensas directamente al inventario si el jugador está en línea.
*   **Observación**: Si el inventario del jugador está lleno, el objeto se cae al suelo (`dropItem`). Esto es un comportamiento estándar pero podría resultar en pérdida de recompensas si el jugador no se da cuenta.

### 3.4. Emparejamiento Estricto
El método `nextOpponents` ordena toda la lista de espera por ELO y empareja siempre a los dos mejores.
*   **Observación**: Esto garantiza partidas equilibradas pero puede hacer que los mismos jugadores se enfrenten repetidamente si se registran simultáneamente.

## Conclusión

El sistema ELO está bien diseñado conceptualmente, pero la implementación técnica de las listas de registro presenta un riesgo significativo de concurrencia que debe ser corregido para garantizar la estabilidad del servidor.
