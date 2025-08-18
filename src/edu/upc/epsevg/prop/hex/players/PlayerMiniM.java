package edu.upc.epsevg.prop.hex.players;

import edu.upc.epsevg.prop.hex.*;
import static edu.upc.epsevg.prop.hex.PlayerType.getColor;
import java.awt.Point;
import java.util.*;

/**
 * Estrategia de jugador automático que implementa el algoritmo MiniMax para Hex.
 * Utiliza una heurística basada en conexiones virtuales, puentes y semi-conexiones.
 */
public class PlayerMiniM implements IPlayer, IAuto {

    /**
     * Nombre del jugador.
     */
    private String name = "HexorcistaMiniMax";

    /**
     * Tipo de jugador que representa al jugador maximizado en el algoritmo MiniMax.
     */
    private PlayerType jugadorMaxim;

    /**
     * Tipo de jugador que representa al jugador minimizado en el algoritmo MiniMax.
     */
    private PlayerType jugadorMinim;

    /**
     * Profundidad máxima de búsqueda del algoritmo MiniMax.
     */
    private int profunditat;

    /**
     * Contador de nodos explorados durante la búsqueda.
     */
    private int nodesExplorats = 0;

    /**
     * Constructor que inicializa el jugador con una profundidad específica.
     *
     * @param profunditat La profundidad máxima de búsqueda.
     * @throws RuntimeException Si la profundidad es menor que 1.
     */
    public PlayerMiniM(int profunditat) {
        if (profunditat < 1) throw new RuntimeException("La profundidad debe ser >= 1.");
        this.profunditat = profunditat;
    }

    /**
     * Método llamado cuando se produce un timeout. Este jugador no realiza ninguna acción en caso de timeout.
     */
    @Override
    public void timeout() {
        // No hacemos nada! Soy muy rápido y nunca hago timeout ;)
    }

    /**
     * Determina el siguiente movimiento del jugador utilizando el algoritmo MiniMax.
     *
     * @param s El estado actual del juego.
     * @return El movimiento elegido por el jugador.
     */
    @Override
    public PlayerMove move(HexGameStatus s) {
        nodesExplorats = 0;
        jugadorMaxim = s.getCurrentPlayer();
        jugadorMinim = PlayerType.opposite(jugadorMaxim);

        Point millorMoviment = miniMax(s);
        return new PlayerMove(millorMoviment, nodesExplorats, profunditat, SearchType.MINIMAX);
    }

    /**
     * Implementa el algoritmo MiniMax para determinar el mejor movimiento.
     *
     * @param s El estado actual del juego.
     * @return El mejor movimiento encontrado.
     */
    private Point miniMax(HexGameStatus s) {
        double heuristicaActual = -30000;
        double alpha = Double.NEGATIVE_INFINITY;
        double beta = Double.POSITIVE_INFINITY;

        List<Point> moviments = obtenirMoviments(s);
        Point millorMoviment = null;

        for (Point moviment : moviments) {
            HexGameStatus aux = new HexGameStatus(s);
            aux.placeStone(moviment);
            nodesExplorats++;

            double valorHeuristic = minValor(aux, profunditat - 1, alpha, beta);
            if (valorHeuristic > heuristicaActual) {
                heuristicaActual = valorHeuristic;
                millorMoviment = moviment;
            }

            alpha = Math.max(alpha, heuristicaActual);
        }

        return millorMoviment;
    }

    /**
     * Calcula el valor mínimo en el algoritmo MiniMax.
     *
     * @param s     El estado actual del juego.
     * @param depth La profundidad restante de búsqueda.
     * @param alpha El valor alpha para la poda alfa-beta.
     * @param beta  El valor beta para la poda alfa-beta.
     * @return El valor heurístico mínimo encontrado.
     */
    private double minValor(HexGameStatus s, int depth, double alpha, double beta) {
        double valorHeuristic = 10000;
        if (s.isGameOver()) {
            return s.GetWinner() == jugadorMaxim ? 10000 : -10000;
        }
        if (depth == 0) return evaluateHeuristica(s);

        List<Point> moviments = obtenirMoviments(s);
        for (Point moviment : moviments) {
            HexGameStatus aux = new HexGameStatus(s);
            aux.placeStone(moviment);
            nodesExplorats++;

            double heuristicaActual = maxValor(aux, depth - 1, alpha, beta);
            valorHeuristic = Math.min(valorHeuristic, heuristicaActual);
            beta = Math.min(beta, valorHeuristic);

            if (alpha >= beta) break;
        }

        return valorHeuristic;
    }

    /**
     * Calcula el valor máximo en el algoritmo MiniMax.
     *
     * @param s     El estado actual del juego.
     * @param depth La profundidad restante de búsqueda.
     * @param alpha El valor alpha para la poda alfa-beta.
     * @param beta  El valor beta para la poda alfa-beta.
     * @return El valor heurístico máximo encontrado.
     */
    private double maxValor(HexGameStatus s, int depth, double alpha, double beta) {
        double valorHeuristic = -10000;
        if (s.isGameOver()) {
            return s.GetWinner() == jugadorMinim ? -10000 : 10000;
        }
        if (depth == 0) return evaluateHeuristica(s);

        List<Point> moviments = obtenirMoviments(s);
        for (Point moviment : moviments) {
            HexGameStatus aux = new HexGameStatus(s);
            aux.placeStone(moviment);
            nodesExplorats++;

            double heuristicaActual = minValor(aux, depth - 1, alpha, beta);
            valorHeuristic = Math.max(valorHeuristic, heuristicaActual);
            alpha = Math.max(alpha, valorHeuristic);

            if (alpha >= beta) break;
        }

        return valorHeuristic;
    }

    /**
     * Obtiene la lista de movimientos posibles en el estado actual del juego.
     *
     * @param s El estado actual del juego.
     * @return Una lista de puntos que representan los movimientos posibles.
     */
    private List<Point> obtenirMoviments(HexGameStatus s) {
        List<Point> moviments = new ArrayList<>();
        for (MoveNode node : s.getMoves()) {
            moviments.add(node.getPoint());
        }
        return moviments;
    }

    /**
     * Evalúa la heurística del estado actual del juego.
     *
     * @param s El estado actual del juego.
     * @return El valor heurístico calculado.
     */
    private int evaluateHeuristica(HexGameStatus s) {
        int myDistance = Heuristica.runDijkstra(s, getColor(jugadorMaxim)).shortestPath;
        int opponentDistance = Heuristica.runDijkstra(s, getColor(jugadorMinim)).shortestPath;

        int connectivityScore = (opponentDistance - myDistance) * 10;
        return -myDistance + connectivityScore;
    }

    /**
     * Obtiene el nombre del jugador.
     *
     * @return El nombre del jugador.
     */
    @Override
    public String getName() {
        return name;
    }

}