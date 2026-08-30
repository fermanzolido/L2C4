# L2C4 — Chronicle 4: Scions of Destiny

[English](README.md) · **Español**

Un servidor de Lineage 2 Chronicle 4, construido sobre [L2J Mobius](http://www.l2jmobius.org/), corriendo en **Java 25**.

Lo que separa a este fork del resto no es una lista de funciones. Es que **cada afirmación de acá abajo fue medida, con un denominador**, y la medición está en el historial de commits para que la puedas comprobar vos mismo.

---

## Por qué este

Casi todos los datapacks te cuentan qué agregaron. Este te puede decir qué **verificó**, y cuántas cosas miró para poder decirlo.

Cada arreglo de este repositorio salió del mismo método: **contar toda la población y arreglar solo a los que les falta la protección que ya tienen los demás.** Un candado que toman 6 de 7 llamadas. Un `try-with-resources` que usan 354 de 354 conexiones. Una comprobación de nulo que hacen 198 de 198 manejadores de muerte. Cuando el caso raro es un puñado contra cientos, ese caso raro es un bug. Cuando la proporción se da vuelta —37 contra 6— no hay bug, hay una costumbre, y el barrido se tira a la basura en vez de disfrazarse de hallazgo.

Esa disciplina corta para los dos lados, y el registro también guarda los fracasos. Cuatro barridos distintos sobre lógica de quests no produjeron nada útil y se abandonaron; uno hubo que reescribirlo cuatro veces y aun así no lograba distinguir, así que se descartó. **Un cero solo significa algo cuando sabés cuántas cosas se revisaron para llegar a él.**

---

## Qué está verificado

Cada una de estas cifras es un conteo hecho leyendo los datos o el código de verdad, no una estimación.

| Área | Resultado |
|---|---|
| Archivos XML de datos que validan contra su XSD | **1.448 / 1.448** |
| Ids de NPC definidos más de una vez | **0** de 5.784 |
| Conexiones a base de datos fuera de `try-with-resources` | **0** de 354 |
| Llamadas SQL completamente parametrizadas | **468** de 479 — las otras 11 pegan constantes de compilación o identificadores, que JDBC no puede enlazar |
| Camino desde entrada remota hasta SQL sin parametrizar | **ninguno** |
| Descriptores del sistema (archivos, directorios, jars) bien acotados | **25** de 26 — el último es un socket de campo que se cierra en un `finally` |
| Manejadores de `InterruptedException` que responden a la interrupción | **16 / 16** |
| Clases que definen `equals` y `hashCode` de forma coherente | **15 / 15** |
| Tareas periódicas protegidas contra una excepción en su cuerpo | **69 / 69** |
| Comparaciones de texto hechas por referencia en vez de por valor | **0** de 5.146, contra 1.244 por valor |
| Puntos de spawn dentro de la grilla del mundo | **10.167 / 10.167** |
| Páginas de diálogo de cambio de clase presentes para NPCs de C4 | **222 / 222** |

Más **54 pruebas unitarias** repartidas en 15 clases, y un flujo de CI que compila el core, corre las pruebas **y aparte compila los 784 scripts del datapack** — porque `build.xml` solo construye `java/`, así que un cambio en el core que rompa un script pasaría igual con la compilación en verde.

---

## Qué se arregló

### Concurrencia e integridad de datos

- **Duplicación de ítems con depósitos simultáneos.** Las dos versiones de `addItem` fusionaban contra una pila existente sin tomar su candado, mientras que `destroyItem` y `transferItem` sí lo hacían. Una prueba con 8 hilos × 2.000 adiciones perdía como la mitad de los depósitos contra la versión sin candado: 8.519 de 17.000 esperados. Ahora `transferItem` toma los dos candados ordenados por id de objeto.
- **`WorldObject` comparaba por id de objeto y heredaba el hash de identidad.** Dos objetos iguales caían en cubos distintos, y el repositorio declara alrededor de cien sets y mapas que guardan objetos del mundo directamente. Cubierto con cinco pruebas, cuatro de las cuales fallan contra el código viejo.
- **Los guardados de variables descartaban sus cambios al fallar.** El `finally` limpiaba el seguimiento incluso cuando la escritura había fallado, así que los cambios pendientes nunca se reintentaban y la memoria se separaba de la base de datos en silencio. Arreglarlo exigía que el `INSERT` fuera idempotente, y eso exigía darle a `character_variables` e `item_variables` la clave primaria que `account_gsdata` ya tenía — sin ella esas tablas pueden guardar dos filas para la misma variable, y cuál lee el jugador depende del orden de las filas.
- **Un espacio de ids mal construido ya no reparte ids.** Cada una de las cinco consultas que juntan los ids de objeto en uso se tragaba su propio error, así que una consulta que no terminaba borraba en silencio los ids de una tabla entera y el servidor volvía a repartir ids que filas vivas ya tenían. Daño irreversible, y callado. Ahora la extracción avisa si quedó incompleta y el gestor se niega en vez de adivinar.

### Fugas de recursos

Diez descriptores de archivo se soltaban solo si todo salía bien, con el `close()` como última línea de un `try` cuyo `catch` apenas registraba — incluyendo un directorio que quedaba abierto una vez por copia de seguridad, el jar del propio servidor cada vez que un manifiesto no traía fecha de compilación, y un `Files.walk` abierto adentro del `try` de otro `Files.walk` y nunca cerrado.

### Fallas que no se podían ver

- **Los errores de las tareas iban a stderr, nunca al log.** El pool de hilos los contenía bien, pero ningún hilo llevaba manejador y no había ninguno global, así que toda falla de tarea del servidor caía fuera del log donde escribe todo lo demás.
- **Seis hilos ignoraban su propia interrupción**, incluido uno que no es demonio y está en un bucle cuya única otra salida era que todos los juegos de la olimpiada se declararan terminados — un solo juego colgado podía mantener abierta la JVM sin forma de pedirle que pare.

### Contenido y datos

- **Un id de NPC reclamado por dos funciones a la vez.** El `900103` estaba declarado como el cubo de teletransporte de Core y como el Race Manager. El cargador fusiona en vez de reemplazar, así que el NPC resultante era un híbrido con una de las dos funciones rota — y *cuál* de las dos no era fijo, porque los archivos de NPC se procesan en un pool de hilos. El cubo se movió a un id libre; ahora funcionan las dos.
- **Enlaces muertos reparados en vez de borrados.** Un botón "Quest" apuntando a un bypass que nadie implementa, cuando otras 72 páginas usan el que sí existe. Opciones de diálogo apuntando a una convención que ningún manejador lee, cuando la página que responde literalmente a la pregunta ya era un evento atendido. Una página de recompensa mal escrita por un solo carácter, así que un segundo clic no hacía absolutamente nada.
- **Una probabilidad de drop que no podía hacer lo que decía.** `getRandom(1000) < 20.200001` tira 21 en mil, no 20,2 — `getRandom` devuelve un entero. Una de 398 comparaciones de esa forma; las otras 397 usan enteros.
- **La artesanía cobraba de menos.** El costo de HP y MP **por pasada** de una receta dividía un entero entre un entero hacia un decimal, así que un costo de 3 repartido en 5 pasadas cobraba cero.

---

## Configuración del servidor

Los rates están puestos como **una sola curva, no como perillas sueltas.**

**La experiencia es x1** — ritmo retail, incluyendo skill points, rates de grupo, experiencia de quest y rates de mascota. Drops y spoil quedan en **x2**, drops de raid en x2, adena en **x3**, recompensas de quest en x2 con adena de quest en x3. El motivo: a x1 un personaje se queda en cada nivel unas cinco veces más tiempo, así que dejar los drops en x5 significaría cinco veces el botín a lo largo de cinco veces las muertes, y el equipo correría muy por delante del nivel, arruinando justo el farmeo que x1 existe para conservar.

Lo que ya estaba bien para x1 quedó intacto — autoloot prendido, autoloot de raids apagado, vitality retail, delevel y penalidad de muerte retail, y los monstruos campeones, que al 5% de frecuencia y con cinco veces la vida quedan casi neutros por unidad de tiempo.

### El buffer por esquemas

Hay un NPC buffer en **las quince ciudades**, ubicado tomando como referencia el gatekeeper de cada una.

- **Gratis para todos hasta nivel 30.** De ahí para arriba solo atiende cuentas premium, y a cualquier otro le dice que se busque un Prophet o un Swordsinger.
- **Sin buffs de tercera clase.** Cuáles son esos siete se calculó desde los árboles de habilidades — el tier más bajo que aprende cada buff, sacado de la cadena de padres en `PlayerClass` — y no de memoria. Elemental, Divine y Arcane Protection, Siren's Dance, y las Songs of Renewal, Meditation y Champion las aprende únicamente un Cardinal, un Eva's Saint, un Shillien Saint, un Hierophant, un Sword Muse o un Spectral Dancer. Dejarlas afuera es lo que les da a esas clases algo que el NPC no puede hacer.
- **43 buffs, cada uno en el nivel más bajo de su habilidad.**
- **Una hora de duración, aplicada de forma global** y no solo al NPC — así el Might de un Prophet de verdad nunca es la peor opción frente a la del gato.

---

## Compilar

Necesitás **JDK 25** y Apache Ant.

```bash
ant
```

La compilación arma `java/`, produce `LoginServer.jar`, `GameServer.jar` y `DatabaseInstaller.jar`, y ensambla `L2J_Mobius_C4_ScionsOfDestiny.zip` en `../build` junto con el datapack.

Para correr las pruebas como las corre el CI:

```bash
ant test
```

Después preparás la base de datos con `db_installer/DatabaseInstaller`, y arrancás `login/LoginServer` seguido de `game/GameServer`.

---

## Qué **no** está verificado

Dicho de frente, porque los números de arriba valen lo que valgan sus límites.

**El servidor nunca se arrancó.** Todo esto es compilación, validación contra XSD, datos cruzados entre sí, y pruebas unitarias. Nada se observó con un cliente conectado. Los cambios que más ganarían con una pasada en vivo son las posiciones del NPC buffer, que están puestas al lado de cada gatekeeper pero no se miraron dentro del juego, y la clave primaria agregada a `character_variables`, que en una base que ya tenga filas duplicadas necesita la migración de una sola vez que quedó escrita en ese mismo archivo de esquema.

El balance más allá de la curva de rates está sin tocar: no se rebalanceó daño de habilidades, ni tablas de drop, ni estadísticas de NPCs. Este es un servidor Chronicle 4 con números de C4.

---

## Créditos

Construido sobre [L2J Mobius](http://www.l2jmobius.org/), de Mobius y sus colaboradores. La lista de contenido de Chronicle 4 está en [`readme.txt`](readme.txt).
