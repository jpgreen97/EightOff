package eightoff;

import DeckOfCards.CartaInglesa;

/**
 * Representa una de las 8 columnas de juego.
 * Contiene la lista de cartas y la regla de apilamiento para esa columna.
 */
public class TableauDeck {

    // Lista interna que almacena las cartas de esta columna.
    private final java.util.List<CartaInglesa> cartas = new java.util.ArrayList<>();

    /**
     * Devuelve la lista completa de cartas (usado por la UI para dibujar).
     */
    public java.util.List<CartaInglesa> getCartas() { return cartas; }

    /**
     * Añade una carta al final, sin comprobar reglas.
     * Usado para el reparto inicial y para 'Deshacer'.
     */
    public void agregarCartaForzada(CartaInglesa c) { if (c != null) cartas.add(c); }

    /**
     * Devuelve la carta de arriba (la ultima) sin quitarla.
     * Usado para dibujar y para comprobar reglas de movimiento.
     */
    public CartaInglesa getUltimaCarta() {
        if (cartas.isEmpty()) return null;
        return cartas.get(cartas.size() - 1);
    }

    /**
     * Quita y devuelve la carta de arriba (usado al mover cartas).
     */
    public CartaInglesa eliminarUltimaCarta() {
        if (cartas.isEmpty()) return null;
        return cartas.remove(cartas.size() - 1);
    }

    /**
     * Comprueba si una carta esta en esta columna (para buscar origen).
     */
    public boolean contieneCarta(CartaInglesa c) {
        return cartas.contains(c);
    }

    /**
     * Define las reglas de movimiento *hacia* esta columna.
     *
     * Regla correcta combinada:
     * 1. Si la columna esta vacia, solo acepta un Rey.
     * 2. Si no esta vacia, acepta cartas del mismo palo en orden descendente.
     */
    public boolean puedeRecibir(CartaInglesa c) {
        if (c == null) return false;

        // Obtiene la carta que esta actualmente arriba
        CartaInglesa top = getUltimaCarta();

        if (top == null) {
            // Regla 1: Columna vacia -> Solo Reyes
            return c.esRey();
        } else {
            // Regla 2: Columna con cartas -> Mismo palo y descendente
            // Ej: Si top es 10 de Picas, 'c' debe ser 9 de Picas.
            return c.getPalo() == top.getPalo() && c.getValor() + 1 == top.getValor();
        }
    }
}