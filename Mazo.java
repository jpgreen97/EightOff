package DeckOfCards;

import java.util.*;

/**
 * Mazo (Baraja)
 * Esta clase representa la baraja estandar de 52 cartas.
 * Es responsable de crear las 52 cartas, barajarlas y repartirlas.
 * Utiliza la 'ListaDobleCircular'
 */
public class Mazo {

    // La estructura de datos personalizada que almacena las 52 cartas.
    private ListaDobleCircular<CartaInglesa> cartas;

    /**
     * Constructor.
     * Crea una nueva baraja, la llena con las 52 cartas estandar
     * (13 valores * 4 palos) y la baraja automaticamente.
     */
    public Mazo() {
        cartas = new ListaDobleCircular<>();

        // Bucle anidado para crear las 52 cartas
        for (Palo p : Palo.values()) { // Itera sobre los 4 palos
            for (int valor = 1; valor <= 13; valor++) { // Itera sobre los 13 valores
                cartas.insertar(new CartaInglesa(valor, p)); // Crea y anade la carta
            }
        }

        cartas.mezclar(); // Llama al metodo de barajar de la ListaDobleCircular
    }

    /**
     * Saca y devuelve la carta de "arriba" del mazo.
     * Delega a 'eliminarInicio' de la lista.
     * @return La carta sacada.
     */
    public CartaInglesa sacarCarta() {
        return cartas.eliminarInicio();
    }

    /**
     * Comprueba si el mazo ya no tiene cartas.
     * @return true si esta vacio.
     */
    public boolean estaVacio() {
        return cartas.estaVacia();
    }

    /**
     * Devuelve cuantas cartas quedan en el mazo.
     * @return El numero de cartas.
     */
    public int getTamanio() {
        return cartas.tamanio();
    }

    /**
     * Devuelve la referencia a la lista interna.
     */
    public ListaDobleCircular<CartaInglesa> getCartas() {
        return cartas;
    }
}