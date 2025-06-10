package org.universidad.granm;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.Node;
import javafx.geometry.Side;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import org.universidad.granm.metodos.MGranM;
import org.universidad.granm.metodos.MSimplex;

import java.util.ArrayList;
import java.util.List;

/**
 * Controlador principal de la aplicación GranM.
 * Gestiona la interfaz gráfica para la entrada de datos del problema de programación lineal
 * y la interacción con el método Simplex.
 */
public class GranMController {

    /**
     * Array que almacena los coeficientes de la función objetivo.
     */
    private double[] funcionObjetivo;

    /**
     * Matriz que almacena los coeficientes de las restricciones.
     */
    private double[][] restricciones;

    /**
     * Array que almacena los términos independientes de las restricciones.
     */
    private double[] terminosIndependientes;


    private String[] tipoRestriccion;

    private ComboBox<String> comboOperador;
    private List<ComboBox<String>> operadoresRestricciones = new ArrayList<>();

    private ComboBox<String> comboTipoOperacion;


    /**
     * Menú contextual para las opciones de la aplicación.
     */
    private ContextMenu contextMenu;

    /**
     * Índice del paso actual en la visualización del método Simplex.
     */
    private int pasoActual = 0;

    /**
     * Instancia del resolvedor del método Simplex.
     */
    private MSimplex simplex;

    /**
     * Spinner para seleccionar el número de variables del problema.
     */
    @FXML private Spinner<Integer> spinnerVariables;

    /**
     * Spinner para seleccionar el número de restricciones del problema.
     */
    @FXML private Spinner<Integer> spinnerRestricciones;

    /**
     * Contenedor para los campos de la función objetivo.
     */
    @FXML private VBox containerFuncionObjetivo;

    /**
     * Contenedor para los campos de las restricciones.
     */
    @FXML private VBox containerRestricciones;


    /**
     * ScrollPane para las restricciones.
     */
    @FXML private ScrollPane scrollRestricciones;

    /**
     * ScrollPane para la función objetivo.
     */
    @FXML private ScrollPane scrollFuncionObjetivo;

    /**
     * ScrollPane para la solución.
     */
    @FXML private ScrollPane scrollSolucion;

    /**
     * Lista de campos de texto para los coeficientes de la función objetivo.
     */
    private List<TextField> camposFuncionObjetivo = new ArrayList<>();

    /**
     * Lista de listas de campos de texto para los coeficientes de las restricciones.
     */
    private List<List<TextField>> camposRestricciones = new ArrayList<>();

    /**
     * Lista de campos de texto para los términos independientes de las restricciones.
     */
    private List<TextField> camposTerminosIndependientes = new ArrayList<>();

    /**
     * Constructor del controlador.
     * Inicializa el menú contextual con las opciones básicas.
     */
    public GranMController() {
        // Crear el ContextMenu una vez en el constructor
        MenuItem itemNuevo = new MenuItem("Nuevo");
        MenuItem itemGuardar = new MenuItem("Guardar");
        MenuItem itemSalir = new MenuItem("Salir");

        itemNuevo.setOnAction(e -> System.out.println("Nuevo clicado"));
        itemGuardar.setOnAction(e -> System.out.println("Guardar clicado"));
        itemSalir.setOnAction(e -> System.exit(0));

        contextMenu = new ContextMenu(itemNuevo, itemGuardar, itemSalir);
    }

    /**
     * Maneja el evento del botón de menú.
     * Muestra u oculta el menú contextual.
     *
     * @param event Evento de acción que desencadenó este método
     */
    @FXML
    public void menuButton(ActionEvent event) {
        Node source = (Node) event.getSource();
        if (contextMenu.isShowing()) {
            contextMenu.hide();
        } else {
            contextMenu.show(source, Side.BOTTOM, 0, 0);
        }
    }

