package ui;

import DeckOfCards.CartaInglesa;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.layout.*;
import eightoff.EightOffGame; // Importa la logica principal del juego
import eightoff.TableauDeck; // Importa la logica de una columna del tablero
import java.util.List;
import java.util.ArrayList;

/**
 * BoardController (Controlador del Tablero)
 * Es el "cerebro" de la interfaz de usuario (UI).
 * Conecta el archivo FXML (la vista) con la logica del juego (el modelo).
 * Sigue el patron Modelo-Vista-Controlador (MVC).
 */
public class BoardController {

    //  Variables para el FXML
    @FXML
    private BorderPane root; // El panel principal

    @FXML
    private GridPane boardGrid; // parte central donde van las celdas y columnas

    @FXML
    private VBox foundationsBox; // Panel izquierdo para las 4 fundaciones

    //  Variables de Logica
    private EightOffGame juego; // Instancia de la logica del juego

    // Listas para guardar los paneles de destino para el drag-and-drop
    private final java.util.List<Pane> columnPanes = new java.util.ArrayList<>();
    private final java.util.List<Pane> freeCellPanes = new java.util.ArrayList<>();

    // Espacio vertical entre cartas apiladas en una columna
    private static final double CARD_VERTICAL_OFFSET = 25;

    /**
     * Metodo de inicializacion.
     * Se llama automaticamente cuando se carga el FXML.
     */
    @FXML
    public void initialize() {
        juego = new EightOffGame(); // Crea una nueva partida
        refrescarTablero(); // Dibuja el tablero por primera vez
    }

    @FXML
    private void onNuevoJuego() {
        juego = new EightOffGame(); // Resetea la logica del juego
        refrescarTablero(); // Vuelve a dibujar todo
    }

    @FXML
    private void onUndo() {
        juego.deshacerMovimiento(); // Pide a la logica que deshaga
        refrescarTablero(); // Refresca la UI para mostrar el estado anterior
    }

    @FXML
    private void onPista() {
        String pista = juego.darPista();
        if (pista == null)
            mostrarMensaje("No hay movimientos posibles.");
        else
            mostrarMensaje(pista);
    }

