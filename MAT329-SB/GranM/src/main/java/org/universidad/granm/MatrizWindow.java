package org.universidad.granm;

import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.universidad.granm.claseabstracta.SimplexBase;
import org.universidad.granm.metodos.MSimplex;

import static org.universidad.granm.metodos.MSimplex.*;

/**
 * Clase que representa la ventana donde se muestra el proceso paso a paso del método Simplex
 * y la solución final del problema de programación lineal.
 */
public class MatrizWindow {
    /**
     * Ventana principal donde se muestra el contenido.
     */
    private Stage stage;

    /**
     * Contenedor principal vertical que organiza todos los elementos.
     */
    private VBox mainContainer;

    /**
     * Panel de cuadrícula donde se muestra la tabla del método Simplex.
     */
    private GridPane gridPane;

    /**
     * Contenedor horizontal para los botones de navegación.
     */
    private HBox buttonBox;

    /**
     * Etiqueta que muestra el número de paso actual.
     */
    private Label lblPaso;

    /**
     * Botón para navegar al paso anterior.
     */
    private Button btnAnterior;

    /**
     * Botón para navegar al paso siguiente.
     */
    private Button btnSiguiente;

    /**
     * Panel de cuadrícula donde se muestra la solución final.
     */
    private GridPane solucionPane;

    /**
     * Instancia del resolvedor del método Simplex que contiene los datos del problema.
     */
    private SimplexBase simplex;

    /**
     * Índice del paso actual que se está mostrando.
     */
    private int pasoActual = 0;

    /**
     * Constructor que inicializa la ventana con los datos del método Simplex.
     * 
     * @param simplex Instancia del resolvedor del método Simplex con los datos del problema
     */
    public MatrizWindow(SimplexBase simplex) {
        this.simplex = simplex;

        // Inicializar la ventana
        stage = new Stage();
        mainContainer = new VBox(10); // Espacio vertical de 10 píxeles entre componentes
        mainContainer.setStyle("-fx-padding: 20;");

        // Crear componentes de la interfaz
        gridPane = new GridPane();
        gridPane.setHgap(10); // Espacio horizontal entre celdas
        gridPane.setVgap(10); // Espacio vertical entre celdas

        // Contenedor para los botones de navegación
        buttonBox = new HBox(10);
        buttonBox.setStyle("-fx-alignment: center;");

        // Etiqueta para mostrar el número de paso actual
        lblPaso = new Label();
        lblPaso.setStyle("-fx-font-weight: bold;");

        // Botones de navegación
        btnAnterior = new Button("Anterior");
        btnSiguiente = new Button("Siguiente");

        // Panel para mostrar la solución final
        solucionPane = new GridPane();
        solucionPane.setHgap(10);
        solucionPane.setVgap(10);
        solucionPane.setStyle("-fx-padding: 10; -fx-background-color: #f5f5f5;");

        // Configurar acciones de los botones
        btnAnterior.setOnAction(e -> mostrarPasoAnterior());
        btnSiguiente.setOnAction(e -> mostrarPasoSiguiente());

        // Agregar componentes al layout
        buttonBox.getChildren().addAll(btnAnterior, lblPaso, btnSiguiente);
        mainContainer.getChildren().addAll(gridPane, buttonBox, solucionPane);

        // Configurar la escena y la ventana
        Scene scene = new Scene(mainContainer);
        stage.setScene(scene);
        stage.setTitle("Proceso del Método Simplex");
    }

    /**
     * Muestra la ventana con el primer paso del método Simplex y la solución final.
     */
    public void mostrar() {
        mostrarPaso(pasoActual); // Mostrar el primer paso
        if (simplex.isSolucionEncontrada())
            mostrarSolucionFinal(); // Mostrar la solución óptima
        stage.show(); // Hacer visible la ventana
    }

