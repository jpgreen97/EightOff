package ui;

import DeckOfCards.CartaInglesa;
import DeckOfCards.Palo;
import javafx.scene.image.Image;
import java.io.InputStream;
import java.util.*;

/**
 * CardAssets (Recursos de Cartas)
 * Clase utilitaria que maneja la carga de imagenes de cartas.
 * Utiliza un 'CACHE' para no cargar la misma imagen mas de una vez.
 */
public class CardAssets {

    // Un Mapa que actua como cache. Guarda las imagenes que ya se cargaron.
    // La clave (String) es la ruta del archivo (ej. "/cartas/ace_of_spades.png")
    // El valor (Image) es el objeto de imagen ya cargado en memoria.
    private static final Map<String, Image> CACHE = new HashMap<>();

    // Carpeta base dentro de los recursos del proyecto donde se guardan las imagenes.
    private static final String FOLDER = "/cartas/";

    /**
     * Devuelve la imagen de la parte trasera de la carta.
     */
    public static Image backImage() {
        return load(FOLDER + "back.png");
    }

    /**
     * Busca y devuelve la imagen para una carta especifica.
     * @param c La carta logica (datos).
     * @return La imagen (Image) correspondiente. Si no la encuentra, devuelve la trasera.
     */
    public static Image imageFor(CartaInglesa c) {
        // Intenta buscar varios nombres de archivo (ej. .png, .jpg, 2.png)
        for (String path : candidatePaths(c)) {
            Image img = tryLoad(path); // Intenta cargar la imagen
            if (img != null) return img; // Si la encuentra, la devuelve
        }

        // Si el bucle termina, no se encontro ninguna imagen.
        System.out.println("CardAssets PNG no encontrado para: valor=" + c.getValor()
                + " palo=" + safeEnumName(c.getPalo())
                + "  intentos=" + Arrays.toString(candidatePaths(c)));
        return backImage(); // Devuelve la trasera como ultimo recurso
    }

    /**
     * Convierte el valor numerico de la carta (1, 11, 13) a su nombre en ingles.
     */
    private static String rankName(int v) {
        if (v == 1 || v == 14) return "ace"; // As
        switch (v) {
            case 11: return "jack"; // Jota
            case 12: return "queen"; // Reina
            case 13: return "king"; // Rey
            default: return String.valueOf(v); // Numeros 2-10
        }
    }

    /**
     * Convierte el enum 'Palo' (ej. TREBOL) a su nombre de archivo en ingles.
     */
    private static String suitName(Palo p) {
        String n = safeEnumName(p).toLowerCase(Locale.ROOT);
        // Comprueba variaciones del nombre del palo (ej. "treb" o "club")
        if (n.contains("treb") || n.contains("club"))    return "clubs";
        if (n.contains("pica") || n.contains("spade"))   return "spades";
        if (n.contains("cor")  || n.contains("heart"))   return "hearts";
        if (n.contains("diam") || n.contains("diamond")) return "diamonds";
        return "spades"; // Valor por defecto
    }

    /**
     * Metodo seguro para obtener el nombre de un enum
     */
    private static String safeEnumName(Palo p) {
        try { return p.name(); } catch (Throwable t) { return String.valueOf(p); }
    }

    /**
     * Genera una lista de posibles nombres de archivo para una carta.
     * Ej: "ace_of_spades.png", "ace_of_spades2.png", "ace_of_spades.jpg"
     */
    private static String[] candidatePaths(CartaInglesa c) {
        String base = rankName(c.getValor()) + "_of_" + suitName(c.getPalo());
        return new String[] {
                FOLDER + base + ".png",
                FOLDER + base + "2.png",
                FOLDER + base + ".jpg",
                FOLDER + base + ".PNG",
                FOLDER + base + "2.jpg",
                FOLDER + base + "2.PNG",
        };
    }

    /**
     * Intenta cargar una imagen. Primero revisa el cache.
     * Si no esta en cache, la carga desde los recursos.
     * Si no existe, devuelve null (sin error).
     */
    private static Image tryLoad(String path) {
        Image cached = CACHE.get(path); // 1. Revisar cache
        if (cached != null) return cached;

        try (InputStream is = CardAssets.class.getResourceAsStream(path)) { // 2. Cargar recurso
            if (is == null) return null; // No existe
            Image img = new Image(is);
            CACHE.put(path, img); // 3. Guardar en cache
            return img;
        } catch (Exception e) {
            return null; // Error al cargar
        }
    }

    /**
     * Carga una imagen. Es igual a tryLoad, pero este SI lanza un error
     */
    private static Image load(String path) {
        Image cached = CACHE.get(path);
        if (cached != null) return cached;

        try (InputStream is = CardAssets.class.getResourceAsStream(path)) {
            if (is == null) throw new RuntimeException("No se encontro: " + path);
            Image img = new Image(is);
            CACHE.put(path, img);
            return img;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}