    /**
     * Metodo ayudante para mostrar ventanas de alerta.
     */
    private void mostrarMensaje(String texto) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Pista");
        alert.setHeaderText(null);
        alert.setContentText(texto);
        alert.showAndWait();
    }

    /**
     * Dibuja (o redibuja) todo el estado del juego.
     * Lee el estado actual de 'juego' y crea los nodos visuales.
     */
    private void refrescarTablero() {
        // Limpia el tablero anterior para no duplicar
        boardGrid.getChildren().clear();
        columnPanes.clear();
        freeCellPanes.clear();

        // --- 1. Dibuja las FUNDACIONES (izquierda) ---
        List<Node> panesFundacion = foundationsBox.getChildren();
        for (int i = 0; i < panesFundacion.size(); i++) {
            Pane slot = (Pane) panesFundacion.get(i);
            slot.getChildren().clear(); // Limpia el slot
            if (i < juego.getFundaciones().size()) {
                var fund = juego.getFundaciones().get(i);
                if (!fund.getCartas().isEmpty()) {
                    // Dibuja solo la carta de arriba
                    CartaInglesa top = fund.getCartas().get(fund.getCartas().size() - 1);
                    Node nodo = CardView.frontCard(top);
                    slot.getChildren().add(nodo);
                }
            }
        }

        // 2. Dibuja las CELDAS LIBRES
        HBox celdasBox = new HBox(12);
        for (int i = 0; i < 8; i++) {
            StackPane slot = crearSlotVisual(90, 130);
            CartaInglesa carta = (i < juego.getCeldasLibres().size()) ? juego.getCeldasLibres().get(i) : null;
            if (carta != null) {
                Node nodo = CardView.frontCard(carta);
                slot.getChildren().add(nodo);
                // Las cartas en celdas libres solo mueven de una en una
                hacerArrastrable(nodo, carta);
            }
            celdasBox.getChildren().add(slot);
            freeCellPanes.add(slot); // Guarda el slot como destino de drop
        }
        boardGrid.add(celdasBox, 0, 0, 8, 1); // Anade la fila de celdas a la rejilla

        // 3. Dibuja las COLUMNAS
        HBox cols = new HBox(15);
        for (int idx = 0; idx < juego.getColumnas().size(); idx++) {
            TableauDeck col = juego.getColumnas().get(idx);
            Pane columnaPane = new Pane(); // Panel para apilar cartas verticalmente
            columnaPane.setPrefSize(100, 600);
            double y = 0; // Coordenada Y para la siguiente carta

            // Busca el indice de la carta mas alta que forma una pila valida
            int stackStartIndex = col.getCartas().size() - 1; // Por defecto, solo la ultima
            if (stackStartIndex >= 0) {
                for (int i = col.getCartas().size() - 2; i >= 0; i--) {
                    CartaInglesa top = col.getCartas().get(i + 1);
                    CartaInglesa bottom = col.getCartas().get(i);
                    // Regla: mismo palo, descendente
                    if (bottom.getPalo() == top.getPalo() && bottom.getValor() == top.getValor() + 1) {
                        stackStartIndex = i; // La pila valida empieza mas arriba
                    } else {
                        break; // Se rompio la secuencia
                    }
                }
            }

            // Dibuja las cartas en la columna
            for (int i = 0; i < col.getCartas().size(); i++) {
                CartaInglesa carta = col.getCartas().get(i);
                Node nodo = CardView.frontCard(carta);
                nodo.setLayoutY(y); // Posiciona la carta
                columnaPane.getChildren().add(nodo); // Anade el nodo al panel

                // Si la carta es parte de una pila valida, la hace arrastrable
                if (i >= stackStartIndex) {
                    // Crea una COPIA de la pila que se va a arrastrar
                    List<CartaInglesa> stackParaArrastrar = new ArrayList<>(col.getCartas().subList(i, col.getCartas().size()));
                    // Llama al metodo de arrastre para PILAS
                    hacerArrastrable(nodo, stackParaArrastrar, col);
                }

                y += CARD_VERTICAL_OFFSET; // Incrementa Y para la siguiente carta
            }

            if (col.getCartas().isEmpty()) columnaPane.getChildren().add(crearSlotVisual(90, 130));
            columnPanes.add(columnaPane); // Guarda el panel como destino de drop
            cols.getChildren().add(columnaPane);
        }
        boardGrid.add(cols, 0, 1, 8, 1); // Anade la fila de columnas a la rejilla
    }

    /**
     * Metodo de arrastre para UNA SOLA CARTA.
     * Usado por las Celdas Libres.
     */
    private void hacerArrastrable(Node nodo, CartaInglesa carta) {
        final double[] start = new double[2]; // Guarda pos inicial del mouse

        nodo.setOnMousePressed(e -> {
            start[0] = e.getSceneX();
            start[1] = e.getSceneY();
            nodo.toFront(); // Trae la carta al frente
        });

        nodo.setOnMouseDragged(e -> {
            // Mueve la carta siguiendo el mouse
            nodo.setTranslateX(e.getSceneX() - start[0]);
            nodo.setTranslateY(e.getSceneY() - start[1]);
        });

        nodo.setOnMouseReleased(e -> {
            boolean moved = false; // Bandera para saber si el movimiento fue exitoso

            // 1. Intenta mover a fundacion
            for (Node fnode : foundationsBox.getChildren()) {
                if (interseca(nodo, fnode)) {
                    moved = juego.moverAFundacion(carta);
                    if (moved) break;
                }
            }

            // 2. Intenta mover a celda libre
            if (!moved) {
                for (int i = 0; i < freeCellPanes.size(); i++) {
                    if (interseca(nodo, freeCellPanes.get(i))) {
                        moved = juego.moverACelda(carta, i);
                        if (moved) break;
                    }
                }
            }

            // 3. Intenta mover a columna
            if (!moved) {
                for (int i = 0; i < columnPanes.size(); i++) {
                    if (interseca(nodo, columnPanes.get(i))) {
                        moved = juego.moverAColumna(carta, i);
                        if (moved) break;
                    }
                }
            }

            terminarDrag(nodo, moved); // Finaliza el arrastre
        });
    }

    /**
     * Metodo de arrastre para una PILA DE CARTAS (sobrescribe al anterior).
     * Usado por las Columnas.
     */
    private void hacerArrastrable(Node nodo, List<CartaInglesa> stack, TableauDeck origenColumna) {
        final double[] start = new double[2];
        CartaInglesa cartaPrincipal = stack.get(0); // Carta en la que se hizo clic

        // Encuentra todos los nodos graficos de la pila
        Pane parentPane = (Pane) nodo.getParent();
        List<Node> nodosDeLaPila = new ArrayList<>();
        int startIndexEnPane = parentPane.getChildren().indexOf(nodo);

        if (startIndexEnPane != -1) {
            int numNodosEnPila = stack.size();
            for (int i = 0; i < numNodosEnPila; i++) {
                if (startIndexEnPane + i < parentPane.getChildren().size()) {
                    nodosDeLaPila.add(parentPane.getChildren().get(startIndexEnPane + i));
                }
            }
        }

        nodo.setOnMousePressed(e -> {
            start[0] = e.getSceneX();
            start[1] = e.getSceneY();
            for (Node n : nodosDeLaPila) {
                n.toFront(); // Trae toda la pila al frente
            }
        });

        nodo.setOnMouseDragged(e -> {
            double deltaX = e.getSceneX() - start[0];
            double deltaY = e.getSceneY() - start[1];
            // Mueve todos los nodos de la pila juntos
            for (Node n : nodosDeLaPila) {
                n.setTranslateX(deltaX);
                n.setTranslateY(deltaY);
            }
        });

        nodo.setOnMouseReleased(e -> {
            boolean moved = false;

            // 1. Intenta mover a fundacion (solo si la pila es de 1)
            if (stack.size() == 1) {
                for (Node fnode : foundationsBox.getChildren()) {
                    if (interseca(nodo, fnode)) {
                        moved = juego.moverAFundacion(cartaPrincipal);
                        if (moved) break;
                    }
                }
            }

            // 2. Intenta mover a celda libre (solo si la pila es de 1)
            if (!moved && stack.size() == 1) {
                for (int i = 0; i < freeCellPanes.size(); i++) {
                    if (interseca(nodo, freeCellPanes.get(i))) {
                        moved = juego.moverACelda(cartaPrincipal, i);
                        if (moved) break;
                    }
                }
            }

            // 3. Intenta mover a columna (logica de pila)
            if (!moved) {
                for (int i = 0; i < columnPanes.size(); i++) {
                    if (i == juego.getColumnas().indexOf(origenColumna)) continue; // No mover a la misma columna

                    if (interseca(nodo, columnPanes.get(i))) {
                        // Llama al metodo de mover PILA en la logica
                        moved = juego.moverPilaAColumna(stack, origenColumna, i);
                        if (moved) break;
                    }
                }
            }

            // Resetea la posicion de todos los nodos de la pila
            for (Node n : nodosDeLaPila) {
                n.setTranslateX(0);
                n.setTranslateY(0);
            }

            // Si el movimiento fue exitoso, refresca el tablero y comprueba si gano/perdio
            if (moved){
                refrescarTablero();
                String msg = juego.verificarFinJuego();
                if (msg != null) {
                    mostrarMensaje(msg);
                }
            }
        });
    }

    /**
     * Comprueba si dos nodos graficos se superponen.
     */
    private boolean interseca(Node a, Node b) {
        // Obtiene los limites de los nodos en la escena y comprueba interseccion
        var ab = a.localToScene(a.getBoundsInLocal());
        var bb = b.localToScene(b.getBoundsInLocal());
        return ab.intersects(bb);
    }

    /**
     * Finaliza el arrastre de UNA SOLA CARTA.
     * Resetea su posicion y refresca el tablero si el movimiento fue exitoso.
     */
    private void terminarDrag(Node nodo, boolean exito) {
        nodo.setTranslateX(0); // Resetea la posicion X
        nodo.setTranslateY(0); // Resetea la posicion Y
        if (exito) {
            refrescarTablero();

            // Comprueba si el juego termino
            String msg = juego.verificarFinJuego();
            if (msg != null) {
                mostrarMensaje(msg);
            }
        }
    }

    /**
     * Crea un panel 'slot' vacio con estilo.
     * Usado para celdas vacias y columnas vacias.
     */
    private StackPane crearSlotVisual(double w, double h) {
        StackPane slot = new StackPane();
        slot.setPrefSize(w, h);
        // Estilo CSS en linea para el slot (fondo transparente, borde blanco)
        slot.setStyle("-fx-border-color: white; -fx-border-radius: 8; "
                + "-fx-background-color: rgba(255,255,255,0.1); "
                + "-fx-background-radius: 8;");
        return slot;
    }
}