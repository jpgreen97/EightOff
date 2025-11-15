package eightoff;

import DeckOfCards.CartaInglesa;
import DeckOfCards.Palo;
import java.util.ArrayList;
import java.util.List;

/**
 * FoundationDeck (Mazo de Fundacion)
 * Representa una de las 4 pilas de destino (donde se apilan las cartas
 * del As al Rey para ganar).
 * Esta es una clase de 'Modelo' (logica pura).
 */
public class FoundationDeck {

    // El palo (ej. CORAZON) que esta fundacion esta apilando.
    // Se determina cuando se anade el primer As.
    private Palo palo;

    // Lista de cartas actualmente en esta fundacion.
    private final List<CartaInglesa> cartas = new ArrayList<>();

    /**
     * Constructor. Inicializa la fundacion.
     * @param paloInicial El palo que se espera (aunque se define con el primer As).
     */
    public FoundationDeck(Palo paloInicial) {
        this.palo = paloInicial;
    }

    // Getters basicos
    public Palo getPalo() { return palo; }
    public List<CartaInglesa> getCartas() { return cartas; }

    /**
     * Comprueba si la fundacion esta llena (13 cartas, As a Rey).
     */
    public boolean estaCompleta() { return cartas.size() == 13; }

    /**
     * Define las reglas para aceptar una carta.
     * @param c La carta que se intenta anadir.
     * @return true si el movimiento es legal, false si no.
     */
    public boolean puedeRecibir(CartaInglesa c) {
        if (c == null) return false;

        if (cartas.isEmpty()) {
            // Regla 1: Si esta vacia, solo acepta un As (valor 1).
            return c.getValor() == 1;
        } else {
            // Regla 2: Si tiene cartas, debe ser el mismo palo Y el valor siguiente.
            CartaInglesa top = cartas.get(cartas.size() - 1); // La carta de arriba
            // Ej: Si top es 7 de Picas, c debe ser 8 de Picas.
            return c.getPalo() == palo && c.getValor() == top.getValor() + 1;
        }
    }

    /** * Anade la carta a la pila si 'puedeRecibir' es verdadero.
     */
    public void agregarCarta(CartaInglesa c) {
        if (c == null) return;

        // Caso especial: La primera carta (un As) define el palo de la fundacion.
        if (cartas.isEmpty() && c.getValor() == 1) {
            palo = c.getPalo(); // Fija el palo (ej. PICAS)
            cartas.add(c);
        } else if (puedeRecibir(c)) {
            // Anade cartas subsecuentes (2, 3, 4...)
            cartas.add(c);
        }
    }

    /**
     * Devuelve la carta de arriba sin quitarla (para dibujarla).
     */
    public CartaInglesa getUltimaCarta() {
        return cartas.isEmpty() ? null : cartas.get(cartas.size() - 1);
    }

    /**
     * Quita y devuelve la carta de arriba (usado por 'Deshacer').
     */
    public CartaInglesa eliminarUltimaCarta() {
        return cartas.isEmpty() ? null : cartas.remove(cartas.size() - 1);
    }
}