    /**
     * Inicializa los componentes de la interfaz.
     * Configura los valores iniciales de los spinners.
     */
    public void initialize() {
        // Configurar spinner para el número de variables (entre 2 y 20, valor inicial 2)
        SpinnerValueFactory<Integer> valueFactory =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(2, 20, 2);
        spinnerVariables.setValueFactory(valueFactory);

        // Configurar spinner para el número de restricciones (entre 1 y 50, valor inicial 1)
        SpinnerValueFactory<Integer> valueFactory1 =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 50, 1);
        spinnerRestricciones.setValueFactory(valueFactory1);
    }

    /**
     * Limpia todos los contenedores y listas de la interfaz.
     * Útil para reiniciar el formulario o prepararlo para un nuevo problema.
     *
     * @param actionEvent Evento de acción que desencadenó este método
     */
    public void limpiar(ActionEvent actionEvent) {
        if (containerFuncionObjetivo != null) {
            containerFuncionObjetivo.getChildren().clear();
        }
        if (containerRestricciones != null) {
            containerRestricciones.getChildren().clear();
        }
        camposFuncionObjetivo.clear();
        camposRestricciones.clear();
        camposTerminosIndependientes.clear();
    }

    /**
     * Genera la interfaz para introducir los datos del modelo de programación lineal.
     * Crea campos para la función objetivo y las restricciones según los valores de los spinners.
     *
     * @param actionEvent Evento de acción que desencadenó este método
     */
    @FXML
    public void generarModelo(ActionEvent actionEvent) {
        int numVariables = spinnerVariables.getValue();
        int numRestricciones = spinnerRestricciones.getValue();

        // Limpiar la interfaz antes de generar el nuevo modelo
        limpiar(actionEvent);

        // Crear campos para la función objetivo
        GridPane gridFuncion = new GridPane();
        gridFuncion.setHgap(10);  // Espacio entre columnas (coeficientes X1, X2, etc.)
        gridFuncion.setVgap(10);  // Espacio entre filas (etiqueta vs. campo de texto)
        gridFuncion.setStyle("""
                    -fx-padding: 10; -fx-background-color: #f5f5f5; -fx-border-color: #ddd; -fx-border-radius: 5;""");

        Label lblTituloFuncion = new Label("Función Objetivo (coeficientes):");
        gridFuncion.add(lblTituloFuncion, 0, 0, numVariables, 1);
        //              objeto, fila, columna, cantColumnas a ocupar, cantFilas a ocupar

        comboTipoOperacion = new ComboBox<>();
        comboTipoOperacion.getItems().addAll("Minimizar", "Maximizar");
        comboTipoOperacion.setValue("Minimizar");

        gridFuncion.add(comboTipoOperacion, numVariables, 2);

        // Crear campos para cada variable en la función objetivo
        for (int i = 0; i < numVariables; i++) {
            Label lblVariable = new Label("X" + (i + 1) + ":");
            TextField txtCoeficiente = new TextField();
            txtCoeficiente.setPromptText("Coef. X" + (i + 1));

            gridFuncion.add(lblVariable, i, 1);
            gridFuncion.add(txtCoeficiente, i, 2);
            camposFuncionObjetivo.add(txtCoeficiente);
        }

        containerFuncionObjetivo.getChildren().add(gridFuncion);

        // Crear campos para las restricciones
        for (int r = 0; r < numRestricciones; r++) {
            GridPane gridRestriccion = new GridPane();
            gridRestriccion.setHgap(10);
            gridRestriccion.setVgap(10);
            gridRestriccion.setStyle("""
                    -fx-padding: 10; -fx-background-color: #f5f5f5; -fx-border-color: #ddd; -fx-border-radius: 5;""");

            Label lblTituloRestriccion = new Label("Restricción " + (r + 1) + ":");
            gridRestriccion.add(lblTituloRestriccion, 0, 0, numVariables + 2, 1);

            List<TextField> camposRestriccion = new ArrayList<>();

            // Crear campos para cada variable en la restricción
            for (int v = 0; v < numVariables; v++) {
                Label lblVariable = new Label("X" + (v + 1) + ":");
                TextField txtCoeficiente = new TextField();
                txtCoeficiente.setPromptText("Coef. X" + (v + 1));

                gridRestriccion.add(lblVariable, v, 1);
                gridRestriccion.add(txtCoeficiente, v, 2);
                camposRestriccion.add(txtCoeficiente);
            }

            // Selector de operador para la restricción (<=, =, >=)
            comboOperador = new ComboBox<>();
            comboOperador.getItems().addAll("\u2264", "=", "\u2265");
            comboOperador.setValue("\u2264");
            gridRestriccion.add(comboOperador, numVariables, 2);
            operadoresRestricciones.add(comboOperador);

            // Campo para el término independiente de la restricción
            TextField txtTerminoIndependiente = new TextField();
            txtTerminoIndependiente.setPromptText("Valor");
            gridRestriccion.add(new Label("Término:"), numVariables + 1, 1);
            gridRestriccion.add(txtTerminoIndependiente, numVariables + 1, 2);
            camposTerminosIndependientes.add(txtTerminoIndependiente);

            camposRestricciones.add(camposRestriccion);
            containerRestricciones.getChildren().add(gridRestriccion);
        }

        // Después de crear las restricciones principales y antes del botón de resolver
        GridPane gridNoNegatividad = new GridPane();
        gridNoNegatividad.setHgap(10);
        gridNoNegatividad.setVgap(5);
        gridNoNegatividad.setStyle("-fx-padding: 10; -fx-background-color: #f0f0f0;");

        Label lblTituloNoNeg = new Label("Restricciones de no negatividad:");
        gridNoNegatividad.add(lblTituloNoNeg, 0, 0, numVariables, 1);

        for (int i = 0; i < numVariables; i++) {
            Label lblRestriccion = new Label("X" + (i + 1) + " \u2265 0"); //\u2264: >=
            gridNoNegatividad.add(lblRestriccion, i, 1);
        }

        containerRestricciones.getChildren().add(gridNoNegatividad);
        // Botón para resolver el problema
        Button btnResolver = new Button("Resolver Problema");
        btnResolver.setOnAction(this::resolverProblema);
        btnResolver.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        containerRestricciones.getChildren().add(btnResolver);
    }

    /**
     * Resuelve el problema de programación lineal con los datos introducidos.
     * Captura los valores de la función objetivo y las restricciones, crea una instancia
     * de MSimplex y muestra los resultados en una ventana separada.
     *
     * @param event Evento de acción que desencadenó este método
     */
    @FXML
    private void resolverProblema(ActionEvent event) {
        try {
            int numVariables = spinnerVariables.getValue();
            int numRestricciones = spinnerRestricciones.getValue();

            // Capturar coeficientes de la función objetivo
            funcionObjetivo = new double[numVariables];
            for (int i = 0; i < numVariables; i++) {
                funcionObjetivo[i] = Double.parseDouble(camposFuncionObjetivo.get(i).getText());
            }

            // Capturar coeficientes de las restricciones y términos independientes
            restricciones = new double[numRestricciones][numVariables];
            terminosIndependientes = new double[numRestricciones];
            tipoRestriccion = new String[numRestricciones];
            boolean todasRestriccionesSonMenorIgual = true;

            for (int r = 0; r < numRestricciones; r++) {
                for (int v = 0; v < numVariables; v++) {
                    restricciones[r][v] = Double.parseDouble(camposRestricciones.get(r).get(v).getText());
                }
                terminosIndependientes[r] = Double.parseDouble(camposTerminosIndependientes.get(r).getText());
                tipoRestriccion[r] = operadoresRestricciones.get(r).getValue();
                if (!tipoRestriccion[r].equals("\u2264"))
                    todasRestriccionesSonMenorIgual = false;
            }

            // Crear instancia del resolvedor y ejecutar el método Simplex
            boolean maximizar = true;
            if (todasRestriccionesSonMenorIgual && !maximizar)
                simplex = new MSimplex(funcionObjetivo, restricciones, terminosIndependientes);
            else {
                if (comboTipoOperacion.getValue().equals("Maximizar"))
                    maximizar = true;
                else
                    maximizar = false;
                simplex = new MGranM(funcionObjetivo, restricciones, terminosIndependientes, tipoRestriccion, maximizar);
            }
            simplex.resolverMSimplex();

            // Mostrar ventana con el proceso completo del método Simplex
            MatrizWindow window = new MatrizWindow(simplex);
            window.mostrar();
            

        } catch (NumberFormatException e) {
            // Capturar errores de formato en los campos numéricos
            mostrarAlerta("Error", "Por favor ingrese valores numéricos válidos en todos los campos.");
        } catch (Exception e) {
            // Capturar cualquier otro error durante la resolución
            mostrarAlerta("Error", "Ocurrió un error al resolver el problema: " + e.getMessage());
        }
    }

    /**
     * Muestra una alerta de error con el título y mensaje especificados.
     *
     * @param titulo Título de la ventana de alerta
     * @param mensaje Mensaje a mostrar en la alerta
     */
    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}