    /**
     * Muestra un paso específico del método Simplex en la interfaz.
     * 
     * @param paso Índice del paso a mostrar
     */
    private void mostrarPaso(int paso) {
        // Limpiar el panel antes de mostrar el nuevo paso
        gridPane.getChildren().clear();
        gridPane.getColumnConstraints().clear();
        gridPane.getRowConstraints().clear();

        // Obtener los datos del paso actual
        double[][] tabla = simplex.getTablaPaso(paso);
        String descripcion = simplex.getDescripcionPaso(paso);
        String varEntrada = simplex.getVariableEntradaPaso(paso);
        String varSalida = simplex.getVariableSalidaPaso(paso);

        // Configurar tamaño uniforme para todas las columnas
        for (int j = 0; j < tabla[0].length; j++) {
            ColumnConstraints col = new ColumnConstraints();
            col.setMinWidth(80);
            col.setPrefWidth(80);
            col.setMaxWidth(80);
            gridPane.getColumnConstraints().add(col);
        }

        // Mostrar la descripción del paso en la parte superior
        Label lblDesc = new Label(descripcion);
        lblDesc.setStyle("-fx-font-weight: bold;");
        gridPane.add(lblDesc, 0, 0, tabla[0].length, 1);

        // Mostrar las variables de entrada y salida si existen
        if (!varEntrada.isEmpty() || !varSalida.isEmpty()) {
            Label lblVars = new Label("Entra: " + varEntrada + " - Sale: " + varSalida);
            gridPane.add(lblVars, 0, 1, tabla[0].length, 1);
        }

        // Obtener información sobre el número de variables y restricciones
        int numVars = simplex.getNroVariables();
        int numRest = simplex.getNroRestricciones();

        // Agregar encabezados para las variables de decisión (x1, x2, ...)
        for (int j = 1; j < simplex.getNroColumnas() - 1; j++) {
            Label lbl = new Label("X" + (j));
            lbl.setStyle("-fx-font-weight: bold;");
            gridPane.add(lbl, j + 1, 2);
        }

//        // Agregar encabezados para las variables de holgura (s1, s2, ...)
//        for (int j = numVars; j < simplex.getNroColumnas() - 1; j++) {
//            Label lbl = new Label("X" + j);
//            lbl.setStyle("-fx-font-weight: bold;");
//            gridPane.add(lbl, numVars + j + 1, 2);
//        }

        // Agregar encabezado para la función objetivo (Z)
        Label lblZ = new Label("Z");
        lblZ.setStyle("-fx-font-weight: bold;");
        gridPane.add(lblZ, 1, 2);

        // Agregar encabezado para los términos independientes (RHS - Right Hand Side)
        Label lblTI = new Label("RHS");
        lblTI.setStyle("-fx-font-weight: bold;");
        gridPane.add(lblTI, simplex.getNroColumnas(), 2);

        // Mostrar los valores de la tabla Simplex
        for (int i = 0; i < tabla.length; i++) {
            // Etiqueta para identificar cada fila
            Label lblFila = new Label(i == 0 ? "Z" : "X" + i);
            lblFila.setStyle("-fx-font-weight: bold;");
            gridPane.add(lblFila, 0, i + 3);

            // Mostrar cada valor de la tabla con formato de dos decimales
            for (int j = 0; j < tabla[i].length; j++) {
                TextField lblValor = new TextField(String.format("%.2f", redondear(tabla[i][j], decimales, epsilon)));
                lblValor.setEditable(false);
                lblValor.setStyle("-fx-border-color: #ddd; -fx-border-width: 1; -fx-padding: 5;");
                gridPane.add(lblValor, j + 1, i + 3);
            }
        }

        // Actualizar la etiqueta del paso actual y el estado de los botones
        lblPaso.setText("Paso " + (paso + 1) + " de " + simplex.cantPasos());
        btnAnterior.setDisable(paso == 0); // Deshabilitar "Anterior" si es el primer paso
        btnSiguiente.setDisable(paso == simplex.cantPasos() - 1); // Deshabilitar "Siguiente" si es el último paso
    }

    /**
     * Navega al paso anterior del método Simplex si es posible.
     */
    private void mostrarPasoAnterior() {
        if (pasoActual > 0) {
            pasoActual--;
            mostrarPaso(pasoActual);
        }
    }

    /**
     * Navega al paso siguiente del método Simplex si es posible.
     */
    private void mostrarPasoSiguiente() {
        if (pasoActual < simplex.cantPasos() - 1) {
            pasoActual++;
            mostrarPaso(pasoActual);
        }
    }

    /**
     * Muestra la solución óptima del problema en el panel inferior.
     */
    private void mostrarSolucionFinal() {
        solucionPane.getChildren().clear();

        // Título de la sección de solución
        Label titulo = new Label("Solución Óptima");
        titulo.setStyle("-fx-font-size: 16; -fx-font-weight: bold;");
        solucionPane.add(titulo, 0, 0, 2, 1);

        // Mostrar el valor de cada variable de decisión
        for (int i = 0; i < simplex.getNroVariables(); i++) {
            Label lblVar = new Label("x" + (i + 1) + ":");
            Label lblVal = new Label(String.format("%.2f", simplex.solucion.get("x" + (i + 1))));
            solucionPane.add(lblVar, 0, i + 1);
            solucionPane.add(lblVal, 1, i + 1);
        }

        // Mostrar el valor óptimo de la función objetivo
        Label lblZ = new Label("Z óptimo:");
        Label lblZVal = new Label(String.format("%.2f", simplex.solucion.get("z")));
        lblZVal.setStyle("-fx-font-weight: bold;");
        solucionPane.add(lblZ, 0, simplex.getNroVariables() + 1);
        solucionPane.add(lblZVal, 1, simplex.getNroVariables() + 1);
    }
}
