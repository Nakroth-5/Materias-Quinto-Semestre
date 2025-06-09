package org.universidad.granm;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

/**
 * Clase principal de la aplicación GranM.
 * Esta aplicación implementa el método Simplex para resolver problemas de programación lineal
 * con una interfaz gráfica que permite al usuario introducir los datos del problema y
 * visualizar el proceso de resolución paso a paso.
 */
public class GranMApplication extends Application {
    /**
     * Método que inicializa y configura la interfaz gráfica de la aplicación.
     * Carga el archivo FXML que define la estructura de la interfaz y muestra la ventana principal.
     * 
     * @param stage Ventana principal de la aplicación
     * @throws IOException Si ocurre un error al cargar el archivo FXML
     */
    @Override
    public void start(Stage stage) throws IOException {
        // Cargar el archivo FXML que define la estructura de la interfaz
        FXMLLoader fxmlLoader = new FXMLLoader(GranMApplication.class.getResource("GranM-view.fxml"));

        // Crear la escena con el contenido cargado del FXML
        Scene scene = new Scene(fxmlLoader.load(), 320, 240);

        // Configurar y mostrar la ventana principal
        stage.setTitle("Metodos de Simplex - GranM - AIC");
        stage.setScene(scene);
        stage.show();
    }

    /**
     * Método principal que inicia la aplicación JavaFX.
     * 
     * @param args Argumentos de línea de comandos (no utilizados)
     */
    public static void main(String[] args) {
        launch(); // Iniciar la aplicación JavaFX
    }
}
