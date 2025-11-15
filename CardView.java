package ui;

import DeckOfCards.CartaInglesa;
import javafx.scene.Node;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

/**
 * CardView (Vista de Carta)
 * Clase utilitaria que convierte un objeto CartaInglesa (datos)
 * en un objeto grafico 'Node' (visual) de JavaFX.
 */
public class CardView {
    // Define el tamaño estandar de las cartas
    private static final double W = 90, H = 130;

    /**
     * Devuelve el nodo visual para la cara de una carta.
     * @param c La carta logica (datos).
     * @return El 'Node' grafico (visual).
     */
    public static Node frontCard(CartaInglesa c) {
        // Obtiene la imagen de CardAssets y construye el grafico
        return cardFromImage(CardAssets.imageFor(c));
    }

    /**
     * Metodo privado que construye el grafico.
     * Combina un fondo, la imagen y una sombra.
     * @param img La imagen a usar.
     * @return Un StackPane con el grafico final.
     */
    private static Node cardFromImage(Image img) {
        // 1. Fondo blanco con bordes redondos
        Rectangle bg = new Rectangle(W, H);
        bg.setFill(Color.WHITE);
        bg.setStroke(Color.web("#111827")); // Borde sutil
        bg.setArcWidth(12);
        bg.setArcHeight(12);

        // 2. Imagen de la carta
        ImageView iv = new ImageView(img);
        iv.setFitWidth(W);
        iv.setFitHeight(H);
        iv.setPreserveRatio(true);
        iv.setSmooth(true);

        // 3. Clip (mascara) para redondear las esquinas de la imagen
        Rectangle clip = new Rectangle(W, H);
        clip.setArcWidth(12);
        clip.setArcHeight(12);
        iv.setClip(clip);

        // 4. Apila el fondo (bg) y la imagen (iv)
        StackPane root = new StackPane(bg, iv);

        // 5. Añade una sombra para darle profundidad
        root.setEffect(new DropShadow(10, Color.color(0, 0, 0, 0.35)));

        return root;
    }
}