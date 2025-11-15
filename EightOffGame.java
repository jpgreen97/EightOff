package eightoff;

import DeckOfCards.CartaInglesa;
import DeckOfCards.Mazo;
import DeckOfCards.Palo;

import java.util.*;

/**
 * EightOffGame (Logica del Juego)
 * Esta es la clase principal del 'Modelo' (la logica).
 * Contiene todas las reglas, el estado del tablero y los metodos
 * para interactuar con el juego (mover, deshacer, pista).
 */
public class EightOffGame {

    // Estado del Juego
    private List<TableauDeck> columnas;       // Las 8 columnas de juego
    private List<CartaInglesa> celdasLibres;  // Las 8 celdas libres
    private List<FoundationDeck> fundaciones; // Las 4 pilas de fundacion
    private Mazo mazo;                        // El mazo para repartir
    private Stack<Movimiento> historial;      // Pila para el 'Undo' (deshacer)
    private boolean juegoTerminado;

    /**
     * Constructor. Llama a iniciarJuego() para preparar el tablero.
     */
    public EightOffGame() {
        iniciarJuego();
    }

    /**
     * Prepara un nuevo juego.
     * Crea las listas, el mazo, y reparte las cartas.
     */
    public void iniciarJuego() {
        mazo = new Mazo(); // Mazo se baraja solo al crearse
        columnas = new ArrayList<>(8);
        celdasLibres = new ArrayList<>(8); // 8 celdas
        for (int i = 0; i < 8; i++) {
            celdasLibres.add(null); // Llena con 8 espacios vacios
        }
        fundaciones = new ArrayList<>(4);
        historial = new Stack<>();
        juegoTerminado = false;

        // Crea 4 fundaciones (una por palo)
        for (Palo p : Palo.values()) {
            fundaciones.add(new FoundationDeck(p));
        }

        // Crea 8 columnas vacias
        for (int i = 0; i < 8; i++) {
            columnas.add(new TableauDeck());
        }

        // Reparto de Cartas (Reglas Eight Off)

        // 48 cartas -> 8 columnas, 6 por columna
        for (int ronda = 0; ronda < 6; ronda++) {
            for (int col = 0; col < 8; col++) {
                CartaInglesa c = mazo.sacarCarta();
                if (c != null) makeUp(c); // Pone la carta boca arriba
                columnas.get(col).agregarCartaForzada(c);
            }
        }

        // 4 cartas restantes -> primeras 4 celdas libres
        for (int i = 0; i < 4; i++) {
            CartaInglesa c = mazo.sacarCarta();
            if (c != null) makeUp(c);
            celdasLibres.set(i, c);
        }
    }

    /**
     * Metodo ayudante para poner una carta boca arriba (si existe el metodo).
     */
    private static void makeUp(CartaInglesa c) {
        try { c.makeFaceUp(); } catch (Throwable ignored) { /* Ignora si no existe */ }
    }

    /**
     * Intenta mover una carta (desde celda o columna) a una fundacion.
     * @return true si el movimiento fue exitoso.
     */
    public boolean moverAFundacion(CartaInglesa carta) {
        TableauDeck origen = buscarColumnaDe(carta); // Busca en columnas

        if (origen == null) {
            // Origen es una CELDA LIBRE
            int idx = celdasLibres.indexOf(carta);
            if (idx >= 0) { // Si la encontro
                for (FoundationDeck f : fundaciones) {
                    if (f.puedeRecibir(carta)) { // Comprueba regla de fundacion
                        f.agregarCarta(carta);
                        celdasLibres.set(idx, null); // Vacia la celda origen
                        historial.push(new Movimiento(carta, idx, "fundacion", f.getPalo().ordinal())); // Guarda undo
                        return true;
                    }
                }
                return false; // No cabe en ninguna fundacion
            }
            return false; // No esta en las celdas (error)
        }

        // Origen es una COLUMNA
        for (FoundationDeck f : fundaciones) {
            if (f.puedeRecibir(carta)) { // Comprueba regla
                f.agregarCarta(carta);
                origen.eliminarUltimaCarta(); // Quita de columna origen
                historial.push(new Movimiento(carta, origen, "fundacion", f.getPalo().ordinal())); // Guarda undo
                return true;
            }
        }
        return false;
    }

