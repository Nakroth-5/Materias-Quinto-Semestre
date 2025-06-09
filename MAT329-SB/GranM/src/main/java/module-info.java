/**
 * Módulo principal de la aplicación GranM.
 * Este módulo define las dependencias necesarias para la aplicación y configura
 * la visibilidad de los paquetes para JavaFX.
 */
module org.universidad.granm {
    // Dependencias de JavaFX necesarias para la interfaz gráfica
    requires javafx.controls;  // Controles básicos de JavaFX (botones, campos de texto, etc.)
    requires javafx.fxml;      // Soporte para archivos FXML que definen la estructura de la interfaz

    // Dependencias adicionales
    requires org.controlsfx.controls;  // Biblioteca de controles adicionales para JavaFX
    requires java.xml;                 // Soporte para procesamiento XML

    // Configuración de visibilidad de paquetes
    opens org.universidad.granm to javafx.fxml;         // Permite que JavaFX acceda al paquete principal
    exports org.universidad.granm.metodos;              // Expone el paquete de métodos para uso externo
    opens org.universidad.granm.metodos to javafx.fxml; // Permite que JavaFX acceda al paquete de métodos
    exports org.universidad.granm;                      // Expone el paquete principal para uso externo
}
