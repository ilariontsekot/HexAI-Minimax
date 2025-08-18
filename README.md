# HexorcistaID: AI Player para Hex con Minimax y Poda Alpha-Beta

**HexorcistaID** es un motor de inteligencia artificial para el juego de mesa **Hex**, desarrollado en Java y listo para compilar en NetBeans.  
Implementa estrategias clásicas de IA como **MiniMax**, **Alpha-Beta Pruning**, **Iterative Deepening Search** y **Zobrist Hashing** para ofrecer un agente competitivo.  

---

## 2. Poda Alpha-Beta  
Para optimizar el cálculo, se aplica **Alpha-Beta Pruning**, reduciendo ramas que no pueden mejorar el resultado esperado.  
Esto disminuye de forma significativa el número de nodos evaluados, permitiendo mayor profundidad de búsqueda en el mismo tiempo.  

Ejemplo simplificado en pseudocódigo:  

```python
def minimax(node, depth, alpha, beta, maximizing):
    if depth == 0 or node.is_terminal():
        return evaluate(node)

    if maximizing:
        value = -float("inf")
        for child in node.children():
            value = max(value, minimax(child, depth-1, alpha, beta, False))
            alpha = max(alpha, value)
            if alpha >= beta:
                break  # poda
        return value
    else:
        value = float("inf")
        for child in node.children():
            value = min(value, minimax(child, depth-1, alpha, beta, True))
            beta = min(beta, value)
            if beta <= alpha:
                break  # poda
        return value
```

<p align="center"> <img src="funcionamiento.png" alt="Funcionamiento del motor" /> </p>
---

## 3. Búsqueda con Profundización Iterativa (IDS)  
El motor emplea **Iterative Deepening Search (IDS)**: explora primero profundidades pequeñas y aumenta progresivamente hasta agotar el tiempo disponible.  
Esto asegura:  

- Una jugada válida rápida en situaciones críticas.  
- Mayor precisión si el tiempo de cálculo lo permite.  

---

## 4. Tablas de Transposición con Zobrist Hashing  
Para evitar recalcular posiciones repetidas, cada estado del tablero se representa con un **hash único** mediante **Zobrist Hashing**.  
Esto permite almacenar evaluaciones intermedias en una **tabla de transposición**, reduciendo redundancia y acelerando la búsqueda en tableros grandes.  

---

## 5. Heurística: Dijkstra y Puentes Estratégicos  
La evaluación del tablero combina dos ideas principales:  

- **Dijkstra**: estima la distancia mínima entre los bordes que el jugador debe conectar.  
- **Puentes**: refuerzan la conectividad propia y bloquean caminos del oponente.  

Función heurística aproximada:  

$$
h(s) = d_{oponente}(s) - d_{jugador}(s)
$$


- Un valor **negativo** indica ventaja para el jugador.  
- Un valor **positivo** indica ventaja rival.  

---

## 6. Estrategia de Decisión  
El agente selecciona movimientos priorizando:  

1. Reducir su propia distancia a la victoria.  
2. Reforzar la conectividad mediante **puentes**.  
3. Bloquear caminos críticos del rival.  
4. Ajustar la profundidad de cálculo según el **tiempo disponible**.  

---

## 7. Complejidad y Eficiencia  
- **MiniMax sin poda**:  

$$
O(b^d), \quad \text{con } b = \text{factor de ramificación}, \; d = \text{profundidad}
$$

- **Con Alpha-Beta**:  

$$
O(b^{d/2})
$$

- **Con tablas de transposición**: se reducen aún más los cálculos redundantes, mejorando la eficiencia global.  