    /**
     * Intenta mover una carta (desde celda o columna) a una celda libre.
     * @return true si el movimiento fue exitoso.
     */
    public boolean moverACelda(CartaInglesa carta, int celdaIndex) {
        // Si celdaIndex es -1, busca la primera celda vacia
        if (celdaIndex < 0) {
            for (int i = 0; i < celdasLibres.size(); i++) {
                if (celdasLibres.get(i) == null)
                    return moverACelda(carta, i); // Llama de nuevo con el indice encontrado
            }
            return false; // No hay celdas vacias
        }
        if (celdaIndex >= celdasLibres.size()) return false; // Indice fuera de rango

        TableauDeck origen = buscarColumnaDe(carta);

        if (origen != null) {
            // Origen es COLUMNA
            if (celdasLibres.get(celdaIndex) != null) return false; // Celda destino no esta vacia
            celdasLibres.set(celdaIndex, carta);
            origen.eliminarUltimaCarta();
            historial.push(new Movimiento(carta, origen, "celda", celdaIndex));
            return true;
        }

        // Origen es CELDA (moviendo de celda a celda)
        int idx = celdasLibres.indexOf(carta);
        if (idx >= 0){
            if (celdasLibres.get(celdaIndex) == null){ // Destino debe estar vacio
                celdasLibres.set(celdaIndex, carta);
                celdasLibres.set(idx, null); // Vacia celda origen
                historial.push(new Movimiento(carta, idx, "celda", celdaIndex));
                return true;
            }
            return false;
        }
        return false;
    }

    /**
     * Intenta mover UNA SOLA carta (desde celda o columna) a una columna.
     * @return true si el movimiento fue exitoso.
     */
    public boolean moverAColumna(CartaInglesa carta, int destIndex) {
        if (destIndex < 0 || destIndex >= columnas.size()) return false;
        TableauDeck destino = columnas.get(destIndex);
        if (!destino.puedeRecibir(carta)) return false; // Comprueba regla de columna

        TableauDeck origen = buscarColumnaDe(carta);
        if (origen != null) {
            // Origen es COLUMNA
            destino.agregarCartaForzada(carta);
            origen.eliminarUltimaCarta();
            historial.push(new Movimiento(carta, origen, "columna", destIndex));
            return true;
        }
        int idx = celdasLibres.indexOf(carta);
        if (idx >= 0) {
            // Origen es CELDA
            destino.agregarCartaForzada(carta);
            celdasLibres.set(idx, null);
            historial.push(new Movimiento(carta, idx, "columna", destIndex));
            return true;
        }
        return false;
    }

    /**
     * Logica de pista simple. Devuelve la *primera* carta que encuentre
     * que se pueda mover a *algun* lado.
     */
    public String darPista() {
        //  Revisar celdas libres -> fundacion
        for (int i = 0; i < celdasLibres.size(); i++) {
            CartaInglesa c = celdasLibres.get(i);
            if (c == null) continue;
            for (FoundationDeck f : fundaciones) {
                if (f.puedeRecibir(c)) {
                    return "Mueve el " + c.getValor() + " de " + c.getPalo() + " (Celda) a la fundacion.";
                }
            }
        }

        //  Revisar columnas -> fundacion
        for (int i = 0; i < columnas.size(); i++) {
            CartaInglesa c = columnas.get(i).getUltimaCarta();
            if (c == null) continue;
            for (FoundationDeck f : fundaciones) {
                if (f.puedeRecibir(c)) {
                    return "Mueve el " + c.getValor() + " de " + c.getPalo() + " (Columna " + (i + 1) + ") a la fundacion.";
                }
            }
        }

        //  Revisar celdas libres -> columna
        for (int i = 0; i < celdasLibres.size(); i++) {
            CartaInglesa c = celdasLibres.get(i);
            if (c == null) continue;
            for (int j = 0; j < columnas.size(); j++) {
                if (columnas.get(j).puedeRecibir(c)) {
                    return "Mueve el " + c.getValor() + " de " + c.getPalo() + " (Celda) a la Columna " + (j + 1) + ".";
                }
            }
        }

        //  Revisar columnas -> columna (movimiento de pila)
        for (int i = 0; i < columnas.size(); i++) {
            TableauDeck colOrigen = columnas.get(i);
            if (colOrigen.getCartas().isEmpty()) continue;

            // --- Logica de deteccion de pila (copiada del BoardController) ---
            int stackStartIndex = colOrigen.getCartas().size() - 1;
            if (stackStartIndex >= 0) {
                for (int k = colOrigen.getCartas().size() - 2; k >= 0; k--) {
                    CartaInglesa top = colOrigen.getCartas().get(k + 1);
                    CartaInglesa bottom = colOrigen.getCartas().get(k);
                    if (bottom.getPalo() == top.getPalo() && bottom.getValor() == top.getValor() + 1) {
                        stackStartIndex = k;
                    } else {
                        break;
                    }
                }
            }

            for (int k = stackStartIndex; k < colOrigen.getCartas().size(); k++) {
                List<CartaInglesa> pila = new ArrayList<>(colOrigen.getCartas().subList(k, colOrigen.getCartas().size()));
                CartaInglesa cartaDeAbajo = pila.get(0);
                // Buscar un destino
                for (int j = 0; j < columnas.size(); j++) {
                    if (i == j) continue; // Misma columna
                    TableauDeck colDestino = columnas.get(j);
                    if (colDestino.puedeRecibir(cartaDeAbajo)) {
                        String pilaStr = (pila.size() > 1) ? " la pila (" + cartaDeAbajo.getValor() + "...)" : " el " + cartaDeAbajo.getValor() + " de " + cartaDeAbajo.getPalo();
                        return "Mueve" + pilaStr + " (Columna " + (i + 1) + ") a la Columna " + (j + 1) + ".";
                    }
                }
            }
        }

        if (getCeldasLibresVacias() > 0) {
            for (int i = 0; i < columnas.size(); i++) {
                TableauDeck col = columnas.get(i);

                if (col.getCartas().size() < 2) continue;

                CartaInglesa cartaDeArriba = col.getUltimaCarta();
                CartaInglesa cartaDeAbajo = col.getCartas().get(col.getCartas().size() - 2);
                // Comprueba si 'cartaDeAbajo' tiene un movimiento que ayude
                boolean movDesbloqueado = false;

                // A Fundacion
                for (FoundationDeck f : fundaciones) {
                    if (f.puedeRecibir(cartaDeAbajo)) {
                        movDesbloqueado = true;
                        break;
                    }
                }

                // A Columna
                if (!movDesbloqueado) {
                    for (int j = 0; j < columnas.size(); j++) {
                        if (i == j) continue;
                        if (columnas.get(j).puedeRecibir(cartaDeAbajo)) {
                            movDesbloqueado = true;
                            break;
                        }
                    }
                }

                // Si mover la carta de arriba desbloquea algo, es una buena pista
                if (movDesbloqueado) {
                    return "Mueve el " + cartaDeArriba.getValor() + " de " + cartaDeArriba.getPalo() + " (Columna " + (i + 1) + ") a una celda libre (para desbloquear el " + cartaDeAbajo.getValor() + ").";
                }
            }
        }

        return null; // No hay movimientos
    }

