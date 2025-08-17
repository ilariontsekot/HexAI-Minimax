HexorcistaID: Análisis Técnico del AI Player para Hex
#MiniMax

El núcleo del jugador HexorcistaID se basa en MiniMax, un algoritmo de búsqueda adversaria que evalúa todos los posibles movimientos hasta una profundidad determinada, asumiendo que el oponente juega de manera óptima. Cada nodo del árbol de juego representa un estado del tablero, y la función de evaluación heurística estima la utilidad del nodo para el jugador actual. El algoritmo selecciona la jugada que maximiza la ganancia mínima esperada (maximiza la seguridad ante el mejor contraataque del rival).

#AlphaBetaPruning

Para optimizar MiniMax, se implementa poda Alpha-Beta, que evita evaluar ramas del árbol que no pueden mejorar el resultado final. Esto reduce exponencialmente el número de nodos visitados y permite explorar mayor profundidad en el mismo tiempo de decisión, manteniendo la exactitud de MiniMax.

#IterativeDeepeningSearch

HexorcistaID utiliza Iterative Deepening Search (IDS), realizando búsquedas incrementales en profundidad hasta el límite de tiempo. IDS combina la ventaja de encontrar jugadas válidas rápidas con la posibilidad de refinar la decisión a medida que se profundiza. Esto garantiza que siempre se devuelva la mejor jugada encontrada hasta ese momento, incluso si el tiempo expira inesperadamente.

#ZobristHashing

Cada estado del tablero se representa mediante un hash único generado con Zobrist Hashing, permitiendo almacenar y reutilizar evaluaciones previas en una tabla de transposición. Esto evita re-calcular nodos idénticos en distintas ramas del árbol, acelerando la exploración y mejorando la eficiencia del algoritmo en tableros grandes.

#HeuristicaDijkstra

La heurística principal combina Dijkstra para estimar la distancia mínima entre los bordes que el jugador debe conectar y el concepto de “puentes” estratégicos. Cada celda se pondera según su contribución al camino más corto hacia la victoria. La evaluación global del tablero refleja no sólo la proximidad a ganar, sino también la solidez de las conexiones intermedias.

#DecisionMaking

El jugador toma decisiones basadas en:

Priorizar movimientos que reduzcan la distancia mínima hacia la victoria (Dijkstra).

Maximizar la creación de puentes defensivos y ofensivos, que bloquean al oponente y refuerzan la conectividad propia.

Evaluar las amenazas inmediatas del rival, asignando alta penalización a posiciones que puedan permitir una victoria rápida contraria.

Ajustar profundidad y exploración según tiempo disponible, gracias a IDS y poda Alpha-Beta.

#AnalisisComportamiento

Exploración eficiente: poda Alpha-Beta combinada con IDS permite balancear profundidad y cobertura del árbol.

Robustez frente a oponentes fuertes: la heurística basada en caminos mínimos y puentes prioriza jugadas estratégicamente seguras.

Capacidad de adaptación: IDS asegura que siempre se devuelve la mejor jugada posible según el tiempo, evitando movimientos arbitrarios en estados complejos.

Complejidad: O(b^d) sin poda, se reduce sustancialmente con Alpha-Beta, y la tabla de transposición evita evaluaciones repetidas, haciendo factible su uso incluso en tableros grandes.
