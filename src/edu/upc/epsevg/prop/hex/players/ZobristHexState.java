// ------------------------------------------------------------------------
// Segunda clase: ZobristHexState
// ------------------------------------------------------------------------
package edu.upc.epsevg.prop.hex.players;

import edu.upc.epsevg.prop.hex.HexGameStatus;
import java.awt.Point;
import java.util.Random;

/**
 * Clase que encapsula un estado de un juego de Hex junto con un valor hash 
 * calculado mediante Zobrist Hashing. Esto permite un acceso y comparación 
 * muy rápidos en la tabla de transposición.
 * 
 * <p>El enfoque Zobrist consiste en asignar un valor aleatorio a cada posición 
 * del tablero según su ocupante (vacío, color +1 o color -1), y luego mezclar 
 * todos esos valores mediante XOR para formar un hash único. Adicionalmente, 
 * se incluyen valores aleatorios diferentes para indicar si el turno es del 
 * jugador +1 o -1.</p>
 */
public class ZobristHexState {

    /**
     * Tabla Zobrist que asocia a cada celda (x,y) y a un ocupante (0, 1, 2)
     * un número aleatorio. Esto se usa para mezclar con XOR y formar el hash.
     */
    private static int[][][] zobrist;

    /**
     * Valor aleatorio que se mezcla en el hash si el jugador actual es +1.
     */
    private static int zobristPlayer1;

    /**
     * Valor aleatorio que se mezcla en el hash si el jugador actual es -1.
     */
    private static int zobristPlayer2;

    /**
     * Valor de hash actual para este estado.
     */
    private int myHash;

    /**
     * Indica si el valor de hash está actualizado o debe recalcularse.
     */
    private boolean hashValid;

    /**
     * Referencia interna al estado real del juego (tablero, turnos, etc.).
     */
    private HexGameStatus internalStatus;

    /**
     * Construye un estado Zobrist a partir de un {@link HexGameStatus}.
     * 
     * @param gs Estado de juego original, que será clonado internamente.
     */
    public ZobristHexState(HexGameStatus gs) {
        int size = gs.getSize();
        initZobrist(size);

        // Se clona el estado interno para mantener la inmutabilidad externa.
        this.internalStatus = new HexGameStatus(gs);
        this.hashValid = false;
    }

    /**
     * Constructor de copia. Crea un nuevo estado basado en otro {@code ZobristHexState}.
     * 
     * @param other Estado del cual se copiará.
     */
    public ZobristHexState(ZobristHexState other) {
        this.internalStatus = new HexGameStatus(other.internalStatus);
        this.myHash = other.myHash;
        this.hashValid = other.hashValid;
    }

    /**
     * Inicializa los valores Zobrist si todavía no se han generado para 
     * un tablero de tamaño {@code n} x {@code n}.
     * 
     * @param n Tamaño del tablero.
     */
    private static void initZobrist(int n) {
        if (zobrist != null && zobrist.length == n) {
            // Si ya estaba inicializado para este tamaño, no se hace nada.
            return;
        }
        zobrist = new int[n][n][3];
        Random rnd = new Random();

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                for (int k = 0; k < 3; k++) {
                    zobrist[i][j][k] = rnd.nextInt();
                }
            }
        }
        // Valores especiales para indicar turno de +1 o -1.
        zobristPlayer1 = rnd.nextInt();
        zobristPlayer2 = rnd.nextInt();
    }

    /**
     * Devuelve el hashCode de este estado (se recalcula si no está marcado como válido).
     * 
     * @return Un valor entero que representa el hash Zobrist para este estado.
     */
    @Override
    public int hashCode() {
        if (!hashValid) {
            computeHash();
        }
        return myHash;
    }

    /**
     * Determina si dos estados son iguales comparando sus valores de hash.
     * 
     * <p>En teoría puede haber colisiones, pero en la práctica son muy poco probables.</p>
     * 
     * @param o Objeto a comparar.
     * @return {@code true} si el hash es igual; {@code false} en caso contrario.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        if (!(o instanceof ZobristHexState)) return false;
        ZobristHexState z = (ZobristHexState) o;
        return this.hashCode() == z.hashCode();
    }

    /**
     * Recalcula el hash mezclando (mediante XOR) los valores aleatorios asociados 
     * a la ocupación de cada celda y el turno del jugador actual.
     */
    private void computeHash() {
        int size = internalStatus.getSize();
        int tmpHash = 0;

        // Mezclar el jugador actual (1 o -1).
        if (internalStatus.getCurrentPlayerColor() == 1) {
            tmpHash ^= zobristPlayer1;
        } else {
            tmpHash ^= zobristPlayer2;
        }

        // Mezclar cada celda según su ocupante: 0 = vacío, 1 = +1, 2 = -1.
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                int occupant = internalStatus.getPos(i, j); // 0, +1 o -1
                int index = (occupant == 1) ? 1 : (occupant == -1) ? 2 : 0;
                tmpHash ^= zobrist[i][j][index];
            }
        }
        myHash = tmpHash;
        hashValid = true;
    }

    /**
     * Realiza un movimiento en la posición indicada, colocando la piedra 
     * del jugador actual, y fuerza el recálculo futuro del hash.
     * 
     * @param p Coordenada donde colocar la piedra.
     */
    public void placeStone(Point p) {
        internalStatus.placeStone(p);
        hashValid = false; // Se invalida el hash para recalcularlo en la siguiente ocasión.
    }

    /**
     * Indica si la partida ha finalizado.
     * 
     * @return {@code true} si el estado representa un juego terminado; de lo contrario, {@code false}.
     */
    public boolean isGameOver() {
        return internalStatus.isGameOver();
    }

    /**
     * Obtiene el color del ganador en caso de que el juego haya terminado.
     * 
     * @return +1 si ganó el primer jugador, -1 si ganó el segundo, 
     *         0 si no hay ganador (raro en Hex).
     */
    public int getWinnerColor() {
        if (!internalStatus.isGameOver()) {
            return 0;
        }
        return internalStatus.getCurrentPlayerColor();
    }

    /**
     * Devuelve el estado interno de {@link HexGameStatus}, útil para obtener 
     * información adicional como el tamaño del tablero o generar movimientos.
     * 
     * @return El objeto {@link HexGameStatus} interno.
     */
    public HexGameStatus getInternalStatus() {
        return internalStatus;
    }

    /**
     * Devuelve el tamaño del tablero.
     * 
     * @return Un entero con el número de filas y columnas.
     */
    public int getSize() {
        return internalStatus.getSize();
    }

    /**
     * Devuelve el ocupante de la celda en la posición (x, y).
     * 
     * @param x Fila de la celda.
     * @param y Columna de la celda.
     * @return 1 si la celda está ocupada por el jugador +1, 
     *        -1 si la ocupa el jugador -1, 
     *         0 si está vacía.
     */
    public int getPos(int x, int y) {
        return internalStatus.getPos(x, y);
    }
}