    /**
     * Metodo ayudante de 'darPista'. Comprueba si una carta
     * tiene algun destino legal.
     */
    private boolean hayLugarPara(CartaInglesa carta) {
        //  A fundacion
        for (FoundationDeck f : fundaciones) {
            if (f.puedeRecibir(carta)) return true;
        }
        //  A columna
        for (TableauDeck t : columnas) {
            if (t.puedeRecibir(carta)) return true;
        }
        //  A celda libre
        for (CartaInglesa c : celdasLibres) {
            if (c == null) return true; // Hay un slot vacio
        }
        return false;
    }

    /**
     * Metodo ayudante. Encuentra en que columna esta una carta.
     * @return El TableauDeck o null si no esta en ninguna.
     */
    private TableauDeck buscarColumnaDe(CartaInglesa carta) {
        for (TableauDeck t : columnas) {
            if (t.contieneCarta(carta)) return t;
        }
        return null;
    }

    /**
     * Revierte el ultimo movimiento guardado en la pila 'historial'.
     */
    public void deshacerMovimiento() {
        if (historial.isEmpty()) return; // No hay nada que deshacer
        Movimiento mov = historial.pop(); // Saca el ultimo movimiento

        List<CartaInglesa> pilaARegresar = new ArrayList<>();

        //  Quitar la(s) carta(s) del DESTINO
        switch (mov.destino) {
            case "celda" -> {
                pilaARegresar.add(celdasLibres.get(mov.indiceDestino));
                celdasLibres.set(mov.indiceDestino, null);
            }
            case "fundacion" -> {
                pilaARegresar.add(fundaciones.get(mov.indiceDestino).eliminarUltimaCarta());
            }
            case "columna" -> {
                TableauDeck destino = columnas.get(mov.indiceDestino);
                // Quita 'numCartas' (usualmente 1, o mas si fue una pila)
                for (int i = 0; i < mov.numCartas; i++) {
                    pilaARegresar.add(0, destino.eliminarUltimaCarta()); // add(0,...) mantiene el orden
                }
            }
        }

        // PASO 2: Devolver la(s) carta(s) al ORIGEN
        if (mov.origen != null) {
            // El origen era una COLUMNA
            for (CartaInglesa c : pilaARegresar) {
                mov.origen.agregarCartaForzada(c);
            }
        } else if (mov.indiceOrigenCelda != -1) {
            // El origen era una CELDA LIBRE
            celdasLibres.set(mov.indiceOrigenCelda, pilaARegresar.get(0));
        }
    }

