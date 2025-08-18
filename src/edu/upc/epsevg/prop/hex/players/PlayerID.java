/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package edu.upc.epsevg.prop.hex.players;

import edu.upc.epsevg.prop.hex.HexGameStatus;
import edu.upc.epsevg.prop.hex.IAuto;
import edu.upc.epsevg.prop.hex.IPlayer;
import edu.upc.epsevg.prop.hex.PlayerMove;
import edu.upc.epsevg.prop.hex.PlayerType;
import static edu.upc.epsevg.prop.hex.PlayerType.getColor;
import edu.upc.epsevg.prop.hex.SearchType;
import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Esta clase representa un jugador de Hex que implementa la técnica MiniMax
 * con Iterative Deepening (IDS) y sin heurística (para los estados no terminales
 * se utiliza un valor de evaluación = 0, salvo que se use {@link #evaluateHeuristica}).
 * 
 * <p>La clase también implementa una tabla de transposición (HashMap de 
 * {@link ZobristHexState} a {@link PlayerID.TTData}) para acelerar
 * el proceso de búsqueda. Durante el cálculo del movimiento, se realiza una 
 * búsqueda progresiva (iterativa) en profundidad desde 1 hasta el tope 
 * indicado por {@code maxDepthAllowed} o hasta que se alcance un tiempo límite 
 * (timeout). En caso de llegar a la señal de timeout a mitad de una iteración, 
 * se tomará la mejor jugada obtenida en la iteración anterior.</p>
 */
public class PlayerID implements IPlayer, IAuto {

    /**
     * Nombre que se mostrará en la interfaz de usuario.
     */
    private String name; 

    /**
     * Bandera para indicar cuando se ha alcanzado el timeout.
     */
    private boolean timeoutFlag;

    /**
     * Contador de nodos explorados durante la búsqueda.
     */
    private int exploredNodes;

    /**
     * Profundidad máxima de búsqueda que se está intentando en la iteración actual.
     */
    private int currentMaxDepth;

    /**
     * Última profundidad completa que se pudo explorar antes de que expirara el tiempo.
     */
    private int finalUsedDepth;

    /**
     * Profundidad máxima permitida (puede ser {@code Integer.MAX_VALUE}).
     */
    private int maxDepthAllowed;

    /**
     * Color del jugador actual. Se utiliza 1 o -1 en Hex.
     */
    private int myColor;

    /**
     * Color del oponente, opuesto al valor de {@code myColor}.
     */
    private int oppColor;

    /**
     * Mejor movimiento encontrado en las iteraciones de IDS.
     */
    private Point bestMove;

    /**
     * Tabla de transposición basada en estados Zobrist.
     */
    private HashMap<ZobristHexState, TTData> transpositionTable;

    /**
     * Constructor por defecto. Inicializa el nombre del jugador y la tabla de transposición.
     * Establece la profundidad máxima en un valor muy grande.
     */
    public PlayerID () {
        this.name = "HexorcistaID";
        this.maxDepthAllowed = Integer.MAX_VALUE; 
        this.transpositionTable = new HashMap<>();
    }

    /**
     * Método que se invoca cuando expira el tiempo de búsqueda. 
     * Establece la bandera {@code timeoutFlag} a {@code true}.
     */
    @Override
    public void timeout() {
        timeoutFlag = true;
    }

    /**
     * Calcula la jugada que debe realizar el jugador en el estado actual del juego.
     * 
     * <p>Se realiza un Iterative Deepening Search (IDS), es decir, se llama varias veces 
     * a MiniMax (con poda alpha-beta) aumentando la profundidad de uno en uno, hasta 
     * alcanzar el límite {@link #maxDepthAllowed} o hasta que se produzca el timeout. 
     * Si el timeout ocurre en medio de una profundidad dada, se toma la mejor jugada 
     * obtenida en la profundidad anterior.</p>
     * 
     * @param gs Estado actual del juego Hex.
     * @return Un objeto {@link PlayerMove} que contiene la mejor jugada encontrada, 
     *         así como estadísticas sobre la exploración (nodos explorados y profundidad usada).
     */
    @Override
    public PlayerMove move(HexGameStatus gs) {
        // Inicialización de variables para la nueva búsqueda.
        exploredNodes = 0;
        currentMaxDepth = 1;
        finalUsedDepth = 0;
        timeoutFlag = false;
        bestMove = null;

        // Asignar colores (jugador actual y oponente).
        myColor = gs.getCurrentPlayerColor(); // 1 ó -1
        oppColor = -myColor;

        // Construir un estado Zobrist a partir del estado de juego.
        ZobristHexState initialState = new ZobristHexState(gs);

        // Iterative Deepening.
        while (!timeoutFlag && currentMaxDepth <= maxDepthAllowed) {
            Point moveCandidate = runMiniMax(initialState, currentMaxDepth);
            if (!timeoutFlag && moveCandidate != null) {
                bestMove = moveCandidate;
                finalUsedDepth = currentMaxDepth;
            }
            currentMaxDepth++;
        }

        // Si no se encontró ninguna jugada (muy poco probable), se elige la primera vacía como respaldo.
        if (bestMove == null) {
            List<Point> moves = getAllMoves(gs);
            if (!moves.isEmpty()) {
                bestMove = moves.get(0);
            }
        }

        // Devolver la jugada junto con estadísticas de búsqueda.
        return new PlayerMove(bestMove, exploredNodes, finalUsedDepth, SearchType.MINIMAX);
    }

    /**
     * Ejecuta el algoritmo MiniMax con poda alpha-beta hasta la profundidad indicada.
     * 
     * @param zState Estado inicial de la búsqueda (con hash Zobrist).
     * @param depth Profundidad máxima a la cual se realizará la búsqueda en esta iteración.
     * @return El movimiento (coordenadas x,y) que se considera óptimo para el jugador MAX.
     */
    private Point runMiniMax(ZobristHexState zState, int depth) {
        double alpha = Double.NEGATIVE_INFINITY;
        double beta  = Double.POSITIVE_INFINITY;
        double bestVal = Double.NEGATIVE_INFINITY;
        Point chosenMove = null;

        List<Point> moves = getAllMoves(zState);

        // Para cada movimiento posible, se realiza un paso de MiniMax (jugador MIN a continuación).
        for (Point mv : moves) {
            if (timeoutFlag) break;  // Si hay timeout, se corta la búsqueda.

            ZobristHexState nextState = new ZobristHexState(zState);
            nextState.placeStone(mv);

            double value = minValue(nextState, depth - 1, alpha, beta);
            if (value > bestVal) {
                bestVal = value;
                chosenMove = mv;
            }
            alpha = Math.max(alpha, bestVal);
            if (alpha >= beta) {
                // Poda alpha-beta.
                break;
            }
        }
        return chosenMove;
    }

    /**
     * Función para el jugador MIN dentro de MiniMax con poda alpha-beta.
     * 
     * @param zState Estado actual con hash Zobrist.
     * @param depth Profundidad restante de la búsqueda.
     * @param alpha Límite inferior de la poda alpha-beta.
     * @param beta Límite superior de la poda alpha-beta.
     * @return El valor mínimo que el jugador MIN puede forzar desde este estado.
     */
    private double minValue(ZobristHexState zState, int depth, double alpha, double beta) {
        if (timeoutFlag) {
            return 0; // Regreso inmediato en caso de timeout.
        }
        // Comprobar si es un estado terminal.
        if (zState.isGameOver()) {
            return terminalEvaluation(zState);
        }
        // Si se llega a la profundidad límite, se usa la heurística (por defecto 0, o la definida).
        if (depth == 0) {
            exploredNodes++;
            return evaluateHeuristica(zState);
        }

        // Consultar la tabla de transposición.
        TTData entry = transpositionTable.get(zState);
        if (entry != null && entry.depth >= (currentMaxDepth - depth)) {
            // Valor almacenado que puede reutilizarse.
            return entry.value;
        }

        double value = Double.POSITIVE_INFINITY;
        List<Point> moves = getAllMoves(zState);

        for (Point mv : moves) {
            if (timeoutFlag) {
                break;
            }
            ZobristHexState aux = new ZobristHexState(zState);
            aux.placeStone(mv);
            double tmp = maxValue(aux, depth - 1, alpha, beta);
            value = Math.min(value, tmp);

            beta = Math.min(beta, value);
            if (beta <= alpha) {
                // Poda alpha-beta.
                break;
            }
        }

        // Almacenar en la tabla de transposición.
        transpositionTable.put(zState, new TTData(value, currentMaxDepth - depth));
        return value;
    }

    /**
     * Función para el jugador MAX dentro de MiniMax con poda alpha-beta.
     * 
     * @param zState Estado actual con hash Zobrist.
     * @param depth Profundidad restante de la búsqueda.
     * @param alpha Límite inferior de la poda alpha-beta.
     * @param beta Límite superior de la poda alpha-beta.
     * @return El valor máximo que el jugador MAX puede forzar desde este estado.
     */
    private double maxValue(ZobristHexState zState, int depth, double alpha, double beta) {
        if (timeoutFlag) {
            return 0;
        }
        // Comprobar si es un estado terminal.
        if (zState.isGameOver()) {
            return terminalEvaluation(zState);
        }
        // Profundidad límite.
        if (depth == 0) {
            exploredNodes++;
            return evaluateHeuristica(zState);
        }

        // Revisar la tabla de transposición.
        TTData entry = transpositionTable.get(zState);
        if (entry != null && entry.depth >= (currentMaxDepth - depth)) {
            return entry.value;
        }

        double value = Double.NEGATIVE_INFINITY;
        List<Point> moves = getAllMoves(zState);

        for (Point mv : moves) {
            if (timeoutFlag) {
                break;
            }
            ZobristHexState aux = new ZobristHexState(zState);
            aux.placeStone(mv);
            double tmp = minValue(aux, depth - 1, alpha, beta);
            value = Math.max(value, tmp);

            alpha = Math.max(alpha, value);
            if (alpha >= beta) {
                // Poda alpha-beta.
                break;
            }
        }

        // Almacenar en la tabla de transposición.
        transpositionTable.put(zState, new TTData(value, currentMaxDepth - depth));
        return value;
    }

    /**
     * Evalúa el resultado de un estado terminal:
     * <ul>
     *   <li>+∞ si el ganador es {@code myColor}.</li>
     *   <li>-∞ si el ganador es {@code oppColor}.</li>
     *   <li>0 en caso de que no haya un ganador válido (muy raro en Hex).</li>
     * </ul>
     * 
     * @param zState Estado del juego (terminal).
     * @return Un valor representativo de la utilidad en este estado.
     */
    private double terminalEvaluation(ZobristHexState zState) {
        int winner = zState.getWinnerColor();
        if (winner == myColor) {
            return Double.POSITIVE_INFINITY;
        } else if (winner == oppColor) {
            return Double.NEGATIVE_INFINITY;
        }
        return 0;
    }

    /**
     * Obtiene todos los movimientos disponibles (celdas libres) a partir de un estado Zobrist.
     * 
     * @param zState Estado Zobrist que contiene un {@link HexGameStatus} interno.
     * @return Lista de puntos disponibles para colocar la siguiente piedra.
     */
    private List<Point> getAllMoves(ZobristHexState zState) {
        HexGameStatus st = zState.getInternalStatus();
        return getAllMoves(st);
    }

    /**
     * Obtiene todos los movimientos disponibles (celdas libres) en un estado {@link HexGameStatus}.
     * 
     * @param st Estado del juego.
     * @return Lista de puntos disponibles (celdas vacías).
     */
    private List<Point> getAllMoves(HexGameStatus st) {
        List<Point> res = new ArrayList<>();
        int n = st.getSize();
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (st.getPos(i, j) == 0) {
                    res.add(new Point(i, j));
                }
            }
        }
        return res;
    }
    
    /**
     * Evalúa de manera heurística un estado de juego no terminal.
     * 
     * <p>Actualmente se emplea un posible cálculo basado en la diferencia de distancias
     * mediante Dijkstra, para aproximar la cercanía de la conexión de cada jugador.
     * 
     * @param s Estado de juego Zobrist.
     * @return Un valor entero que representa la evaluación heurística.
     */
    private int evaluateHeuristica(ZobristHexState s) {
        // Esta llamada asume que la clase HeuristicaID tiene un método runDijkstra
        // que retorna un objeto con la distancia más corta (shortestPath).
        int myDistance = HeuristicaID.runDijkstra(s, myColor).shortestPath;
        int opponentDistance = HeuristicaID.runDijkstra(s, oppColor).shortestPath;

        int connectivityScore = (opponentDistance - myDistance) * 10;
        return -myDistance + connectivityScore;
    } 

    /**
     * Devuelve el nombre del jugador que será mostrado en la interfaz.
     * 
     * @return Cadena con el nombre del jugador.
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * Clase interna para almacenar datos de la tabla de transposición,
     * incluyendo el valor calculado y la profundidad a la que se obtuvo.
     */
    private static class TTData {
        /**
         * Valor de evaluación almacenado.
         */
        double value;
        /**
         * Profundidad a la que se calculó este valor.
         */
        int depth;

        /**
         * Constructor para encapsular el valor y la profundidad.
         * 
         * @param v Valor de evaluación.
         * @param d Profundidad en la que se calculó.
         */
        TTData(double v, int d) {
            value = v;
            depth = d;
        }
    }
}