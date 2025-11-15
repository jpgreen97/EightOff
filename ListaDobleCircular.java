package DeckOfCards;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Implementacion de una Lista Doblemente Circular generica.
 * Se usa para almacenar las cartas en el 'Mazo'.
 * @param <T> El tipo de dato a almacenar (en este caso, CartaInglesa).
 */
public class ListaDobleCircular<T> implements Iterable<T>{

    // 'cabeza' es el punto de entrada a la lista. Es el "primer" nodo.
    private NodoDoble<T> cabeza;

    /**
     * Clase interna que representa un nodo (un eslabon) de la lista.
     * Cada nodo almacena un dato y referencias a su nodo 'siguiente' y 'anterior'.
     */
    private static class NodoDoble<T> {
        T dato; // El dato almacenado (la carta)
        NodoDoble<T> siguiente; // Referencia al proximo nodo
        NodoDoble<T> anterior; // Referencia al nodo previo

        NodoDoble(T dato) {
            this.dato = dato;
        }
    }

    /**
     * Comprueba si la lista no tiene nodos.
     * @return true si cabeza es null.
     */
    public boolean estaVacia() {
        return cabeza == null;
    }

    /**
     * Inserta un nuevo dato al final de la lista (justo antes de la cabeza).
     * Mantiene la estructura circular.
     */
    public void insertar(T dato) {
        NodoDoble<T> nuevo = new NodoDoble<>(dato);
        if (cabeza == null) {
            // Si es el primer nodo, se apunta a si mismo
            cabeza = nuevo;
            cabeza.siguiente = cabeza;
            cabeza.anterior = cabeza;
        } else {
            //
            // Si ya hay nodos, se inserta entre el ultimo y la cabeza
            NodoDoble<T> ultimo = cabeza.anterior; // El ultimo es el anterior a la cabeza

            ultimo.siguiente = nuevo; // El (ex)ultimo apunta al nuevo
            nuevo.anterior = ultimo;    // El nuevo apunta al (ex)ultimo
            nuevo.siguiente = cabeza;   // El nuevo apunta a la cabeza
            cabeza.anterior = nuevo;    // La cabeza apunta al nuevo como su anterior
        }
    }

    /**
     * Elimina y devuelve el primer elemento (la cabeza) de la lista.
     * Usado por Mazo.sacarCarta().
     * @return El dato del nodo eliminado, o null si esta vacia.
     */
    public T eliminarInicio() {
        if (cabeza == null) return null; // No hay nada que eliminar

        T valor = cabeza.dato; // Guarda el dato para devolverlo

        if (cabeza.siguiente == cabeza) {
            // Caso 1: Solo hay un nodo en la lista
            cabeza = null;
        } else {
            //
            // Caso 2: Hay multiples nodos
            NodoDoble<T> ultimo = cabeza.anterior;
            NodoDoble<T> nuevaCabeza = cabeza.siguiente;

            // Se saltan la cabeza original
            ultimo.siguiente = nuevaCabeza;
            nuevaCabeza.anterior = ultimo;

            cabeza = nuevaCabeza; // Se actualiza la referencia a la cabeza
        }
        return valor;
    }

    /**
     * Recorre la lista y aplica una funcion (Consumer) a cada elemento.
     * Usado por 'mezclar' para copiar los elementos a una lista temporal.
     */
    public void recorrer(java.util.function.Consumer<T> accion) {
        if (cabeza == null) return;

        NodoDoble<T> actual = cabeza;
        do {
            accion.accept(actual.dato); // Aplica la funcion
            actual = actual.siguiente;
        } while (actual != cabeza); // Se detiene cuando da la vuelta completa
    }

    /**
     * Mezcla (baraja) los elementos de la lista.
     */
    public void mezclar() {
        // 1. Copia todos los elementos a un ArrayList temporal.
        java.util.List<T> listaTemporal = new java.util.ArrayList<>();
        recorrer(listaTemporal::add); // '::add' es una referencia al metodo 'add' de la lista

        // 2. Baraja el ArrayList usando el metodo eficiente de Java.
        java.util.Collections.shuffle(listaTemporal);

        // 3. Vacia la lista circular y la vuelve a llenar con los elementos barajados.
        cabeza = null;
        for (T elemento : listaTemporal) {
            insertar(elemento);
        }
    }

    /**
     * Devuelve el numero de elementos en la lista.
     * Lo hace contando (recorriendo) la lista.
     */
    public int tamanio() {
        if (cabeza == null) return 0;
        int count = 0;
        NodoDoble<T> actual = cabeza;
        do {
            count++;
            actual = actual.siguiente;
        } while (actual != cabeza);
        return count;
    }

    /**
     * Proporciona un Iterador estandar de Java.
     * Esto permite usar la lista en bucles 'for-each'.
     * (ej: for (CartaInglesa c : miListaCircular) { ... })
     */
    @Override
    public Iterator<T> iterator() {
        return new Iterator<>() {
            NodoDoble<T> actual = cabeza;
            boolean primerPaso = true; // Control para listas de un solo nodo

            @Override
            public boolean hasNext() {
                // Hay proximo si la lista no esta vacia Y (es la primera vez o aun no damos la vuelta)
                return cabeza != null && (primerPaso || actual != cabeza);
            }

            @Override
            public T next() {
                if (cabeza == null) throw new NoSuchElementException();
                T dato = actual.dato;
                actual = actual.siguiente;
                primerPaso = false;
                return dato;
            }
        };
    }
}