    /**
     * Comprueba si el juego termino (victoria o bloqueo).
     * @return Un String con el mensaje de fin, o null si el juego sigue.
     */
    public String verificarFinJuego() {
        // 1. Comprobar Victoria (todas las fundaciones llenas)
        boolean todasCompletas = fundaciones.stream().allMatch(FoundationDeck::estaCompleta);
        if (todasCompletas) {
            juegoTerminado = true;
            return "Felicidades, ganaste!";
        }

        // 2. Comprobar Bloqueo (revisando movimientos desde columnas)
        for (TableauDeck col : columnas) {
            CartaInglesa top = col.getUltimaCarta();
            if (top != null && hayLugarPara(top)) {
                return null; // Hay movimiento, el juego sigue
            }
        }

        // 3. Comprobar Bloqueo (revisando movimientos desde celdas libres)
        for (CartaInglesa c : celdasLibres) {
            if (c != null && hayLugarPara(c)) {
                return null; // Hay movimiento, el juego sigue
            }
        }

        // 4. Si no hay victoria ni movimientos -> Bloqueo
        juegoTerminado = true;
        return "No hay mas movimientos posibles. Fin del juego.";
    }

    /**
     * Metodo ayudante. Cuenta cuantas celdas libres estan vacias.
     */
    private int getCeldasLibresVacias(){
        int count = 0;
        for (CartaInglesa c : celdasLibres) {
            if (c == null) count++;
        }
        return count;
    }

    /**
     * Intenta mover una PILA de cartas (desde una columna) a otra columna.
     * @return true si el movimiento fue exitoso.
     */
    public boolean moverPilaAColumna(List<CartaInglesa> pila, TableauDeck origen, int destIndex) {
        //  Comprobaciones basicas
        if (pila == null || pila.isEmpty()) return false;
        if (destIndex < 0 || destIndex >= columnas.size()) return false; // Indice no valido
        CartaInglesa cartaDeAbajoPila = pila.get(0);
        TableauDeck destino = columnas.get(destIndex);

        if (!destino.puedeRecibir(cartaDeAbajoPila)) {
            // El destino no acepta esta carta
            return false;
        }

        int k = pila.size();

        //  Quitar 'k' cartas de la columna origen
        for (int i = 0; i < k; i++) {
            origen.eliminarUltimaCarta();
        }

        //  Anadir la pila (carta por carta) al destino
        for (CartaInglesa c : pila) {
            destino.agregarCartaForzada(c);
        }

        //  Guardar UN movimiento en el historial (con 'k' numero de cartas)
        historial.push(new Movimiento(cartaDeAbajoPila, origen, "columna", destIndex, k));

        return true; // Movimiento exitoso
    }

    /**
     * Objeto de datos que guarda toda la informacion de un solo movimiento
     * para poder revertirlo (Undo).
     */
    private static class Movimiento {
        CartaInglesa carta;        // La carta principal movida
        TableauDeck origen;        // La columna origen (null si fue celda)
        String destino;            // "celda", "fundacion" o "columna"
        int indiceDestino;         // Indice del destino
        int numCartas;             // 1 para movimientos normales, >1 para pilas
        int indiceOrigenCelda;     // Indice de la celda origen (-1 si fue columna)

        // Constructor para mover PILA (desde Columna)
        Movimiento(CartaInglesa carta, TableauDeck origen, String destino, int indiceDestino, int numCartas) {
            this(carta, origen, destino, indiceDestino, numCartas, -1);
        }

        // Constructor para mover 1 CARTA (desde Columna)
        Movimiento(CartaInglesa carta, TableauDeck origen, String destino, int indiceDestino) {
            this(carta, origen, destino, indiceDestino, 1, -1);
        }

        // Constructor para mover 1 CARTA (desde Celda)
        Movimiento(CartaInglesa carta, int indiceOrigenCelda, String destino, int indiceDestino) {
            this(carta, null, destino, indiceDestino, 1, indiceOrigenCelda);
        }

        // Constructor "Maestro" que usan los otros
        Movimiento(CartaInglesa carta, TableauDeck origen, String destino, int indiceDestino, int numCartas, int indiceOrigenCelda) {
            this.carta = carta;
            this.origen = origen;
            this.destino = destino;
            this.indiceDestino = indiceDestino;
            this.numCartas = numCartas;
            this.indiceOrigenCelda = indiceOrigenCelda;
        }
    }

    public List<TableauDeck> getColumnas() { return columnas; }
    public List<CartaInglesa> getCeldasLibres() { return celdasLibres; }
    public List<FoundationDeck> getFundaciones() { return fundaciones; }
}