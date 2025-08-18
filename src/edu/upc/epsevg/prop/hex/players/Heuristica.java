package edu.upc.epsevg.prop.hex.players;

import edu.upc.epsevg.prop.hex.HexGameStatus;
import java.awt.Point;
import java.util.*;

/**
 * Clase que proporciona métodos para calcular heurísticas en el juego Hex utilizando el algoritmo de Dijkstra.
 */
public class Heuristica {

    /**
     * Clase que almacena información sobre el camino más corto.
     */
    public static class PathInfo {
        /**
         * Longitud del camino más corto encontrado.
         */
        public int shortestPath;

        /**
         * Constructor por defecto que inicializa el camino más corto con un valor máximo.
         */
        public PathInfo() {
            this.shortestPath = Integer.MAX_VALUE;
        }

        @Override
        public String toString() {
            return "PathInfo{" +
                   "shortestPath=" + shortestPath +
                   '}';
        }
    }

    /**
     * Clase interna que representa un nodo en el camino con su posición y distancia.
     */
    private static class NodePath implements Comparable<NodePath> {
        /**
         * Posición del nodo en el tablero.
         */
        Point position;

        /**
         * Distancia acumulada hasta este nodo.
         */
        int distance; // costo

        /**
         * Constructor que inicializa la posición y la distancia del nodo.
         *
         * @param position La posición del nodo.
         * @param distance La distancia acumulada.
         */
        NodePath(Point position, int distance) {
            this.position = position;
            this.distance = distance;
        }

        @Override
        public int compareTo(NodePath other) {
            return Integer.compare(this.distance, other.distance);
        }
    }

    /**
     * Ejecuta el algoritmo de Dijkstra para calcular el camino más corto para un jugador específico.
     *
     * @param state  El estado actual del juego.
     * @param player El jugador para el cual se calcula el camino.
     * @return Un objeto PathInfo que contiene la longitud del camino más corto.
     */
    public static PathInfo runDijkstra(HexGameStatus state, int player) {
        int size = state.getSize();
        int[][] dist = new int[size][size];
        for (int[] row : dist) {
            Arrays.fill(row, Integer.MAX_VALUE);
        }

        PriorityQueue<NodePath> queue = new PriorityQueue<>();

        // Inicialización de puntos de partida
        if (player == 1) {
            for (int y = 0; y < size; y++) {
                int cellValue = state.getPos(0, y);
                if (cellValue == player || cellValue == 0) {
                    int startCost = (cellValue == player) ? 0 : 1;
                    dist[0][y] = startCost;
                    queue.add(new NodePath(new Point(0, y), startCost));
                }
            }
        } else {
            for (int x = 0; x < size; x++) {
                int cellValue = state.getPos(x, 0);
                if (cellValue == player || cellValue == 0) {
                    int startCost = (cellValue == player) ? 0 : 1;
                    dist[x][0] = startCost;
                    queue.add(new NodePath(new Point(x, 0), startCost));
                }
            }
        }

        int shortestPath = Integer.MAX_VALUE;

        // Movimientos en hexágono representado en una cuadrícula
        int[][] deltas = { {-1,0}, {1,0}, {0,-1}, {0,1}, {-1,1}, {1,-1} };

        while (!queue.isEmpty()) {
            NodePath current = queue.poll();
            Point pos = current.position;

            // Si hemos alcanzado el objetivo
            if (isGoal(pos, player, size) && current.distance < shortestPath) {
                shortestPath = current.distance;
                break; // Heurística: la primera vez que alcanzamos el objetivo es la distancia mínima
            }

            // Visitar vecinos
            for (int[] d : deltas) {
                int nx = pos.x + d[0];
                int ny = pos.y + d[1];

                if (nx < 0 || ny < 0 || nx >= size || ny >= size) continue;

                int cellValue = state.getPos(nx, ny);
                // Podemos pasar por celdas del jugador o vacías
                if (cellValue == player || cellValue == 0) {
                    // Coste adicional sólo si la celda es vacía
                    int cost = current.distance + ((cellValue == player) ? 0 : 1);
                    if (cost < dist[nx][ny]) {
                        dist[nx][ny] = cost;
                        queue.add(new NodePath(new Point(nx, ny), cost));
                    }
                }
            }

            // Agregar puentes (bridges)
            Puentes(state, player, size, dist, queue, current);
        }

        PathInfo info = new PathInfo();
        info.shortestPath = shortestPath;

        // Ajuste de shortestPath si está a punto de ganar
        if (info.shortestPath <= 1) {
            info.shortestPath = 0;
        }

        return info;
    }

    /**
     * Agrega puentes al cálculo del camino más corto.
     *
     * @param state    El estado actual del juego.
     * @param player   El jugador para el cual se calcula el puente.
     * @param size     El tamaño del tablero.
     * @param dist     Matriz de distancias.
     * @param queue    Cola de prioridad para el algoritmo de Dijkstra.
     * @param current  Nodo actual en el recorrido.
     */
    private static void Puentes(HexGameStatus state, int player, int size, int[][] dist, PriorityQueue<NodePath> queue, NodePath current) {
        Point pos = current.position;
        // Definir los posibles puentes
        int[][] bridgeOffsets = { {-2, 1}, {2, -1}, {-1, -2}, {1, 2}, {-2, -1}, {2, 1} };

        for (int[] offset : bridgeOffsets) {
            int bx = pos.x + offset[0];
            int by = pos.y + offset[1];

            if (bx < 0 || by < 0 || bx >= size || by >= size) continue;

            if (state.getPos(bx, by) == 0) { // Si el puente está en una celda vacía
                List<Point> intermediates = Intermedio(pos, new Point(bx, by), size);
                boolean validBridge = true;

                for (Point p : intermediates) {
                    if (p.x < 0 || p.y < 0 || p.x >= size || p.y >= size || state.getPos(p.x, p.y) != 0) {
                        validBridge = false;
                        break;
                    }
                }

                if (validBridge) {
                    int cost = current.distance + 1; // Los puentes tienen un coste menor
                    if (cost < dist[bx][by]) {
                        dist[bx][by] = cost;
                        queue.add(new NodePath(new Point(bx, by), cost));
                    }
                }
            }
        }
    }

    /**
     * Calcula los puntos intermedios entre dos puntos para formar un puente.
     *
     * @param start   Punto de inicio.
     * @param bridge  Punto final del puente.
     * @param size    Tamaño del tablero.
     * @return Una lista de puntos intermedios.
     */
    private static List<Point> Intermedio(Point start, Point bridge, int size) {
        List<Point> intermediates = new ArrayList<>();
        // Lógica para calcular los puntos intermedios entre start y bridge
        int dx = (bridge.x - start.x) / 2;
        int dy = (bridge.y - start.y) / 2;
        intermediates.add(new Point(start.x + dx, start.y));
        intermediates.add(new Point(start.x, start.y + dy));
        return intermediates;
    }

    /**
     * Verifica si un punto alcanza el objetivo para el jugador.
     *
     * @param pos    Punto a verificar.
     * @param player Jugador para el cual se verifica el objetivo.
     * @param size   Tamaño del tablero.
     * @return {@code true} si el punto alcanza el objetivo, {@code false} en caso contrario.
     */
    private static boolean isGoal(Point pos, int player, int size) {
        if (player == 1) {
            return pos.x == size - 1;
        } else {
            return pos.y == size - 1;
        }
    }
}