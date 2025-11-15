package DeckOfCards;

/**
 * CartaInglesa
 * Esta clase es el 'Modelo de Datos' mas fundamental.
 * Representa una unica carta, con su valor y palo.
 * Es 'Comparable' para permitir ordenar mazos.
 */
public class CartaInglesa implements Comparable<CartaInglesa> {

    private int valor;        // 1..13 (As=1, J=11, Q=12, K=13)
    private Palo palo;        // El enum Palo (TREBOL, PICA, etc.)
    private boolean faceUp = false; // Estado: boca arriba (true) o boca abajo (false)

    /**
     * Constructor para crear una nueva carta.
     * @param valor El valor numerico (1-13).
     * @param palo El palo (enum).
     */
    public CartaInglesa(int valor, Palo palo) {
        this.valor = valor;
        this.palo = palo;
        this.faceUp = false; // Todas las cartas se crean boca abajo por defecto
    }

    // --- Getters ---
    // Devuelven el valor y el palo de la carta.
    public int getValor() { return valor; }
    public Palo getPalo() { return palo; }

    /**
     * Metodo de comparacion (de la interfaz Comparable).
     * Permite ordenar listas de cartas.
     * Ordena primero por palo, y luego por valor.
     */
    @Override
    public int compareTo(CartaInglesa o) {
        // Compara el indice ordinal del enum Palo (ej. TREBOL=0, PICA=3)
        int c = Integer.compare(this.palo.ordinal(), o.palo.ordinal());
        if (c != 0) return c; // Si los palos son diferentes, devuelve esa comparacion

        // Si los palos son iguales, compara por valor
        return Integer.compare(this.valor, o.valor);
    }

    //  Metodos ayudantes de reglas ---

    /** Comprueba si la carta es un As (valor 1). */
    public boolean esAs()  { return valor == 1; }

    /** Comprueba si la carta es un Rey (valor 13). */
    public boolean esRey() { return valor == 13; }

    /** * Regla de fundacion (Mismo palo, ascendente).
     */
    public boolean puedeIrEnFundacionSobre(CartaInglesa topFund) {
        if (topFund == null) return this.esAs(); // Si esta vacia, solo As
        return this.palo == topFund.palo && this.valor == topFund.valor + 1;
    }

    /**
     * Coloca la carta boca arriba.
     */
    public void makeFaceUp() {
        faceUp = true;
    }

}